/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
public class TestCase5V extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode la = vlBuilder.createLoad("la", 20, BusCell.Direction.TOP);
        SwitchNode ba = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "ba", false, false);
        SwitchNode da = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "da", false, false);
        vlBuilder.connectNode(la, ba);
        vlBuilder.connectNode(ba, da);
        vlBuilder.connectNode(da, bbs);

        FeederNode lb = vlBuilder.createLoad("lb", 10, BusCell.Direction.TOP);
        SwitchNode bb = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bb", false, false);
        SwitchNode db = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db", false, false);
        FictitiousNode fn = vlBuilder.createFictitiousNode(3);
        vlBuilder.connectNode(lb, bb);
        vlBuilder.connectNode(bb, fn);
        vlBuilder.connectNode(fn, db);
        vlBuilder.connectNode(db, bbs);

        SwitchNode bs = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bs", false, false);
        vlBuilder.connectNode(la, bs);
        vlBuilder.connectNode(bs, fn);

    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", true);
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCase5V.json"), toJson(g, "/TestCase5V.json"));
    }
}
