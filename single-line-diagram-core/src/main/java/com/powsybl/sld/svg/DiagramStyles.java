/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class DiagramStyles {

    public static final String WIRE_STYLE_CLASS = "wire";
    public static final String GRID_STYLE_CLASS = "grid";
    public static final String LABEL_STYLE_CLASS = "component-label";
    public static final String GRAPH_LABEL_STYLE_CLASS = "graph-label";
    public static final String ARROW_LABEL_STYLE_CLASS = "arrow-label";
    public static final String ARROW_ACTIVE_CLASS = "arrow-p";
    public static final String ARROW_REACTIVE_CLASS = "arrow-q";
    public static final String UP_CLASS = "up";
    public static final String DOWN_CLASS = "down";
    public static final String HIDDEN_INTERNAL_NODE_CLASS = "hidden-internal-node";
    public static final String DISCONNECTED_STYLE_CLASS = "disconnected";
    public static final String OPEN_SWITCH_STYLE_CLASS = "open";
    public static final String CLOSED_SWITCH_STYLE_CLASS = "closed";
    public static final String WIRE_DISCONNECTED = "wire-disconnected";
    public static final String WIRE_CONNECTED_DISCONNECTED = "wire-connected-disconnected";
    public static final String WIRE_DISCONNECTED_CONNECTED = "wire-disconnected-connected";
    public static final String BOTTOM_FEEDER = "bottom-feeder";
    public static final String TOP_FEEDER = "top-feeder";
    public static final String CONSTANT_COLOR_CLASS = "constant-color";
    private static final String ID_PREFIX = "id";

    private DiagramStyles() {
    }

    public static String escapeClassName(String input) {
        Objects.requireNonNull(input);
        String temp = input;
        // class name length should be at least 2
        if (temp.length() < 2) {
            temp = StringUtils.leftPad(temp, 2, "_");
        }
        return escape(temp);
    }

    public static String escapeId(String input) {
        Objects.requireNonNull(input);
        String temp = ID_PREFIX + input;
        return escape(temp);
    }

    public static String unescapeId(String input) {
        Objects.requireNonNull(input);
        return unescape(input.substring(ID_PREFIX.length()));
    }

    public static String escape(String input) {
        Objects.requireNonNull(input);

        StringBuilder sb = new StringBuilder();

        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (Character.isAlphabetic(chars[i]) || Character.isDigit(chars[i])) {
                sb.append(chars[i]);
            } else {
                sb.append("_").append((int) chars[i]).append("_");
            }
        }
        return sb.toString();
    }

    public static String unescape(String input) {
        Objects.requireNonNull(input);
        StringBuilder out = new StringBuilder();
        StringReader sr = new StringReader(input);

        try {
            int c = sr.read();
            while (c != -1) {
                if (c == 95) {
                    StringBuilder sb = new StringBuilder();
                    int n = sr.read();
                    while (n != 95 && n != -1) {
                        sb.append((char) n);
                        n = sr.read();
                    }
                    int x = Integer.parseInt(sb.toString());
                    out.append((char) x);
                } else {
                    out.append((char) c);
                }
                c = sr.read();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }
}
