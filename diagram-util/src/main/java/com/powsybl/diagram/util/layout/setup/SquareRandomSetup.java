/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.setup;

import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;

import java.util.Objects;
import java.util.Random;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SquareRandomSetup<V, E> implements Setup<V, E> {
    /**
     * The seed used for the randomization
     */
    protected static final long DEFAULT_SEED = 3L;
    /**
     * The random object
     */
    private final Random random;
    /**
     * The center of the graph in the 2D space
     */
    protected Vector2D center = new Vector2D(0, 0);
    /**
     * A scaling factor for the position of points
     */
    protected double scale = 1;

    public SquareRandomSetup(Random random) {
        Objects.requireNonNull(random);
        this.random = random;
    }

    public SquareRandomSetup() {
        this.random = new Random(DEFAULT_SEED);
    }

    /**
     * Put all points in a square of side 1, centered by default on (0, 0)
     * @param layoutContext the context of the layout, the graph and the position of points
     */
    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        Objects.requireNonNull(layoutContext);
        layoutContext.setCenter(center);
        for (V vertex : layoutContext.getSimpleGraph().vertexSet()) {
            if (layoutContext.getFixedNodes().contains(vertex)) {
                layoutContext.getFixedPoints().put(vertex, layoutContext.getInitialPoints().get(vertex));
            } else {
                Point initialPoint = layoutContext.getInitialPoints().get(vertex);
                layoutContext.getMovingPoints().put(vertex, Objects.requireNonNullElseGet(initialPoint, () -> new Point(
                        layoutContext.getOrigin().getPosition().getX() + scale * (random.nextDouble() - 0.5),
                        layoutContext.getOrigin().getPosition().getY() + scale * (random.nextDouble() - 0.5)
                )));
            }
        }
        layoutContext.getAllPoints().putAll(layoutContext.getMovingPoints());
        layoutContext.getAllPoints().putAll(layoutContext.getFixedPoints());
    }
}
