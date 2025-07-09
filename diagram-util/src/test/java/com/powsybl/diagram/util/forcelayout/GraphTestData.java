/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
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

    private static final Random RANDOM = new Random(3L);

    private GraphTestData() {
        throw new AssertionError("Instantiating utility class GraphTestData");
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

    public static LayoutContext<String, DefaultEdge> getForcegraph() {
        LayoutContext<String, DefaultEdge> layoutContext = new LayoutContext<>(getGraph());
        setup(layoutContext);
        return layoutContext;
    }

    private static Graph<String, DefaultEdge> getGraph() {
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < getPoints().length; i++) {
            graph.addVertex(String.valueOf(i));
        }
        graph.addEdge("0", "1");
        graph.addEdge("1", "2");
        graph.addEdge("2", "0");
        graph.addEdge("0", "3");
        return graph;
    }

    private static void setup(LayoutContext<String, DefaultEdge> layoutContext) {
        Map<String, Point> initialPoints = new HashMap<>();
        Point[] points = getPoints();
        for (int i = 0; i < points.length; i++) {
            initialPoints.put(String.valueOf(i), points[i]);
        }
        layoutContext.setInitialPoints(initialPoints);
        Set<String> fixedNodes = new HashSet<>();
        fixedNodes.add("1");
        layoutContext.setFixedNodes(fixedNodes);
        SimpleSetup<String, DefaultEdge> setup = new SimpleSetup<>(RANDOM);
        setup.run(layoutContext);
    }
}
