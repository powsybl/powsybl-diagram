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
 * Sorted map where keys are intervals and values byte values.
 */
public final class IntervalByteMap extends AbstractIntervalMap<Byte> {

    private byte[] values;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public IntervalByteMap() {
        super();
        values = new byte[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public IntervalByteMap(int capacity) {
        super(capacity);
        values = new byte[capacity];
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
    public IntervalByteMap(double[] keys, byte[] vals) {
        super(keys);
        values = new byte[vals.length];
        System.arraycopy(vals, 0, values, 0, vals.length);
    }

    /**
     * Get the value for the given interval.
     *
     * @param interval interval
     * @return found value or the default value if not found
     * @throws IllegalArgumentException if the element doesn't exist
     */
    public byte getByte(Interval interval) {
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
    public byte getByte(Interval interval, byte defaultValue) {
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
    public byte[] toByteArray() {
        return (byte[]) toNativeArray();
    }

    @Override
    public Class<Byte> getTypeClass() {
        return Byte.class;
    }

    @Override
    public boolean isSupported(Estimator estimator) {
        return estimator.is(Estimator.MIN, Estimator.MAX, Estimator.FIRST, Estimator.LAST, Estimator.AVERAGE);
    }

    @Override
    protected Object getMax(Interval interval) {
        Double max = getMaxDouble(interval);
        return max != null ? max.byteValue() : null;
    }

    @Override
    protected Object getMin(Interval interval) {
        Double min = getMinDouble(interval);
        return min != null ? min.byteValue() : null;
    }

    @Override
    protected Object getAverage(Interval interval) {
        Double average = getAverageDouble(interval);
        return average != null ? average : null;
    }

    @Override
    protected Byte getValue(int index) {
        return values[index];
    }

    @Override
    protected Object getValuesArray() {
        return values;
    }

    @Override
    protected void setValuesArray(Object array) {
        values = (byte[]) array;
    }
}
