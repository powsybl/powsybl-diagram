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
package com.powsybl.sld.force.layout.gephi.graph.api;

import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractTimestampMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalShortMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampShortMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampStringMap;
import com.powsybl.sld.force.layout.gephi.graph.impl.ArraysParser;
import com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils;
import com.powsybl.sld.force.layout.gephi.graph.impl.GraphStoreConfiguration;
import com.powsybl.sld.force.layout.gephi.graph.impl.IntervalsParser;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanOpenHashSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalLongMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractIntervalMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalStringMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampLongMap;
import com.powsybl.sld.force.layout.gephi.graph.impl.TimestampsParser;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Set of utility methods to manipulate supported attribute types.
 * <p>
 * The attribute system is built with a set of supported column types. This
 * class contains utilities to parse and convert supported types. It also
 * contains utilities to manipulate primitive arrays (the preferred array type)
 * and date/time types. Default time zone for parsing/printing dates is UTC.
 */
public final class AttributeUtils {

    private static final Set<Class> SUPPORTED_TYPES;
    private static final Map<Class, Class> TYPES_STANDARDIZATION;
    private static final DateTimeFormatter DATE_TIME_PARSER;
    private static final DateTimeFormatter DATE_PRINTER;
    private static final DateTimeFormatter DATE_TIME_PRINTER;
    private static final DecimalFormat TIMESTAMP_PRINTER;

    // These are used to avoid creating a lot of new instances of
    // DateTimeFormatter
    private static final Map<DateTimeZone, DateTimeFormatter> DATE_PRINTERS_BY_TIMEZONE;
    private static final Map<DateTimeZone, DateTimeFormatter> DATE_TIME_PRINTERS_BY_TIMEZONE;
    private static final Map<DateTimeZone, DateTimeFormatter> DATE_TIME_PARSERS_BY_TIMEZONE;

    // Collectio types to speedup lookup
    private static final Set<Class> TYPED_LIST_TYPES;
    private static final Set<Class> TYPED_SET_TYPES;
    private static final Set<Class> TYPED_MAP_TYPES;

    static {
        final Set<Class> supportedTypes = new HashSet<Class>();

        // Primitives
        supportedTypes.add(Boolean.class);
        supportedTypes.add(boolean.class);
        supportedTypes.add(Integer.class);
        supportedTypes.add(int.class);
        supportedTypes.add(Short.class);
        supportedTypes.add(short.class);
        supportedTypes.add(Long.class);
        supportedTypes.add(long.class);
        supportedTypes.add(BigInteger.class);
        supportedTypes.add(Byte.class);
        supportedTypes.add(byte.class);
        supportedTypes.add(Float.class);
        supportedTypes.add(float.class);
        supportedTypes.add(Double.class);
        supportedTypes.add(double.class);
        supportedTypes.add(BigDecimal.class);
        supportedTypes.add(Character.class);
        supportedTypes.add(char.class);

        // Objects
        supportedTypes.add(String.class);

        // Primitives Array
        supportedTypes.add(Boolean[].class);
        supportedTypes.add(boolean[].class);
        supportedTypes.add(Integer[].class);
        supportedTypes.add(int[].class);
        supportedTypes.add(Short[].class);
        supportedTypes.add(short[].class);
        supportedTypes.add(Long[].class);
        supportedTypes.add(long[].class);
        supportedTypes.add(BigInteger[].class);
        supportedTypes.add(Byte[].class);
        supportedTypes.add(byte[].class);
        supportedTypes.add(Float[].class);
        supportedTypes.add(float[].class);
        supportedTypes.add(Double[].class);
        supportedTypes.add(double[].class);
        supportedTypes.add(BigDecimal[].class);
        supportedTypes.add(Character[].class);
        supportedTypes.add(char[].class);

        // Objects array
        supportedTypes.add(String[].class);

        // Dynamic (timestamps)
        supportedTypes.add(TimestampSet.class);
        supportedTypes.add(TimestampBooleanMap.class);
        supportedTypes.add(TimestampIntegerMap.class);
        supportedTypes.add(TimestampShortMap.class);
        supportedTypes.add(TimestampLongMap.class);
        supportedTypes.add(TimestampByteMap.class);
        supportedTypes.add(TimestampFloatMap.class);
        supportedTypes.add(TimestampDoubleMap.class);
        supportedTypes.add(TimestampCharMap.class);
        supportedTypes.add(TimestampStringMap.class);

        // Dynamic (intervals)
        supportedTypes.add(IntervalSet.class);
        supportedTypes.add(IntervalBooleanMap.class);
        supportedTypes.add(IntervalIntegerMap.class);
        supportedTypes.add(IntervalShortMap.class);
        supportedTypes.add(IntervalLongMap.class);
        supportedTypes.add(IntervalByteMap.class);
        supportedTypes.add(IntervalFloatMap.class);
        supportedTypes.add(IntervalDoubleMap.class);
        supportedTypes.add(IntervalCharMap.class);
        supportedTypes.add(IntervalStringMap.class);

        // Lists, Maps, Sets
        supportedTypes.add(List.class);
        supportedTypes.add(Set.class);
        supportedTypes.add(Map.class);

        // Assign
        SUPPORTED_TYPES = Collections.unmodifiableSet(supportedTypes);

        // Primitive types standardization
        final Map<Class, Class> typesStandardization = new HashMap<Class, Class>();
        typesStandardization.put(boolean.class, Boolean.class);
        typesStandardization.put(int.class, Integer.class);
        typesStandardization.put(short.class, Short.class);
        typesStandardization.put(long.class, Long.class);
        typesStandardization.put(byte.class, Byte.class);
        typesStandardization.put(float.class, Float.class);
        typesStandardization.put(double.class, Double.class);
        typesStandardization.put(char.class, Character.class);

        // Array standardization
        typesStandardization.put(Boolean[].class, boolean[].class);
        typesStandardization.put(Integer[].class, int[].class);
        typesStandardization.put(Short[].class, short[].class);
        typesStandardization.put(Long[].class, long[].class);
        typesStandardization.put(Byte[].class, byte[].class);
        typesStandardization.put(Float[].class, float[].class);
        typesStandardization.put(Double[].class, double[].class);
        typesStandardization.put(Character[].class, char[].class);

        // Assign
        TYPES_STANDARDIZATION = Collections.unmodifiableMap(typesStandardization);

        // Datetime - make sure UTC timezone is used by default
        DATE_TIME_PARSER = ISODateTimeFormat.dateOptionalTimeParser()
                .withZone(GraphStoreConfiguration.DEFAULT_TIME_ZONE);
        DATE_PRINTER = ISODateTimeFormat.date().withZone(GraphStoreConfiguration.DEFAULT_TIME_ZONE);
        DATE_TIME_PRINTER = ISODateTimeFormat.dateTime().withZone(GraphStoreConfiguration.DEFAULT_TIME_ZONE);

        DATE_PRINTERS_BY_TIMEZONE = new HashMap<DateTimeZone, DateTimeFormatter>();
        DATE_TIME_PRINTERS_BY_TIMEZONE = new HashMap<DateTimeZone, DateTimeFormatter>();
        DATE_TIME_PARSERS_BY_TIMEZONE = new HashMap<DateTimeZone, DateTimeFormatter>();

        DATE_PRINTERS_BY_TIMEZONE.put(DATE_PRINTER.getZone(), DATE_PRINTER);
        DATE_TIME_PRINTERS_BY_TIMEZONE.put(DATE_TIME_PRINTER.getZone(), DATE_TIME_PRINTER);
        DATE_TIME_PARSERS_BY_TIMEZONE.put(DATE_TIME_PARSER.getZone(), DATE_TIME_PARSER);

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        decimalFormatSymbols.setInfinity(FormattingAndParsingUtils.INFINITY);
        // 1 to 4 decimals
        TIMESTAMP_PRINTER = new DecimalFormat("0.0###", decimalFormatSymbols);

        // List types
        TYPED_LIST_TYPES = new HashSet<Class>();
        TYPED_LIST_TYPES.add(IntArrayList.class);
        TYPED_LIST_TYPES.add(FloatArrayList.class);
        TYPED_LIST_TYPES.add(DoubleArrayList.class);
        TYPED_LIST_TYPES.add(ShortArrayList.class);
        TYPED_LIST_TYPES.add(LongArrayList.class);
        TYPED_LIST_TYPES.add(ByteArrayList.class);
        TYPED_LIST_TYPES.add(BooleanArrayList.class);
        TYPED_LIST_TYPES.add(CharArrayList.class);

        // Set types
        TYPED_SET_TYPES = new HashSet<Class>();
        TYPED_SET_TYPES.add(IntOpenHashSet.class);
        TYPED_SET_TYPES.add(FloatOpenHashSet.class);
        TYPED_SET_TYPES.add(DoubleOpenHashSet.class);
        TYPED_SET_TYPES.add(ShortOpenHashSet.class);
        TYPED_SET_TYPES.add(LongOpenHashSet.class);
        TYPED_SET_TYPES.add(ByteOpenHashSet.class);
        TYPED_SET_TYPES.add(BooleanOpenHashSet.class);
        TYPED_SET_TYPES.add(CharOpenHashSet.class);

        // Map types
        TYPED_MAP_TYPES = new HashSet<Class>();
        TYPED_MAP_TYPES.add(Int2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Float2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Double2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Short2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Long2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Byte2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Char2ObjectOpenHashMap.class);
    }

    private AttributeUtils() {
        // Only static methods
    }

    private static DateTimeFormatter getDateTimeFormatterByTimeZone(Map<DateTimeZone, DateTimeFormatter> cache, DateTimeFormatter baseFormatter, DateTimeZone timeZone) {
        if (timeZone == null) {
            return baseFormatter;
        }

        DateTimeFormatter formatter = cache.get(timeZone);
        if (formatter == null) {
            formatter = baseFormatter.withZone(timeZone);
            cache.put(timeZone, formatter);
        }

        return formatter;
    }

    private static DateTimeFormatter getDateTimeParserByTimeZone(DateTimeZone timeZone) {
        return getDateTimeFormatterByTimeZone(DATE_TIME_PARSERS_BY_TIMEZONE, DATE_TIME_PARSER, timeZone);
    }

    private static DateTimeFormatter getDateTimePrinterByTimeZone(DateTimeZone timeZone) {
        return getDateTimeFormatterByTimeZone(DATE_TIME_PRINTERS_BY_TIMEZONE, DATE_TIME_PRINTER, timeZone);
    }

    private static DateTimeFormatter getDatePrinterByTimeZone(DateTimeZone timeZone) {
        return getDateTimeFormatterByTimeZone(DATE_PRINTERS_BY_TIMEZONE, DATE_PRINTER, timeZone);
    }

    /**
     * Returns the string representation of the given value.
     *
     * @param value value
     * @return string representation
     */
    public static String print(Object value) {
        return print(value, TimeFormat.DOUBLE, null);
    }

    /**
     * Returns the string representation of the given value.
     *
     * @param value value
     * @param timeFormat time format
     * @param timeZone time zone
     * @return string representation
     */
    public static String print(Object value, TimeFormat timeFormat, DateTimeZone timeZone) {
        if (value == null) {
            return "null";
        }
        if (value instanceof TimeSet) {
            return ((TimeSet) value).toString(timeFormat, timeZone);
        }
        if (value instanceof TimeMap) {
            return ((TimeMap) value).toString(timeFormat, timeZone);
        }
        if (value.getClass().isArray()) {
            return printArray(value);
        }
        return value.toString();
    }

    /**
     * Parses the given string using the type class provided and returns an
     * instance.
     *
     * @param str string to parse
     * @param typeClassParam class of the desired type
     * @param timeZone time zone to use or null to use default time zone (UTC),
     *        for dynamic types only
     * @return an instance of the type class, or null if <em>str</em> is null or
     *         empty
     */
    public static Object parse(String str, Class typeClassParam, DateTimeZone timeZone) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        Class typeClass = typeClassParam;
        if (typeClassParam.isPrimitive()) {
            typeClass = getStandardizedType(typeClassParam); // For primitives we can
                                                        // use auto-unboxing
        }

        // Simple and primitive types:
        if (typeClass.equals(String.class)) {
            return str;
        } else if (typeClass.equals(Byte.class)) {
            return new Byte(str);
        } else if (typeClass.equals(Short.class)) {
            return new Short(str);
        } else if (typeClass.equals(Integer.class)) {
            return new Integer(str);
        } else if (typeClass.equals(Long.class)) {
            return new Long(str);
        } else if (typeClass.equals(Float.class)) {
            return new Float(str);
        } else if (typeClass.equals(Double.class)) {
            return new Double(str);
        } else if (typeClass.equals(BigInteger.class)) {
            return new BigInteger(str);
        } else if (typeClass.equals(BigDecimal.class)) {
            return new BigDecimal(str);
        } else if (typeClass.equals(Boolean.class)) {
            if (str.length() == 1) {
                if (str.charAt(0) == '1') {
                    return Boolean.TRUE;
                } else if (str.charAt(0) == '0') {
                    return Boolean.FALSE;
                }
            }
            return Boolean.valueOf(str);
        } else if (typeClass.equals(Character.class)) {
            if (str.length() > 1) {
                throw new IllegalArgumentException("The string has a length > 1");
            }
            return str.charAt(0);
        }

        // Interval types:
        if (typeClass.equals(IntervalSet.class)) {
            return IntervalsParser.parseIntervalSet(str, timeZone);
        } else if (typeClass.equals(IntervalStringMap.class)) {
            return IntervalsParser.parseIntervalMap(String.class, str, timeZone);
        } else if (typeClass.equals(IntervalByteMap.class)) {
            return IntervalsParser.parseIntervalMap(Byte.class, str, timeZone);
        } else if (typeClass.equals(IntervalShortMap.class)) {
            return IntervalsParser.parseIntervalMap(Short.class, str, timeZone);
        } else if (typeClass.equals(IntervalIntegerMap.class)) {
            return IntervalsParser.parseIntervalMap(Integer.class, str, timeZone);
        } else if (typeClass.equals(IntervalLongMap.class)) {
            return IntervalsParser.parseIntervalMap(Long.class, str, timeZone);
        } else if (typeClass.equals(IntervalFloatMap.class)) {
            return IntervalsParser.parseIntervalMap(Float.class, str, timeZone);
        } else if (typeClass.equals(IntervalDoubleMap.class)) {
            return IntervalsParser.parseIntervalMap(Double.class, str, timeZone);
        } else if (typeClass.equals(IntervalBooleanMap.class)) {
            return IntervalsParser.parseIntervalMap(Boolean.class, str, timeZone);
        } else if (typeClass.equals(IntervalCharMap.class)) {
            return IntervalsParser.parseIntervalMap(Character.class, str, timeZone);
        }

        // Timestamp types:
        if (typeClass.equals(TimestampSet.class)) {
            return TimestampsParser.parseTimestampSet(str, timeZone);
        } else if (typeClass.equals(TimestampStringMap.class)) {
            return TimestampsParser.parseTimestampMap(String.class, str, timeZone);
        } else if (typeClass.equals(TimestampByteMap.class)) {
            return TimestampsParser.parseTimestampMap(Byte.class, str, timeZone);
        } else if (typeClass.equals(TimestampShortMap.class)) {
            return TimestampsParser.parseTimestampMap(Short.class, str, timeZone);
        } else if (typeClass.equals(TimestampIntegerMap.class)) {
            return TimestampsParser.parseTimestampMap(Integer.class, str, timeZone);
        } else if (typeClass.equals(TimestampLongMap.class)) {
            return TimestampsParser.parseTimestampMap(Long.class, str, timeZone);
        } else if (typeClass.equals(TimestampFloatMap.class)) {
            return TimestampsParser.parseTimestampMap(Float.class, str, timeZone);
        } else if (typeClass.equals(TimestampDoubleMap.class)) {
            return TimestampsParser.parseTimestampMap(Double.class, str, timeZone);
        } else if (typeClass.equals(TimestampBooleanMap.class)) {
            return TimestampsParser.parseTimestampMap(Boolean.class, str, timeZone);
        } else if (typeClass.equals(TimestampCharMap.class)) {
            return TimestampsParser.parseTimestampMap(Character.class, str, timeZone);
        }

        // Array types:
        if (typeClass.equals(boolean[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Boolean[].class, str);
        } else if (typeClass.equals(char[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Character[].class, str);
        } else if (typeClass.equals(byte[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Byte[].class, str);
        } else if (typeClass.equals(short[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Short[].class, str);
        } else if (typeClass.equals(int[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Integer[].class, str);
        } else if (typeClass.equals(long[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Long[].class, str);
        } else if (typeClass.equals(float[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Float[].class, str);
        } else if (typeClass.equals(double[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Double[].class, str);
        } else if (typeClass.equals(Boolean[].class) || typeClass.equals(String[].class) || typeClass
                .equals(Character[].class) || typeClass.equals(Byte[].class) || typeClass.equals(Short[].class) || typeClass
                .equals(Integer[].class) || typeClass.equals(Long[].class) || typeClass.equals(Float[].class) || typeClass
                .equals(Double[].class) || typeClass.equals(BigInteger[].class) || typeClass.equals(BigDecimal[].class)) {
            return ArraysParser.parseArray(typeClass, str);
        }

        throw new IllegalArgumentException("Unsupported type " + typeClass.getClass().getCanonicalName());
    }

    /**
     * Parses the given string using the type class provided and returns an
     * instance.
     *
     * Default time zone is used (UTC) for dynamic types (timestamps/intervals).
     *
     * @param str string to parse
     * @param typeClass class of the desired type
     * @return an instance of the type class, or null if <em>str</em> is null or
     *         empty
     */
    public static Object parse(String str, Class typeClass) {
        return parse(str, typeClass, null);
    }

    /**
     * Returns the primitive type for the given wrapped primitive.
     * <p>
     * Example: Returns <em>int.class</em> given <em>Integer.class</em>
     *
     * @param type type to get the primitive type from
     * @return primitive type
     */
    public static Class getPrimitiveType(Class type) {
        if (!type.isPrimitive()) {
            if (type.equals(Boolean.class)) {
                return boolean.class;
            } else if (type.equals(Integer.class)) {
                return int.class;
            } else if (type.equals(Short.class)) {
                return short.class;
            } else if (type.equals(Long.class)) {
                return long.class;
            } else if (type.equals(Byte.class)) {
                return byte.class;
            } else if (type.equals(Float.class)) {
                return float.class;
            } else if (type.equals(Double.class)) {
                return double.class;
            } else if (type.equals(Character.class)) {
                return char.class;
            }
        }
        throw new IllegalArgumentException("The type should be a wrapped primitive");
    }

    /**
     * Returns the primitive array given a wrapped primitive array.
     * <p>
     * Example: Returns <em>int[]</em> array given an <em>Integer[]</em> array
     *
     * @param array wrapped primitive array instance
     * @return primitive array instance
     * @throws IllegalArgumentException Thrown if any of the array values is
     *         null
     */
    public static Object getPrimitiveArray(Object[] array) {
        if (!isSupported(array.getClass())) {
            throw new IllegalArgumentException("Unsupported type " + array.getClass().getCanonicalName());
        }
        Class arrayClass = array.getClass().getComponentType();
        if (!arrayClass.isPrimitive() && (arrayClass == Double.class || arrayClass == Float.class || arrayClass == Long.class || arrayClass == Integer.class || arrayClass == Short.class || arrayClass == Character.class || arrayClass == Byte.class || arrayClass == Boolean.class)) {
            Class primitiveClass = getPrimitiveType(arrayClass);

            int arrayLength = array.length;
            Object primitiveArray = Array.newInstance(primitiveClass, arrayLength);

            for (int i = 0; i < arrayLength; i++) {
                Object obj = array[i];
                Array.set(primitiveArray, i, obj);
            }
            return primitiveArray;
        }
        return array;
    }

    /**
     * Returns the set of types supported.
     *
     * @return set of supported types
     */
    public static Set<Class> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    /**
     * Returns true if <em>type</em> is a supported type.
     *
     * @param type type to test support
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(Class type) {
        if (type == null) {
            throw new NullPointerException();
        }
        return SUPPORTED_TYPES.contains(type) || isCollectionType(type) || isMapType(type);
    }

    /**
     * Returns the standardized type for the given type class.
     * <p>
     * For instance, <code>getStandardizedType(int.class)</code> would return
     * <code>Integer.class</code>.
     *
     * @param type type to standardize
     * @return standardized type
     */
    public static Class getStandardizedType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        Class t = TYPES_STANDARDIZATION.get(type);
        if (t != null) {
            return t;
        }
        return type;
    }

    /**
     * Returns true if <em>type</em> is a standardized type.
     * <p>
     * Non standardized types are transformed into standardized types using
     * {@link #getStandardizedType(Class) }.
     *
     * @param type the type to test
     * @return true if <em>type</em> is standardized, false otherwise
     */
    public static boolean isStandardizedType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        return TYPES_STANDARDIZATION.get(type) == null;
    }

    /**
     * Returns the dynamic timestamp map value type for the given type.
     *
     * @param typeParam static type
     * @return timestamp map type
     */
    public static Class<? extends AbstractTimestampMap> getTimestampMapType(Class typeParam) {
        if (!isSupported(typeParam)) {
            throw new IllegalArgumentException("Unsupported type " + typeParam.getCanonicalName());
        }
        Class type = getStandardizedType(typeParam);
        if (type.equals(Boolean.class)) {
            return TimestampBooleanMap.class;
        } else if (type.equals(Integer.class)) {
            return TimestampIntegerMap.class;
        } else if (type.equals(Short.class)) {
            return TimestampShortMap.class;
        } else if (type.equals(Long.class)) {
            return TimestampLongMap.class;
        } else if (type.equals(Byte.class)) {
            return TimestampByteMap.class;
        } else if (type.equals(Float.class)) {
            return TimestampFloatMap.class;
        } else if (type.equals(Double.class)) {
            return TimestampDoubleMap.class;
        } else if (type.equals(Character.class)) {
            return TimestampCharMap.class;
        } else if (type.equals(String.class)) {
            return TimestampStringMap.class;
        }
        throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
    }

    /**
     * Returns the dynamic timestamp map value type for the given type.
     *
     * @param typeParam static type
     * @return timestamp map type
     */
    public static Class<? extends AbstractIntervalMap> getIntervalMapType(Class typeParam) {
        if (!isSupported(typeParam)) {
            throw new IllegalArgumentException("Unsupported type " + typeParam.getCanonicalName());
        }
        Class type = getStandardizedType(typeParam);
        if (type.equals(Boolean.class)) {
            return IntervalBooleanMap.class;
        } else if (type.equals(Integer.class)) {
            return IntervalIntegerMap.class;
        } else if (type.equals(Short.class)) {
            return IntervalShortMap.class;
        } else if (type.equals(Long.class)) {
            return IntervalLongMap.class;
        } else if (type.equals(Byte.class)) {
            return IntervalByteMap.class;
        } else if (type.equals(Float.class)) {
            return IntervalFloatMap.class;
        } else if (type.equals(Double.class)) {
            return IntervalDoubleMap.class;
        } else if (type.equals(Character.class)) {
            return IntervalCharMap.class;
        } else if (type.equals(String.class)) {
            return IntervalStringMap.class;
        }
        throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
    }

    /**
     * Returns the static type for the given time map type.
     *
     * @param type time map type
     * @return static type
     */
    public static Class getStaticType(Class<? extends TimeMap> type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }

        if (type.equals(TimestampBooleanMap.class) || type.equals(IntervalBooleanMap.class)) {
            return Boolean.class;
        } else if (type.equals(TimestampIntegerMap.class) || type.equals(IntervalIntegerMap.class)) {
            return Integer.class;
        } else if (type.equals(TimestampShortMap.class) || type.equals(IntervalShortMap.class)) {
            return Short.class;
        } else if (type.equals(TimestampLongMap.class) || type.equals(IntervalLongMap.class)) {
            return Long.class;
        } else if (type.equals(TimestampByteMap.class) || type.equals(IntervalByteMap.class)) {
            return Byte.class;
        } else if (type.equals(TimestampFloatMap.class) || type.equals(IntervalFloatMap.class)) {
            return Float.class;
        } else if (type.equals(TimestampDoubleMap.class) || type.equals(IntervalDoubleMap.class)) {
            return Double.class;
        } else if (type.equals(TimestampCharMap.class) || type.equals(IntervalCharMap.class)) {
            return Character.class;
        } else if (type.equals(TimestampStringMap.class) || type.equals(IntervalStringMap.class)) {
            return String.class;
        }
        throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
    }

    /**
     * Transform the given <em>value</em> instance in a standardized type if
     * necessary.
     * <p>
     * This function transforms wrapped primitive arrays in primitive arrays.
     *
     * @param value value to standardize
     * @return standardized value, or <em>value</em> if already standardized
     */
    public static Object standardizeValue(Object value) {
        if (value == null) {
            return null;
        }
        Class type = value.getClass();
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        if (type.isArray() && !type.getComponentType().isPrimitive()) {
            return getPrimitiveArray((Object[]) value);
        }
        if (List.class.isAssignableFrom(type)) {
            return getStandardizedList((List) value);
        } else if (Set.class.isAssignableFrom(type)) {
            return getStandardizedSet((Set) value);
        } else if (Map.class.isAssignableFrom(type)) {
            return getStandardizedMap((Map) value);
        }
        return value;
    }

    private static List getStandardizedList(List list) {
        Class listClass = list.getClass();
        if (TYPED_LIST_TYPES.contains(listClass)) {
            return list;
        }

        Class oCls = null;
        for (Object o : list) {
            if (o != null) {
                if (oCls == null) {
                    oCls = o.getClass();
                } else if (!o.getClass().equals(oCls)) {
                    throw new IllegalArgumentException("The list contains mixed classes");
                }
            }
        }
        if (oCls != null && !(isSimpleType(oCls) || isArrayType(oCls))) {
            throw new IllegalArgumentException("The list contains unsupported type " + oCls.getClass()
                    .getCanonicalName());
        }
        if (oCls != null) {
            if (oCls.equals(Integer.class)) {
                return new IntArrayList(list);
            } else if (oCls.equals(Float.class)) {
                return new FloatArrayList(list);
            } else if (oCls.equals(Double.class)) {
                return new DoubleArrayList(list);
            } else if (oCls.equals(Short.class)) {
                return new ShortArrayList(list);
            } else if (oCls.equals(Byte.class)) {
                return new ByteArrayList(list);
            } else if (oCls.equals(Long.class)) {
                return new LongArrayList(list);
            } else if (oCls.equals(Boolean.class)) {
                return new BooleanArrayList(list);
            } else if (oCls.equals(Character.class)) {
                return new CharArrayList(list);
            }
        }
        List result = new ObjectArrayList(list.size());
        for (Object o : list) {
            result.add(standardizeValue(o));
        }
        return result;
    }

    private static Set getStandardizedSet(Set set) {
        Class listClass = set.getClass();
        if (TYPED_LIST_TYPES.contains(listClass)) {
            return set;
        }

        Class oCls = null;
        for (Object o : set) {
            if (o != null) {
                if (oCls == null) {
                    oCls = o.getClass();
                } else if (!o.getClass().equals(oCls)) {
                    throw new IllegalArgumentException("The set contains mixed classes");
                }
            }
        }
        if (oCls != null && !(isSimpleType(oCls) || isArrayType(oCls))) {
            throw new IllegalArgumentException("The set contains unsupported type " + oCls.getClass()
                    .getCanonicalName());
        }
        if (oCls != null) {
            if (oCls.equals(Integer.class)) {
                return new IntOpenHashSet(set);
            } else if (oCls.equals(Float.class)) {
                return new FloatOpenHashSet(set);
            } else if (oCls.equals(Double.class)) {
                return new DoubleOpenHashSet(set);
            } else if (oCls.equals(Short.class)) {
                return new ShortOpenHashSet(set);
            } else if (oCls.equals(Byte.class)) {
                return new ByteOpenHashSet(set);
            } else if (oCls.equals(Long.class)) {
                return new LongOpenHashSet(set);
            } else if (oCls.equals(Boolean.class)) {
                return new BooleanOpenHashSet(set);
            } else if (oCls.equals(Character.class)) {
                return new CharOpenHashSet(set);
            }
        }
        Set result = new ObjectOpenHashSet(set.size());
        for (Object o : set) {
            result.add(standardizeValue(o));
        }
        return result;
    }

    private static Map getStandardizedMap(Map<?, ?> map) {
        Class mapClass = map.getClass();
        if (TYPED_MAP_TYPES.contains(mapClass)) {
            return map;
        }

        Class oCls = null;
        for (Map.Entry entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null) {
                if (oCls == null) {
                    oCls = key.getClass();
                } else if (!key.getClass().equals(oCls)) {
                    throw new IllegalArgumentException("The map contains mixed key classes");
                }
            }
            if (value != null && !(isSimpleType(value.getClass()) || isArrayType(value.getClass()))) {
                throw new IllegalArgumentException("The map contains unsupported value type " + value.getClass()
                        .getCanonicalName());
            }
        }
        if (oCls != null && !isSimpleType(oCls)) {
            throw new IllegalArgumentException("The map contains unsupported key type " + oCls.getClass()
                    .getCanonicalName());
        }
        if (oCls != null) {
            if (oCls.equals(Integer.class)) {
                return new Int2ObjectOpenHashMap(map);
            } else if (oCls.equals(Float.class)) {
                return new Float2ObjectOpenHashMap(map);
            } else if (oCls.equals(Double.class)) {
                return new Double2ObjectOpenHashMap(map);
            } else if (oCls.equals(Short.class)) {
                return new Short2ObjectOpenHashMap(map);
            } else if (oCls.equals(Byte.class)) {
                return new Byte2ObjectOpenHashMap(map);
            } else if (oCls.equals(Long.class)) {
                return new Long2ObjectOpenHashMap(map);
            } else if (oCls.equals(Character.class)) {
                return new Char2ObjectOpenHashMap(map);
            }
        }
        Map result = new Object2ObjectOpenHashMap(map.size());
        for (Map.Entry o : map.entrySet()) {
            result.put(o.getKey(), standardizeValue(o.getValue()));
        }
        return result;
    }

    /**
     * Returns true if <em>type</em> is a number type.
     * <p>
     * This can be true for static, arrays and dynamic types.
     *
     * @param typeParam type to test
     * @return true if <em>type</em> is a number type, false otherwise
     */
    public static boolean isNumberType(Class typeParam) {
        if (!isSupported(typeParam)) {
            throw new IllegalArgumentException("Unsupported type " + typeParam.getCanonicalName());
        }
        Class type = getStandardizedType(typeParam);
        return Number.class.isAssignableFrom(type) || int[].class.isAssignableFrom(type) || float[].class
                .isAssignableFrom(type) || double[].class.isAssignableFrom(type) || byte[].class.isAssignableFrom(type) || short[].class
                .isAssignableFrom(type) || long[].class.isAssignableFrom(type) || type
                .equals(TimestampIntegerMap.class) || type.equals(TimestampFloatMap.class) || type
                .equals(TimestampDoubleMap.class) || type.equals(TimestampLongMap.class) || type
                .equals(TimestampShortMap.class) || type.equals(TimestampByteMap.class) || type
                .equals(IntervalIntegerMap.class) || type.equals(IntervalFloatMap.class) || type
                .equals(IntervalDoubleMap.class) || type.equals(IntervalLongMap.class) || type
                .equals(IntervalShortMap.class) || type.equals(IntervalByteMap.class);
    }

    /**
     * Returns true if <em>type</em> is a string type
     * <p>
     * This can be true for static, arrays and dynamic types.
     *
     * @param type type to test
     * @return true if <em>type</em> is a string type, false otherwise
     */
    public static boolean isStringType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        return type.equals(String.class) || type.equals(String[].class) || type.equals(TimestampStringMap.class) || type
                .equals(IntervalStringMap.class);
    }

    /**
     * Returns true if <em>type</em> is a boolean type
     * <p>
     * This can be true for static, arrays and dynamic types.
     *
     * @param typeParam type to test
     * @return true if <em>type</em> is a boolean type, false otherwise
     */
    public static boolean isBooleanType(Class typeParam) {
        if (!isSupported(typeParam)) {
            throw new IllegalArgumentException("Unsupported type " + typeParam.getCanonicalName());
        }
        Class type = getStandardizedType(typeParam);
        return type.equals(Boolean.class) || type.equals(boolean[].class) || type.equals(TimestampBooleanMap.class) || type
                .equals(IntervalBooleanMap.class);
    }

    /**
     * Returns true if <em>type</em> is a dynamic type.
     *
     * @param type type to test
     * @return true if <em>type</em> is a dynamic type, false otherwise
     */
    public static boolean isDynamicType(Class type) {
        return (!type.equals(AbstractTimestampMap.class) && AbstractTimestampMap.class.isAssignableFrom(type)) || type
                .equals(TimestampSet.class) || (!type.equals(AbstractIntervalMap.class) && AbstractIntervalMap.class
                .isAssignableFrom(type)) || type.equals(IntervalSet.class);
    }

    /**
     * Returns true if <em>type</em> is a simple type.
     * <p>
     * Simple types are primitives, String and wrapper types (e.g. Integer).
     *
     * @param type type to test
     * @return true if <em>type</em> is a simple type, false otherwise
     */
    public static boolean isSimpleType(Class type) {
        return (type.isPrimitive() && type != void.class) || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class;
    }

    /**
     * Returns true if <em>type</em> is an array type.
     *
     * @param type type to test
     * @return true if <em>type</em> is an array type, false otherwise
     */
    public static boolean isArrayType(Class type) {
        return type.isArray() && isSupported(type.getComponentType());
    }

    /**
     * Returns true if <em>type</em> is a collection type.
     * <p>
     * Collection types are either <em>List</em> or <em>Set</em>.
     *
     * @param type type to test
     * @return true if <em>type</em> is a collection type, false otherwise
     */
    public static boolean isCollectionType(Class type) {
        return List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type);
    }

    /**
     * Returns true if <em>type</em> is a map type.
     * <p>
     * Collection types implement the <em>Map</em> interface.
     *
     * @param type type to test
     * @return true if <em>type</em> is a map type, false otherwise
     */
    public static boolean isMapType(Class type) {
        return Map.class.isAssignableFrom(type);
    }

    /**
     * Returns the type name for the given type.
     *
     * @param typeParam type to get its name
     * @return type name
     */
    public static String getTypeName(Class typeParam) {
        if (!isSupported(typeParam)) {
            throw new IllegalArgumentException("Unsupported type " + typeParam.getCanonicalName());
        }
        Class type = getStandardizedType(typeParam);
        return type.getSimpleName().toLowerCase();
    }

    /**
     * Parses the given time and returns its milliseconds representation.
     *
     * @param dateTime type to parse
     * @param timeZone time zone to use or null to use default time zone (UTC)
     * @return milliseconds representation
     */
    public static double parseDateTime(String dateTime, DateTimeZone timeZone) {
        return getDateTimeParserByTimeZone(timeZone).parseDateTime(dateTime).getMillis();
    }

    /**
     * Parses the given time and returns its milliseconds representation.
     * Default time zone is used (UTC).
     *
     * @param dateTime the type to parse
     * @return milliseconds representation
     */
    public static double parseDateTime(String dateTime) {
        return parseDateTime(dateTime, null);
    }

    /**
     * Parses an ISO date with or without time or a timestamp (in milliseconds).
     * Returns the date or timestamp converted to a timestamp in milliseconds.
     *
     * @param timeStr Date or timestamp string
     * @param timeZone Time zone to use or null to use default time zone (UTC)
     * @return Timestamp
     */
    public static double parseDateTimeOrTimestamp(String timeStr, DateTimeZone timeZone) {
        return FormattingAndParsingUtils.parseDateTimeOrTimestamp(timeStr, timeZone);
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
        return FormattingAndParsingUtils.parseDateTimeOrTimestamp(timeStr, null);
    }

    /**
     * Returns the string representation of the given timestamp.
     *
     * @param timestamp the time, in milliseconds
     * @return formatted timestamp
     */
    public static String printTimestamp(double timestamp) {
        return TIMESTAMP_PRINTER.format(timestamp);
    }

    /**
     * Returns the date's string representation of the given timestamp.
     *
     * @param timestamp time, in milliseconds
     * @param timeZone time zone to use or null to use default time zone (UTC)
     * @return formatted date
     */
    public static String printDate(double timestamp, DateTimeZone timeZone) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            return printTimestamp(timestamp);
        }
        return getDatePrinterByTimeZone(timeZone).print((long) timestamp);
    }

    /**
     * Returns the date's string representation of the given timestamp. Default
     * time zone is used (UTC).
     *
     * @param timestamp time, in milliseconds
     * @return formatted date
     */
    public static String printDate(double timestamp) {
        return printDate(timestamp, null);
    }

    /**
     * Returns the time's string representation of the given timestamp.
     *
     * @param timestamp time, in milliseconds
     * @param timeZone time zone to use or null to use default time zone (UTC)
     * @return formatted time
     */
    public static String printDateTime(double timestamp, DateTimeZone timeZone) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            return printTimestamp(timestamp);
        }
        return getDateTimePrinterByTimeZone(timeZone).print((long) timestamp);
    }

    /**
     * Returns the time's tring representation of the given timestamp. Default
     * time zone is used (UTC).
     *
     * @param timestamp time, in milliseconds
     * @return formatted time
     */
    public static String printDateTime(double timestamp) {
        return printDateTime(timestamp, null);
    }

    /**
     * Returns the string representation of the given timestamp in the given
     * format.
     *
     * @param timestamp time, in milliseconds
     * @param timeFormat time format
     * @param timeZone time zone to use or null to use default time zone (UTC).
     * @return formatted timestamp
     */
    public static String printTimestampInFormat(double timestamp, TimeFormat timeFormat, DateTimeZone timeZone) {
        switch (timeFormat) {
            case DATE:
                return AttributeUtils.printDate(timestamp, timeZone);
            case DATETIME:
                return AttributeUtils.printDateTime(timestamp, timeZone);
            case DOUBLE:
                return AttributeUtils.printTimestamp(timestamp);
        }

        throw new UnsupportedOperationException("Unknown TimeFormat");
    }

    /**
     * Returns the string representation of the given timestamp in the given
     * format. Default time zone is used (UTC).
     *
     * @param timestamp time, in milliseconds
     * @param timeFormat time format
     * @return formatted timestamp
     */
    public static String printTimestampInFormat(double timestamp, TimeFormat timeFormat) {
        return printTimestampInFormat(timestamp, timeFormat, null);
    }

    /**
     * Returns the string representation of the given array. The used format is
     * the same format supported by
     * {@link #parse(String, Class)} method
     *
     * @param arr Input array. Can be an array of objects or primitives.
     * @return formatted array
     */
    public static String printArray(Object arr) {
        return FormattingAndParsingUtils.printArray(arr);
    }

    /**
     * Returns true if the given column is a node column.
     *
     * @param colum column to test
     * @return true if the column is a node column, false otherwise
     */
    public static boolean isNodeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Node.class);
    }

    /**
     * Returns true if the given column is an edge column.
     *
     * @param colum column to test
     * @return true if the column is an edge column, false otherwise
     */
    public static boolean isEdgeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Edge.class);
    }
}
