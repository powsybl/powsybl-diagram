/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class FourSubstationsNetworkTest extends AbstractTest {

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters()
                .setInjectionsAdded(true));
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedScale(0.5));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new TopologicalStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        // Open the coupler to get a two-nodes voltage level
        network.getSwitch("S1VL2_COUPLER").setOpen(true);

        // Add shunt capacitor
        VoltageLevel s1vl2 = network.getVoltageLevel("S1VL2");
        s1vl2.getNodeBreakerView().newDisconnector().setId("S1VL2_BBS1_SCC_DISCONNECTOR").setOpen(true).setNode1(0).setNode2(24).add();
        s1vl2.getNodeBreakerView().newBreaker().setId("S1VL2_BBS1_SCC_BREAKER").setOpen(false).setNode1(24).setNode2(25).add();
        ShuntCompensator shuntCapacitor = s1vl2.newShuntCompensator()
                .setId("SHUNT_CAPACITOR")
                .setNode(25)
                .setSectionCount(1)
                .newLinearModel()
                .setMaximumSectionCount(1)
                .setBPerSection(0.032)
                .add()
                .add();
        shuntCapacitor.getTerminal().setQ(1920.0).setP(0.);

        // Add battery
        VoltageLevel s1vl1 = network.getVoltageLevel("S1VL1");
        s1vl1.getNodeBreakerView().newDisconnector().setId("S1VL1_BATTERY_DISCONNECTOR").setNode1(0).setNode2(5).add();
        s1vl1.getNodeBreakerView().newBreaker().setId("S1VL1_BATTERY_BREAKER").setNode1(5).setNode2(6).add();
        Battery battery = s1vl1.newBattery()
                .setId("BATTERY")
                .setNode(6)
                .setTargetP(50.)
                .setTargetQ(2.)
                .setMinP(-5)
                .setMaxP(60)
                .add();
        battery.getTerminal().setP(15.);

        // Override NaN value on shunt
        network.getShuntCompensator("SHUNT").getTerminal().setP(0.);

        assertSvgEquals("/four_substations.svg", network);
    }
}
