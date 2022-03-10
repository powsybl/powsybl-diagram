/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestParallelFeedersOnBus extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode load1 = vlBuilder.createLoad("l1");
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        FeederNode load2 = vlBuilder.createLoad("l2");
        vlBuilder.connectNode(bbs, load1);
        vlBuilder.connectNode(load1, b);
        vlBuilder.connectNode(b, load2);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", true);
        voltageLevelGraphLayout(g);
        assertEquals(toString("/testParallelFeedersOnBus.json"), toJson(g, "/testParallelFeedersOnBus.json"));
    }
}
