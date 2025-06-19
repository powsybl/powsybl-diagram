/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.layouts;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.Helpers;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.Atlas2Parameters;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class Atlas2LayoutTest {

    @Test
    void calculateLayoutNoBH() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForceGraph1();
        Atlas2Parameters<String, DefaultEdge> layoutParameters = new Atlas2Parameters.Builder().withBarnesHutTheta(0).build();
        LayoutAlgorithm<String, DefaultEdge> atlas2 = new Atlas2Layout<>(layoutParameters);
        atlas2.calculateLayout(forceGraph);
        StringWriter sw = new StringWriter();
        forceGraph.toSVG(v -> String.format("Vertex %s", v), sw);
        Helpers helper = new Helpers();
        assertEquals(helper.toString("/atlas2_5_nodes_no_BH.svg"), sw.toString());
    }

    @Test
    void calculateLayoutYesBH() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForceGraph1();
        Atlas2Parameters<String, DefaultEdge> layoutParameters = new Atlas2Parameters.Builder().withBarnesHutTheta(1.5).build();
        LayoutAlgorithm<String, DefaultEdge> atlas2 = new Atlas2Layout<>(layoutParameters);
        atlas2.calculateLayout(forceGraph);
        StringWriter sw = new StringWriter();
        forceGraph.toSVG(v -> String.format("Vertex %s", v), sw);
        Helpers helper = new Helpers();
        assertEquals(helper.toString("/atlas2_5_nodes_yes_BH.svg"), sw.toString());
    }
}
