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
 * A force that attracts all the points towards the center of the graph. This force does not depend on the number of edge of the point.
 * The force is the same no matter the distance to the center.
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class AttractToCenterForceUnit<V, E> implements Force<V, E> {
    private final double forceIntensity;

    public AttractToCenterForceUnit(double forceIntensity) {
        this.forceIntensity = forceIntensity;
    }

    @Override
    public Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext) {
        Vector2D force = Vector2D.calculateUnitVector(point, layoutContext.getOrigin());
        force.multiplyBy(forceIntensity);
        return force;
    }
}

