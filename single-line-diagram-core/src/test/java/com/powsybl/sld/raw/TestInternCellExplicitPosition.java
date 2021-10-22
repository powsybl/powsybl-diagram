/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.BusCell.Direction;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestInternCellExplicitPosition extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl",
                380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        FeederNode load1 = vlBuilder.createLoad("l1", 3, BusCell.Direction.TOP);
        SwitchNode dl1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dl1", false, false);
        SwitchNode bl1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bl1", false, false);
        vlBuilder.connectNode(bbs1, dl1);
        vlBuilder.connectNode(dl1, bl1);
        vlBuilder.connectNode(bl1, load1);

        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 2, 1);
        FeederNode load2 = vlBuilder.createLoad("l2", 1, BusCell.Direction.TOP);
        SwitchNode dl2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dl2", false, false);
        SwitchNode bl2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bl2", false, false);
        vlBuilder.connectNode(bbs2, dl2);
        vlBuilder.connectNode(dl2, bl2);
        vlBuilder.connectNode(bl2, load2);

        SwitchNode dc1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dc1", false, false);
        SwitchNode dc2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dc2", false, false);
        SwitchNode bc = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bc", false, false, 2,
                Direction.BOTTOM);
        vlBuilder.connectNode(bbs1, dc1);
        vlBuilder.connectNode(bc, dc1);
        vlBuilder.connectNode(bc, dc2);
        vlBuilder.connectNode(bbs2, dc2);

    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestInternCellExplicitPosition.json"), toJson(g, "/TestInternCellExplicitPosition.json"));
    }
}
