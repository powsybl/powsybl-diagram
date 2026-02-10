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
<<<<<<<< HEAD:diagram-util/src/test/java/com/powsybl/diagram/util/layout/forces/AttractToCenterForceByEdgeNumberUnitTest.java
class AttractToCenterForceByEdgeNumberUnitTest {
========
class AttractToCenterForceDegreeBasedUnitTest {
>>>>>>>> main:diagram-util/src/test/java/com/powsybl/diagram/util/layout/forces/AttractToCenterForceDegreeBasedUnitTest.java

    @Test
    void apply() {
        double delta = 1e-5;
<<<<<<<< HEAD:diagram-util/src/test/java/com/powsybl/diagram/util/layout/forces/AttractToCenterForceByEdgeNumberUnitTest.java
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        AttractToCenterForceByEdgeNumberUnit<String, DefaultEdge> attractToCenterForceByEdgeNumberUnit = new AttractToCenterForceByEdgeNumberUnit<>(0.01);
        attractToCenterForceByEdgeNumberUnit.init(layoutContext);
========
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext();
        AttractToCenterForceDegreeBasedUnit<String, DefaultEdge> attractToCenterForceDegreeBasedUnit = new AttractToCenterForceDegreeBasedUnit<>(0.01);
        attractToCenterForceDegreeBasedUnit.init(layoutContext);
>>>>>>>> main:diagram-util/src/test/java/com/powsybl/diagram/util/layout/forces/AttractToCenterForceDegreeBasedUnitTest.java
        String[] vertexToTest = {
            "0",
            "1",
            "4",
        };
        Vector2D[] resultVector = {
            new Vector2D(-0.01789, -0.03578),
            new Vector2D(0.022461, -0.019887),
            new Vector2D(-0.007071, -0.007071)
        };

<<<<<<<< HEAD:diagram-util/src/test/java/com/powsybl/diagram/util/layout/forces/AttractToCenterForceByEdgeNumberUnitTest.java
        ForceTestUtil.testForceCalculation(layoutContext, attractToCenterForceByEdgeNumberUnit, vertexToTest, resultVector, delta);
========
        ForceTestUtil.testForceCalculation(layoutContext, attractToCenterForceDegreeBasedUnit, vertexToTest, resultVector, delta);
>>>>>>>> main:diagram-util/src/test/java/com/powsybl/diagram/util/layout/forces/AttractToCenterForceDegreeBasedUnitTest.java
    }
}
