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
import java.util.Random;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringySetup<V, E> implements Setup<V, E> {

    @Override
    public void setup(ForceGraph<V, E> forceGraph, Random random) {
        int nbUnknownPositions = forceGraph.getSimpleGraph().vertexSet().size() - forceGraph.getInitialPoints().size();

        // Initialize the missing positions by use the default random number generator.
        // Apply a scale depending on the number of unknown positions to have an expected mean distance remain around the same value.
        // The positions are around the center of given initial positions.
        double scale = Math.sqrt(nbUnknownPositions) * 5;
        Vector2D initialPointsCenter = new Vector2D();
        if (!forceGraph.getInitialPoints().isEmpty()) {
            forceGraph.getInitialPoints().values().stream()
                    .map(Point::getPosition)
                    .forEach(initialPointsCenter::add);
            initialPointsCenter.divideBy(forceGraph.getInitialPoints().size());
        }
        forceGraph.setCenter(initialPointsCenter);

        for (V vertex : forceGraph.getSimpleGraph().vertexSet()) {
            if (forceGraph.getFixedNodes().contains(vertex)) {
                forceGraph.getFixedPoints().put(vertex, forceGraph.getInitialPoints().get(vertex));
            } else {
                Point initialPoint = forceGraph.getInitialPoints().get(vertex);
                forceGraph.getMovingPoints().put(vertex, Objects.requireNonNullElseGet(initialPoint, () -> new Point(
                        forceGraph.getOrigin().getPosition().getX() + scale * (random.nextDouble() - 0.5),
                        forceGraph.getOrigin().getPosition().getY() + scale * (random.nextDouble() - 0.5)
                )));
            }
        }
        // this is the same as SimpleSetup, could probably put a function that does that in abstract setup
        // TODO wait for refactor merge to avoid too many conflicts
        forceGraph.getAllPoints().putAll(forceGraph.getMovingPoints());
        forceGraph.getAllPoints().putAll(forceGraph.getFixedPoints());
    }
}
