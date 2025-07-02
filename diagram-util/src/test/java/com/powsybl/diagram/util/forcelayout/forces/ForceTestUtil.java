/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class ForceTestUtil {

    private ForceTestUtil() {
        throw new AssertionError("Instantiating utility class ForceTestUtil");
    }

    public static void testForceCalculation(
            ForceGraph<String, DefaultEdge> forceGraph,
            Force<String, DefaultEdge> force,
            String[] vertexToTest,
            Vector2D[] resultForces,
            double delta
    ) {
        assertEquals(vertexToTest.length, resultForces.length);
        for (int i = 0; i < vertexToTest.length; ++i) {
            String forThisVertex = vertexToTest[i];
            Point point = getPoint(forceGraph, forThisVertex);
            Vector2D result = force.apply(
                    forThisVertex,
                    point,
                    forceGraph
            );
            Vector2D expected = resultForces[i];
            assertEquals(expected.getX(), result.getX(), delta);
            assertEquals(expected.getY(), result.getY(), delta);
        }
    }

    private static Point getPoint(ForceGraph<String, DefaultEdge> forceGraph, String vertex) {
        Point point = forceGraph.getMovingPoints().get(vertex);
        if (point != null) {
            return point;
        } else {
            point = forceGraph.getFixedPoints().get(vertex);
            if (point != null) {
                return point;
            } else {
                throw new NoSuchElementException("There is no point corresponding to the given vertex");
            }
        }
    }
}

