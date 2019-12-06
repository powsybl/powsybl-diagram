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

import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeFormat;
import com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import org.joda.time.DateTimeZone;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Abstract class that implement a sorted map between intervals and attribute
 * values.
 * <p>
 * Implementations which extend this class customize the map for a unique type,
 * which is represented by the <code>T</code> parameter.
 *
 * @param <T> Value type
 */
public abstract class AbstractIntervalMap<T> implements TimeMap<Interval, T> {

    protected double[] array;
    protected int size = 0;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public AbstractIntervalMap() {
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
    public AbstractIntervalMap(int capacity) {
        array = new double[capacity];
        Arrays.fill(array, Double.MAX_VALUE);
    }

    /**
     * Constructor with an initial interval map.
     * <p>
     * The given array must be sorted and contain no duplicates.
     *
     * @param keys initial set content
     */
    protected AbstractIntervalMap(double[] keys) {
        array = new double[keys.length];
        System.arraycopy(keys, 0, array, 0, keys.length);
        size = keys.length / 2;
    }

    @Override
    public boolean put(Interval interval, T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Object values = getValuesArray();
        int valuesLength = Array.getLength(values);

        final int index = putInner(interval.getLow(), interval.getHigh());
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
    public boolean remove(Interval interval) {
        Object values = getValuesArray();

        final int removeIndex = removeInner(interval.getLow(), interval.getHigh());
        if (removeIndex >= 0) {
            if (removeIndex != size) {
                System.arraycopy(values, removeIndex + 1, values, removeIndex, size - removeIndex);
            }
            return true;
        }
        return false;
    }

    @Override
    public Object get(Interval interval, Estimator estimator) {
        if (estimator == null) {
            return get(interval, (T) null);
        }

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
    public T get(Interval interval, T defaultValue) {
        final int index = getIndex(interval.getLow(), interval.getHigh());
        if (index >= 0) {
            return getValue(index / 2);
        }
        return defaultValue;
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

    protected Object toNativeArray() {
        Object values = getValuesArray();
        int length = Array.getLength(values);
        if (size < length - 1) {
            Object res = Array.newInstance(values.getClass().getComponentType(), size);
            System.arraycopy(values, 0, res, 0, size);
            return res;
        }
        return values;
    }

    protected abstract T getValue(int index);

    protected abstract Object getValuesArray();

    protected abstract void setValuesArray(Object array);

    protected int putInner(double intervalStart, double intervalEnd) {
        int realSize = size * 2;
        int index = Arrays.binarySearch(array, 0, realSize, intervalStart);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (insertIndex % 2 == 1) {
                overlappingIntervallException();
            }

            if (insertIndex < realSize) {
                double currentValue = array[insertIndex];
                if (intervalEnd > currentValue) {
                    overlappingIntervallException();
                }
            }

            if (realSize < array.length - 2) {
                if (insertIndex < realSize) {
                    System.arraycopy(array, insertIndex, array, insertIndex + 2, realSize - insertIndex);
                }
                array[insertIndex] = intervalStart;
                array[insertIndex + 1] = intervalEnd;
            } else {
                double[] newArray = new double[array.length + 2];
                System.arraycopy(array, 0, newArray, 0, insertIndex);
                System.arraycopy(array, insertIndex, newArray, insertIndex + 2, array.length - insertIndex);
                newArray[insertIndex] = intervalStart;
                newArray[insertIndex + 1] = intervalEnd;
                array = newArray;
            }

            size++;
            return -(insertIndex / 2) - 1;
        } else {
            int insertIndex = index % 2 == 0 ? index : index - 1;
            int shift;
            for (; insertIndex < realSize && insertIndex >= 0; insertIndex += shift) {
                double startValue = array[insertIndex];
                double endValue = array[insertIndex + 1];
                if (startValue > intervalStart) {
                    if (intervalEnd >= endValue) {
                        overlappingIntervallException();
                    }
                    break;
                }
                if (startValue == intervalStart && endValue > intervalEnd) {
                    if (intervalEnd <= startValue) {
                        break;
                    } else {
                        overlappingIntervallException();
                    }
                }
                shift = intervalEnd > endValue ? 2 : intervalEnd < endValue ? -2 : intervalStart > startValue ? 2 : 0;
                if (shift == 0) {
                    return insertIndex / 2;
                }
                if (startValue == intervalStart && endValue < intervalEnd && startValue != endValue) {
                    overlappingIntervallException();
                }
            }

            if (realSize < array.length - 2) {
                if (insertIndex < realSize) {
                    System.arraycopy(array, insertIndex, array, insertIndex + 2, realSize - insertIndex);
                }
                array[insertIndex] = intervalStart;
                array[insertIndex + 1] = intervalEnd;
            } else {
                double[] newArray = new double[array.length + 2];
                System.arraycopy(array, 0, newArray, 0, insertIndex);
                System.arraycopy(array, insertIndex, newArray, insertIndex + 2, array.length - insertIndex);
                newArray[insertIndex] = intervalStart;
                newArray[insertIndex + 1] = intervalEnd;
                array = newArray;
            }

            size++;
            return -(insertIndex / 2) - 1;
        }
    }

    protected int removeInner(double intervalStart, double intervalEnd) {
        int realSize = size * 2;
        int index = Arrays.binarySearch(array, 0, realSize, intervalStart);
        if (index >= 0) {
            int removeIndex = index % 2 == 0 ? index : index - 1;
            int shift;
            for (; removeIndex < realSize && removeIndex >= 0; removeIndex += shift) {
                double startValue = array[removeIndex];
                if (startValue > intervalStart) {
                    return -1;
                }
                double endValue = array[removeIndex + 1];
                if (startValue == intervalStart && endValue > intervalEnd) {
                    return -1;
                }
                if ((shift = intervalEnd > endValue ? 2 : intervalEnd < endValue ? -2 : intervalStart > startValue ? 2
                        : 0) == 0) {
                    if (removeIndex == realSize - 2) {
                        size--;
                    } else {
                        System.arraycopy(array, removeIndex + 2, array, removeIndex, realSize - removeIndex - 2);
                        size--;
                    }
                    return removeIndex / 2;
                }

            }
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

    /**
     * Returns true if this map contains an interval that starts or ends at
     * <code>timestamp</code>.
     *
     * @param timestamp timestamp
     * @return true if contains, false otherwise
     */
    public boolean contains(double timestamp) {
        int realSize = size * 2;
        int index = Arrays.binarySearch(array, 0, realSize, timestamp);
        return index >= 0 && index < realSize;
    }

    protected int getIndex(double intervalStart, double intervalEnd) {
        int realSize = size * 2;
        int index = Arrays.binarySearch(array, 0, realSize, intervalStart);
        if (index >= 0) {
            int foundIndex = index % 2 == 0 ? index : index - 1;
            int shift;
            for (; foundIndex < realSize && foundIndex >= 0; foundIndex += shift) {
                double startValue = array[foundIndex];
                if (startValue > intervalStart) {
                    return -1;
                }
                double endValue = array[foundIndex + 1];
                if (startValue == intervalStart && endValue > intervalEnd) {
                    return -1;
                }
                if ((shift = intervalEnd > endValue ? 2 : intervalEnd < endValue ? -2 : intervalStart > startValue ? 2
                        : 0) == 0) {
                    return foundIndex;
                }
            }
        }
        return -1;
    }

    protected int[] getOverlappingIntervals(double intervalStart, double intervalEnd) {
        int realSize = size * 2;
        int index = Arrays.binarySearch(array, 0, realSize, intervalStart);
        if (index >= 0) {
            int startIndex = index % 2 == 0 ? index : index - 1;
            for (; startIndex - 2 >= 0;) {
                if (array[startIndex - 2] == intervalStart) {
                    startIndex -= 2;
                } else {
                    break;
                }
            }

            int[] res = new int[size - (startIndex / 2)];
            int i = 0;
            for (; startIndex < realSize && array[startIndex] <= intervalEnd; startIndex += 2) {
                res[i++] = startIndex / 2;
            }
            if (res.length != i) {
                return Arrays.copyOf(res, i);
            }
            return res;
        } else {
            int startIndex = (-index - 1) % 2 == 0 ? (-index - 1) : -index - 2;
            if (startIndex < realSize && array[startIndex] <= intervalEnd) {
                int[] res = new int[size - (startIndex / 2)];
                int i = 0;
                for (; startIndex < realSize && array[startIndex] <= intervalEnd; startIndex += 2) {
                    res[i++] = startIndex / 2;
                }
                if (i != 0) {
                    if (res.length != i) {
                        return Arrays.copyOf(res, i);
                    }
                    return res;
                }
            }
        }
        return new int[0];
    }

    protected double[] getIntervalsWeight(double intervalStart, double intervalEnd, int[] intervals) {
        double[] res = new double[intervals.length];
        for (int i = 0; i < intervals.length; i++) {
            double start = array[i * 2];
            double end = array[i * 2 + 1];
            if (start != end) {
                start = Math.max(intervalStart, start);
                end = Math.min(intervalEnd, end);
                res[i] = end - start;
            }
        }
        return res;
    }

    @Override
    public boolean contains(Interval interval) {
        return getIndex(interval.getLow(), interval.getHigh()) >= 0;
    }

    @Override
    public Interval[] toKeysArray() {
        Interval[] res = new Interval[size];
        for (int i = 0; i < size; i++) {
            res[i] = new Interval(array[i * 2], array[i * 2 + 1]);
        }
        return res;
    }

    /**
     * Returns an array of all intervals in this set.
     * <p>
     * The intervals are represented in a flat and sorted array (e.g.
     * {[1.0,2.0], [5.0,6.0]}) returns [1.0,2.0,5.0,6.0]).
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all intervals
     */
    public double[] getIntervals() {
        int realSize = size * 2;
        if (realSize < array.length) {
            double[] res = new double[realSize];
            System.arraycopy(array, 0, res, 0, realSize);
            return res;
        } else {
            return array;
        }
    }

    @Override
    public void clear() {
        size = 0;
        array = new double[0];
    }

    private void overlappingIntervallException() {
        throw new IllegalArgumentException("Overlapping intervals aren't allowed");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.size;
        int realSize = size * 2;
        for (int i = 0; i < realSize; i++) {
            double t = this.array[i];
            hash = 29 * hash + (int) (Double.doubleToLongBits(t) ^ (Double.doubleToLongBits(t) >>> 32));
            if (i % 2 == 0) {
                Object obj = this.getValue(i / 2);
                hash = 29 * hash + obj.hashCode();
            }
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
        final AbstractIntervalMap<?> other = (AbstractIntervalMap<?>) obj;
        if (this.size != other.size) {
            return false;
        }
        int realSize = size * 2;
        for (int i = 0; i < realSize; i++) {
            double i1 = this.array[i];
            double i2 = other.array[i];
            if (i1 != i2) {
                return false;
            }
            if (i % 2 == 0) {
                Object o1 = this.getValue(i / 2);
                Object o2 = other.getValue(i / 2);
                if ((o1 == null && o2 != null) || (o1 != null && o2 == null) || (o1 != null && o2 != null && !o1
                        .equals(o2))) {
                    return false;
                }
            }
        }
        return true;
    }

    // Estimators
    protected Object getFirst(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        return getValue(intervals[0]);
    }

    protected Object getLast(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        return getValue(intervals[intervals.length - 1]);
    }

    protected Object getMin(final Interval interval) {
        Double min = getMinDouble(interval);
        return min != null ? min.doubleValue() : null;
    }

    protected Double getMinDouble(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < intervals.length; i++) {
            double val = ((Number) getValue(intervals[i])).doubleValue();
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
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < intervals.length; i++) {
            double val = ((Number) getValue(intervals[i])).doubleValue();
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
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        double[] weights = getIntervalsWeight(interval.getLow(), interval.getHigh(), intervals);
        BigDecimal result = new BigDecimal(0.0);
        BigDecimal period = new BigDecimal(0.0);
        for (int i = 0; i < intervals.length; i++) {
            BigDecimal w = new BigDecimal(weights[i]);
            period = period.add(w);
            w = w.multiply(new BigDecimal(((Number) getValue(intervals[i])).doubleValue()));
            result = result.add(w);
        }
        return result.divide(period, 10, RoundingMode.HALF_EVEN);
    }

    protected Double getAverageDouble(final Interval interval) {
        if (size == 0) {
            return null;
        }
        int[] intervals = getOverlappingIntervals(interval.getLow(), interval.getHigh());
        if (intervals.length == 0) {
            return null;
        }
        double[] weights = getIntervalsWeight(interval.getLow(), interval.getHigh(), intervals);
        double result = 0.0;
        double period = 0.0;
        for (int i = 0; i < intervals.length; i++) {
            double w = weights[i];
            period += w;
            w *= ((Number) getValue(intervals[i])).doubleValue();
            result += w;
        }
        return result / period;
    }

    @Override
    public String toString(TimeFormat timeFormat, DateTimeZone timeZone) {
        if (size == 0) {
            return FormattingAndParsingUtils.EMPTY_VALUE;
        }

        T[] values = toValuesArray();

        StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < size; i++) {
            sb.append('[');
            sb.append(AttributeUtils.printTimestampInFormat(array[i * 2], timeFormat, timeZone));
            sb.append(", ");
            sb.append(AttributeUtils.printTimestampInFormat(array[i * 2 + 1], timeFormat, timeZone));

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

    @Override
    public String toString(TimeFormat timeFormat) {
        return toString(timeFormat, null);
    }

    @Override
    public String toString() {
        return toString(TimeFormat.DOUBLE, null);
    }
}
