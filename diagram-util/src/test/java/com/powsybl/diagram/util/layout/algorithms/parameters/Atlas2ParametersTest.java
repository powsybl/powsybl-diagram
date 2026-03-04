/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.algorithms.parameters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
class Atlas2ParametersTest {

    int maxSteps = 3728;
    double repulsion = 3.43;
    double edgeAttraction = 0.7;
    double attractToCenter = 0.12;
    double speedFactor = 1.1;
    double maxSpeedFactor = 12.45;
    double swingTolerance = 0.8;
    double maxGlobalSpeedIncreaseRatio = 1.69;
    boolean repulsionFromFixedPointsEnabled = false;
    boolean attractToCenterEnabled = false;

    @Test
    void checkBuilder() {
        Atlas2Parameters parameters = new Atlas2Parameters.Builder()
                .withMaxSteps(maxSteps)
                .withRepulsionIntensity(repulsion)
                .withEdgeAttractionIntensity(edgeAttraction)
                .withAttractToCenterIntensity(attractToCenter)
                .withSpeedFactor(speedFactor)
                .withMaxSpeedFactor(maxSpeedFactor)
                .withSwingTolerance(swingTolerance)
                .withMaxGlobalSpeedIncreaseRatio(maxGlobalSpeedIncreaseRatio)
                .withRepulsionFromFixedPointsEnabled(repulsionFromFixedPointsEnabled)
                .withAttractToCenterEnabled(attractToCenterEnabled)
                .build();

        assertEquals(maxSteps, parameters.getMaxSteps());
        assertEquals(repulsion, parameters.getRepulsionIntensity());
        assertEquals(edgeAttraction, parameters.getEdgeAttractionIntensity());
        assertEquals(attractToCenter, parameters.getAttractToCenterIntensity());
        assertEquals(speedFactor, parameters.getSpeedFactor());
        assertEquals(maxSpeedFactor, parameters.getMaxSpeedFactor());
        assertEquals(swingTolerance, parameters.getSwingTolerance());
        assertEquals(maxGlobalSpeedIncreaseRatio, parameters.getMaxGlobalSpeedIncreaseRatio());
        assertEquals(repulsionFromFixedPointsEnabled, parameters.isRepulsionFromFixedPointsEnabled());
        assertEquals(attractToCenterEnabled, parameters.isAttractToCenterEnabled());
    }
}
