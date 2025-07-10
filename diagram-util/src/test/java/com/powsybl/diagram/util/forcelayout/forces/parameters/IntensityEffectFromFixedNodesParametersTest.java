/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces.parameters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class IntensityEffectFromFixedNodesParametersTest {

    @Test
    void setEffectFromFixedNodes() {
        IntensityEffectFromFixedNodesParameters intensityEffectFromFixedNodesParameters = new IntensityEffectFromFixedNodesParameters();
        assertTrue(intensityEffectFromFixedNodesParameters.isEffectFromFixedNodes());
        assertEquals(1, intensityEffectFromFixedNodesParameters.getForceIntensity());
        intensityEffectFromFixedNodesParameters.setEffectFromFixedNodes(false);
        assertFalse(intensityEffectFromFixedNodesParameters.isEffectFromFixedNodes());
        assertEquals(1, intensityEffectFromFixedNodesParameters.getForceIntensity());

    }
}
