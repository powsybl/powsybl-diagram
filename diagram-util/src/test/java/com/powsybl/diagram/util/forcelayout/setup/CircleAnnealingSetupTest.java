/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.Helpers;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class CircleAnnealingSetupTest {

    @Test
    void setup() {
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForceGraph2NoSetup();
        CircleAnnealingSetup<String, DefaultEdge> circleAnnealingSetup = new CircleAnnealingSetup<>();
        circleAnnealingSetup.setup(forceGraph, GraphTestData.RANDOM);
        StringWriter sw = new StringWriter();
        forceGraph.toSVG(v -> String.format("Vertex %s", v), sw);
        Helpers helper = new Helpers();
        assertEquals(helper.toString("/circle_annealing_setup.svg"), sw.toString());
    }
}
