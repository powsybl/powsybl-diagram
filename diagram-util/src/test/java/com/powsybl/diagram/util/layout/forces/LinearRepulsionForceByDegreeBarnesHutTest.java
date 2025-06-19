/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityEffectFromFIxedNodesBarnesHutParameters;
import com.powsybl.diagram.util.forcelayout.geometry.*;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class LinearRepulsionForceByDegreeBarnesHutTest {

    @Test
    void calculateForce() {
        double delta = 1e-3;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForceGraph2();
        Quadtree quadtree = new Quadtree(forceGraph.getAllPoints().values(), (Point point) -> point.getPointVertexDegree() + 1);
        QuadtreeContainer quadtreeContainer = new QuadtreeContainer();
        quadtreeContainer.setQuadtree(quadtree);
        LinearRepulsionForceByDegreeBarnesHut<String, DefaultEdge> linearRepulsionForceByDegreeBarnesHut = new LinearRepulsionForceByDegreeBarnesHut<>(
                new IntensityEffectFromFIxedNodesBarnesHutParameters(
                1.2,
                true,
                1.5,
                quadtreeContainer
                )
        );
        String[] vertexToTest = {
            "1",
            "3"
        };
        Vector2D[] resultVector = {
            new Vector2D(-43.04168, -62.56815),
            new Vector2D(-42.662327, 33.520947)
        };
        ForceTestUtil.testForceCalculation(forceGraph, linearRepulsionForceByDegreeBarnesHut, vertexToTest, resultVector, delta);

        LinearRepulsionForceByDegreeBarnesHut<String, DefaultEdge> linearRepulsionForceByDegreeBarnesHut2 = new LinearRepulsionForceByDegreeBarnesHut<>(
            new IntensityEffectFromFIxedNodesBarnesHutParameters(
                0.7,
                true,
                1.2,
                quadtreeContainer
            )
        );
        String[] vertexToTest2 = {"1"};
        Vector2D[] resultVector2 = {
            new Vector2D(-27.523551875, -40.034076755)
        };

        ForceTestUtil.testForceCalculation(forceGraph, linearRepulsionForceByDegreeBarnesHut2, vertexToTest2, resultVector2, delta);
    }
}
