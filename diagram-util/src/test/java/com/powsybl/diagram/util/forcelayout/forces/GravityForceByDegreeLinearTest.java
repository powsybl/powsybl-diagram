/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class GravityForceByDegreeLinearTest {

    @Test
    void calculateForce() {
        double delta = 1e-5;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        GravityForceByDegreeLinear<String, DefaultEdge> gravityForceByDegreeLinear = new GravityForceByDegreeLinear<>(
                new IntensityParameter(0.01)
        );
        String[] vertexToTest = {
            "0",
            "1",
            "4",
        };
        Vector2D[] resultVector = {
            new Vector2D(-0.04, -0.08),
            new Vector2D(0.0942, -0.0834),
            new Vector2D(-0.05, -0.05)
        };

        ForceTestUtil.testForceCalculation(forceGraph, gravityForceByDegreeLinear, vertexToTest, resultVector, delta);
    }
}
