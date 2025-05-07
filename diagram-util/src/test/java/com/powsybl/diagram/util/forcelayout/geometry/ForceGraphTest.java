/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class ForceGraphTest {
    @Test
    void graphCreation() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForcegraph();
        assertEquals(5, forceGraph.getGraph().vertexSet().size());
        assertEquals(4, forceGraph.getGraph().edgeSet().size());
        assertEquals(1, forceGraph.getFixedPoints().size());
        assertEquals(1, forceGraph.getFixedNodes().size());
        assertEquals(5, forceGraph.getInitialPoints().size());
        assertEquals(4, forceGraph.getMovingPoints().size());
    }

}
