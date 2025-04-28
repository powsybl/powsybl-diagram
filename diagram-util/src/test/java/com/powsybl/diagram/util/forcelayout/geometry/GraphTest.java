/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;


/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class GraphTest {
    @Test
    void testGraphCreation() {
        List<Point> points = new ArrayList<>();
        points.add(new Point(1,2));
        points.add(new Point(-3.14, 2.78));
        points.add(new Point(1.414, 2345));
        points.add(new Point(26.1, -746));
        points.add(new Point(45, 45));

        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(points.get(0), points.get(1)));
        edges.add(new Edge(points.get(2), points.get(1)));
        edges.add(new Edge(points.get(1), points.get(2))); // same as previous one
        edges.add(new Edge(points.get(0), points.get(3)));
        edges.add(new Edge(points.get(0), points.get(2)));
        edges.add(new Edge(points.get(0), points.get(1))); // same as the first one

        List<Edge> uniqueEdges = new ArrayList<>();
        uniqueEdges.add(edges.get(0));
        uniqueEdges.add(edges.get(1));
        uniqueEdges.add(edges.get(3));
        uniqueEdges.add(edges.get(4));

        Graph got = new Graph(edges);
        HashMap<Point, Edge[]> expectedPoints = new HashMap<>();
        expectedPoints.put(points.get(0), new Edge[] {edges.get(0), edges.get(3), edges.get(4)});
        expectedPoints.put(points.get(1), new Edge[] {edges.get(0), edges.get(1)});
        expectedPoints.put(points.get(2), new Edge[] {edges.get(1), edges.get(4)});
        expectedPoints.put(points.get(3), new Edge[] {edges.get(3)});

        // edges might not be in the same order, check that there is the same number in both lists
        assertEquals(uniqueEdges.size(), got.getEdges().length);
        // and check that all the edges in one list are in the other list
        // since there are no repetitions, we know that the lists are the same, it doesn't actually matter the order they are in
        assertTrue(uniqueEdges.containsAll(Arrays.asList(got.getEdges())));
        // for each entry, check that we have the correct edges, and that there are the correct number of points
        Map<Point, Edge[]> actualPoints = got.getPoints();
        assertEquals(expectedPoints.size(), actualPoints.size());
        for (Map.Entry<Point, Edge[]> entry : expectedPoints.entrySet()) {
            Point expectedPoint = entry.getKey();
            Edge[] expectedEdges = entry.getValue();
            assertTrue(actualPoints.containsKey(expectedPoint));
            Edge[] actualEdges = actualPoints.get(expectedPoint);
            assertEquals(expectedEdges.length, actualEdges.length);
            assertTrue(Arrays.asList(actualEdges).containsAll(Arrays.asList(expectedEdges)));
        }
    }
}
