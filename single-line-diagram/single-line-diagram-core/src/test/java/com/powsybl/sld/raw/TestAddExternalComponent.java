/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.library.SldResourcesComponentLibrary;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.svg.styles.BasicStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
class TestAddExternalComponent extends AbstractTestCaseRaw {
    private static final String CHEESE = "CHEESE";

    @BeforeEach
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode load = vlBuilder.createLoad("l", 0, TOP);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        Node cheese = NodeFactory.createEquipmentNode(vlBuilder.getGraph(), Node.NodeType.INTERNAL, "cheese", null, null, CHEESE, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d, b);
        vlBuilder.connectNode(b, cheese);
        vlBuilder.connectNode(cheese, load);
    }

    @Override
    protected SldResourcesComponentLibrary getResourcesComponentLibrary() {
        return new SldResourcesComponentLibrary("cheese", "/ConvergenceLibrary", "/CheeseLibrary");
    }

    @Test
    void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCheese.svg"),
                toSVG(g, "/TestCheese.svg", getResourcesComponentLibrary(), layoutParameters, svgParameters, getLabelRawProvider(), new BasicStyleProvider()));
    }
}
