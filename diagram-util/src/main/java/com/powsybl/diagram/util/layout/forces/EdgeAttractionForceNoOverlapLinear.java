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
import org.jgrapht.Graphs;

/**
 * An attraction force used to prevent overlapping points, given their pointSize
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class EdgeAttractionForceNoOverlapLinear<V, E> extends AbstractNoOverlapForce<V, E> {
    private final double forceIntensity;

    public EdgeAttractionForceNoOverlapLinear(double forceIntensity, double pointSizeScale, double pointSizeOffset) {
        super(pointSizeScale, pointSizeOffset);
        this.forceIntensity = forceIntensity;
    }

    @Override
    public Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext) {
        Vector2D resultingForce = new Vector2D();
        for (V otherVertex : Graphs.neighborSetOf(layoutContext.getSimpleGraph(), vertex)) {
            Point otherPoint = layoutContext.getAllPoints().get(otherVertex);
            forceBetweenPoints(resultingForce, point, otherPoint);
        }
        return resultingForce;
    }

    private void forceBetweenPoints(Vector2D resultingForce, Point point, Point otherPoint) {
        Vector2D force = Vector2D.calculateVectorBetweenPoints(point, otherPoint);
        // check that there is no overlap between the points
        if (force.magnitude() > 2 * pointSize) {
            force.multiplyBy(forceIntensity);
            resultingForce.add(force);
        }
    }
}

