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

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class GravityForceLinear<V, E> extends AbstractForce<V, E, IntensityParameter> {

    public GravityForceLinear(IntensityParameter forceParameter) {
        super(forceParameter);
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph) {
        // we don't use a unit vector to follow the previous convention, even though this is a bit strange
        // it means that nodes will generally not get further than a certain distance from the center, instead of leaving room to other nodes for expanding
        // that makes graphs more compact, but it could also cause issues with big graphs, where it would be too compact
        return Vector2D.calculateVectorBetweenPoints(correspondingPoint, forceGraph.getOrigin()).multiply(forceParameter.getForceIntensity());
    }
}
