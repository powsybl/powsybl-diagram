/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class TestParallelFeedersOnBus extends AbstractTestCaseRaw {

    @BeforeEach
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
    void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/testParallelFeedersOnBus.json"), toJson(g, "/testParallelFeedersOnBus.json"));
    }
}
