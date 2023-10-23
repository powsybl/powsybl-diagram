/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <pre>
 *     l
 *     |
 *     b
 *    / \
 *   |   |
 * -d1---|---- bbs1
 * -----d2---- bbs2
 *
 * </pre>
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
class TestCase2 extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        buildVl("vl");
        buildVl("vlUnstack");
    }

    private void buildVl(String id) {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder(id, 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 2, 1);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        Node f = vlBuilder.createConnectivityNode("2");
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        FeederNode load = vlBuilder.createLoad("l", 0, TOP);
        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(d1, f);
        vlBuilder.connectNode(bbs2, d2);
        vlBuilder.connectNode(d2, f);
        vlBuilder.connectNode(f, b);
        vlBuilder.connectNode(b, load);
    }

    @Test
    void testStacked() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCase2Stacked.json"), toJson(g, "/TestCase2Stacked.json"));
    }

    @Test
    void testUnstacked() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vlUnstack");
        new PositionVoltageLevelLayoutFactory()
                .setFeederStacked(false)
                .create(g)
                .run(layoutParameters);
        assertEquals(toString("/TestCase2UnStackedCell.json"), toJson(g, "/TestCase2UnStackedCell.json"));
    }
}
