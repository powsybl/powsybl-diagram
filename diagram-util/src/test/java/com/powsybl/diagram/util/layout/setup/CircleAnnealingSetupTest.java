/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.setup;

import com.powsybl.diagram.util.layout.GraphTestData;
import com.powsybl.diagram.util.layout.ResourceUtils;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class CircleAnnealingSetupTest {

    @Test
    void setup() {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Trace");
        LayoutContext<String, DefaultEdge> layoutContext = new LayoutContext<>(GraphTestData.getGraph2());
        CircleAnnealingSetup<String, DefaultEdge> circleAnnealingSetup = new CircleAnnealingSetup<>(GraphTestData.RANDOM);
        circleAnnealingSetup.run(layoutContext);
        StringWriter sw = new StringWriter();
        layoutContext.toSVG(v -> String.format("Vertex %s", v), sw);
        assertEquals(ResourceUtils.toString("circle_annealing_setup.svg"), sw.toString());
    }
}
