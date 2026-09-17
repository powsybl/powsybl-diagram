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
class CoulombForceTest {

    @Test
    void apply() {
        double delta = 1e-4;
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        CoulombForce<String, DefaultEdge> force = new CoulombForce<>(2, true);
        String[] vertexToTest = {"0"};
        Vector2D[] resultVector = {
            new Vector2D(-3.9493, 3.8805)
        };
        ForceTestUtil.testForceCalculation(layoutContext, force, vertexToTest, resultVector, delta);
    }
}
