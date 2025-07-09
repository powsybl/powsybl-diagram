/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.Helpers;
import com.powsybl.diagram.util.forcelayout.Layout;
import com.powsybl.diagram.util.forcelayout.layouts.SpringyLayout;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.SpringyParameters;
import com.powsybl.diagram.util.forcelayout.setup.SpringySetup;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class LayoutContextTest {
    @TempDir
    File tempDirectory;

    @Test
    void graphCreation() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getForcegraph();
        assertEquals(5, layoutContext.getSimpleGraph().vertexSet().size());
        assertEquals(4, layoutContext.getSimpleGraph().edgeSet().size());
        assertEquals(1, layoutContext.getFixedPoints().size());
        assertEquals(1, layoutContext.getFixedNodes().size());
        assertEquals(5, layoutContext.getInitialPoints().size());
        assertEquals(4, layoutContext.getMovingPoints().size());
    }

    @Test
    void setFixedPoints() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getForcegraph();
        Map<String, Point> fixedPoints = new HashMap<>();
        fixedPoints.put("2", new Point(1.414, 15));
        fixedPoints.put("4", new Point(0, 0));
        fixedPoints.put("-1", new Point(-2, 6));
        layoutContext.setFixedPoints(fixedPoints);
        assertEquals(2, layoutContext.getInitialPoints().size());
    }

    @Test
    void toSvg() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getForcegraph();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        Layout<String, DefaultEdge> layout = new Layout<>(
                new SpringySetup<>(new Random(3L)),
                new SpringyLayout<>(new SpringyParameters.Builder().build())
        );
        layout.run(layoutContext);
        StringWriter sw = new StringWriter();
        layout.toSVG(tooltip, sw);
        Helpers helpers = new Helpers();
        assertEquals(helpers.toString("/springy_5_nodes.svg"), sw.toString());
    }

    @Test
    void notExecuted() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getForcegraph();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        Layout<String, DefaultEdge> layout = new Layout<>(
                new SpringySetup<>(new Random(3L)),
                new SpringyLayout<>(new SpringyParameters.Builder().build())
        );
        assertDoesNotThrow(() -> layout.toSVG(tooltip, tempDirectory.toPath().resolve("test.svg")));
        assertDoesNotThrow(() -> layoutContext.getStablePosition("1", false));
    }
}
