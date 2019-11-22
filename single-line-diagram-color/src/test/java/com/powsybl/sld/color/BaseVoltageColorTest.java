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
        assertEquals(Arrays.asList("RTE"), baseVoltageColor.getProfiles());
        // getDefaultProfile
        assertEquals("RTE", baseVoltageColor.getDefaultProfile());
        // getBaseVoltageNames
        assertEquals(Arrays.asList("400", "225"), baseVoltageColor.getBaseVoltageNames("RTE"));
        // getBaseVoltageName
        assertNull(baseVoltageColor.getBaseVoltageName(500, "RTE"));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(450, "RTE"));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(400, "RTE"));
        assertEquals("400", baseVoltageColor.getBaseVoltageName(300, "RTE"));
        assertEquals("225", baseVoltageColor.getBaseVoltageName(250, "RTE"));
        assertEquals("225", baseVoltageColor.getBaseVoltageName(180, "RTE"));
        assertNull(baseVoltageColor.getBaseVoltageName(150, "RTE"));
        // getColor by name
        assertEquals("#BBBBBBB", baseVoltageColor.getColor("400", "RTE"));
        assertEquals("#AAAAAAA", baseVoltageColor.getColor("225", "RTE"));
        assertNull(baseVoltageColor.getColor("150", "RTE"));
        // getColor by voltage
        assertNull(baseVoltageColor.getColor(500, "RTE"));
        assertEquals("#BBBBBBB", baseVoltageColor.getColor(450, "RTE"));
        assertEquals("#BBBBBBB", baseVoltageColor.getColor(400, "RTE"));
        assertEquals("#BBBBBBB", baseVoltageColor.getColor(300, "RTE"));
        assertEquals("#AAAAAAA", baseVoltageColor.getColor(250, "RTE"));
        assertEquals("#AAAAAAA", baseVoltageColor.getColor(180, "RTE"));
        assertNull(baseVoltageColor.getColor(150, "RTE"));
    }

}
