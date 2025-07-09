/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.Layout;
import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;

import java.util.Objects;
import java.util.Random;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringySetup<V, E> implements Setup<V, E> {
    private final Random random;

    public SpringySetup(Random random) {
        this.random = random;
    }

    public SpringySetup() {
        this.random = new Random(3L);
    }

    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        int nbUnknownPositions = layoutContext.getSimpleGraph().vertexSet().size() - layoutContext.getInitialPoints().size();

        // Initialize the missing positions by use the default random number generator.
        // Apply a scale depending on the number of unknown positions to have an expected mean distance remain around the same value.
        // The positions are around the center of given initial positions.
        double scale = Math.sqrt(nbUnknownPositions) * 5;
        Vector2D initialPointsCenter = new Vector2D();
        if (!layoutContext.getInitialPoints().isEmpty()) {
            layoutContext.getInitialPoints().values().stream()
                    .map(Point::getPosition)
                    .forEach(initialPointsCenter::add);
            initialPointsCenter.divideBy(layoutContext.getInitialPoints().size());
        }
        layoutContext.setCenter(initialPointsCenter);

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
    }
}
