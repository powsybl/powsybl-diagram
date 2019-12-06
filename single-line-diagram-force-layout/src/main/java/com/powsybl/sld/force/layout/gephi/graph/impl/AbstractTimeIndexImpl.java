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

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.ElementIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeIndex;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractTimeIndexImpl<T extends Element, K, S extends TimeSet<K>, M extends TimeMap<K, ?>> implements TimeIndex<T> {

    // Data
    protected final GraphLock lock;
    protected final AbstractTimeIndexStore<T, K, S, M> timestampIndexStore;
    protected final boolean mainIndex;
    protected TimeIndexEntry[] timestamps;
    protected int elementCount;

    protected AbstractTimeIndexImpl(AbstractTimeIndexStore<T, K, S, M> store, boolean main) {
        timestampIndexStore = store;
        mainIndex = main;
        timestamps = new TimeIndexEntry[0];
        lock = store.graphLock;
    }

    public boolean hasElements() {
        return elementCount > 0;
    }

    public void clear() {
        timestamps = new TimeIndexEntry[0];
        elementCount = 0;
    }

    protected void add(int timestampIndex, Element element) {
        ensureArraySize(timestampIndex);
        TimeIndexEntry entry = timestamps[timestampIndex];
        if (entry == null) {
            entry = addTimestamp(timestampIndex);
        }
        if (entry.add(element)) {
            elementCount++;
        }
    }

    protected void remove(int timestampIndex, Element element) {
        TimeIndexEntry entry = timestamps[timestampIndex];
        if (entry.remove(element)) {
            elementCount--;
            if (entry.isEmpty()) {
                clearEntry(timestampIndex);
            }
        }
    }

    protected TimeIndexEntry addTimestamp(final int index) {
        ensureArraySize(index);
        TimeIndexEntry entry = new TimeIndexEntry();
        timestamps[index] = entry;
        return entry;
    }

    private void ensureArraySize(int index) {
        if (index >= timestamps.length) {
            TimeIndexEntry[] newArray = new TimeIndexEntry[index + 1];
            System.arraycopy(timestamps, 0, newArray, 0, timestamps.length);
            timestamps = newArray;
        }
    }

    private void clearEntry(int index) {
        timestamps[index] = null;
    }

    protected void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    protected void readLock() {
        if (lock != null) {
            lock.readLock();
        }
    }

    protected void readUnlock() {
        if (lock != null) {
            lock.readUnlock();
        }
    }

    protected void writeLock() {
        if (lock != null) {
            lock.writeLock();
        }
    }

    protected void writeUnlock() {
        if (lock != null) {
            lock.writeUnlock();
        }
    }

    protected static class TimeIndexEntry {

        protected final ObjectSet<Element> elementSet;

        public TimeIndexEntry() {
            elementSet = new ObjectOpenHashSet<Element>();
        }

        public boolean add(Element element) {
            return elementSet.add(element);
        }

        public boolean remove(Element element) {
            return elementSet.remove(element);
        }

        public boolean isEmpty() {
            return elementSet.isEmpty();
        }
    }

    protected class ElementIteratorImpl implements Iterator<Element> {

        private final ObjectIterator<Element> itr;

        public ElementIteratorImpl(ObjectIterator<Element> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            final boolean hasNext = itr.hasNext();
            if (!hasNext) {
                readUnlock();
            }
            return hasNext;
        }

        @Override
        public Element next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    protected class ElementIterableImpl implements ElementIterable {

        protected final Iterator<Element> iterator;

        public ElementIterableImpl(Iterator<Element> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<Element> iterator() {
            return iterator;
        }

        @Override
        public Element[] toArray() {
            List<Element> list = new ArrayList<Element>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Element[0]);
        }

        @Override
        public Collection<Element> toCollection() {
            List<Element> list = new ArrayList<Element>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            readUnlock();
        }
    }
}
