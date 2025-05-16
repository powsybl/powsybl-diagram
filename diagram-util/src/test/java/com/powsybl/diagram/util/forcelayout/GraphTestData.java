/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.setup.SimpleSetup;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class GraphTestData {

    private GraphTestData() {
        throw new AssertionError("Instantiating utility class");
    }

    public static Point[] getPoints() {
        return new Point[] {
            new Point(1, 2),
            new Point(-3.14, 2.78),
            new Point(1.414, 15),
            new Point(1.5, 1.5),
            new Point(5, 5),
        };
    }

    public static ForceGraph<String, DefaultEdge> getForcegraph() {
        ForceGraph<String, DefaultEdge> forceGraph = new ForceGraph<>(getGraph());
        setup(forceGraph);
        return forceGraph;
    }

    private static Graph<String, DefaultEdge> getGraph() {
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex("0");
        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");
        graph.addVertex("4");
        graph.addEdge("0", "1");
        graph.addEdge("1", "2");
        graph.addEdge("2", "0");
        graph.addEdge("0", "3");
        return graph;
    }

    private static void setup(ForceGraph<String, DefaultEdge> forceGraph) {
        Map<String, Point> initialPoints = new HashMap<>();
        Point[] points = getPoints();
        for (int i = 0; i < points.length; i++) {
            initialPoints.put(String.valueOf(i), points[i]);
        }
        forceGraph.setInitialPoints(initialPoints);
        Set<String> fixedNodes = new HashSet<>();
        fixedNodes.add("1");
        forceGraph.setFixedNodes(fixedNodes);
        SimpleSetup<String, DefaultEdge> setup = new SimpleSetup<>();
        setup.setup(forceGraph);
    }
}
