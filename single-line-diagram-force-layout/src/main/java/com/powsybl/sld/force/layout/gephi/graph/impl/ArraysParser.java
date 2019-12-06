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

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.COMMA;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.EMPTY_VALUE;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.LEFT_BOUND_BRACKET;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.LEFT_BOUND_SQUARE_BRACKET;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.RIGHT_BOUND_BRACKET;
import static com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils.RIGHT_BOUND_SQUARE_BRACKET;

/**
 * <p>
 * Class for parsing array types.
 * </p>
 *
 * <p>
 * The format for all arrays is like {@code [value1, value2, value3]}
 *
 * </p>
 *
 * @author Eduardo Ramos
 */
public final class ArraysParser {

    /**
     * Parses an array of any non-primitive type.
     *
     * @param <T> Non primitive type
     * @param arrayTypeClass Array type to parse
     * @param input Input string to parse
     * @return Parsed array
     * @throws IllegalArgumentException Any parse exception
     */
    public static <T> T[] parseArray(Class<T[]> arrayTypeClass, String input) throws IllegalArgumentException {
        if (input == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Class<T> componentType = (Class<T>) arrayTypeClass.getComponentType();

        if (input.equalsIgnoreCase(EMPTY_VALUE)) {
            @SuppressWarnings("unchecked")
            T[] result = (T[]) Array.newInstance(componentType, 0);
            return result;
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
                    case RIGHT_BOUND_SQUARE_BRACKET:
                    case RIGHT_BOUND_BRACKET:
                    case LEFT_BOUND_BRACKET:
                    case LEFT_BOUND_SQUARE_BRACKET:
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                    case COMMA:
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
                        String value = FormattingAndParsingUtils.parseValue(reader);
                        if (value.equals("null")) {
                            value = null; // Special null value only when not in
                                         // literal parsing mode
                        }
                        values.add(value);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected expection while parsing array value", ex);
        }

        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(componentType, values.size());
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != null) {
                result[i] = FormattingAndParsingUtils.convertValue(componentType, values.get(i));
            } else {
                result[i] = null;
            }
        }

        return result;
    }

    /**
     * Parses an array of any primitive type.
     *
     * @param <T> Primitive type wrapper. For example Integer for int array or
     *        Long for long array.
     * @param arrayTypeClass Array type to parse
     * @param input Input string to parse
     * @return Parsed array
     * @throws IllegalArgumentException Parsing exception, or if any of the
     *         parsed array values is null
     */
    public static <T> Object parseArrayAsPrimitiveArray(Class<T[]> arrayTypeClass, String input) throws IllegalArgumentException {
        T[] array = parseArray(arrayTypeClass, input);
        return AttributeUtils.getPrimitiveArray(array);
    }

    private ArraysParser() {
    }
}
