/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class GravityForceLinearTest {

    @Test
    void calculateForce() {
        double delta = 1e-5;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        GravityForceLinear<String, DefaultEdge> gravityForceLinear = new GravityForceLinear<>(
                new IntensityParameter(0.01)
        );
        Vector2D onPoint1 = gravityForceLinear.calculateForce(
                "1",
                forceGraph.getFixedPoints().get("1"),
                forceGraph
        );
        assertEquals(0.0314, onPoint1.getX(), delta);
        assertEquals(-0.0278, onPoint1.getY(), delta);
        Vector2D onPoint2 = gravityForceLinear.calculateForce(
                "2",
                forceGraph.getMovingPoints().get("2"),
                forceGraph
        );
        assertEquals(-0.01414, onPoint2.getX(), delta);
        assertEquals(-0.15, onPoint2.getY(), delta);
        Vector2D onPoint4 = gravityForceLinear.calculateForce(
                "4",
                forceGraph.getMovingPoints().get("4"),
                forceGraph
        );
        assertEquals(-0.05, onPoint4.getX(), delta);
        assertEquals(-0.05, onPoint4.getY(), delta);
    }
}