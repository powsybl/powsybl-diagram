/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.model.*;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.FictitiousNode;
import com.powsybl.sld.model.nodes.SwitchNode;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

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
public class TestUndefinedBlockExternCell extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        FictitiousNode f0 = vlBuilder.createFictitiousNode("f0");
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d, f0);

        FictitiousNode f1 = vlBuilder.createFictitiousNode("f1");
        FeederNode l1 = vlBuilder.createLoad("l1", 0, TOP);
        vlBuilder.connectNode(f0, f1);
        vlBuilder.connectNode(f1, l1);

        FictitiousNode f2 = vlBuilder.createFictitiousNode("f2");
        FeederNode l2 = vlBuilder.createLoad("l2", 1, TOP);
        vlBuilder.connectNode(f1, f2);
        vlBuilder.connectNode(f2, l2);

        vlBuilder.connectNode(f2, f0);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildOrphanVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestUndefinedBlockExternCell.json"), toJson(g, "/TestUndefinedBlockExternCell.json"));
    }
}
