/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.LayoutAlgorithm;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.LayoutParameters;
import com.powsybl.diagram.util.forcelayout.setup.Setup;
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
public class Layout<V, E> {
    private Setup<V, E> setup;
    private LayoutAlgorithm<V, E> layoutAlgorithm;
    private boolean hasBeenExecuted = false;
    private LayoutContext<V, E> layoutContext;
    private Vector2D center = new Vector2D(0, 0);

    // Suppress the warning about possible unsafe Random, because we use this for simulation and not cryptography
    @java.lang.SuppressWarnings("java:S2245")
    private final Random random = new Random(3L);

    private static final Logger LOGGER = LoggerFactory.getLogger(Layout.class);

    Layout(Setup<V, E> setup, LayoutParameters<V, E> layoutParameters) {
        this.setup = setup;
        chooseLayoutAlgorithm(layoutParameters);
    }

    private void chooseLayoutAlgorithm(LayoutParameters<V, E> layoutParameters) {
        this.layoutAlgorithm = layoutParameters.createLayout();
    }

    public void run(LayoutContext<V, E> layoutContext) {
        this.layoutContext = layoutContext;
        this.layoutContext.setCenter(center);
        long start = System.nanoTime();
        setup.setup(layoutContext, random);
        long setupEnd = System.nanoTime();
        LOGGER.info("Setup took {} s", (setupEnd - start) / 1e9);
        layoutAlgorithm.calculateLayout(layoutContext);
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
        layoutContext.toSVG(tooltip, writer);
    }

    public Vector2D getStablePosition(V vertex) {
        return layoutContext.getStablePosition(vertex, hasBeenExecuted);
    }

    public void setCenter(Vector2D center) {
        this.center = center;
    }

    public Vector2D getCenter() {
        if (layoutContext != null) {
            return this.layoutContext.getOrigin().getPosition();
        } else {
            return this.center;
        }
    }
}
