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

/**
 * A repulsion between a point and all the other points of the graph. This is similar to the electrostatic repulsion force.
 * The force is stronger at close distance and gets smaller as points get further away.
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class CoulombForce<V, E> implements Force<V, E> {

    private final double forceIntensity;
    private final boolean effectFromFixedNodes;

    public CoulombForce(double forceIntensity, boolean effectFromFixedNodes) {
        this.forceIntensity = forceIntensity;
        this.effectFromFixedNodes = effectFromFixedNodes;
    }

    @Override
    public Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext) {
        Vector2D resultingForce = new Vector2D(0, 0);
        for (Point otherMovingPoint : layoutContext.getMovingPoints().values()) {
            if (otherMovingPoint == point) {
                continue;
            }
            coulombBetweenPoints(resultingForce, point, otherMovingPoint);
        }
        if (effectFromFixedNodes) {
            for (Point otherFixedPoint : layoutContext.getFixedPoints().values()) {
                coulombBetweenPoints(resultingForce, point, otherFixedPoint);
            }
        }
        return resultingForce;
    }

    private void coulombBetweenPoints(Vector2D resultingForce, Point point, Point otherPoint) {
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherPoint, point);
        double magnitude = force.magnitude();
        // this is the contracted version of calculating Coulomb, if we take V as the vector from otherPoint to point, then
        // if we take M(V) the magnitude of the vector V:
        // F = V / M(V) * forceIntensity / M(V)^2
        // The original version of the code did F = V / M(V) * forceIntensity / (M(V)^2 * 0.5 + 0.1)
        // adding 0.1 was to prevent division by 0 (which could still happen when normalizing the vector
        // multiplying by 0.5 is supposed to be used to tell that each point is moved by half of the force (since both points move)
        // but that was incorrect since we multiply by 0.5 at the denominator, meaning we multiply by 2 the force
        // In a goal of getting the same results after the refactor, this implementation of Coulomb does the same
        // even if it could be considered "incorrect" compared to the original goal of those operations
        double intensity = forceIntensity / (magnitude * magnitude * magnitude * 0.5 + 0.1 * magnitude);
        force.multiplyBy(intensity);
        resultingForce.add(force);
    }
}
