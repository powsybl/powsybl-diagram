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
class GravityForceByEdgeNumberTest {

    @Test
    void calculateForce() {
        double delta = 1e-5;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        GravityForceByEdgeNumber<String, DefaultEdge> gravityForceByEdgeNumber = new GravityForceByEdgeNumber<>(
                new IntensityParameter(0.01)
        );
        Vector2D onPoint0 = gravityForceByEdgeNumber.calculateForce(
                "0",
                forceGraph.getMovingPoints().get("0"),
                forceGraph
        );
        assertEquals(-0.01789, onPoint0.getX(), delta);
        assertEquals(-0.03578, onPoint0.getY(), delta);
        Vector2D onPoint1 = gravityForceByEdgeNumber.calculateForce(
                "1",
                forceGraph.getFixedPoints().get("1"),
                forceGraph
        );
        assertEquals(0.022461, onPoint1.getX(), delta);
        assertEquals(-0.019887, onPoint1.getY(), delta);
        Vector2D onPoint4 = gravityForceByEdgeNumber.calculateForce(
                "4",
                forceGraph.getMovingPoints().get("4"),
                forceGraph
        );
        assertEquals(-0.007071, onPoint4.getX(), delta);
        assertEquals(-0.007071, onPoint4.getY(), delta);
    }
}
