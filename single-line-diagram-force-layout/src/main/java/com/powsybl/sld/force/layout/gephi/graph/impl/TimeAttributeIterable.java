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

import com.powsybl.sld.force.layout.gephi.graph.api.Interval;

import java.util.Iterator;
import java.util.Map;

public class TimeAttributeIterable implements Iterable<Map.Entry> {

    private static Iterator<Map.Entry> EMPTY_ITERATOR = new EmptyIterator();
    protected static Iterable<Map.Entry> EMPTY_ITERABLE = new Iterable<Map.Entry>() {

        @Override
        public Iterator<Map.Entry> iterator() {
            return EMPTY_ITERATOR;
        }
    };

    private final Interval[] intervals;
    private final double[] timestamps;
    private final Object[] values;

    public TimeAttributeIterable(double[] timestamps, Object[] values) {
        this.timestamps = timestamps;
        this.values = values;
        this.intervals = null;
    }

    public TimeAttributeIterable(Interval[] intervals, Object[] values) {
        this.intervals = intervals;
        this.values = values;
        this.timestamps = null;
    }

    @Override
    public Iterator<Map.Entry> iterator() {
        if (intervals != null) {
            return new IntervalAttributeIterator(intervals, values);
        } else {
            return new TimestampAttributeIterator(timestamps, values);
        }
    }

    private static class TimestampAttributeIterator implements Iterator<Map.Entry> {

        private final TimestampEntry timestampEntry = new TimestampEntry();
        private final double[] timestamps;
        private final Object[] values;
        private int index;

        public TimestampAttributeIterator(double[] timestamps, Object[] values) {
            this.timestamps = timestamps;
            this.values = values;
        }

        @Override
        public boolean hasNext() {
            return index < timestamps.length;
        }

        @Override
        public Map.Entry<Double, Object> next() {
            timestampEntry.timestamp = timestamps[index];
            timestampEntry.value = values[index++];
            return timestampEntry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    private static class IntervalAttributeIterator implements Iterator<Map.Entry> {

        private final IntervalEntry intervalEntry = new IntervalEntry();
        private final Interval[] intervals;
        private final Object[] values;
        private int index;

        public IntervalAttributeIterator(Interval[] intervals, Object[] values) {
            this.intervals = intervals;
            this.values = values;
        }

        @Override
        public boolean hasNext() {
            return index < intervals.length;
        }

        @Override
        public Map.Entry<Interval, Object> next() {
            intervalEntry.interval = intervals[index];
            intervalEntry.value = values[index++];
            return intervalEntry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    private static class TimestampEntry implements Map.Entry<Double, Object> {

        private double timestamp;
        private Object value;

        @Override
        public Double getKey() {
            return timestamp;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Not supported");
        }

    }

    private static class IntervalEntry implements Map.Entry<Interval, Object> {

        private Interval interval;
        private Object value;

        @Override
        public Interval getKey() {
            return interval;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    protected static class EmptyIterator implements Iterator<Map.Entry> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Map.Entry next() {
            throw new UnsupportedOperationException("Not supposed to call this for empty iterator.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supposed to call this for empty iterator.");
        }
    }
}
