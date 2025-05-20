/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.Graphs;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LinearEdgeAttractionForce<V, E> extends AbstractForce<V, E> {
    private final IntensityParameter forceParameter;

    public LinearEdgeAttractionForce(IntensityParameter forceParameter) {
        this.forceParameter = forceParameter;
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph) {
        Vector2D resultingForce = new Vector2D();
        for (V otherVertex : Graphs.neighborSetOf(forceGraph.getGraph(), forThisVertex)) {
            Point otherPoint = forceGraph.getAllPoints().get(otherVertex);
            forceBetweenPoints(resultingForce, correspondingPoint, otherPoint);
        }
        return resultingForce;
    }

    private void forceBetweenPoints(Vector2D resultingForce, Point correspondingPoint, Point otherPoint) {
        Vector2D force = Vector2D.calculateVectorBetweenPoints(correspondingPoint, otherPoint);
        force.multiplyBy(forceParameter.getForceIntensity());
        resultingForce.add(force);
    }
}

