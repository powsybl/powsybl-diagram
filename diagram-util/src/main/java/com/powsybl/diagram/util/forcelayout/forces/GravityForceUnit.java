/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityParameter;
import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class GravityForceUnit<V, E> implements Force<V, E> {
    private final IntensityParameter forceParameter;

    public GravityForceUnit(IntensityParameter forceParameter) {
        this.forceParameter = forceParameter;
    }

    @Override
    public Vector2D apply(V forThisVertex, Point correspondingPoint, LayoutContext<V, E> layoutContext) {
        Vector2D force = Vector2D.calculateUnitVector(correspondingPoint, layoutContext.getOrigin());
        force.multiplyBy(forceParameter.getForceIntensity());
        return force;
    }
}

