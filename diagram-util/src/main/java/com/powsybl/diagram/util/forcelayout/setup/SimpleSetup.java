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

import java.util.Objects;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SimpleSetup<V, E> extends AbstractSetup<V, E> {

    // very similar to SpringySetup, but the center of the graph is always (0,0) instead of the center of all the points
    @Override
    public void setup(ForceGraph<V, E> forceGraph) {
        for (V vertex : forceGraph.getSimpleGraph().vertexSet()) {
            if (forceGraph.getFixedNodes().contains(vertex)) {
                forceGraph.getFixedPoints().put(vertex, forceGraph.getInitialPoints().get(vertex));
            } else {
                Point initialPoint = forceGraph.getInitialPoints().get(vertex);
                forceGraph.getMovingPoints().put(vertex, Objects.requireNonNullElseGet(initialPoint, () -> new Point(
                        forceGraph.getOrigin().getPosition().getX() + (random.nextDouble() - 0.5),
                        forceGraph.getOrigin().getPosition().getY() + (random.nextDouble() - 0.5)
                )));
            }
        }
    }
}
