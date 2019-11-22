/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.powsybl.sld.util.HSLColor;
import com.powsybl.sld.util.RGBColor;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class TestHSLColor {

    @Test
    public void test() {
        String red = "#FF0000";
        HSLColor color = HSLColor.parse(red);
        assertEquals(new RGBColor(255, 0, 0), color.toRGBColor());
        List<RGBColor> gradient = color.getColorGradient(3);
        assertEquals(3, gradient.size());
        assertEquals("#7F6C00", gradient.get(0).toString());
        assertEquals("#FF0000", gradient.get(1).toString());
        assertEquals("#F6B2FF", gradient.get(2).toString());
    }
}
