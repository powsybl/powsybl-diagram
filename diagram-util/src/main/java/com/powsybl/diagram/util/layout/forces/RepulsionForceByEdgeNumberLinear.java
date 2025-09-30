/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces;

import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;

import java.util.Map;

/**
 * A repulsion force between a point and all the other points of the graph. It is simpler to calculate than the coulomb force.
 * It depends on the number of edges of the points, it's stronger when points are closer and weaker when points are further away.
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class RepulsionForceByEdgeNumberLinear<V, E> extends AbstractByEdgeNumberForce<V, E> {

    private final double forceIntensity;
    private final boolean effectFromFixedNodes;

    public RepulsionForceByEdgeNumberLinear(double forceIntensity, boolean effectFromFixedNodes) {
        this.forceIntensity = forceIntensity;
        this.effectFromFixedNodes = effectFromFixedNodes;
    }

    @Override
    public Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext) {
        Vector2D resultingForce = new Vector2D();
        int thisVertexDegree = point.getPointVertexDegree();
        for (Map.Entry<V, Point> otherVertexPoint : layoutContext.getMovingPoints().entrySet()) {
            if (otherVertexPoint.getValue() != point) {
                linearRepulsionBetweenPoints(
                        forceIntensity,
                        resultingForce,
                        thisVertexDegree,
                        point,
                        otherVertexPoint.getValue()
                );
            }
        }
        if (effectFromFixedNodes) {
            for (Map.Entry<V, Point> otherVertexPoint : layoutContext.getFixedPoints().entrySet()) {
                linearRepulsionBetweenPoints(
                        forceIntensity,
                        resultingForce,
                        thisVertexDegree,
                        point,
                        otherVertexPoint.getValue()
                );
            }
        }
        return resultingForce;
    }

    private void linearRepulsionBetweenPoints(
            double forceIntensity,
            Vector2D resultingForce,
            int vertexDegree,
            Point point,
            Point otherPoint
    ) {
        // The force goes from the otherPoint to the point (repulsion)
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherPoint, point);
        // divide by magnitude^2 because the force multiplies the unit vector by something/magnitude
        // the unit vector is Vector/magnitude, thus the force is Vector/magnitude * something/magnitude, thus Vector/magnitude^2
        // if we just use the vector and not the unit vector, points that are further away will have the same influence as points that are close
        // this is easy to explain as the formula is Vector * k * deg(n1) * deg(n2)/distance
        // which would be UnitVector * k * deg(n1) * deg(n2)
        // all UnitVector will have the same magnitude of 1, giving only the direction, thus the force becomes dependant only on the degree of the nodes
        // the name "linear" is a bit misleading, as its technically inverse linear (1 / distance)
        double intensity = forceIntensity
                * (vertexDegree + 1)
                * (otherPoint.getPointVertexDegree() + 1)
                / force.magnitudeSquare();
        force.multiplyBy(intensity);
        resultingForce.add(force);
    }
}
