/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class DiagramStylesTest {

    @Test
    public void test() {
        String input = "ab_cd.ef gh";
        String escaped = DiagramStyles.escapeId(input);
        assertEquals(input, DiagramStyles.unescapeId(escaped));
    }

    @Test
    public void test2() {
        String input = "_c";
        String escaped = DiagramStyles.escapeClassName(input);
        assertEquals("_95_c", escaped);
    }
}
