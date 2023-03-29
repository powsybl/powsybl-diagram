/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <pre>
 *
 *       la     lb
 *       |      |
 *      nsa- |  bb
 *       |  bs  |
 *       ba  |- nsb
 *       |      |
 * bbs---da-----db---
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class TestCase5V extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode la = vlBuilder.createLoad("la", 20, TOP);
        SwitchNode ba = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "ba", false, false);
        SwitchNode da = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "da", false, false);
        vlBuilder.connectNode(la, ba);
        vlBuilder.connectNode(ba, da);
        vlBuilder.connectNode(da, bbs);

        FeederNode lb = vlBuilder.createLoad("lb", 10, TOP);
        SwitchNode bb = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bb", false, false);
        SwitchNode db = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db", false, false);
        ConnectivityNode fn = vlBuilder.createConnectivityNode("3");
        vlBuilder.connectNode(lb, bb);
        vlBuilder.connectNode(bb, fn);
        vlBuilder.connectNode(fn, db);
        vlBuilder.connectNode(db, bbs);

        SwitchNode bs = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bs", false, false);
        vlBuilder.connectNode(la, bs);
        vlBuilder.connectNode(bs, fn);

    }

    @Test
    void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCase5V.json"), toJson(g, "/TestCase5V.json"));
    }
}
