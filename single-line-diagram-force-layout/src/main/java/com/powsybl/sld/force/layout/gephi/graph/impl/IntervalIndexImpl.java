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

import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import com.powsybl.sld.force.layout.gephi.graph.api.ElementIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractIntervalMap;

import java.util.Map;

public class IntervalIndexImpl<T extends Element> extends AbstractTimeIndexImpl<T, Interval, IntervalSet, AbstractIntervalMap<?>> {

    public IntervalIndexImpl(AbstractTimeIndexStore<T, Interval, IntervalSet, AbstractIntervalMap<?>> store, boolean main) {
        super(store, main);
    }

    @Override
    public double getMinTimestamp() {
        if (mainIndex) {
            Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
            if (!sortedMap.isEmpty()) {
                return sortedMap.getLow();
            }
        } else {
            Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
            if (!sortedMap.isEmpty()) {
                for (Map.Entry<Interval, Integer> entry : sortedMap.entrySet()) {
                    int index = entry.getValue();
                    if (index < timestamps.length) {
                        TimeIndexEntry intervalEntry = timestamps[index];
                        if (intervalEntry != null) {
                            return entry.getKey().getLow();
                        }
                    }
                }
            }
        }
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getMaxTimestamp() {
        if (mainIndex) {
            Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
            if (!sortedMap.isEmpty()) {
                return sortedMap.getHigh();
            }
        } else {
            // TODO Better algorithm to find max
            Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
            if (!sortedMap.isEmpty()) {
                double max = Double.NEGATIVE_INFINITY;
                boolean found = false;
                for (Map.Entry<Interval, Integer> entry : sortedMap.entrySet()) {
                    int index = entry.getValue();
                    if (index < timestamps.length) {
                        TimeIndexEntry intervalEntry = timestamps[index];
                        if (intervalEntry != null) {
                            found = true;
                            max = Math.max(max, entry.getKey().getHigh());
                        }
                    }
                }
                if (found) {
                    return max;
                }
            }

        }
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public ElementIterable get(double timestamp) {
        checkDouble(timestamp);

        readLock();
        ObjectSet<Element> elements = new ObjectOpenHashSet<Element>();
        Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
        if (!sortedMap.isEmpty()) {
            for (Integer index : sortedMap.values(timestamp)) {
                if (index < timestamps.length) {
                    TimeIndexEntry ts = timestamps[index];
                    if (ts != null) {
                        elements.addAll(ts.elementSet);
                    }
                }
            }
        }
        if (!elements.isEmpty()) {
            return new ElementIterableImpl(new ElementIteratorImpl(elements.iterator()));
        }
        readUnlock();
        return ElementIterable.EMPTY;
    }

    @Override
    public ElementIterable get(Interval interval) {

        readLock();
        ObjectSet<Element> elements = new ObjectOpenHashSet<Element>();
        Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
        if (!sortedMap.isEmpty()) {
            for (Integer index : sortedMap.values(interval)) {
                if (index < timestamps.length) {
                    TimeIndexEntry ts = timestamps[index];
                    if (ts != null) {
                        elements.addAll(ts.elementSet);
                    }
                }
            }
        }
        if (!elements.isEmpty()) {
            return new ElementIterableImpl(new ElementIteratorImpl(elements.iterator()));
        }
        readUnlock();
        return ElementIterable.EMPTY;
    }
}
