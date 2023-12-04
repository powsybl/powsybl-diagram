/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.util.IdUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 */
class IdUtilTest {

    @Test
    void test() {
        String input = "ab_cd.ef gh";
        String escaped = IdUtil.escapeId(input);
        assertEquals(input, IdUtil.unescapeId(escaped));
    }

    @Test
    void test2() {
        String input = "_c";
        String escaped = IdUtil.escapeClassName(input);
        assertEquals("_95_c", escaped);
    }
}
