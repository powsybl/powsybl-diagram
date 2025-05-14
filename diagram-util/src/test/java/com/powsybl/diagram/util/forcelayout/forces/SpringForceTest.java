/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.SpringyLayout;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class SpringForceTest {

    @Test
    void calculateForce() {
        double delta = 1e-3;
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        SpringForce<String, DefaultEdge> springForce = new SpringForce<>(
                SpringyLayout.initializeSprings(forceGraph)
        );
        Vector2D onPoint3 = springForce.calculateForce(
                "3",
                forceGraph.getMovingPoints().get("3"),
                forceGraph
        );
        assertEquals(10.355339, onPoint3.getX(), delta);
        assertEquals(-10.355339, onPoint3.getY(), delta);
        Vector2D onPoint1 = springForce.calculateForce(
                "1",
                forceGraph.getFixedPoints().get("1"),
                forceGraph
        );
        assertEquals(368.104, onPoint1.getX(), delta);
        assertEquals(534.405, onPoint1.getY(), delta);
        Vector2D onPoint4 = springForce.calculateForce(
                "4",
                forceGraph.getMovingPoints().get("4"),
                forceGraph
        );
        assertEquals(0, onPoint4.getX(), delta);
        assertEquals(0, onPoint4.getY(), delta);

    }
}
