/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.InternalNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <PRE>
 *
 * fict2
 * |
 * d2
 * |
 * b
 * |
 * fict1
 * |
 * d1
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestIncompleteFeederIssue extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
    }

    @Test
    public void test() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        InternalNode fict1 = vlBuilder.createInternalNode("fict1");
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        InternalNode fict2 = vlBuilder.createInternalNode("fict2");
        vlBuilder.connectNode(bbs, d1);
        vlBuilder.connectNode(d1, fict1);
        vlBuilder.connectNode(fict1, b2);
        vlBuilder.connectNode(b2, d2);
        vlBuilder.connectNode(d2, fict2);
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestIncompleteFeederIssue.json"), toJson(g, "/TestIncompleteFeederIssue.json"));
    }
}
