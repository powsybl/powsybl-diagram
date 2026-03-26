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
class AttractToCenterForceUnitTest {

    @Test
    void apply() {
        double delta = 1e-5;
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        AttractToCenterForceUnit<String, DefaultEdge> attractToCenterForceUnit = new AttractToCenterForceUnit<>(0.01);
        String[] vertexToTest = {
            "1",
            "2",
            "4"
        };
        Vector2D[] resultVector = {
            new Vector2D(0.007487, -0.006629),
            new Vector2D(-0.000939, -0.009956),
            new Vector2D(-0.007071, -0.007071)
        };
        ForceTestUtil.testForceCalculation(layoutContext, attractToCenterForceUnit, vertexToTest, resultVector, delta);
    }
}
