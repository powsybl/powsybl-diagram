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
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractTimestampMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampShortMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampStringMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampLongMap;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * <p>
 * Class for parsing timestamp types.
 * </p>
 *
 * <p>
 * The standard format for {@link AbstractTimestampMap} is &lt;[timestamp, value1];
 * [timestamp, value2]&gt;.
 * </p>
 *
 * <p>
 * The standard format for {@link TimestampSet} is &lt;[timestamp1, timestamp2,
 * timestamp3, ...]&gt;.
 * </p>
 *
 * <p>
 * Timestamps values can be both numbers and ISO dates or datetimes. Dates and
 * datetimes will be converted to their millisecond-precision timestamp.
 * </p>
 *
 * Examples of valid timestamp maps are:
 * <ul>
 * <li>&lt;(1, 2, v1); [3, 5, v2]&gt;</li>
 * <li>[1.15,2.21, "literal value ' \" ,[]()"]</li>
 * <li>[1.15,2.21, 'literal value " \' ,[]()']</li>
 * </ul>
 *
 * Examples of valid timestamp sets are:
 * <ul>
 * <li>&lt;[1,2]; [3, 4]&gt;</li>
 * <li>[1,2]</li>
 * <li>[1,2] (5,6)</li>
 * </ul>
 *
 * <p>
 * The most correct examples are those that include &lt; &gt; and proper commas
 * and semicolons for separation, but the parser will be indulgent when
 * possible.
 * </p>
 *
 * @author Eduardo Ramos
 */
public final class TimestampsParser {

    /**
     * Parses a {@link TimestampSet} type with one or more timestamps.
     *
     * @param input Input string to parse
     * @param timeZone Time zone to use or null to use default time zone (UTC)
     * @return Resulting {@link TimestampSet}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if there are no timestamps in the
     *         input string or bounds cannot be parsed into doubles or
     *         dates/datetimes.
     */
    public static TimestampSet parseTimestampSet(String input, DateTimeZone timeZone) throws IllegalArgumentException {
        if (input == null) {
            return null;
        }

        if (input.equalsIgnoreCase(FormattingAndParsingUtils.EMPTY_VALUE)) {
            return new TimestampSet();
        }

        ArrayList<String> values = new ArrayList<String>();
        try {
            StringReader reader = new StringReader(input + ' '); // Add 1 space
                                                                // so
                                                                // reader.skip
                                                                // function
                                                                // always works
                                                                // when
                                                                // necessary
                                                                // (end of
                                                                // string not
                                                                // reached).
            int r;
            char c;
            while ((r = reader.read()) != -1) {
                c = (char) r;
                switch (c) {
                    case FormattingAndParsingUtils.DYNAMIC_TYPE_LEFT_BOUND:
                    case FormattingAndParsingUtils.DYNAMIC_TYPE_RIGHT_BOUND:
                    case FormattingAndParsingUtils.RIGHT_BOUND_SQUARE_BRACKET:
                    case FormattingAndParsingUtils.RIGHT_BOUND_BRACKET:
                    case FormattingAndParsingUtils.LEFT_BOUND_BRACKET:
                    case FormattingAndParsingUtils.LEFT_BOUND_SQUARE_BRACKET:
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                    case FormattingAndParsingUtils.COMMA:
                        // Ignore special characters and leading whitespace or
                        // similar until a value or literal starts:
                        break;
                    case '"':
                    case '\'':
                        values.add(FormattingAndParsingUtils.parseLiteral(reader, c));
                        break;
                    default:
                        reader.skip(-1); // Go backwards 1 position, for reading
                                        // start of value
                        values.add(FormattingAndParsingUtils.parseValue(reader));
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected expection while parsing timestamps", ex);
        }

        TimestampSet result = new TimestampSet(values.size());

        for (String value : values) {
            result.add(FormattingAndParsingUtils.parseDateTimeOrTimestamp(value, timeZone));
        }

        return result;
    }

    /**
     * Parses a {@link TimestampSet} type with one or more timestamps. Default
     * time zone is used (UTC).
     *
     * @param input Input string to parse
     * @return Resulting {@link TimestampSet}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if there are no timestamps in the
     *         input string or bounds cannot be parsed into doubles or
     *         dates/datetimes.
     */
    public static TimestampSet parseTimestampSet(String input) throws IllegalArgumentException {
        return parseTimestampSet(input, null);
    }

    /**
     * Parses a {@link AbstractTimestampMap} type with one or more timestamps, and their
     * associated values.
     *
     * @param <T> Underlying type of the {@link AbstractTimestampMap} values
     * @param typeClassParam Simple type or {@link AbstractTimestampMap} subtype for the
     *        result values.
     * @param input Input string to parse
     * @param timeZone Time zone to use or null to use default time zone (UTC)
     * @return Resulting {@link AbstractTimestampMap}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if type class is not supported,
     *         any of the timestamps don't have a value or have an invalid
     *         value, there are no timestamps in the input string or bounds
     *         cannot be parsed into doubles or dates/datetimes.
     */
    public static <T> AbstractTimestampMap<T> parseTimestampMap(Class<T> typeClassParam, String input, DateTimeZone timeZone) throws IllegalArgumentException {
        if (typeClassParam == null) {
            throw new IllegalArgumentException("typeClass required");
        }

        if (input == null) {
            return null;
        }

        AbstractTimestampMap result;

        Class typeClass = AttributeUtils.getStandardizedType(typeClassParam);
        if (typeClass.equals(String.class)) {
            result = new TimestampStringMap();
        } else if (typeClass.equals(Byte.class)) {
            result = new TimestampByteMap();
        } else if (typeClass.equals(Short.class)) {
            result = new TimestampShortMap();
        } else if (typeClass.equals(Integer.class)) {
            result = new TimestampIntegerMap();
        } else if (typeClass.equals(Long.class)) {
            result = new TimestampLongMap();
        } else if (typeClass.equals(Float.class)) {
            result = new TimestampFloatMap();
        } else if (typeClass.equals(Double.class)) {
            result = new TimestampDoubleMap();
        } else if (typeClass.equals(Boolean.class)) {
            result = new TimestampBooleanMap();
        } else if (typeClass.equals(Character.class)) {
            result = new TimestampCharMap();
        } else {
            throw new IllegalArgumentException("Unsupported type " + typeClass.getClass().getCanonicalName());
        }

        if (input.equalsIgnoreCase(FormattingAndParsingUtils.EMPTY_VALUE)) {
            return result;
        }

        StringReader reader = new StringReader(input + ' '); // Add 1 space so
                                                            // reader.skip
                                                            // function always
                                                            // works when
                                                            // necessary (end of
                                                            // string not
                                                            // reached).

        try {
            int r;
            char c;
            while ((r = reader.read()) != -1) {
                c = (char) r;
                switch (c) {
                    case FormattingAndParsingUtils.LEFT_BOUND_SQUARE_BRACKET:
                    case FormattingAndParsingUtils.LEFT_BOUND_BRACKET:
                        parseTimestampAndValue(typeClass, reader, result, timeZone);
                        break;
                    default:
                        // Ignore other chars outside of bounds
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected expection while parsing timestamps", ex);
        }

        return result;
    }

    /**
     * Parses a {@link AbstractTimestampMap} type with one or more timestamps, and their
     * associated values. Default time zone is used (UTC).
     *
     * @param <T> Underlying type of the {@link AbstractTimestampMap} values
     * @param typeClass Simple type or {@link AbstractTimestampMap} subtype for the
     *        result values.
     * @param input Input string to parse
     * @return Resulting {@link AbstractTimestampMap}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if type class is not supported,
     *         any of the timestamps don't have a value or have an invalid
     *         value, there are no timestamps in the input string or bounds
     *         cannot be parsed into doubles or dates/datetimes.
     */
    public static <T> AbstractTimestampMap<T> parseTimestampMap(Class<T> typeClass, String input) throws IllegalArgumentException {
        return parseTimestampMap(typeClass, input, null);
    }

    private static <T> void parseTimestampAndValue(Class<T> typeClass, StringReader reader, AbstractTimestampMap<T> result, DateTimeZone timeZone) throws IOException {
        ArrayList<String> values = new ArrayList<String>();

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case FormattingAndParsingUtils.RIGHT_BOUND_SQUARE_BRACKET:
                case FormattingAndParsingUtils.RIGHT_BOUND_BRACKET:
                    addTimestampAndValue(typeClass, values, result, timeZone);
                    return;
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case FormattingAndParsingUtils.COMMA:
                    // Ignore leading whitespace or similar until a value or
                    // literal starts:
                    break;
                case '"':
                case '\'':
                    values.add(FormattingAndParsingUtils.parseLiteral(reader, c));
                    break;
                default:
                    reader.skip(-1); // Go backwards 1 position, for reading
                                    // start of value
                    values.add(FormattingAndParsingUtils.parseValue(reader));
            }
        }

        addTimestampAndValue(typeClass, values, result, timeZone);
    }

    private static <T> void addTimestampAndValue(Class<T> typeClass, ArrayList<String> values, AbstractTimestampMap<T> result, DateTimeZone timeZone) {
        if (values.size() != 2) {
            throw new IllegalArgumentException("Each timestamp and value array must have 2 values");
        }

        double timestamp = FormattingAndParsingUtils.parseDateTimeOrTimestamp(values.get(0), timeZone);

        String valString = values.get(1);
        T value = FormattingAndParsingUtils.convertValue(typeClass, valString);

        result.put(timestamp, value);
    }

    private TimestampsParser() {
    }
}
