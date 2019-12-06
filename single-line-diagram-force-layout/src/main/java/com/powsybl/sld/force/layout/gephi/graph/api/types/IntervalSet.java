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

import com.powsybl.sld.force.layout.gephi.graph.api.TimeFormat;
import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.impl.FormattingAndParsingUtils;
import org.joda.time.DateTimeZone;

import java.util.Arrays;

/**
 * Sorted set for intervals.
 */
public final class IntervalSet implements TimeSet<Interval> {

    private double[] array;
    private int size = 0;

    /**
     * Default constructor.
     * <p>
     * The set is empty with zero capacity.
     */
    public IntervalSet() {
        array = new double[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * intervals is known in advance as it minimizes array resizes.
     *
     * @param capacity interval capacity
     */
    public IntervalSet(int capacity) {
        array = new double[capacity * 2];
        Arrays.fill(array, Integer.MAX_VALUE);
    }

    /**
     * Constructor with an initial interval set.
     * <p>
     * The given array must be sorted and containing an even number of elements.
     *
     * @param arr initial set content
     */
    public IntervalSet(double[] arr) {
        array = new double[arr.length];
        System.arraycopy(arr, 0, array, 0, arr.length);
        size = arr.length / 2;
    }

    @Override
    public boolean add(Interval interval) {
        return addInner(interval.getLow(), interval.getHigh()) >= 0;
    }

    @Override
    public boolean remove(Interval interval) {
        return removeInner(interval.getLow(), interval.getHigh()) >= 0;
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
     * Returns true if this set contains an interval that starts or ends at
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

    @Override
    public boolean contains(Interval interval) {
        int realSize = size * 2;
        int index = Arrays.binarySearch(array, 0, realSize, interval.getLow());
        if (index >= 0) {
            int foundIndex = index % 2 == 0 ? index : index - 1;
            int shift;
            for (; foundIndex < realSize && foundIndex >= 0; foundIndex += shift) {
                double startValue = array[foundIndex];
                if (startValue > interval.getLow()) {
                    return false;
                }
                double endValue = array[foundIndex + 1];
                if (startValue == interval.getLow() && endValue > interval.getHigh()) {
                    return false;
                }
                if ((shift = interval.getHigh() > endValue ? 2 : interval.getHigh() < endValue ? -2 : interval
                        .getLow() > startValue ? 2 : 0) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns an array of all intervals in this set in a flat format.
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
    public Interval[] toArray() {
        Interval[] res = new Interval[size];
        for (int i = 0; i < size; i++) {
            res[i] = new Interval(array[i * 2], array[i * 2 + 1]);
        }
        return res;
    }

    @Override
    public Object toPrimitiveArray() {
        return toArray();
    }

    @Override
    public void clear() {
        size = 0;
        array = new double[0];
    }

    private int addInner(double intervalStart, double intervalEnd) {
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
            return insertIndex;
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
                    return -1;
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
            return insertIndex;
        }
    }

    private int removeInner(double intervalStart, double intervalEnd) {
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
                    return removeIndex;
                }

            }
        }
        return -1;
    }

    private void overlappingIntervallException() {
        throw new IllegalArgumentException("Overlapping intervals aren't allowed");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.size;
        int realSize = size * 2;
        for (int i = 0; i < realSize; i++) {
            double t = this.array[i];
            hash = 37 * hash + (int) (Double.doubleToLongBits(t) ^ (Double.doubleToLongBits(t) >>> 32));
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
        final IntervalSet other = (IntervalSet) obj;
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
        }
        return true;
    }

    @Override
    public String toString(TimeFormat timeFormat, DateTimeZone timeZone) {
        if (size == 0) {
            return FormattingAndParsingUtils.EMPTY_VALUE;
        }

        StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < size; i++) {
            sb.append('[');
            sb.append(AttributeUtils.printTimestampInFormat(array[i * 2], timeFormat, timeZone));
            sb.append(", ");
            sb.append(AttributeUtils.printTimestampInFormat(array[i * 2 + 1], timeFormat, timeZone));
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
