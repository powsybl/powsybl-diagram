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
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
class TestUndefinedBlockExternCell extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        ConnectivityNode f0 = vlBuilder.createConnectivityNode("f0");
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d, f0);

        ConnectivityNode f1 = vlBuilder.createConnectivityNode("f1");
        FeederNode l1 = vlBuilder.createLoad("l1", 0, TOP);
        vlBuilder.connectNode(f0, f1);
        vlBuilder.connectNode(f1, l1);

        ConnectivityNode f2 = vlBuilder.createConnectivityNode("f2");
        FeederNode l2 = vlBuilder.createLoad("l2", 1, TOP);
        vlBuilder.connectNode(f1, f2);
        vlBuilder.connectNode(f2, l2);

        vlBuilder.connectNode(f2, f0);
    }

    @Test
    void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestUndefinedBlockExternCell.json"), toJson(g, "/TestUndefinedBlockExternCell.json"));
    }
}
