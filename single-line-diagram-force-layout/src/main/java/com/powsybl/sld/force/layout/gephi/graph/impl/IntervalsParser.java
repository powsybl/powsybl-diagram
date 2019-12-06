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

import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalLongMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractIntervalMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalShortMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalStringMap;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.COMMA;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.EMPTY_VALUE;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.LEFT_BOUND_BRACKET;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.LEFT_BOUND_SQUARE_BRACKET;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.RIGHT_BOUND_BRACKET;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.RIGHT_BOUND_SQUARE_BRACKET;

/**
 * <p>
 * Class for parsing interval types.
 * </p>
 *
 * <p>
 * The standard format for {@link AbstractIntervalMap} is &lt;[start, end, value1];
 * [start, end, value2]&gt;.
 * </p>
 *
 * <p>
 * The standard format for {@link IntervalSet} is &lt;[start, end]; [start,
 * end]&gt;.
 * </p>
 *
 * <p>
 * Start and end values can be both numbers and ISO dates or datetimes. Dates
 * and datetimes will be converted to their millisecond-precision timestamp.
 * </p>
 *
 * Examples of valid interval maps are:
 * <ul>
 * <li>&lt;(1, 2, v1); [3, 5, v2]&gt;</li>
 * <li>[1.15,2.21, "literal value ' \" ,[]()"]</li>
 * <li>[1.15,2.21, 'literal value " \' ,[]()']</li>
 * </ul>
 *
 * Examples of valid interval sets are:
 * <ul>
 * <li>&lt;[1,2]; [3, 4]&gt;</li>
 * <li>[1,2]</li>
 * <li>[1,2] (5,6)</li>
 * </ul>
 *
 * <p>
 * <b>All open intervals will be converted to closed intervals</b>, as only
 * closed intervals are supported.
 * </p>
 *
 * <p>
 * The most correct examples are those that include &lt; &gt; and proper commas
 * and semicolons for separation, but the parser will be indulgent when
 * possible.
 * </p>
 *
 * @author Eduardo Ramos
 */
public final class IntervalsParser {

    /**
     * Parses a {@link IntervalSet} type with one or more intervals.
     *
     * @param input Input string to parse
     * @param timeZone Time zone to use or null to use default time zone (UTC)
     * @return Resulting {@link IntervalSet}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if there are no intervals in the
     *         input string or bounds cannot be parsed into doubles or
     *         dates/datetimes.
     */
    public static IntervalSet parseIntervalSet(String input, DateTimeZone timeZone) throws IllegalArgumentException {
        if (input == null) {
            return null;
        }

        if (input.equalsIgnoreCase(EMPTY_VALUE)) {
            return new IntervalSet();
        }

        List<IntervalWithValue<Object>> intervals;
        try {
            intervals = parseIntervals(null, input, timeZone);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected expection while parsing intervals", ex);
        }

        IntervalSet result = new IntervalSet(intervals.size());
        for (IntervalWithValue<Object> interval : intervals) {
            result.add(interval.getInterval());
        }

        return result;
    }

    /**
     * Parses a {@link IntervalSet} type with one or more intervals. Default
     * time zone is used (UTC).
     *
     * @param input Input string to parse
     * @return Resulting {@link IntervalSet}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if there are no intervals in the
     *         input string or bounds cannot be parsed into doubles or
     *         dates/datetimes.
     */
    public static IntervalSet parseIntervalSet(String input) throws IllegalArgumentException {
        return parseIntervalSet(input, null);
    }

    /**
     * Parses a {@link AbstractIntervalMap} type with one or more intervals, and their
     * associated values.
     *
     * @param <T> Underlying type of the {@link AbstractIntervalMap} values
     * @param typeClassParam Simple type or {@link AbstractIntervalMap} subtype for the
     *        result intervals' values.
     * @param input Input string to parse
     * @param timeZone Time zone to use or null to use default time zone (UTC)
     * @return Resulting {@link AbstractIntervalMap}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if type class is not supported,
     *         any of the intervals don't have a value or have an invalid value,
     *         there are no intervals in the input string or bounds cannot be
     *         parsed into doubles or dates/datetimes.
     */
    public static <T> AbstractIntervalMap<T> parseIntervalMap(Class<T> typeClassParam, String input, DateTimeZone timeZone) throws IllegalArgumentException {
        Class<T> typeClass = typeClassParam;
        if (typeClass == null) {
            throw new IllegalArgumentException("typeClass required");
        }

        if (input == null) {
            return null;
        }

        List<IntervalWithValue<T>> intervals;
        try {
            intervals = parseIntervals(typeClass, input, timeZone);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected expection while parsing intervals", ex);
        }
        int capacity = intervals.size();

        AbstractIntervalMap result;
        typeClass = AttributeUtils.getStandardizedType(typeClass);
        if (typeClass.equals(String.class)) {
            result = new IntervalStringMap(capacity);
        } else if (typeClass.equals(Byte.class)) {
            result = new IntervalByteMap(capacity);
        } else if (typeClass.equals(Short.class)) {
            result = new IntervalShortMap(capacity);
        } else if (typeClass.equals(Integer.class)) {
            result = new IntervalIntegerMap(capacity);
        } else if (typeClass.equals(Long.class)) {
            result = new IntervalLongMap(capacity);
        } else if (typeClass.equals(Float.class)) {
            result = new IntervalFloatMap(capacity);
        } else if (typeClass.equals(Double.class)) {
            result = new IntervalDoubleMap(capacity);
        } else if (typeClass.equals(Boolean.class)) {
            result = new IntervalBooleanMap(capacity);
        } else if (typeClass.equals(Character.class)) {
            result = new IntervalCharMap(capacity);
        } else {
            throw new IllegalArgumentException("Unsupported type " + typeClass.getClass().getCanonicalName());
        }

        for (IntervalWithValue<T> interval : intervals) {
            T value = interval.getValue();
            if (value == null) {
                throw new IllegalArgumentException("A value must be provided for each interval");
            }
            result.put(interval.getInterval(), interval.getValue());
        }

        return result;
    }

    /**
     * Parses a {@link AbstractIntervalMap} type with one or more intervals, and their
     * associated values. Default time zone is used (UTC).
     *
     * @param <T> Underlying type of the {@link AbstractIntervalMap} values
     * @param typeClass Simple type or {@link AbstractIntervalMap} subtype for the
     *        result intervals' values.
     * @param input Input string to parse
     * @return Resulting {@link AbstractIntervalMap}, or null if the input equals
     *         '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if type class is not supported,
     *         any of the intervals don't have a value or have an invalid value,
     *         there are no intervals in the input string or bounds cannot be
     *         parsed into doubles or dates/datetimes.
     */
    public static <T> AbstractIntervalMap<T> parseIntervalMap(Class<T> typeClass, String input) throws IllegalArgumentException {
        return parseIntervalMap(typeClass, input, null);
    }

    /**
     * Parses intervals with values (of {@code typeClass} Class) or without
     * values (null {@code typeClass} Class)
     *
     * @param <T> Type of the interval value
     * @param typeClass Class of the intervals' values or null to parse
     *        intervals without values
     * @param inputParam Input to parse
     * @param timeZone Time zone to use or null to use default time zone (UTC)
     * @return List of Interval
     */
    private static <T> List<IntervalWithValue<T>> parseIntervals(Class<T> typeClass, String inputParam, DateTimeZone timeZone) throws IOException, IllegalArgumentException {
        String input = inputParam;
        if (input == null) {
            return null;
        }

        input = input.trim();

        List<IntervalWithValue<T>> intervals = new ArrayList<IntervalWithValue<T>>();

        if (input.equalsIgnoreCase(EMPTY_VALUE)) {
            return intervals;
        }

        StringReader reader = new StringReader(input + ' '); // Add 1 space so
                                                            // reader.skip
                                                            // function always
                                                            // works when
                                                            // necessary (end of
                                                            // string not
                                                            // reached).

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case LEFT_BOUND_SQUARE_BRACKET:
                case LEFT_BOUND_BRACKET:
                    intervals.add(parseInterval(typeClass, reader, timeZone));
                    break;
                default:
                    // Ignore other chars outside of intervals
            }
        }

        if (intervals.isEmpty()) {
            throw new IllegalArgumentException("No dynamic intervals could be parsed");
        }

        return intervals;
    }

    private static <T> IntervalWithValue<T> parseInterval(Class<T> typeClass, StringReader reader, DateTimeZone timeZone) throws IOException {
        ArrayList<String> values = new ArrayList<String>();

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case RIGHT_BOUND_SQUARE_BRACKET:
                case RIGHT_BOUND_BRACKET:
                    return buildInterval(typeClass, values, timeZone);
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case COMMA:
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

        return buildInterval(typeClass, values, timeZone);
    }

    private static <T> IntervalWithValue<T> buildInterval(Class<T> typeClass, ArrayList<String> values, DateTimeZone timeZone) {
        if (typeClass == null && values.size() != 2) {
            throw new IllegalArgumentException("Each interval must have 2 values");
        } else if (typeClass != null && values.size() != 3) {
            throw new IllegalArgumentException("Each interval must have 3 values");
        }

        double low = FormattingAndParsingUtils.parseDateTimeOrTimestamp(values.get(0), timeZone);
        double high = FormattingAndParsingUtils.parseDateTimeOrTimestamp(values.get(1), timeZone);

        if (typeClass == null) {
            return new IntervalWithValue(low, high, null);
        } else {
            String valString = values.get(2);
            Object value = FormattingAndParsingUtils.convertValue(typeClass, valString);

            return new IntervalWithValue(low, high, value);
        }
    }

    /**
     * Represents an Interval with an associated value for it. Only for internal
     * usage in this class.
     *
     * @author Eduardo Ramos
     * @param <T> Type of the value
     */
    private static class IntervalWithValue<T> {

        private final Interval interval;
        private final T value;

        public IntervalWithValue(double low, double high, T value) {
            this.interval = new Interval(low, high);
            this.value = value;
        }

        public IntervalWithValue(Interval interval, T value) {
            this.interval = interval;
            this.value = value;
        }

        public Interval getInterval() {
            return interval;
        }

        public T getValue() {
            return value;
        }
    }

    private IntervalsParser() {
    }
}
