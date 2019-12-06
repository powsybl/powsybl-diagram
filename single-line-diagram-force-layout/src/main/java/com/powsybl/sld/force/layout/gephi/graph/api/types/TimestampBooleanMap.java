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
package com.powsybl.sld.force.layout.gephi.graph.api.types;

import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;

import java.util.Arrays;

/**
 * Sorted map where keys are timestamp indices and values boolean values.
 */
public final class TimestampBooleanMap extends AbstractTimestampMap<Boolean> {

    private boolean[] values;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public TimestampBooleanMap() {
        super();
        values = new boolean[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public TimestampBooleanMap(int capacity) {
        super(capacity);
        values = new boolean[capacity];
    }

    /**
     * Constructor with an initial timestamp map.
     * <p>
     * The <code>keys</code> array must be sorted and contain no duplicates.
     *
     * @param keys initial keys content
     * @param vals initial values content
     */
    public TimestampBooleanMap(double[] keys, boolean[] vals) {
        super(keys);
        values = new boolean[vals.length];
        System.arraycopy(vals, 0, values, 0, vals.length);
    }

    /**
     * Get the value for the given timestamp.
     *
     * @param timestamp timestamp
     * @return found value or the default value if not found
     * @throws IllegalArgumentException if the element doesn't exist
     */
    public boolean getBoolean(double timestamp) {
        final int index = getIndex(timestamp);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    /**
     * Get the value for the given timestamp.
     * <p>
     * Return <code>defaultValue</code> if the value is not found.
     *
     * @param timestamp timestamp
     * @param defaultValue default value
     * @return found value or the default value if not found
     */
    public boolean getBoolean(double timestamp, boolean defaultValue) {
        final int index = getIndex(timestamp);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    @Override
    protected Object getMin(Interval interval) {
        if (size == 0) {
            return null;
        }
        double lowBound = interval.getLow();
        double highBound = interval.getHigh();
        int index = Arrays.binarySearch(array, lowBound);
        if (index < 0) {
            index = -index - 1;
        }

        boolean t = false;
        for (int i = index; i < size && array[i] <= highBound; i++) {
            boolean val = values[i];
            if (!val) {
                return Boolean.FALSE;
            } else {
                t = true;
            }
        }
        if (t) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    protected Object getMax(Interval interval) {
        if (size == 0) {
            return null;
        }
        double lowBound = interval.getLow();
        double highBound = interval.getHigh();
        int index = Arrays.binarySearch(array, lowBound);
        if (index < 0) {
            index = -index - 1;
        }

        boolean f = false;
        for (int i = index; i < size && array[i] <= highBound; i++) {
            boolean val = values[i];
            if (val) {
                return Boolean.TRUE;
            } else {
                f = true;
            }
        }
        if (f) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

    /**
     * Returns an array of all values in this map.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all values
     */
    public boolean[] toBooleanArray() {
        return (boolean[]) toPrimitiveArray();
    }

    @Override
    public boolean isSupported(Estimator estimator) {
        return estimator.is(Estimator.MIN, Estimator.MAX, Estimator.FIRST, Estimator.LAST);
    }

    @Override
    protected Boolean getValue(int index) {
        return values[index];
    }

    @Override
    protected Object getValuesArray() {
        return values;
    }

    @Override
    protected void setValuesArray(Object array) {
        values = (boolean[]) array;
    }
}
