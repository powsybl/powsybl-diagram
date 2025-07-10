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
class IntensityParameterTest {

    @Test
    void setForceIntensity() {
        IntensityParameter intensityParameter = new IntensityParameter();
        assertEquals(1, intensityParameter.getForceIntensity());
        intensityParameter.setForceIntensity(3.14);
        assertEquals(3.14, intensityParameter.getForceIntensity());
    }
}
