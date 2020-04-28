/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.color;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BaseVoltageColorTest {

    @Test
    public void test() {
        BaseVoltageColor baseVoltageColor = BaseVoltageColor.fromInputStream(getClass().getResourceAsStream("/base-voltages.yml"));
        // getProfiles
        assertEquals(Collections.singletonList("Default"), baseVoltageColor.getProfiles());
        // getDefaultProfile
        assertEquals("Default", baseVoltageColor.getDefaultProfile());
        // getBaseVoltageNames
        assertEquals(Arrays.asList("0", "400", "225", "150", "90", "63", "45", "20"), baseVoltageColor.getBaseVoltageNames("Default"));
        // getBaseVoltageName
        assertFalse(baseVoltageColor.getBaseVoltageName(500, "Default").isPresent());
        assertEquals("400", baseVoltageColor.getBaseVoltageName(450, "Default").orElseThrow(AssertionError::new));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(400, "Default").orElseThrow(AssertionError::new));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(300, "Default").orElseThrow(AssertionError::new));
        assertEquals("225", baseVoltageColor.getBaseVoltageName(250, "Default").orElseThrow(AssertionError::new));
        assertEquals("225", baseVoltageColor.getBaseVoltageName(180, "Default").orElseThrow(AssertionError::new));
        assertFalse(baseVoltageColor.getBaseVoltageName(700, "Default").isPresent());
        // getColor by name
        assertEquals("#ff0000", baseVoltageColor.getColor("400", "Default").orElseThrow(AssertionError::new));
        assertEquals("#228b22", baseVoltageColor.getColor("225", "Default").orElseThrow(AssertionError::new));
        assertFalse(baseVoltageColor.getColor("???", "Default").isPresent());
        // getColor by voltage
        assertFalse(baseVoltageColor.getColor(500, "Default").isPresent());
        assertEquals("#ff0000", baseVoltageColor.getColor(450, "Default").orElseThrow(AssertionError::new));
        assertEquals("#ff0000", baseVoltageColor.getColor(400, "Default").orElseThrow(AssertionError::new));
        assertEquals("#ff0000", baseVoltageColor.getColor(300, "Default").orElseThrow(AssertionError::new));
        assertEquals("#228b22", baseVoltageColor.getColor(250, "Default").orElseThrow(AssertionError::new));
        assertEquals("#228b22", baseVoltageColor.getColor(180, "Default").orElseThrow(AssertionError::new));
        assertFalse(baseVoltageColor.getColor(700, "Default").isPresent());
    }
}
