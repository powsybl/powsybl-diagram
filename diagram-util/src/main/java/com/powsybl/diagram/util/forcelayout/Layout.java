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
import com.powsybl.diagram.util.forcelayout.layouts.BasicLayout;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.BasicParameters;
import com.powsybl.diagram.util.forcelayout.setup.Setup;
import com.powsybl.diagram.util.forcelayout.setup.SimpleBarycenterSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Main entrypoint for the layout module, create a layout, use {@link #run(LayoutContext)} to start the calculations.
 * Use either {@link #toSVG(Function, Path)} or {@link #toSVG(Function, Writer)} to get the default svg implementation<br>
 * Alternatively, use {@link #getStablePosition(Object)} to get the position of the point corresponding to the vertex object, this allows to write your own output<br>
 * Note that you do not need to create a new <code>Layout</code> to run on a different <code>layoutContext</code>, you can just re-use the same layout object and start with run again
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Layout<V, E> {
    private final Setup<V, E> setup;
    private final LayoutAlgorithm<V, E> layoutAlgorithm;
    private boolean hasBeenExecuted = false;
    private LayoutContext<V, E> layoutContext;
    private Vector2D center = new Vector2D(0, 0);

    private static final Logger LOGGER = LoggerFactory.getLogger(Layout.class);

    /**
     * Create a new Layout instance with the provided setup and algorithm
     * @param setup the setup to be used
     * @param layoutAlgorithm the algorithm to place the points once the setup has been applied
     */
    public Layout(Setup<V, E> setup, LayoutAlgorithm<V, E> layoutAlgorithm) {
        this.setup = setup;
        this.layoutAlgorithm = layoutAlgorithm;
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
        this.layoutContext = layoutContext;
        this.layoutContext.setCenter(center);
        long start = System.nanoTime();
        setup.run(layoutContext);
        long setupEnd = System.nanoTime();
        LOGGER.info("Setup took {} s", (setupEnd - start) / 1e9);
        layoutAlgorithm.run(layoutContext);
        LOGGER.info("Layout calculations took {} s", (System.nanoTime() - setupEnd) / 1e9);
        hasBeenExecuted = true;
    }

    /**
     * Write a svg at the provided path, using the tooltip as a text appearing when hovering a given vertex of the graph in the SVG
     * @param tooltip associates each vertex of the graph to a message which will be displayed when hovering the mouse over the vertex in the SVG
     * @param path the path to write this SVG to
     * @throws IOException if the path does not exist, the program is lacking permission, or other reasons for which the SVG could not be written
     */
    public void toSVG(Function<V, String> tooltip, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            toSVG(tooltip, writer);
        }
    }

    /**
     * Write a svg in the provided writer, using the tooltip as a text appearing when hovering a given vertex of the graph in the SVG
     * @param tooltip associates each vertex of the graph to a message which will be displayed when hovering the mouse over the vertex in the SVG
     * @param writer the writer in which to write the SVG
     */
    public void toSVG(Function<V, String> tooltip, Writer writer) {
        if (!hasBeenExecuted) {
            LOGGER.warn("Force layout has not been executed yet");
            return;
        }
        layoutContext.toSVG(tooltip, writer);
    }

    /**
     * Get the position of the point associated to the vertex, this will throw a warning if the {@link #run(LayoutContext)} has not finished, but will return a result nonetheless
     * @param vertex the vertex of the graph that is in <code>layoutContext</code> of the <code>run</code>
     * @return the position of the point associated with the vertex
     */
    public Vector2D getStablePosition(V vertex) {
        return layoutContext.getStablePosition(vertex, hasBeenExecuted);
    }

    /**
     * Set the center position of the entire graph in the 2D space, default is (0, 0), this can be changed by setup for example
     * @param center the position of the center
     */
    public void setCenter(Vector2D center) {
        this.center = center;
    }

    /**
     * Get the center position of the graph in the 2D space
     * @return the position of the center
     */
    public Vector2D getCenter() {
        if (layoutContext != null) {
            return this.layoutContext.getOrigin().getPosition();
        } else {
            return this.center;
        }
    }
}
