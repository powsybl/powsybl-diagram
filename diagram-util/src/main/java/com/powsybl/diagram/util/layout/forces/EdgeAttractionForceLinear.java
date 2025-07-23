/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces;

import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import org.jgrapht.Graphs;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class EdgeAttractionForceLinear<V, E> implements Force<V, E> {
    private final double forceIntensity;

    public EdgeAttractionForceLinear(double forceIntensity) {
        this.forceIntensity = forceIntensity;
    }

    @Override
    public Vector2D apply(V forThisVertex, Point correspondingPoint, LayoutContext<V, E> layoutContext) {
        Vector2D resultingForce = new Vector2D();
        for (V otherVertex : Graphs.neighborSetOf(layoutContext.getSimpleGraph(), forThisVertex)) {
            Point otherPoint = layoutContext.getAllPoints().get(otherVertex);
            forceBetweenPoints(resultingForce, correspondingPoint, otherPoint);
        }
        return resultingForce;
    }

    private void forceBetweenPoints(Vector2D resultingForce, Point correspondingPoint, Point otherPoint) {
        Vector2D force = Vector2D.calculateVectorBetweenPoints(correspondingPoint, otherPoint);
        force.multiplyBy(forceIntensity);
        resultingForce.add(force);
    }
}

