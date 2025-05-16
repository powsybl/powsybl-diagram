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
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class CoulombForceTest {

    @Test
    void calculateForce() {
        double delta = 0.001;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        CoulombForce<String, DefaultEdge> force = new CoulombForce<>(new IntensityEffectFromFixedNodesParameters(2, true));
        Vector2D resultingVector = force.calculateForce("0", forceGraph.getMovingPoints().get("0"), forceGraph);
        assertEquals(-3.9493, resultingVector.getX(), delta);
        assertEquals(3.8805, resultingVector.getY(), delta);
    }
}
