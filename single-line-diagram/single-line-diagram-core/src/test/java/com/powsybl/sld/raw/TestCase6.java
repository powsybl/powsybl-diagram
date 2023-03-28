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
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <pre>
 *              b
 *           /     \
 *          |       |
 * bbs1.1 -d1- ds1 -|-- bbs1.2
 * bbs2.1 ---- ds2 -d2- bbs2.2
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class TestCase6 extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);

        BusNode bbs11 = vlBuilder.createBusBarSection("bbs1.1", 1, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs1.2", 1, 2);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs2.1", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs2.2", 2, 2);

        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        vlBuilder.connectNode(bbs11, d1);
        vlBuilder.connectNode(d1, b);
        vlBuilder.connectNode(b, d2);
        vlBuilder.connectNode(d2, bbs22);

        SwitchNode ds1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "ds1", false, false);
        vlBuilder.connectNode(bbs11, ds1);
        vlBuilder.connectNode(ds1, bbs12);
        SwitchNode ds2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "ds2", false, false);
        vlBuilder.connectNode(bbs21, ds2);
        vlBuilder.connectNode(ds2, bbs22);

    }

    @Test
    void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCase6CouplingNonFlatHorizontal.json"), toJson(g, "/TestCase6CouplingNonFlatHorizontal.json"));
    }
}
