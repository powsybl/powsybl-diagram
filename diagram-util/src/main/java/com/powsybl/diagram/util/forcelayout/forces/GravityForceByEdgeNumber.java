/*
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
import com.powsybl.diagram.util.forcelayout.optimizationsartifacts.OptimizationArtifactsContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.diagram.util.forcelayout.geometry.ForceGraph.ORIGIN;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class GravityForceByEdgeNumber<V, E> implements Force<V, E, IntensityParameter> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GravityForceByEdgeNumber.class);

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph, IntensityParameter forceParameter) {
        // magnitude = k * (deg (point) + 1)
        // with deg(p) the degree of p, ie the number of connected nodes, that is to say the number of edges
        // this means less connected points will end more on the sides of the graph
        double magnitude = forceParameter.getForceIntensity() * (forceGraph.getGraph().degreeOf(forThisVertex) + 1);
        return Vector2D.calculateUnitVector(correspondingPoint, ORIGIN).multiply(magnitude);
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph, IntensityParameter forceParameter, OptimizationArtifactsContainer optimizationArtifacts) {
        LOGGER.warn("There is no optimized version for the gravity force, directly call the unoptimized version instead to avoid unnecessary redirection calls");
        return calculateForce(forThisVertex, correspondingPoint, forceGraph, forceParameter);
    }
}
