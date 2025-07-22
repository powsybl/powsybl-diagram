/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.layouts.LayoutAlgorithm;
import com.powsybl.diagram.util.forcelayout.layouts.BasicLayout;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.BasicParameters;
import com.powsybl.diagram.util.forcelayout.setup.Setup;
import com.powsybl.diagram.util.forcelayout.setup.SimpleBarycenterSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/**
 * Main entrypoint for the layout module, create a layout, use {@link #run(LayoutContext)} to start the calculations.
 * To transform the output of this to a SVG, use either {@link LayoutContext#toSVG(Function, Writer)} or {@link LayoutContext#toSVG(Function, Path)} to get the default svg implementation<br>
 * Note that you do not need to create a new <code>Layout</code> to run on a different <code>layoutContext</code>, you can just re-use the same layout object and start with run again
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Layout<V, E> {
    private final Setup<V, E> setup;
    private final LayoutAlgorithm<V, E> layoutAlgorithm;

    private static final Logger LOGGER = LoggerFactory.getLogger(Layout.class);

    /**
     * Create a new Layout instance with the provided setup and algorithm
     * @param setup the setup to be used
     * @param layoutAlgorithm the algorithm to place the points once the setup has been applied
     */
    public Layout(Setup<V, E> setup, LayoutAlgorithm<V, E> layoutAlgorithm) {
        this.setup = Objects.requireNonNull(setup);
        this.layoutAlgorithm = Objects.requireNonNull(layoutAlgorithm);
    }

    /**
     * Get the default Basic algorithm, with {@link SimpleBarycenterSetup} and {@link BasicLayout}
     * @return a ready to run basic algorithm, with default parameters
     */
    public static <V, E> Layout<V, E> getBasicDefaultLayout() {
        return new Layout<>(
                new SimpleBarycenterSetup<>(),
                new BasicLayout<>(
                        new BasicParameters.Builder().build()
                )
        );
    }

    /**
     * Run the setup and the algorithm of layout on the provided layoutContext
     * @param layoutContext the context of the layout, containing the graph and the position of the points
     */
    public void run(LayoutContext<V, E> layoutContext) {
        Objects.requireNonNull(layoutContext);
        long start = System.nanoTime();
        setup.run(layoutContext);
        long setupEnd = System.nanoTime();
        LOGGER.info("Setup took {} s", (setupEnd - start) / 1e9);
        layoutAlgorithm.run(layoutContext);
        LOGGER.info("Layout calculations took {} s", (System.nanoTime() - setupEnd) / 1e9);
    }
}
