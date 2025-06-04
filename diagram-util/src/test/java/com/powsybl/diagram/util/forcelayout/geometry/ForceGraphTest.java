/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;

import com.powsybl.diagram.util.forcelayout.ForceLayout;
import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.Helpers;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class ForceGraphTest {
    @TempDir
    File tempDirectory;

    @Test
    void graphCreation() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        assertEquals(5, forceGraph.getSimpleGraph().vertexSet().size());
        assertEquals(4, forceGraph.getSimpleGraph().edgeSet().size());
        assertEquals(1, forceGraph.getFixedPoints().size());
        assertEquals(1, forceGraph.getFixedNodes().size());
        assertEquals(5, forceGraph.getInitialPoints().size());
        assertEquals(4, forceGraph.getMovingPoints().size());
    }

    @Test
    void setFixedPoints() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        Map<String, Point> fixedPoints = new HashMap<>();
        fixedPoints.put("2", new Point(1.414, 15));
        fixedPoints.put("4", new Point(0, 0));
        fixedPoints.put("-1", new Point(-2, 6));
        forceGraph.setFixedPoints(fixedPoints);
        assertEquals(2, forceGraph.getInitialPoints().size());
    }

    @Test
    void toSvg() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        ForceLayout<String, DefaultEdge> forceLayout = new ForceLayout<>(forceGraph);
        forceLayout.execute();
        StringWriter sw = new StringWriter();
        forceLayout.toSVG(tooltip, sw);
        Helpers helpers = new Helpers();
        assertEquals(helpers.toString("/springy_5_nodes.svg"), sw.toString());
    }

    @Test
    void notExecuted() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        ForceLayout<String, DefaultEdge> forceLayout = new ForceLayout<>(forceGraph);
        assertDoesNotThrow(() -> forceLayout.toSVG(tooltip, tempDirectory.toPath().resolve("test.svg")));
    }
}
