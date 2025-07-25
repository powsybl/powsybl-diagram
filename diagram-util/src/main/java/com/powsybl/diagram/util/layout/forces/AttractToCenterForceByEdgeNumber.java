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
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class AttractToCenterForceByEdgeNumber<V, E> implements Force<V, E> {

    private final double forceIntensity;

    public AttractToCenterForceByEdgeNumber(double forceIntensity) {
        this.forceIntensity = forceIntensity;
    }

    @Override
    public void init(LayoutContext<V, E> layoutContext) {
        for (Map.Entry<V, Point> entry : layoutContext.getAllPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(layoutContext.getSimpleGraph().degreeOf(entry.getKey()));
        }
    }

    @Override
    public Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext) {
        // magnitude = k * (deg (point) + 1)
        // with deg(p) the degree of p, ie the number of connected nodes, that is to say the number of edges
        // this means less connected points will end more on the sides of the graph
        double magnitude = forceIntensity * (point.getPointVertexDegree() + 1);
        Vector2D force = Vector2D.calculateUnitVector(point, layoutContext.getOrigin());
        force.multiplyBy(magnitude);
        return force;
    }
}
