/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.powsybl.commons.exceptions.UncheckedSaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TestLoadInvalidExternalComponent {

    @Test
    void test() {
        assertThrows(UncheckedSaxException.class, () -> new ResourcesComponentLibrary("invalid", "/ConvergenceLibrary", "/InvalidLibrary"));
    }
}
