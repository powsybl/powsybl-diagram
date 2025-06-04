/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityEffectFromFixedNodesParameters;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class CoulombForceTest {

    @Test
    void calculateForce() {
        double delta = 1e-4;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        CoulombForce<String, DefaultEdge> force = new CoulombForce<>(new IntensityEffectFromFixedNodesParameters(2, true));
        String[] vertexToTest = {"0"};
        Vector2D[] resultVector = {
            new Vector2D(-3.9493, 3.8805)
        };
        ForceTestUtil.testForceCalculation(forceGraph, force, vertexToTest, resultVector, delta);
    }
}
