/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class TestInternCellExplicitPosition extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        FeederNode load1 = vlBuilder.createLoad("l1", 3, TOP, false);
        SwitchNode dl1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dl1", false, false);
        SwitchNode bl1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bl1", false, false);
        vlBuilder.connectNode(bbs1, dl1);
        vlBuilder.connectNode(dl1, bl1);
        vlBuilder.connectNode(bl1, load1);

        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 2, 1);
        FeederNode load2 = vlBuilder.createLoad("l2", 1, TOP, false);
        SwitchNode dl2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dl2", false, false);
        SwitchNode bl2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bl2", false, false);
        vlBuilder.connectNode(bbs2, dl2);
        vlBuilder.connectNode(dl2, bl2);
        vlBuilder.connectNode(bl2, load2);

        SwitchNode dc11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dc11", false, false);
        SwitchNode dc12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dc12", false, false);
        SwitchNode bc1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bc1", false, false, 2,
                BOTTOM);
        vlBuilder.connectNode(bbs1, dc11);
        vlBuilder.connectNode(bc1, dc11);
        vlBuilder.connectNode(bc1, dc12);
        vlBuilder.connectNode(bbs2, dc12);

        SwitchNode dc21 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dc21", false, false);
        SwitchNode dc22 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dc22", false, false);
        SwitchNode bc2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bc2", false, false, 4,
                null);
        vlBuilder.connectNode(bbs1, dc21);
        vlBuilder.connectNode(bc2, dc21);
        vlBuilder.connectNode(bc2, dc22);
        vlBuilder.connectNode(bbs2, dc22);

    }

    @Test
    void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestInternCellExplicitPosition.json"), toJson(g, "/TestInternCellExplicitPosition.json"));
    }
}
