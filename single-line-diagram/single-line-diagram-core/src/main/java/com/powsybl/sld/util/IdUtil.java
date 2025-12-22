/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class IdUtil {

    private static final String ID_PREFIX = "id";

    private IdUtil() {
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

        for (char aChar : chars) {
            if (Character.isAlphabetic(aChar) || Character.isDigit(aChar)) {
                sb.append(aChar);
            } else {
                sb.append("_").append((int) aChar).append("_");
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
