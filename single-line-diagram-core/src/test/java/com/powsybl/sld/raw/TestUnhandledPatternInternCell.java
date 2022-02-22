/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.model.*;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FictitiousNode;
import com.powsybl.sld.model.nodes.SwitchNode;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestUnhandledPatternInternCell extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 1, 2);
        BusNode bbs3 = vlBuilder.createBusBarSection("bbs3", 1, 3);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode b1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        SwitchNode d3 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d3", false, false);
        SwitchNode b3 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        FictitiousNode f = vlBuilder.createFictitiousNode("F");
        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(b1, d1);
        vlBuilder.connectNode(b1, f);
        vlBuilder.connectNode(bbs2, d2);
        vlBuilder.connectNode(b2, d2);
        vlBuilder.connectNode(b2, f);
        vlBuilder.connectNode(bbs3, d3);
        vlBuilder.connectNode(b3, d3);
        vlBuilder.connectNode(b3, f);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildOrphanVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(InternCell.Shape.UNHANDLEDPATTERN, ((InternCell) g.getCells().iterator().next()).getShape());
    }
}
