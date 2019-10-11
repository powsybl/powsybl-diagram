/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class DiagramStyles {

    public static final String WIRE_STYLE_CLASS = "wire";
    public static final String GRID_STYLE_CLASS = "grid";
    public static final String BUS_STYLE_CLASS = "busbar-section";
    public static final String LABEL_STYLE_CLASS = "component-label";

    private DiagramStyles() {
    }

    public static String escapeClassName(String input) {
        Objects.requireNonNull(input);
        String temp = input;
        // class name length should be at least 2
        if (temp.length() < 2) {
            temp = StringUtils.leftPad(temp, 2, "_");
        }
        return escapeId(temp);
    }

    public static String escapeId(String input) {
        Objects.requireNonNull(input);
        String temp = input;
        // class name cannot start with a digit
        temp = Character.isDigit(temp.charAt(0)) ? "d" + temp : temp;
        // class name cannot begin with two hyphens or a hyphen followed by a digit
        if (temp.startsWith("--") || (temp.charAt(0) == '-' && Character.isDigit(temp.charAt(1)))) {
            temp = "d" + temp;
        }
        // Substitution of all non authorized characters
        return Pattern.compile("[^\\_\\-a-zA-Z0-9][^\\_\\-a-zA-Z0-9]*", 32).matcher(temp).replaceAll("_");
    }
}
