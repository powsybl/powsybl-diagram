/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.SpringyParameters;
import com.powsybl.diagram.util.forcelayout.setup.SetupEnum;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class LayoutAlgorithmRunnerTest {
    @TempDir
    File tempDirectory;

    @Test
    void svgNotExecuted() {
        LayoutAlgorithmRunner<String, DefaultEdge> runner = new LayoutAlgorithmRunner<>(
                SetupEnum.SPRINGY,
                new SpringyParameters.Builder().build()
        );
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        assertDoesNotThrow(() -> runner.toSVG(tooltip, tempDirectory.toPath().resolve("test.svg")));
    }

    @Test
    void testCenter() {
        LayoutAlgorithmRunner<String, DefaultEdge> runner = new LayoutAlgorithmRunner<>(
                SetupEnum.SIMPLE,
                new SpringyParameters.Builder().build()
        );
        Vector2D newCenter = new Vector2D(-445, 23.3);
        runner.setCenter(newCenter);
        assertEquals(newCenter.getX(), runner.getCenter().getX());
        assertEquals(newCenter.getY(), runner.getCenter().getY());
        Vector2D otherNewCenter = new Vector2D(3.4, -6.1);
        runner.setCenter(otherNewCenter);
        ForceGraph<String, DefaultEdge> forceGraph = GraphTestData.getForceGraph1();
        runner.run(forceGraph);
        assertEquals(otherNewCenter.getX(), runner.getCenter().getX());
        assertEquals(otherNewCenter.getY(), runner.getCenter().getY());
    }
}
