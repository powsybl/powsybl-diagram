/**
 * Copyright (c) 2025-2026, RTE (http://www.rte-france.com)
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
class AttractToCenterForceDegreeBasedLinearTest {

    @Test
    void calculateForce() {
        double delta = 1e-5;
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext();
        AttractToCenterForceDegreeBasedLinear<String, DefaultEdge> attractToCenterForceDegreeBasedLinear = new AttractToCenterForceDegreeBasedLinear<>(0.01);
        attractToCenterForceDegreeBasedLinear.init(layoutContext);
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

        ForceTestUtil.testForceCalculation(layoutContext, attractToCenterForceDegreeBasedLinear, vertexToTest, resultVector, delta);
    }
}
