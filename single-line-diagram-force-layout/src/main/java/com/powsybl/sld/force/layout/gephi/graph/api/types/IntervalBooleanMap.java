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

/**
 * Sorted map where keys are intervals and values boolean values.
 */
public final class IntervalBooleanMap extends AbstractIntervalMap<Boolean> {

    private boolean[] values;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public IntervalBooleanMap() {
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
    public IntervalBooleanMap(int capacity) {
        super(capacity);
        values = new boolean[capacity];
    }

    /**
     * Constructor with an initial interval map.
     * <p>
     * The <code>keys</code> array must be in the same format returned by
     * {@link #getIntervals() }.
     *
     * @param keys initial keys content
     * @param vals initial values content
     */
    public IntervalBooleanMap(double[] keys, boolean[] vals) {
        super(keys);
        values = new boolean[vals.length];
        System.arraycopy(vals, 0, values, 0, vals.length);
    }

    /**
     * Get the value for the given interval.
     *
     * @param interval interval
     * @return found value or the default value if not found
     * @throws IllegalArgumentException if the element doesn't exist
     */
    public boolean getBoolean(Interval interval) {
        final int index = getIndex(interval.getLow(), interval.getHigh());
        if (index >= 0) {
            return values[index / 2];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    /**
     * Get the value for the given interval.
     * <p>
     * Return <code>defaultValue</code> if the value is not found.
     *
     * @param interval interval
     * @param defaultValue default value
     * @return found value or the default value if not found
     */
    public boolean getBoolean(Interval interval, boolean defaultValue) {
        final int index = getIndex(interval.getLow(), interval.getHigh());
        if (index >= 0) {
            return values[index / 2];
        }
        return defaultValue;
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
        return (boolean[]) toNativeArray();
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

    @Override
    public boolean isSupported(Estimator estimator) {
        return estimator.is(Estimator.FIRST, Estimator.LAST, Estimator.MIN, Estimator.MAX);
    }

    @Override
    protected Object getMin(Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        for (int i = 0; i < intervals.length; i++) {
            if (getValue(intervals[i]).equals(Boolean.FALSE)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    protected Object getMax(Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        for (int i = 0; i < intervals.length; i++) {
            if (getValue(intervals[i]).equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
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
