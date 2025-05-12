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
class GravityForceUnitTest {

    @Test
    void calculateForce() {
        double delta = 1e-5;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        GravityForceUnit<String, DefaultEdge> gravityForceUnit = new GravityForceUnit<>(
                new IntensityParameter(0.01)
        );
        Vector2D onPoint1 = gravityForceUnit.calculateForce(
                "1",
                forceGraph.getFixedPoints().get("1"),
                forceGraph
        );
        assertEquals(0.007487, onPoint1.x(), delta);
        assertEquals(-0.006629, onPoint1.y(), delta);
        Vector2D onPoint2 = gravityForceUnit.calculateForce(
                "2",
                forceGraph.getMovingPoints().get("2"),
                forceGraph
        );
        assertEquals(-0.000939, onPoint2.x(), delta);
        assertEquals(-0.009956, onPoint2.y(), delta);
        Vector2D onPoint4 = gravityForceUnit.calculateForce(
                "4",
                forceGraph.getMovingPoints().get("4"),
                forceGraph
        );
        assertEquals(-0.007071, onPoint4.x(), delta);
        assertEquals(-0.007071, onPoint4.y(), delta);
    }
}
