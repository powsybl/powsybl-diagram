/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.forceparameter.SpringParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.optimizationsartifacts.OptimizationArtifactsContainer;
import org.jgrapht.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringForce<V, E> implements Force<V, E, SpringParameter> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringForce.class);

    /// This is Hooke's Law
    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph, SpringParameter forceParameter) {
        Vector2D resultingForce = new Vector2D(0, 0);
        for (V otherVertex : Graphs.neighborSetOf(forceGraph.getGraph(), forThisVertex)) {
            Point otherPoint = forceGraph.getMovingPoints().get(otherVertex);
            if (otherPoint == null) {
                otherPoint = forceGraph.getFixedPoints().get(otherVertex);
            }
            if (otherPoint == null) {
                throw new NullPointerException(String.format("No such point corresponding to the given vertex in either moving or non-moving points: Vertex %s", otherVertex));
            }

            Vector2D direction = Vector2D.calculateVectorBetweenPoints(correspondingPoint, otherPoint);
            double displacement = direction.magnitude() - forceParameter.getLength();
            Vector2D unitDirection = direction.normalize();

            // multiply by 0.5 because each vertex will move half of the distance, assuming both are free
            // should this be different if the other point is not moving ?
            Vector2D force = unitDirection.multiply(forceParameter.getStiffness() * displacement * 0.5);
            resultingForce.add(force);
        }
        return resultingForce;
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph, SpringParameter forceParameter, OptimizationArtifactsContainer optimizationArtifacts) {
        LOGGER.warn("There is no optimized version for the spring force, directly call the unoptimized version instead to avoid unnecessary redirection calls");
        return calculateForce(forThisVertex, correspondingPoint, forceGraph, forceParameter);
    }
}
