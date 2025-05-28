/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityEffectFromFixedNodesWithVertexDegreeParameters;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;

import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LinearRepulsionForceByDegree<V, E> extends AbstractForce<V, E> {

    private final IntensityEffectFromFixedNodesWithVertexDegreeParameters forceParameter;

    public LinearRepulsionForceByDegree(IntensityEffectFromFixedNodesWithVertexDegreeParameters forceParameter) {
        this.forceParameter = forceParameter;
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph) {
        Vector2D resultingForce = new Vector2D();
        // TODO we could make this faster if we knew the index of the vertex, maybe store it in the correspondingPoint ?
        int thisVertexDegree = forceGraph.getSimpleGraph().degreeOf(forThisVertex);
        int otherVertexIndex = 0;
        for (Map.Entry<V, Point> otherVertexPoint : forceGraph.getMovingPoints().entrySet()) {
            if (!(otherVertexPoint.getValue() == correspondingPoint)) {
                linearRepulsionBetweenPoints(
                        resultingForce,
                        thisVertexDegree,
                        correspondingPoint,
                        otherVertexPoint,
                        otherVertexIndex
                );
            }
            ++otherVertexIndex;
        }
        if (forceParameter.isEffectFromFixedNodes()) {
            for (Map.Entry<V, Point> otherVertexPoint : forceGraph.getFixedPoints().entrySet()) {
                linearRepulsionBetweenPoints(
                        resultingForce,
                        thisVertexDegree,
                        correspondingPoint,
                        otherVertexPoint,
                        otherVertexIndex
                );
                ++otherVertexIndex;
            }
        }
        return resultingForce;
    }

    private void linearRepulsionBetweenPoints(
            Vector2D resultingForce,
            int thisVertexDegree,
            Point correspondingPoint,
            Map.Entry<V, Point> otherVertexPoint,
            int otherVertexIndex
    ) {
        // The force goes from the otherPoint to the correspondingPoint (repulsion)
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherVertexPoint.getValue(), correspondingPoint);
        // divide by magnitude^2 because we multiply the factor with a degree -1 in magnitude by the unit vector
        // the unit vector is Vector/magnitude, thus the force is something/magnitude * Vector/magnitude, thus something/magnitude^2
        // if we just use the vector and not the unit vector, points that are further away will have the same influence as points that are close
        // this is easy to explain as the formula is Vector * k * deg(n1) * deg(n2)/distance
        // which would be UnitVector * k * deg(n1) * deg(n2)
        // all UnitVector will have the same magnitude of 1, giving only the direction, thus the force becomes dependant only on the degree of the nodes
        double intensity = forceParameter.getForceIntensity()
                * (thisVertexDegree + 1)
                * (forceParameter.getDegreeOfVertex()[otherVertexIndex] + 1)
                / force.magnitudeSquare();
        force.multiplyBy(intensity);
        resultingForce.add(force);
    }
}
