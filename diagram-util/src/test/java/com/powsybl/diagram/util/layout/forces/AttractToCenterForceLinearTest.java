/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces;

import com.powsybl.diagram.util.layout.GraphTestData;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class AttractToCenterForceLinearTest {

    @Test
    void apply() {
        double delta = 1e-5;
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext();
        AttractToCenterForceLinear<String, DefaultEdge> attractToCenterForceLinear = new AttractToCenterForceLinear<>(0.01);
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
        ForceTestUtil.testForceCalculation(layoutContext, attractToCenterForceLinear, vertexToTest, resultVector, delta);
    }
}
