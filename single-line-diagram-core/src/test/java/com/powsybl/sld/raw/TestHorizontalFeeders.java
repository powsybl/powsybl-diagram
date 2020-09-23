/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestHorizontalFeeders extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode load = vlBuilder.createLoad("l", 0, BusCell.Direction.TOP);
        load.setOrientation(Orientation.RIGHT);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d, b);
        vlBuilder.connectNode(b, load);
    }

    @Test
    public void test() {
        Graph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
/*
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(layoutParameters);
        toSVG(g, "/test.svg");
*/
//        assertEquals(toString("/TestCase1.json"), toJson(g, "/TestCase1.json"));
    }
}
