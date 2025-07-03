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
import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class GravityForceLinearTest {

    @Test
    void apply() {
        double delta = 1e-5;
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getForcegraph();
        GravityForceLinear<String, DefaultEdge> gravityForceLinear = new GravityForceLinear<>(
                new IntensityParameter(0.01)
        );
        String[] vertexToTest = {
            "1",
            "2",
            "4"
        };
        Vector2D[] resultVector = {
            new Vector2D(0.0314, -0.0278),
            new Vector2D(-0.01414, -0.15),
            new Vector2D(-0.05, -0.05)
        };
        ForceTestUtil.testForceCalculation(layoutContext, gravityForceLinear, vertexToTest, resultVector, delta);
    }
}
