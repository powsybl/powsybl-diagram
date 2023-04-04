/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class TypeOfEdgeInfoTest extends AbstractTest {

    Network network;

    @Before
    public void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setVoltageLevelDetails(false)
                .setFixedWidth(800)
                .setEdgeStartShift(2));
        network = NetworkTestFactory.createTwoVoltageLevels();
        Line l1 = network.getLine("l1");
        l1.getTerminal1().setP(100).setQ(10);
        l1.getTerminal2().setP(99).setQ(11);
        l1.getTerminal1().getBusView().getBus().setV(380);
        l1.getTerminal2().getBusView().getBus().setV(379);
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
    public void testReactivePowerInfoLabel() {
        getSvgParameters().setEdgeInfoDisplayed(SvgParameters.EdgeInfoEnum.REACTIVE_POWER);
        assertEquals(toString("/edge_info_reactive_power.svg"), generateSvgString(network, "/edge_info_reactive_power.svg"));
    }

    @Test
    public void testCurrentInfoLabel() {
        getSvgParameters().setEdgeInfoDisplayed(SvgParameters.EdgeInfoEnum.CURRENT);
        assertEquals(toString("/edge_info_current.svg"), generateSvgString(network, "/edge_info_current.svg"));
    }

}
