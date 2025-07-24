/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.layout;

import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.setup.SquareRandomSetup;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class GraphTestData {

    private static final Random RANDOM = new Random(3L);

    private GraphTestData() {
        throw new AssertionError("Instantiating utility class GraphTestData");
    }

    public static Point[] getPoints1() {
        return new Point[] {
            new Point(1, 2),
            new Point(-3.14, 2.78),
            new Point(1.414, 15),
            new Point(1.5, 1.5),
            new Point(5, 5),
        };
    }

    public static Point[] getPoints2() {
        return new Point[] {
            new Point(0.5, 0.5),
            new Point(0, 0),
            new Point(0.22, 0.22),
            new Point(-1.29, 1.18),
            new Point(-0.77, -0.73),
            new Point(-1.38, -0.7),
            new Point(1.14, -0.92),
            new Point(0.87, 0.93),
            new Point(0.25, 0.34),
            new Point(0.63, 1.01)
        };
    }

    public static Graph<String, DefaultEdge> getGraph1() {
        Point[] points = getPoints1();
        Graph<String, DefaultEdge> graph = buildGraphVertex(points);
        graph.addEdge("0", "1");
        graph.addEdge("1", "2");
        graph.addEdge("2", "0");
        graph.addEdge("0", "3");
        return graph;
    }

    public static LayoutContext<String, DefaultEdge> getLayoutContext1() {
        Graph<String, DefaultEdge> graph = getGraph1();
        LayoutContext<String, DefaultEdge> layoutContext = new LayoutContext<>(graph);
        Set<String> fixedNodes = new HashSet<>();
        fixedNodes.add("1");
        setup(layoutContext, fixedNodes, getPoints1());
        return layoutContext;
    }

    public static Graph<String, DefaultEdge> getGraph2() {
        Point[] points = getPoints2();
        Graph<String, DefaultEdge> graph = buildGraphVertex(points);
        graph.addEdge("0", "2");
        graph.addEdge("1", "2");
        graph.addEdge("1", "3");
        graph.addEdge("2", "8");
        graph.addEdge("2", "9");
        graph.addEdge("3", "4");
        graph.addEdge("3", "5");
        graph.addEdge("4", "5");
        graph.addEdge("7", "9");
        return graph;
    }

    public static LayoutContext<String, DefaultEdge> getLayoutContext2() {
        Graph<String, DefaultEdge> graph = getGraph2();
        LayoutContext<String, DefaultEdge> layoutContext = new LayoutContext<>(graph);
        Set<String> fixedNodes = new HashSet<>();
        setup(layoutContext, fixedNodes, getPoints2());
        return layoutContext;
    }

    private static Graph<String, DefaultEdge> buildGraphVertex(Point[] points) {
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < points.length; ++i) {
            graph.addVertex(String.valueOf(i));
        }
        return graph;
    }

    private static void setup(LayoutContext<String, DefaultEdge> layoutContext, Set<String> fixedNodes, Point[] points) {
        Map<String, Point> initialPoints = new HashMap<>();
        for (int i = 0; i < points.length; i++) {
            initialPoints.put(String.valueOf(i), points[i]);
        }
        layoutContext.setInitialPoints(initialPoints);
        layoutContext.setFixedNodes(fixedNodes);
        SquareRandomSetup<String, DefaultEdge> setup = new SquareRandomSetup<>(RANDOM);
        setup.run(layoutContext);
        for (Map.Entry<String, Point> entry : layoutContext.getMovingPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(layoutContext.getSimpleGraph().degreeOf(entry.getKey()));
        }
        for (Map.Entry<String, Point> entry : layoutContext.getFixedPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(layoutContext.getSimpleGraph().degreeOf(entry.getKey()));
        }
    }
}
