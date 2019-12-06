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
import com.powsybl.sld.force.layout.gephi.graph.api.TimeFormat;
import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils;
import org.joda.time.DateTimeZone;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Abstract class that implement a sorted map between timestamp and attribute
 * values.
 * <p>
 * Implementations which extend this class customize the map for a unique type,
 * which is represented by the <code>T</code> parameter.
 *
 * @param <T> Value type
 */
public abstract class AbstractTimestampMap<T> implements TimeMap<Double, T> {

    protected double[] array;
    protected int size = 0;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public AbstractTimestampMap() {
        array = new double[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public AbstractTimestampMap(int capacity) {
        array = new double[capacity];
        Arrays.fill(array, Double.MAX_VALUE);
    }

    /**
     * Constructor with an initial timestamp map.
     * <p>
     * The given array must be sorted and contain no duplicates.
     *
     * @param keys initial set content
     */
    protected AbstractTimestampMap(double[] keys) {
        array = new double[keys.length];
        System.arraycopy(keys, 0, array, 0, keys.length);
        size = keys.length;
    }

    @Override
    public boolean put(Double timestamp, T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Object values = getValuesArray();
        int valuesLength = Array.getLength(values);

        final int index = putInner(timestamp);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size - 1 < valuesLength) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                Array.set(values, insertIndex, value);
            } else {
                Object newArray = Array.newInstance(values.getClass().getComponentType(), valuesLength + 1);
                System.arraycopy(values, 0, newArray, 0, insertIndex);
                System.arraycopy(values, insertIndex, newArray, insertIndex + 1, valuesLength - insertIndex);
                Array.set(newArray, insertIndex, value);
                setValuesArray(newArray);
            }
            return true;
        } else {
            Array.set(values, index, value);
        }
        return false;
    }

    @Override
    public boolean remove(Double timestamp) {
        Object values = getValuesArray();

        final int removeIndex = removeInner(timestamp);
        if (removeIndex >= 0) {
            if (removeIndex != size) {
                System.arraycopy(values, removeIndex + 1, values, removeIndex, size - removeIndex);
            }
            return true;
        }
        return false;
    }

    @Override
    public T get(Double timestamp, T defaultValue) {
        final int index = getIndex(timestamp);
        if (index >= 0) {
            return getValue(index);
        }
        return defaultValue;
    }

    @Override
    public Object get(Interval interval, Estimator estimator) {
        if (!isSupported(estimator)) {
            throw new UnsupportedOperationException("Not supported estimator.");
        }
        switch (estimator) {
            case AVERAGE:
                return getAverage(interval);
            case MIN:
                return getMin(interval);
            case MAX:
                return getMax(interval);
            case FIRST:
                return getFirst(interval);
            case LAST:
                return getLast(interval);
            default:
                throw new UnsupportedOperationException("Not supported estimator.");
        }
    }

    @Override
    public T[] toValuesArray() {
        Object values = getValuesArray();
        int length = Array.getLength(values);
        if (values.getClass().getComponentType().isPrimitive() || size < length) {
            T[] res = (T[]) Array.newInstance(getTypeClass(), size);
            for (int i = 0; i < size; i++) {
                res[i] = (T) Array.get(values, i);
            }
            return res;
        } else {
            return (T[]) values;
        }
    }

    protected Object toPrimitiveArray() {
        Object values = getValuesArray();
        int length = Array.getLength(values);
        if (size < length - 1) {
            Object res = Array.newInstance(values.getClass().getComponentType(), size);
            System.arraycopy(values, 0, res, 0, size);
            return res;
        }
        return values;
    }

    @Override
    public abstract Class<T> getTypeClass();

    @Override
    public abstract boolean isSupported(Estimator estimator);

    protected abstract T getValue(int index);

    protected abstract Object getValuesArray();

    protected abstract void setValuesArray(Object array);

    protected int putInner(double timestamp) {
        int index = Arrays.binarySearch(array, 0, size, timestamp);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < array.length) {
                if (insertIndex < size) {
                    System.arraycopy(array, insertIndex, array, insertIndex + 1, size - insertIndex);
                }
                array[insertIndex] = timestamp;
            } else {
                double[] newArray = new double[array.length + 1];
                System.arraycopy(array, 0, newArray, 0, insertIndex);
                System.arraycopy(array, insertIndex, newArray, insertIndex + 1, array.length - insertIndex);
                newArray[insertIndex] = timestamp;
                array = newArray;
            }

            size++;
        }
        return index;
    }

    protected int removeInner(double timestamp) {
        int index = Arrays.binarySearch(array, 0, size, timestamp);
        if (index >= 0) {
            int removeIndex = index;

            if (removeIndex == size - 1) {
                size--;
            } else {
                System.arraycopy(array, removeIndex + 1, array, removeIndex, size - removeIndex - 1);
                size--;
            }

            return removeIndex;
        }
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    protected int getIndex(double timestamp) {
        return Arrays.binarySearch(array, 0, size, timestamp);
    }

    protected int[] getOverlappingTimestamps(double intervalStart, double intervalEnd) {
        int index = Arrays.binarySearch(array, 0, size, intervalStart);
        index = index >= 0 ? index : (-index - 1);
        if (index < size) {
            int[] res = new int[size - index];
            int i = 0;
            for (; index < size && array[index] <= intervalEnd; index++) {
                res[i++] = index;
            }
            if (res.length != i) {
                return Arrays.copyOf(res, i);
            }
            return res;
        }
        return new int[0];
    }

    @Override
    public boolean contains(Double timestamp) {
        return getIndex(timestamp) >= 0;
    }

    /**
     * Returns an array of all timestamps in this map.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all timestamps
     */
    public double[] getTimestamps() {
        if (size < array.length) {
            double[] res = new double[size];
            System.arraycopy(array, 0, res, 0, size);
            return res;
        } else {
            return array;
        }
    }

    @Override
    public Double[] toKeysArray() {
        Double[] res = new Double[size];
        for (int i = 0; i < size; i++) {
            res[i] = array[i];
        }
        return res;
    }

    @Override
    public void clear() {
        size = 0;
        array = new double[0];
        setValuesArray(Array.newInstance(getValuesArray().getClass().getComponentType(), 0));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.size;
        for (int i = 0; i < size; i++) {
            double t = this.array[i];
            hash = 29 * hash + (int) (Double.doubleToLongBits(t) ^ (Double.doubleToLongBits(t) >>> 32));
            Object obj = this.getValue(i);
            hash = 29 * hash + obj.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractTimestampMap<?> other = (AbstractTimestampMap<?>) obj;
        if (this.size != other.size) {
            return false;
        }
        for (int i = 0; i < this.array.length && i < other.array.length; i++) {
            double i1 = this.array[i];
            double i2 = other.array[i];
            if (i1 != i2) {
                return false;
            }
            Object o1 = this.getValue(i);
            Object o2 = other.getValue(i);
            if ((o1 == null && o2 != null) || (o1 != null && o2 == null) || (o1 != null && o2 != null && !o1.equals(o2))) {
                return false;
            }
        }
        return true;
    }

    // Estimators
    protected Object getFirst(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] timestamps = getOverlappingTimestamps(interval.getLow(), interval.getHigh());
        if (timestamps.length == 0) {
            return null;
        }
        return getValue(timestamps[0]);
    }

    protected Object getLast(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] timestamps = getOverlappingTimestamps(interval.getLow(), interval.getHigh());
        if (timestamps.length == 0) {
            return null;
        }
        return getValue(timestamps[timestamps.length - 1]);
    }

    protected Object getMin(final Interval interval) {
        Double min = getMinDouble(interval);
        return min != null ? min.doubleValue() : null;
    }

    protected Double getMinDouble(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] timestamps = getOverlappingTimestamps(interval.getLow(), interval.getHigh());
        if (timestamps.length == 0) {
            return null;
        }
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < timestamps.length; i++) {
            double val = ((Number) getValue(timestamps[i])).doubleValue();
            min = Math.min(val, min);
        }
        return min;
    }

    protected Object getMax(final Interval interval) {
        Double max = getMaxDouble(interval);
        return max != null ? max.doubleValue() : null;
    }

    protected Double getMaxDouble(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] timestamps = getOverlappingTimestamps(interval.getLow(), interval.getHigh());
        if (timestamps.length == 0) {
            return null;
        }
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < timestamps.length; i++) {
            double val = ((Number) getValue(timestamps[i])).doubleValue();
            max = Math.max(val, max);
        }
        return max;
    }

    protected Object getAverage(final Interval interval) {
        BigDecimal average = getAverageBigDecimal(interval);
        return average != null ? average.doubleValue() : null;
    }

    protected BigDecimal getAverageBigDecimal(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] timestamps = getOverlappingTimestamps(interval.getLow(), interval.getHigh());
        if (timestamps.length == 0) {
            return null;
        } else if (timestamps.length == 1) {
            return new BigDecimal(((Number) getValue(timestamps[0])).doubleValue());
        }
        BigDecimal result = new BigDecimal(0.0);
        BigDecimal period = new BigDecimal(0.0);
        BigDecimal two = new BigDecimal(2.0);
        for (int i = 1; i < timestamps.length; i++) {
            BigDecimal p = new BigDecimal(array[timestamps[i]] - array[timestamps[i - 1]]);
            period = period.add(p);
            BigDecimal lowVal = new BigDecimal(((Number) getValue(timestamps[i - 1])).doubleValue());
            BigDecimal highVal = new BigDecimal(((Number) getValue(timestamps[i])).doubleValue());
            result = result.add(lowVal.add(highVal).divide(two).multiply(p));
        }
        return result.divide(period, 10, RoundingMode.HALF_EVEN);
    }

    protected Double getAverageDouble(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] timestamps = getOverlappingTimestamps(interval.getLow(), interval.getHigh());
        if (timestamps.length == 0) {
            return null;
        } else if (timestamps.length == 1) {
            return ((Number) getValue(timestamps[0])).doubleValue();
        }
        double result = 0.0;
        double period = 0.0;
        for (int i = 1; i < timestamps.length; i++) {
            double p = array[timestamps[i]] - array[timestamps[i - 1]];
            period += p;
            double lowVal = ((Number) getValue(timestamps[i - 1])).doubleValue();
            double highVal = ((Number) getValue(timestamps[i])).doubleValue();
            result += p * (lowVal + highVal) / 2.0;
        }
        return result / period;
    }

    public String toString(TimeFormat timeFormat, DateTimeZone timeZone) {
        if (size == 0) {
            return FormattingAndParsingUtils.EMPTY_VALUE;
        }

        T[] values = toValuesArray();

        StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < size; i++) {
            sb.append('[');
            sb.append(AttributeUtils.printTimestampInFormat(array[i], timeFormat, timeZone));

            sb.append(", ");
            String stringValue = values[i].toString();
            if (FormattingAndParsingUtils.containsDynamicSpecialCharacters(stringValue) || stringValue.trim().isEmpty()) {
                sb.append('"');
                sb.append(stringValue.replace("\\", "\\\\").replace("\"", "\\\""));
                sb.append('"');
            } else {
                sb.append(stringValue);
            }

            sb.append(']');

            if (i < size - 1) {
                sb.append("; ");
            }
        }
        sb.append('>');

        return sb.toString();
    }

    public String toString(TimeFormat timeFormat) {
        return toString(timeFormat, null);
    }

    @Override
    public String toString() {
        return toString(TimeFormat.DOUBLE, null);
    }
}
