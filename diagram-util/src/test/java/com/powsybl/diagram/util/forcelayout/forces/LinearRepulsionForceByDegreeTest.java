/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityEffectFromFixedNodesParameters;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class LinearRepulsionForceByDegreeTest {

    @Test
    void calculateForce() {
        double delta = 1e-4;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        setupPoints(forceGraph);
        LinearRepulsionForceByDegree<String, DefaultEdge> linearRepulsionForceByDegree = new LinearRepulsionForceByDegree<>(
                new IntensityEffectFromFixedNodesParameters(
                        0.34,
                        true
                )
        );
        String[] vertexToTest = {"0", "4"};
        Vector2D[] resultVector = {
            new Vector2D(-1.99586, 2.06396),
            new Vector2D(0.463784, 0.201774),
        };

        ForceTestUtil.testForceCalculation(forceGraph, linearRepulsionForceByDegree, vertexToTest, resultVector, delta);

        LinearRepulsionForceByDegree<String, DefaultEdge> linearRepulsionForceByDegreeNoFixed = new LinearRepulsionForceByDegree<>(
                new IntensityEffectFromFixedNodesParameters(
                        0.34,
                        false
                )
        );

        Vector2D[] resultVectorNoFixed = {
            new Vector2D(-2.94758, 2.24327),
            new Vector2D(0.347152, 0.169965),
        };

        ForceTestUtil.testForceCalculation(forceGraph, linearRepulsionForceByDegreeNoFixed, vertexToTest, resultVectorNoFixed, delta);
    }

    private void setupPoints(ForceGraph<String, DefaultEdge> forceGraph) {
        for (Map.Entry<String, Point> entry : forceGraph.getMovingPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(forceGraph.getSimpleGraph().degreeOf(entry.getKey()));
        }
        for (Map.Entry<String, Point> entry : forceGraph.getFixedPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(forceGraph.getSimpleGraph().degreeOf(entry.getKey()));
        }
    }
}
