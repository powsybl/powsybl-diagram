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
class SpringParameterTest {

    @Test
    void setParameters() {
        SpringParameter springParameter = new SpringParameter();
        assertEquals(1, springParameter.getLength());
        assertEquals(1, springParameter.getStiffness());
        springParameter.setLength(4.2);
        springParameter.setStiffness(0.6);
        assertEquals(4.2, springParameter.getLength());
        assertEquals(0.6, springParameter.getStiffness());
    }
}
