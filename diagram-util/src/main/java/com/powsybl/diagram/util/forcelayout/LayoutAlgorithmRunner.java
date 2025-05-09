/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.layouts.AbstractLayoutAlgorithm;
import com.powsybl.diagram.util.forcelayout.layouts.layoutsparameters.AbstractLayoutParameters;
import com.powsybl.diagram.util.forcelayout.setup.AbstractSetup;
import com.powsybl.diagram.util.forcelayout.setup.SetupEnum;
import com.powsybl.diagram.util.forcelayout.setup.SpringySetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LayoutAlgorithmRunner<V, E> {
    private AbstractSetup<V, E> setup;
    private AbstractLayoutAlgorithm<V, E> layoutAlgorithm;
    private boolean hasBeenExecuted = false;
    private ForceGraph<V, E> forceGraph;

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutAlgorithmRunner.class);

    LayoutAlgorithmRunner(SetupEnum setupChoice, AbstractLayoutParameters<V, E> layoutParameters) {
        chooseSetup(setupChoice);
        chooseLayoutAlgorithm(layoutParameters);
    }

    private void chooseSetup(SetupEnum setupChoice) {
        this.setup = switch (setupChoice) {
            case SPRINGY -> new SpringySetup<>();
        };
    }

    private void chooseLayoutAlgorithm(AbstractLayoutParameters<V, E> layoutParameters) {
        this.layoutAlgorithm = layoutParameters.createLayout();
    }

    public void run(ForceGraph<V, E> forceGraph) {
        this.forceGraph = forceGraph;
        setup.setup(forceGraph);
        layoutAlgorithm.calculateLayout(forceGraph);
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
}
