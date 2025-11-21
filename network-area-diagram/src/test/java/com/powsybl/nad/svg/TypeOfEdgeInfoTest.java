/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class TypeOfEdgeInfoTest extends AbstractTest {

    Network network;
    DefaultLabelProvider labelProvider;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setVoltageLevelDetails(false)
                .setFixedWidth(800)
                .setEdgeStartShift(2));
        network = Networks.createTwoVoltageLevels();
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
        return labelProvider;
    }

    @Test
    void testReactivePowerInfoLabel() {
        labelProvider = new DefaultLabelProvider.Builder()
            .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.REACTIVE_POWER)
            .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .build(network, getSvgParameters());
        assertSvgEquals("/edge_info_reactive_power.svg", network);
    }

    @Test
    void testCurrentInfoLabel() {
        labelProvider = new DefaultLabelProvider.Builder()
            .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.CURRENT)
            .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .build(network, getSvgParameters());
        assertSvgEquals("/edge_info_current.svg", network);
    }

}
