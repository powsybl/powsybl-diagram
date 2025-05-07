/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityEffectFromFixedNodesParameters;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.Graphs;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class CoulombForce<V, E> extends AbstractForce<V, E, IntensityEffectFromFixedNodesParameters> {

    public CoulombForce(IntensityEffectFromFixedNodesParameters forceParameter) {
        super(forceParameter);
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph) {
        Vector2D resultingForce = new Vector2D(0, 0);
        for (V otherVertex : forceGraph.getGraph().vertexSet()) {
            if (otherVertex == forThisVertex) {
                continue;
            }
            // it would be good to have a way of knowing directly from the graph if a vertex will be moving or not
            Point otherPoint = forceGraph.getMovingPoints().get(otherVertex);
            if (otherPoint == null) {
                // it might be a fixed point, only check if it exists in the fixed point if we actually care what the effect of fixed nodes is
                if (forceParameter.isEffectFromFixedNodes()) {
                    otherPoint = forceGraph.getFixedPoints().get(otherVertex);
                    if (otherPoint == null) {
                        throw new NullPointerException(String.format("The other vertex does not have a corresponding point in either the moving or non moving points : Vertex %s", otherVertex));
                    }
                } else {
                    // if we don't care, just go to the next vertex
                    continue;
                }
            }

            // direction of the force is from the other point, to the point we are considering (it's repulsion, so it goes away from the other point)
            Vector2D direction = Vector2D.calculateVectorBetweenPoints(otherPoint, correspondingPoint);
            Vector2D unitDirection = direction.normalize();
            // 0.5 because we assume both points are moving, so each does half of the movement. Add 0.1 to avoid division by 0 errors
            Vector2D force = unitDirection.multiply(forceParameter.getForceIntensity()).divide(direction.magnitudeSquare() * 0.5 + 0.1);
            // might be good to have a method to add to a vector2D in place, but it's not currently possible because Vector2D's fields are final
            resultingForce = resultingForce.add(force);
        }
        return resultingForce;
    }
}
