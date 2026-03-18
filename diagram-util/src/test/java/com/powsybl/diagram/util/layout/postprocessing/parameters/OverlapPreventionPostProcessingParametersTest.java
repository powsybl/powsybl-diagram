/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.postprocessing.parameters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
class OverlapPreventionPostProcessingParametersTest {
    double pointSizeScale = 0.001;
    double pointSizeOffset = 3.8;
    double edgeAttractionIntensity = 1.13;
    double repulsionNoOverlap = 12.3;
    double repulsionWithOverlap = 342.9;
    double repulsionZoneRatio = 11.1;
    double attractToCenterIntensity = 0.87;

    @Test
    void checkBuilder() {
        OverlapPreventionPostProcessingParameters parameters = new OverlapPreventionPostProcessingParameters.Builder()
                .withPointSizeScale(pointSizeScale)
                .withPointSizeOffset(pointSizeOffset)
                .withEdgeAttractionIntensity(edgeAttractionIntensity)
                .withRepulsionNoOverlapIntensity(repulsionNoOverlap)
                .withRepulsionWithOverlapIntensity(repulsionWithOverlap)
                .withRepulsionZoneRatio(repulsionZoneRatio)
                .withAttractToCenterIntensity(attractToCenterIntensity)
                .build();

        assertEquals(pointSizeScale, parameters.getPointSizeScale());
        assertEquals(pointSizeOffset, parameters.getPointSizeOffset());
        assertEquals(edgeAttractionIntensity, parameters.getEdgeAttractionIntensity());
        assertEquals(repulsionNoOverlap, parameters.getRepulsionNoOverlapIntensity());
        assertEquals(repulsionWithOverlap, parameters.getRepulsionWithOverlapIntensity());
        assertEquals(repulsionZoneRatio, parameters.getRepulsionZoneRatio());
        assertEquals(attractToCenterIntensity, parameters.getAttractToCenterIntensity());
    }
}
