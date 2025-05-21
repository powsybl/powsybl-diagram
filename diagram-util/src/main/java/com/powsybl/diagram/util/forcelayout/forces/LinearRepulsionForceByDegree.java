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

import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LinearRepulsionForceByDegree<V, E> extends AbstractForce<V, E> {

    private final IntensityEffectFromFixedNodesParameters forceParameter;

    public LinearRepulsionForceByDegree(IntensityEffectFromFixedNodesParameters forceParameter) {
        this.forceParameter = forceParameter;
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph) {
        Vector2D resultingForce = new Vector2D();
        int thisVertexDegree = forceGraph.getSimpleGraph().degreeOf(forThisVertex);
        for (Map.Entry<V, Point> otherVertexPoint : forceGraph.getMovingPoints().entrySet()) {
            if (otherVertexPoint.getValue() == correspondingPoint) {
                continue;
            }
            linearRepulsionBetweenPoints(
                    resultingForce,
                    thisVertexDegree,
                    correspondingPoint,
                    otherVertexPoint,
                    forceGraph
            );
        }
        if (forceParameter.isEffectFromFixedNodes()) {
            for (Map.Entry<V, Point> otherVertexPoint : forceGraph.getFixedPoints().entrySet()) {
                linearRepulsionBetweenPoints(
                        resultingForce,
                        thisVertexDegree,
                        correspondingPoint,
                        otherVertexPoint,
                        forceGraph
                );
            }
        }
        return resultingForce;
    }

    private void linearRepulsionBetweenPoints(
            Vector2D resultingForce,
            int thisVertexDegree,
            Point correspondingPoint,
            Map.Entry<V, Point> otherVertexPoint,
            ForceGraph<V, E> forceGraph
    ) {
        // The force goes from the otherPoint to the correspondingPoint (repulsion)
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherVertexPoint.getValue(), correspondingPoint);
        double intensity = forceParameter.getForceIntensity()
                * (thisVertexDegree + 1)
                * (forceGraph.getSimpleGraph().degreeOf(otherVertexPoint.getKey()) + 1)
                / (force.magnitude());
        force.multiplyBy(intensity);
        resultingForce.add(force);
    }
}
