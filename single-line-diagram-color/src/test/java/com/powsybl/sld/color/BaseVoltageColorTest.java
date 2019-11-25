/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.color;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BaseVoltageColorTest {

    @Test
    public void test() throws IOException, URISyntaxException {
        Path configFile = Paths.get(getClass().getResource("/base-voltages.yml").toURI());
        BaseVoltageColor baseVoltageColor = new BaseVoltageColor(configFile);
        // getProfiles
        assertEquals(Arrays.asList("Default"), baseVoltageColor.getProfiles());
        // getDefaultProfile
        assertEquals("Default", baseVoltageColor.getDefaultProfile());
        // getBaseVoltageNames
        assertEquals(Arrays.asList("0", "400", "225"), baseVoltageColor.getBaseVoltageNames("Default"));
        // getBaseVoltageName
        assertNull(baseVoltageColor.getBaseVoltageName(500, "Default"));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(450, "Default"));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(400, "Default"));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(300, "Default"));
        assertEquals("225", baseVoltageColor.getBaseVoltageName(250, "Default"));
        assertEquals("225", baseVoltageColor.getBaseVoltageName(180, "Default"));
        assertNull(baseVoltageColor.getBaseVoltageName(150, "Default"));
        // getColor by name
        assertEquals("#FF0000", baseVoltageColor.getColor("400", "Default"));
        assertEquals("#228B22", baseVoltageColor.getColor("225", "Default"));
        assertNull(baseVoltageColor.getColor("150", "Default"));
        // getColor by voltage
        assertNull(baseVoltageColor.getColor(500, "Default"));
        assertEquals("#FF0000", baseVoltageColor.getColor(450, "Default"));
        assertEquals("#FF0000", baseVoltageColor.getColor(400, "Default"));
        assertEquals("#FF0000", baseVoltageColor.getColor(300, "Default"));
        assertEquals("#228B22", baseVoltageColor.getColor(250, "Default"));
        assertEquals("#228B22", baseVoltageColor.getColor(180, "Default"));
        assertNull(baseVoltageColor.getColor(150, "Default"));
    }

}
