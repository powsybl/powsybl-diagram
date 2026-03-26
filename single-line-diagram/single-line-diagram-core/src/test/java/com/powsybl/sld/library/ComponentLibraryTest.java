/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ComponentLibraryTest {

    @Test
    void test() {
        List<SldComponentLibrary> libraries = SldComponentLibrary.findAll();
        assertEquals(2, libraries.size());
        SldComponentLibrary cvg = SldComponentLibrary.find("Convergence").orElse(null);
        assertNotNull(cvg);
        assertEquals("Convergence", cvg.getName());
        assertEquals(35, cvg.getComponentsSize().size());
    }
}
