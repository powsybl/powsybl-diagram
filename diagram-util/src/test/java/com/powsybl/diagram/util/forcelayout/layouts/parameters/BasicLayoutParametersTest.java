/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.layouts.parameters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class BasicLayoutParametersTest {
    @Test
    void testBuilder() {
        BasicLayoutParameters parameters = new BasicLayoutParameters.Builder()
                .withMaxSteps(324)
                .withMinEnergyThreshold(0.8)
                .withDeltaTime(0.06)
                .withRepulsion(7.9)
                .withFriction(-3.4)
                .withMaxSpeed(107)
                .withRepulsionForceFromFixedPoints(false)
                .withAttractToCenterForce(false)
                .build();

        assertEquals(324, parameters.getMaxSteps());
        assertEquals(0.8, parameters.getMinEnergyThreshold());
        assertEquals(0.06, parameters.getDeltaTime());
        assertEquals(7.9, parameters.getRepulsion());
        assertEquals(-3.4, parameters.getFriction());
        assertEquals(107, parameters.getMaxSpeed());
        assertFalse(parameters.isRepulsionForceFromFixedPoints());
        assertFalse(parameters.isAttractToCenterForce());
    }
}
