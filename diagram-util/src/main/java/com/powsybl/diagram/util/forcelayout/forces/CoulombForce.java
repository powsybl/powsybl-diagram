/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityEffectFromFixedNodesParameters;
import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class CoulombForce<V, E> implements Force<V, E> {

    private final IntensityEffectFromFixedNodesParameters forceParameter;

    public CoulombForce(IntensityEffectFromFixedNodesParameters forceParameter) {
        this.forceParameter = forceParameter;
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
        if (forceParameter.isEffectFromFixedNodes()) {
            for (Point otherFixedPoint : layoutContext.getFixedPoints().values()) {
                coulombBetweenPoints(resultingForce, point, otherFixedPoint);
            }
        }
        return resultingForce;
    }

    private void coulombBetweenPoints(Vector2D resultingForce, Point correspondingPoint, Point otherPoint) {
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherPoint, correspondingPoint);
        double magnitude = force.magnitude();
        /// this is the contracted version of calculating Coulomb, if we take V as the vector from otherPoint to correspondingPoint, then
        /// if we take M(V) the magnitude of the vector V:
        /// F = V / M(V) * forceIntensity / M(V)^2
        /// The original version of the code did F = V / M(V) * forceIntensity / (M(V)^2 * 0.5 + 0.1)
        /// adding 0.1 was to prevent division by 0 (which could still happen when normalizing the vector
        /// multiplying by 0.5 is supposed to be used to tell that each point is moved by half of the force (since both points move)
        /// but that was incorrect since we multiply by 0.5 at the denominator, meaning we multiply by 2 the force
        /// In a goal of getting the same results after the refactor, this implementation of Coulomb does the same
        /// even if it could be considered "incorrect" compared to the original goal of those operations
        double intensity = forceParameter.getForceIntensity() / (magnitude * magnitude * magnitude * 0.5 + 0.1 * magnitude);
        force.multiplyBy(intensity);
        resultingForce.add(force);
    }
}
