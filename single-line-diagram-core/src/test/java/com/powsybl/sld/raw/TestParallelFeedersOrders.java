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
import com.powsybl.sld.model.nodes.FictitiousNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestParallelFeedersOrders extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        FictitiousNode f = vlBuilder.createFictitiousNode("f");
        FeederNode load1 = vlBuilder.createLoad("l1", 0, TOP);
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        FeederNode load2 = vlBuilder.createLoad("l2", 1, TOP);
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d, b);
        vlBuilder.connectNode(b, f);
        vlBuilder.connectNode(f, load1);
        vlBuilder.connectNode(f, b2);
        vlBuilder.connectNode(b2, load2);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildOrphanVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/testParallelFeedersOrders.json"), toJson(g, "/testParallelFeedersOrders.json"));
    }
}
