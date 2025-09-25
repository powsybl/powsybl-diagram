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
class RepulsionForceByEdgeNumberLinearTest {

    @Test
    void calculateForce() {
        double delta = 1e-4;
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext();
        RepulsionForceByEdgeNumberLinear<String, DefaultEdge> repulsionForceByEdgeNumberLinear = new RepulsionForceByEdgeNumberLinear<>(
                        0.34,
                        true
        );
        repulsionForceByEdgeNumberLinear.init(layoutContext);
        String[] vertexToTest = {"0", "4"};
        Vector2D[] resultVector = {
            new Vector2D(-1.99586, 2.06396),
            new Vector2D(0.463784, 0.201774),
        };

        ForceTestUtil.testForceCalculation(layoutContext, repulsionForceByEdgeNumberLinear, vertexToTest, resultVector, delta);

        RepulsionForceByEdgeNumberLinear<String, DefaultEdge> repulsionForceByEdgeNumberLinearNoFixed = new RepulsionForceByEdgeNumberLinear<>(
                        0.34,
                        false
        );

        Vector2D[] resultVectorNoFixed = {
            new Vector2D(-2.94758, 2.24327),
            new Vector2D(0.347152, 0.169965),
        };

        ForceTestUtil.testForceCalculation(layoutContext, repulsionForceByEdgeNumberLinearNoFixed, vertexToTest, resultVectorNoFixed, delta);
    }
}
