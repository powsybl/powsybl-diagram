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
import com.powsybl.diagram.util.forcelayout.layouts.LayoutAlgorithm;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.LayoutParameters;
import com.powsybl.diagram.util.forcelayout.setup.Setup;
import com.powsybl.diagram.util.forcelayout.setup.SetupEnum;
import com.powsybl.diagram.util.forcelayout.setup.SimpleSetup;
import com.powsybl.diagram.util.forcelayout.setup.SpringySetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.Function;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LayoutAlgorithmRunner<V, E> {
    private Setup<V, E> setup;
    private LayoutAlgorithm<V, E> layoutAlgorithm;
    private boolean hasBeenExecuted = false;
    private ForceGraph<V, E> forceGraph;
    private Vector2D center = new Vector2D(0, 0);

    // Suppress the warning about possible unsafe Random, because we use this for simulation and not cryptography
    @java.lang.SuppressWarnings("java:S2245")
    private final Random random = new Random(3L);

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutAlgorithmRunner.class);

    LayoutAlgorithmRunner(SetupEnum setupChoice, LayoutParameters<V, E> layoutParameters) {
        chooseSetup(setupChoice);
        chooseLayoutAlgorithm(layoutParameters);
    }

    private void chooseSetup(SetupEnum setupChoice) {
        this.setup = switch (setupChoice) {
            case SPRINGY -> new SpringySetup<>();
            case SIMPLE -> new SimpleSetup<>();
        };
    }

    private void chooseLayoutAlgorithm(LayoutParameters<V, E> layoutParameters) {
        this.layoutAlgorithm = layoutParameters.createLayout();
    }

    public void run(ForceGraph<V, E> forceGraph) {
        this.forceGraph = forceGraph;
        this.forceGraph.setCenter(center);
        long start = System.nanoTime();
        setup.setup(forceGraph, random);
        long setupEnd = System.nanoTime();
        LOGGER.info("Setup took {} s", (setupEnd - start) / 1e9);
        layoutAlgorithm.calculateLayout(forceGraph);
        LOGGER.info("Layout calculations took {} s", (System.nanoTime() - setupEnd) / 1e9);
        hasBeenExecuted = true;
    }

    public void toSVG(Function<V, String> tooltip, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            toSVG(tooltip, writer);
        }
    }

    public void toSVG(Function<V, String> tooltip, Writer writer) {
        if (!hasBeenExecuted) {
            LOGGER.warn("Force layout has not been executed yet");
            return;
        }
        forceGraph.toSVG(tooltip, writer);
    }

    public Vector2D getStablePosition(V vertex) {
        return forceGraph.getStablePosition(vertex, hasBeenExecuted);
    }

    public void setCenter(Vector2D center) {
        this.center = center;
    }

    public Vector2D getCenter() {
        if (forceGraph != null) {
            return this.forceGraph.getOrigin().getPosition();
        } else {
            return this.center;
        }
    }
}
