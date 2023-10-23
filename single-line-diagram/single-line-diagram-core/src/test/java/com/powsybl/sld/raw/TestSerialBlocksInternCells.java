/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactoryParameters;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class TestSerialBlocksInternCells extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs11 = vlBuilder.createBusBarSection("bbs11", 1, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs12", 1, 2);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);

        // 1-flat Serial
        SwitchNode sd11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "sd11", false, false);
        SwitchNode b11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b11", false, false);
        SwitchNode b12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b12", false, false);
        SwitchNode sd12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "sd12", false, false);
        vlBuilder.connectNode(bbs11, sd11);
        vlBuilder.connectNode(b11, sd11);
        vlBuilder.connectNode(b11, b12);
        vlBuilder.connectNode(b12, sd12);
        vlBuilder.connectNode(bbs12, sd12);

        // 2-CrossOver Serial
        SwitchNode sd21 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "sd21", false, false);
        SwitchNode b21 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b21", false, false);
        SwitchNode b22 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b22", false, false);
        SwitchNode sd22 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "sd22", false, false);
        vlBuilder.connectNode(bbs11, sd21);
        vlBuilder.connectNode(b21, sd21);
        vlBuilder.connectNode(b21, b22);
        vlBuilder.connectNode(b22, sd22);
        vlBuilder.connectNode(bbs22, sd22);

    }

    @Test
    void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        new PositionVoltageLevelLayoutFactory(new PositionVoltageLevelLayoutFactoryParameters().setRemoveUnnecessaryFictitiousNodes(false))
                .create(g)
                .run(layoutParameters);
        assertEquals(toString("/testSerialBlocksInternCells.json"), toJson(g, "/testSerialBlocksInternCells.json"));
    }
}
