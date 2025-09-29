/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.postprocessing;

import com.powsybl.diagram.util.layout.forces.AttractToCenterForceByEdgeNumberLinear;
import com.powsybl.diagram.util.layout.forces.EdgeAttractionForceNoOverlapLinear;
import com.powsybl.diagram.util.layout.forces.Force;
import com.powsybl.diagram.util.layout.forces.RepulsionForceByEdgeNumberNoOverlapLinear;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.postprocessing.parameters.OverlapPreventionPostProcessingParameters;

import java.util.List;
import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class OverlapPreventionPostProcessing<V, E> implements PostProcessing<V, E> {
    private final OverlapPreventionPostProcessingParameters parameters;

    private static final double STARTING_SPEED_FACTOR = 0.1;
    private static final double SPEED_DECREASE_RATIO = 0.97;
    private static final int ITERATION_NUMBER = 90;

    private double pointSize = 15;

    /**
     * @param parameters the parameters of this post-processing
     */
    public OverlapPreventionPostProcessing(
        OverlapPreventionPostProcessingParameters parameters
    ) {
        this.parameters = parameters;
    }

    /**
     * Build an instance of overlap prevention with default parameters
     */
    public OverlapPreventionPostProcessing() {
        this(new OverlapPreventionPostProcessingParameters.Builder().build());
    }

    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        pointSize = parameters.getPointSizeScale() * layoutContext.getAllPoints().size() + parameters.getPointSizeOffset();
        List<Force<V, E>> forces = List.of(
                new EdgeAttractionForceNoOverlapLinear<>(parameters.getEdgeAttractionIntensity(), parameters.getPointSizeScale(), parameters.getPointSizeOffset()),
                new RepulsionForceByEdgeNumberNoOverlapLinear<>(parameters.getRepulsionNoOverlap(), parameters.getRepulsionWithOverlap(), parameters.getPointSizeScale(), parameters.getPointSizeOffset(), parameters.getRepulsionZoneRatio()),
                new AttractToCenterForceByEdgeNumberLinear<>(parameters.getAttractToCenterIntensity())
        );
        Force.initAllForces(forces, layoutContext);
        double speedFactor = STARTING_SPEED_FACTOR;
        for (int i = 0; i < ITERATION_NUMBER; ++i) {
            for (Map.Entry<V, Point> entry : layoutContext.getMovingPoints().entrySet()) {
                Point point = entry.getValue();
                for (Force<V, E> force : forces) {
                    Vector2D resultingForce = force.apply(entry.getKey(), point, layoutContext);
                    point.applyForce(resultingForce);
                }
                // update points position directly instead of all at the same time
                updatePointPosition(point, speedFactor);
            }
            speedFactor *= SPEED_DECREASE_RATIO;
        }
    }

    private void updatePointPosition(Point point, double speedFactor) {
        point.getForces().multiplyBy(speedFactor);
        double totalMagnitude = point.getForces().magnitude();
        // add protection to not move more than 2 times the point size
        if (totalMagnitude > 2 * pointSize) {
            point.getForces().multiplyBy(2 * pointSize / totalMagnitude);
        }

        point.getPosition().add(point.getForces());
        point.resetForces();
    }
}

