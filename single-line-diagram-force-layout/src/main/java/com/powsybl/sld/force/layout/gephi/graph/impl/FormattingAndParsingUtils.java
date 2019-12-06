/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.powsybl.sld.force.layout.gephi.graph.impl;

import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utils for formatting and parsing special data types (dynamic intervals,
 * timestamps and arrays).
 *
 * @author Eduardo Ramos
 */
public final class FormattingAndParsingUtils {

    // Bounds
    public static final char DYNAMIC_TYPE_LEFT_BOUND = '<';
    public static final char DYNAMIC_TYPE_RIGHT_BOUND = '>';
    public static final char LEFT_BOUND_BRACKET = '(';
    public static final char LEFT_BOUND_SQUARE_BRACKET = '[';
    public static final char RIGHT_BOUND_BRACKET = ')';
    public static final char RIGHT_BOUND_SQUARE_BRACKET = ']';
    public static final char COMMA = ',';

    public static final String EMPTY_VALUE = "<empty>";
    public static final String INFINITY = "Infinity";

    /**
     * Parses an ISO date with or without time or a timestamp (in milliseconds).
     * Returns the date or timestamp converted to a timestamp in milliseconds.
     *
     * @param timeStr Date or timestamp string
     * @param timeZone Time zone to use or null to use default time zone (UTC)
     * @return Timestamp
     */
    public static double parseDateTimeOrTimestamp(String timeStr, DateTimeZone timeZone) {
        double value;
        try {
            // Try first to parse as a single double:
            value = Double.parseDouble(infinityIgnoreCase(timeStr));
            if (Double.isNaN(value)) {
                throw new IllegalArgumentException("NaN is not allowed as an interval bound");
            }
        } catch (Exception ex) {
            value = AttributeUtils.parseDateTime(timeStr, timeZone);
        }

        return value;
    }

    /**
     * Parses an ISO date with or without time or a timestamp (in milliseconds).
     * Returns the date or timestamp converted to a timestamp in milliseconds.
     * Default time zone is used (UTC).
     *
     * @param timeStr Date or timestamp string
     * @return Timestamp
     */
    public static double parseDateTimeOrTimestamp(String timeStr) {
        return parseDateTimeOrTimestamp(timeStr, null);
    }

    /**
     * Parse literal value until detecting the end of it (quote can be ' or ")
     *
     * @param reader Input reader
     * @param quote Quote mode that started this literal (' or ")
     * @return Parsed value
     * @throws IOException Unexpected read error
     */
    protected static String parseLiteral(StringReader reader, char quote) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean escapeEnabled = false;

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            if (c == quote) {
                if (escapeEnabled) {
                    sb.append(quote);
                    escapeEnabled = false;
                } else {
                    return sb.toString();
                }
            } else {
                switch (c) {
                    case '\\':
                        if (escapeEnabled) {
                            sb.append('\\');

                            escapeEnabled = false;
                        } else {
                            escapeEnabled = true;
                        }
                        break;
                    default:
                        if (escapeEnabled) {
                            escapeEnabled = false;
                        }
                        sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Parses a value until end is detected either by a comma or a bounds
     * closing character.
     *
     * @param reader Input reader
     * @return Parsed value
     * @throws IOException Unexpected read error
     */
    protected static String parseValue(StringReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case RIGHT_BOUND_BRACKET:
                case RIGHT_BOUND_SQUARE_BRACKET:
                    reader.skip(-1); // Go backwards 1 position, for detecting
                                    // end of bounds
                case COMMA:
                    return sb.toString().trim();
                default:
                    sb.append(c);
            }
        }

        return sb.toString().trim();
    }

    /**
     * Converts a string parsed with {@link #parseValue(StringReader)}
     * to the target type, taking into account dynamic parsing quirks such as
     * numbers with/without decimals and infinity values.
     *
     * @param <T> Target type
     * @param typeClass Target type class
     * @param valString String to parse
     * @return Converted value
     */
    protected static <T> T convertValue(Class<T> typeClass, String valString) {
        Object value;
        if (typeClass.equals(Byte.class) || typeClass.equals(byte.class) || typeClass.equals(Short.class) || typeClass
                .equals(short.class) || typeClass.equals(Integer.class) || typeClass.equals(int.class) || typeClass
                .equals(Long.class) || typeClass.equals(long.class) || typeClass.equals(BigInteger.class)) {
            value = parseNumberWithoutDecimals((Class<? extends Number>) typeClass, valString);
        } else if (typeClass.equals(Float.class) || typeClass.equals(float.class) || typeClass.equals(Double.class) || typeClass
                .equals(double.class) || typeClass.equals(BigDecimal.class)) {
            value = parseNumberWithDecimals((Class<? extends Number>) typeClass, valString);
        } else {
            value = AttributeUtils.parse(valString, typeClass);
        }

        if (value == null) {
            throw new IllegalArgumentException("Invalid value for type: " + valString);
        }

        return (T) value;
    }

    /**
     * Method for allowing inputs such as "infinity" when parsing decimal
     * numbers
     *
     * @param valueParam Input String
     * @return Input String with fixed "Infinity" syntax if necessary.
     */
    private static String infinityIgnoreCase(String valueParam) {
        String value = valueParam;
        value = value.trim();
        if (value.equalsIgnoreCase(INFINITY)) {
            return INFINITY;
        }
        if (value.equalsIgnoreCase("-" + INFINITY)) {
            return "-" + INFINITY;
        }

        return value;
    }

    private static <T extends Number> T parseNumberWithoutDecimals(Class<T> typeClass, String valStringParam) {
        String valString = removeDecimalDigitsFromString(valStringParam);

        return (T) AttributeUtils.parse(valString, typeClass);
    }

    private static <T extends Number> T parseNumberWithDecimals(Class<T> typeClass, String valStringParam) {
        String valString = infinityIgnoreCase(valStringParam);

        return (T) AttributeUtils.parse(valString, typeClass);
    }

    /**
     * Removes anything after the dot of decimal numbers in a string when
     * necessary. Used for trying to parse decimal numbers as not decimal. For
     * example BigDecimal to BigInteger.
     *
     * @param s String to remove decimal digits
     * @return String without dot and decimal digits.
     */
    private static String removeDecimalDigitsFromString(String s) {
        int firstDotIndex = s.indexOf('.');
        if (firstDotIndex > 0) {
            return s.substring(0, firstDotIndex);
        } else {
            return s;
        }
    }

    private static final char[] DYNAMIC_SPECIAL_CHARACTERS = " ;,()[]\"'".toCharArray();

    /**
     * @param value String value
     * @return True if the string contains special characters for dynamic types
     *         intervals syntax
     */
    public static boolean containsDynamicSpecialCharacters(String value) {
        for (char c : DYNAMIC_SPECIAL_CHARACTERS) {
            if (value.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    public static <T> String printArray(Object arr) {
        if (arr == null) {
            return null;
        }

        int size = Array.getLength(arr);
        if (size == 0) {
            return FormattingAndParsingUtils.EMPTY_VALUE;
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size; i++) {
            Object value = Array.get(arr, i);
            if (value != null) {
                String stringValue = value.toString();
                if (stringValue.equals("null") || containsArraySpecialCharacters(stringValue) || stringValue.trim()
                        .isEmpty()) {
                    sb.append('"');
                    sb.append(stringValue.replace("\\", "\\\\").replace("\"", "\\\""));
                    sb.append('"');
                } else {
                    sb.append(stringValue);
                }
            } else {
                sb.append("null");
            }

            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');

        return sb.toString();
    }

    private static final char[] ARRAY_SPECIAL_CHARACTERS = " ,[]\"'".toCharArray();

    /**
     * @param value String value
     * @return True if the string contains special characters for arrays
     *         intervals syntax
     */
    private static boolean containsArraySpecialCharacters(String value) {
        for (char c : ARRAY_SPECIAL_CHARACTERS) {
            if (value.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    private FormattingAndParsingUtils() {
    }
}
