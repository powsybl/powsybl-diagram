/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces;

import com.powsybl.diagram.util.layout.GraphTestData;
import com.powsybl.diagram.util.layout.geometry.*;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class RepulsionForceByEdgeNumberLinearBarnesHutTest {

    @Test
    void apply() {
        double delta = 1e-3;
        LayoutContext<String, DefaultEdge> forceGraph = GraphTestData.getLayoutContext2();
        Quadtree quadtree = new Quadtree(forceGraph.getAllPoints().values(), (Point point) -> point.getPointVertexDegree() + 1);
        QuadtreeContainer quadtreeContainer = new QuadtreeContainer();
        quadtreeContainer.setQuadtree(quadtree);
        RepulsionForceByEdgeNumberLinearBarnesHut<String, DefaultEdge> repulsionForceByEdgeNumberLinearBarnesHut = new RepulsionForceByEdgeNumberLinearBarnesHut<>(
                1.2,
                true,
                1.5,
                quadtreeContainer
        );
        String[] vertexToTest = {
            "1",
            "3"
        };
        Vector2D[] resultVector = {
            new Vector2D(-43.04168, -62.56815),
            new Vector2D(-42.662327, 33.520947)
        };
        ForceTestUtil.testForceCalculation(forceGraph, repulsionForceByEdgeNumberLinearBarnesHut, vertexToTest, resultVector, delta);

        RepulsionForceByEdgeNumberLinearBarnesHut<String, DefaultEdge> repulsionForceByEdgeNumberLinearBarnesHut2 = new RepulsionForceByEdgeNumberLinearBarnesHut<>(
                0.7,
                true,
                1.2,
                quadtreeContainer
        );
        String[] vertexToTest2 = {"1"};
        Vector2D[] resultVector2 = {
            new Vector2D(-27.523551875, -40.034076755)
        };

        ForceTestUtil.testForceCalculation(forceGraph, repulsionForceByEdgeNumberLinearBarnesHut2, vertexToTest2, resultVector2, delta);
    }
}
