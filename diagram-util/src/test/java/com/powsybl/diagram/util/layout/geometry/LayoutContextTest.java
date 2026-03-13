/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.layout.geometry;

import com.powsybl.diagram.util.layout.GraphTestData;
import com.powsybl.diagram.util.layout.ResourceUtils;
import com.powsybl.diagram.util.layout.Layout;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class LayoutContextTest {
    @TempDir
    File tempDirectory;

    @Test
    void graphCreation() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        assertEquals(5, layoutContext.getSimpleGraph().vertexSet().size());
        assertEquals(4, layoutContext.getSimpleGraph().edgeSet().size());
        assertEquals(1, layoutContext.getFixedPoints().size());
        assertEquals(1, layoutContext.getFixedNodes().size());
        assertEquals(5, layoutContext.getInitialPoints().size());
        assertEquals(4, layoutContext.getMovingPoints().size());
    }

    @Test
    void setFixedPoints() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Map<String, Point> fixedPoints = new HashMap<>();
        fixedPoints.put("2", new Point(1.414, 15));
        fixedPoints.put("4", new Point(0, 0));
        fixedPoints.put("-1", new Point(-2, 6));
        layoutContext.setFixedPoints(fixedPoints);
        assertEquals(2, layoutContext.getInitialPoints().size());
    }

    @Test
    void setCenter() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Vector2D newCenter = new Vector2D(-445, 23.3);
        layoutContext.setCenter(newCenter);
        assertEquals(newCenter.getX(), layoutContext.getCenter().getX());
        assertEquals(newCenter.getY(), layoutContext.getCenter().getY());
        Vector2D otherNewCenter = new Vector2D(3.4, -6.1);
        layoutContext.setCenter(otherNewCenter);
        assertEquals(otherNewCenter.getX(), layoutContext.getCenter().getX());
        assertEquals(otherNewCenter.getY(), layoutContext.getCenter().getY());
    }

    @Test
    void toSvgBasic() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        Layout<String, DefaultEdge> layout = Layout.createBasicForceLayout();
        layout.run(layoutContext);
        StringWriter sw = new StringWriter();
        layoutContext.toSVG(tooltip, sw);
        assertEquals(ResourceUtils.toString("basic_5_nodes.svg"), sw.toString());
    }

    @Test
    void notExecuted() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        assertDoesNotThrow(() -> layoutContext.toSVG(tooltip, tempDirectory.toPath().resolve("test.svg")));
        assertDoesNotThrow(() -> layoutContext.getStablePosition("0"));
    }

    @Test
    void setFixedNodesUnknownNodes() {
        LayoutContext<String, DefaultEdge> layoutContext = new LayoutContext<>(GraphTestData.getGraph1());
        Set<String> fixedNodes = new HashSet<>();
        fixedNodes.add("1");
        fixedNodes.add("2");
        fixedNodes.add("-1");
        fixedNodes.add("a");
        fixedNodes.add("4738387");
        layoutContext.setFixedNodes(fixedNodes);
        Set<String> actualFixedNodes = new HashSet<>();
        actualFixedNodes.add("1");
        actualFixedNodes.add("2");
        assertEquals(actualFixedNodes, layoutContext.getFixedNodes());
    }

    @Test
    void setFixedPointsWithUnknownPoint() {
        LayoutContext<String, DefaultEdge> layoutContext = new LayoutContext<>(GraphTestData.getGraph1());
        Map<String, Point> fixedPoints = new HashMap<>();
        fixedPoints.put("1", new Point(1, 1));
        fixedPoints.put("4", new Point(-2, 3));
        fixedPoints.put("-1", new Point(0, 3));
        fixedPoints.put("dfsfds", new Point(-2, -3.3));
        fixedPoints.put("45", new Point(45, 45));
        layoutContext.setFixedPoints(fixedPoints);
        Map<String, Point> expectedFixedPoints = new HashMap<>();
        expectedFixedPoints.put("1", new Point(1, 1));
        expectedFixedPoints.put("4", new Point(-2, 3));
        Set<Map.Entry<String, Point>> actualFixedPoints = layoutContext.getInitialPoints().entrySet();
        assertEquals(expectedFixedPoints.size(), actualFixedPoints.size());
        for (Map.Entry<String, Point> entry : actualFixedPoints) {
            Vector2D expectedPosition = expectedFixedPoints.get(entry.getKey()).getPosition();
            Vector2D actualPosition = entry.getValue().getPosition();
            assertEquals(expectedPosition.getX(), actualPosition.getX());
            assertEquals(expectedPosition.getY(), actualPosition.getY());
        }
        assertEquals(expectedFixedPoints.keySet(), layoutContext.getFixedNodes());
    }
}
