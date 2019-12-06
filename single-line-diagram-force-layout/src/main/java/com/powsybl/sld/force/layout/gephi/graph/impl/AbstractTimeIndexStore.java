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
import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphView;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeIndex;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeSet;
import com.powsybl.sld.force.layout.gephi.graph.impl.utils.MapDeepEquals;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.powsybl.sld.force.layout.gephi.graph.api.DirectedSubgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractTimeIndexStore<T extends Element, K, S extends TimeSet<K>, M extends TimeMap<K, ?>> {

    // Lock
    protected final GraphLock graphLock;
    // Element
    protected final Class<T> elementType;
    // Timestamp index managament
    protected final Map<K, Integer> timeSortedMap;
    protected final IntSortedSet garbageQueue;
    protected int[] countMap;
    protected int length;
    // Index
    protected AbstractTimeIndexImpl mainIndex;
    protected final Map<GraphView, AbstractTimeIndexImpl> viewIndexes;

    protected AbstractTimeIndexStore(Class<T> type, GraphLock lock, boolean indexed, Map<K, Integer> sortedMap) {
        elementType = type;
        graphLock = lock;

        garbageQueue = new IntRBTreeSet();
        // Subclass
        timeSortedMap = sortedMap;
        countMap = new int[0];

        viewIndexes = indexed ? new Object2ObjectOpenHashMap<GraphView, AbstractTimeIndexImpl>() : null;
    }

    protected abstract void checkK(K k);

    protected abstract double getLow(K k);

    protected abstract AbstractTimeIndexImpl createIndex(boolean main);

    public Integer add(K k) {
        checkK(k);

        Integer id = timeSortedMap.get(k);
        if (id == null) {
            if (!garbageQueue.isEmpty()) {
                id = garbageQueue.firstInt();
                garbageQueue.remove(id);
            } else {
                id = length++;
            }
            timeSortedMap.put(k, id);
            ensureArraySize(id);
            countMap[id] = 1;
        } else {
            countMap[id]++;
        }

        return id;
    }

    public int add(K k, Element element) {
        int timeIndex = add(k);

        if (mainIndex != null) {
            mainIndex.add(timeIndex, element);

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, AbstractTimeIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    boolean node = element instanceof Node;
                    if (node ? graph.contains((Node) element) : graph.contains((Edge) element)) {
                        entry.getValue().add(timeIndex, element);
                    }
                }

            }
        }

        return timeIndex;
    }

    public void add(TimeMap<K, ?> timeMap) {
        for (K timeKey : timeMap.toKeysArray()) {
            add(timeKey);
        }
    }

    public void add(TimeSet<K> timeSet) {
        for (K timeKey : timeSet.toArray()) {
            add(timeKey);
        }
    }

    public Integer remove(K k) {
        checkK(k);

        Integer id = timeSortedMap.get(k);
        if (id != null) {
            if (--countMap[id] == 0) {
                garbageQueue.add(id);
                timeSortedMap.remove(k);
            }
        }
        return id;
    }

    public int remove(K k, Element element) {
        Integer timeIndex = remove(k);
        checkTimeIndex(timeIndex);

        if (mainIndex != null) {
            mainIndex.remove(timeIndex, element);

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, AbstractTimeIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    if (element instanceof Node) {
                        if (graph.contains((Node) element)) {
                            entry.getValue().remove(timeIndex, element);
                        }
                    } else if (graph.contains((Edge) element)) {
                        entry.getValue().remove(timeIndex, element);
                    }
                }
            }
        }

        return timeIndex;
    }

    public void remove(M timeMap) {
        for (K timeKey : timeMap.toKeysArray()) {
            remove(timeKey);
        }
    }

    public void remove(S timeSet) {
        for (K timeKey : timeSet.toArray()) {
            remove(timeKey);
        }
    }

    public boolean contains(K k) {
        checkK(k);

        return timeSortedMap.containsKey(k);
    }

    public void index(Element element) {
        S timeSet = getTimeSet(element);

        if (timeSet != null) {
            add(timeSet);
        }

        for (Object val : element.getAttributes()) {
            if (val != null && val instanceof TimeMap) {
                TimeMap dynamicValue = (TimeMap) val;
                add(dynamicValue);
            }
        }

        if (timeSet != null && mainIndex != null) {
            K[] ts = timeSet.toArray();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestampIndex = timeSortedMap.get(ts[i]);
                mainIndex.add(timestampIndex, element);
            }
        }
    }

    public void clear(Element element) {
        S timeSet = getTimeSet(element);

        if (timeSet != null && mainIndex != null) {
            K[] ts = timeSet.toArray();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestampIndex = timeSortedMap.get(ts[i]);
                mainIndex.remove(timestampIndex, element);
            }

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, AbstractTimeIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    boolean node = element instanceof Node;
                    if (node ? graph.contains((Node) element) : graph.contains((Edge) element)) {
                        for (int i = 0; i < tsLength; i++) {
                            int timestampIndex = timeSortedMap.get(ts[i]);
                            entry.getValue().remove(timestampIndex, element);
                        }
                    }
                }
            }
        }

        if (timeSet != null) {
            remove(timeSet);
        }

        for (Object val : element.getAttributes()) {
            if (val != null && val instanceof TimeMap) {
                TimeMap dynamicValue = (TimeMap) val;
                remove((M) dynamicValue);
            }
        }
    }

    public void clear() {
        timeSortedMap.clear();
        garbageQueue.clear();
        countMap = new int[0];
        length = 0;

        if (mainIndex != null) {
            mainIndex.clear();

            if (!viewIndexes.isEmpty()) {
                for (AbstractTimeIndexImpl index : viewIndexes.values()) {
                    index.clear();
                }
            }
        }
    }

    public int size() {
        return timeSortedMap.size();
    }

    public TimeIndex getIndex(Graph graph) {
        GraphView view = graph.getView();
        if (view.isMainView()) {
            return mainIndex;
        }
        AbstractTimeIndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex == null) {
            // TODO Make the auto-creation optional?
            viewIndex = createViewIndex(graph);
            viewIndexes.put(graph.getView(), viewIndex);
        }
        return viewIndex;
    }

    protected AbstractTimeIndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }

        AbstractTimeIndexImpl viewIndex = createIndex(false);
        // TODO: Check view doesn't exist already
        viewIndexes.put(graph.getView(), viewIndex);

        indexView(graph);

        return viewIndex;
    }

    public void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        AbstractTimeIndexImpl index = viewIndexes.remove(graph.getView());
        if (index != null) {
            index.clear();
        }
    }

    public void indexView(Graph graph) {
        AbstractTimeIndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex != null) {
            graph.readLock();
            try {
                Iterator<T> iterator = null;

                if (elementType.equals(Node.class)) {
                    iterator = (Iterator<T>) graph.getNodes().iterator();
                } else if (elementType.equals(Edge.class)) {
                    iterator = (Iterator<T>) graph.getEdges().iterator();
                }

                if (iterator != null) {
                    while (iterator.hasNext()) {
                        Element element = iterator.next();
                        S set = getTimeSet(element);
                        if (set != null) {
                            K[] ts = set.toArray();
                            int tsLength = ts.length;
                            for (int i = 0; i < tsLength; i++) {
                                int timestamp = timeSortedMap.get(ts[i]);
                                viewIndex.add(timestamp, element);
                            }
                        }
                    }
                }
            } finally {
                graph.readUnlock();
            }
        }
    }

    public void indexInView(T element, GraphView view) {
        AbstractTimeIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            S set = getTimeSet(element);
            if (set != null) {
                K[] ts = set.toArray();
                int tsLength = ts.length;
                for (int i = 0; i < tsLength; i++) {
                    int timestampIndex = timeSortedMap.get(ts[i]);
                    viewIndex.add(timestampIndex, element);
                }
            }
        }
    }

    public void clearInView(T element, GraphView view) {
        AbstractElementImpl elementImpl = (AbstractElementImpl) element;
        AbstractTimeIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            S set = getTimeSet(element);
            if (set != null) {
                K[] ts = set.toArray();
                int tsLength = ts.length;
                for (int i = 0; i < tsLength; i++) {
                    int timestampIndex = timeSortedMap.get(ts[i]);
                    viewIndex.remove(timestampIndex, elementImpl);
                }
            }
        }
    }

    public void clear(GraphView view) {
        AbstractTimeIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            viewIndex.clear();
        }
    }

    public boolean hasIndex() {
        return mainIndex != null;
    }

    private S getTimeSet(Element element) {
        Object[] attributes = element.getAttributes();
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET && GraphStoreConfiguration.ELEMENT_TIMESET_INDEX < attributes.length) {
            return (S) attributes[GraphStoreConfiguration.ELEMENT_TIMESET_INDEX];
        }
        return null;
    }

    private void checkTimeIndex(Integer timeIndex) {
        if (timeIndex == null) {
            throw new IllegalArgumentException("Unknown time index");
        }
    }

    protected void ensureArraySize(int index) {
        if (index >= countMap.length) {
            int newSize = Math
                    .min(Math.max(index + 1, (int) (index * GraphStoreConfiguration.TIMESTAMP_STORE_GROWING_FACTOR)), Integer.MAX_VALUE);
            int[] newArray = new int[newSize];
            System.arraycopy(countMap, 0, newArray, 0, countMap.length);
            countMap = newArray;
        }
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 29 * hash + elementType.hashCode();
        for (Entry<K, Integer> entry : timeSortedMap.entrySet()) {
            hash = 29 * hash + entry.getKey().hashCode();
            hash = 29 * hash + entry.getValue().hashCode();
            hash = 29 * hash + countMap[entry.getValue()];
        }
        return hash;
    }

    public boolean deepEquals(AbstractTimeIndexStore obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        AbstractTimeIndexStore other = (AbstractTimeIndexStore) obj;
        if (!other.elementType.equals(elementType)) {
            return false;
        }
        if (!MapDeepEquals.mapDeepEquals(timeSortedMap, other.timeSortedMap)) {
            return false;
        }
        int[] otherCountMap = other.countMap;
        for (Integer k : timeSortedMap.values()) {
            if (otherCountMap[k] != countMap[k]) {
                return false;
            }
        }
        return true;
    }
}
