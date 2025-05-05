/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static com.powsybl.diagram.util.forcelayout.geometry.ForceGraph.ORIGIN;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringySetup<V, E> implements Setup<V, E> {

    // Suppress the warning about possible unsafe Random, because we use this for simulation and not cryptography
    @java.lang.SuppressWarnings("java:S2245")
    private final Random random = new Random(3L);

    @Override
    public void setup(ForceGraph<V, E> forceGraph) {
        int nbUnknownPositions = forceGraph.getGraph().vertexSet().size() - forceGraph.getInitialPoints().size();

        // Initialize the missing positions by use the default random number generator.
        // Apply a scale depending on the number of unknown positions to have an expected mean distance remain around the same value.
        // The positions are around the center of given initial positions.
        double scale = Math.sqrt(nbUnknownPositions) * 5;
        Optional<Vector2D> initialPointsCenter = forceGraph.getInitialPoints().values().stream()
                .map(Point::getPosition)
                .reduce(Vector2D::add)
                .map(sum -> sum.divide(forceGraph.getInitialPoints().size()));
        ForceGraph.setCenter(initialPointsCenter.orElse(new Vector2D(0, 0)));

        for (V vertex : forceGraph.getGraph().vertexSet()) {
            if (forceGraph.getFixedNodes().contains(vertex)) {
                forceGraph.getFixedPoints().put(vertex, forceGraph.getInitialPoints().get(vertex));
            } else {
                Point initialPoint = forceGraph.getInitialPoints().get(vertex);
                forceGraph.getMovingPoints().put(vertex, Objects.requireNonNullElseGet(initialPoint, () -> new Point(
                        ORIGIN.getPosition().x() + scale * (random.nextDouble() - 0.5),
                        ORIGIN.getPosition().y() + scale * (random.nextDouble() - 0.5)
                )));
            }
        }
    }
}
