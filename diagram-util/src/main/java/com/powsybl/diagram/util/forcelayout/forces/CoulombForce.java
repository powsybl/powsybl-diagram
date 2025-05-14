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
        for (Point otherMovingPoint : forceGraph.getMovingPoints().values()) {
            if (otherMovingPoint == correspondingPoint) {
                continue;
            }
            coulombBetweenPoints(resultingForce, correspondingPoint, otherMovingPoint);
        }
        if (forceParameter.isEffectFromFixedNodes()) {
            for (Point otherFixedPoint : forceGraph.getFixedPoints().values()) {
                coulombBetweenPoints(resultingForce, correspondingPoint, otherFixedPoint);
            }
        }
        return resultingForce;
    }

    private void coulombBetweenPoints(Vector2D resultingForce, Point correspondingPoint, Point otherPoint) {
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherPoint, correspondingPoint);
        double magnitudeSquare = force.magnitudeSquare();
        force.normalize();
        // 0.5 because we assume both points are moving, so each does half of the movement. Add 0.1 to avoid division by 0 errors
        force.multiply(forceParameter.getForceIntensity());
        force.divide(magnitudeSquare * 0.5 + 0.1);
        // might be good to have a method to add to a vector2D in place, but it's not currently possible because Vector2D's fields are final
        resultingForce.add(force);
    }
}
