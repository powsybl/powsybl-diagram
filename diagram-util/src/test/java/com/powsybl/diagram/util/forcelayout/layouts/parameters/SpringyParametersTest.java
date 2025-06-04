/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
<<<<<<<< HEAD:diagram-util/src/test/java/com/powsybl/diagram/util/layout/algorithms/parameters/BasicForceLayoutAlgorithmParametersTest.java
package com.powsybl.diagram.util.layout.algorithms.parameters;
========
package com.powsybl.diagram.util.forcelayout.layouts.parameters;
>>>>>>>> d46ff350 (Rename layouts.layoutsparameters to layouts.parameters):diagram-util/src/test/java/com/powsybl/diagram/util/forcelayout/layouts/parameters/SpringyParametersTest.java

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class BasicForceLayoutAlgorithmParametersTest {
    @Test
    void testBuilder() {
        BasicForceLayoutParameters parameters = new BasicForceLayoutParameters.Builder()
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
