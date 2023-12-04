/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class LimitsTest extends AbstractTest {

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void testVoltageLimits() {
        Network network = Networks.createTwoVoltageLevelsThreeBuses();
        network.getVoltageLevel("vl1")
                .setHighVoltageLimit(385)
                .getBusView().getBus("vl1_0").setV(385.1);
        network.getVoltageLevel("vl2")
                .setLowVoltageLimit(390)
                .getBusView().getBus("vl2_0").setV(388);
        assertEquals(toString("/voltage_limits.svg"), generateSvgString(network, "/voltage_limits.svg"));
    }

    @Test
    void testCurrentLimits() {
        Network network = Networks.createTwoVoltageLevels();
        network.getLine("l1").newCurrentLimits1().setPermanentLimit(250).add();
        network.getLine("l1").getTerminal1().setP(101).setQ(150).getBusView().getBus().setV(390);
        assertEquals(toString("/current_limits.svg"), generateSvgString(network, "/current_limits.svg"));
    }
}
