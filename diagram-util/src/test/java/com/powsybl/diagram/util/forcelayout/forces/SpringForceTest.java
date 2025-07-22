/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class SpringForceTest {

    @Test
    void apply() {
        double delta = 1e-3;
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext();
        SpringForce<String, DefaultEdge> springForce = new SpringForce<>();
        springForce.init(layoutContext);
        String[] vertexToTest = {
            "3",
            "1",
            "4"
        };
        Vector2D[] resultVector = {
            new Vector2D(10.355339, -10.355339),
            new Vector2D(368.104, 534.405),
            new Vector2D(0, 0)
        };
        ForceTestUtil.testForceCalculation(layoutContext, springForce, vertexToTest, resultVector, delta);
    }
}
