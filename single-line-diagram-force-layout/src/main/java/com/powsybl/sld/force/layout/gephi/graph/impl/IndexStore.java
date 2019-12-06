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

import com.powsybl.sld.force.layout.gephi.graph.api.Column;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphView;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.powsybl.sld.force.layout.gephi.graph.api.DirectedSubgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class IndexStore<T extends Element> {

    protected final ColumnStore<T> columnStore;
    protected final TableLock lock;
    protected final IndexImpl<T> mainIndex;
    protected final Map<GraphView, IndexImpl<T>> viewIndexes;

    public IndexStore(ColumnStore<T> columnStore) {
        this.columnStore = columnStore;
        this.mainIndex = new IndexImpl<T>(columnStore);
        this.viewIndexes = new Object2ObjectOpenHashMap<GraphView, IndexImpl<T>>();
        this.lock = columnStore.lock;
    }

    protected void addColumn(ColumnImpl col) {
        mainIndex.addColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.addColumn(col);
        }
    }

    protected void removeColumn(ColumnImpl col) {
        mainIndex.removeColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.removeColumn(col);
        }
    }

    protected boolean hasColumn(ColumnImpl col) {
        return mainIndex.hasColumn(col);
    }

    protected IndexImpl getIndex(Graph graph) {
        GraphView view = graph.getView();
        if (view.isMainView()) {
            return mainIndex;
        }
        lock();
        try {
            IndexImpl<T> viewIndex = viewIndexes.get(graph.getView());
            if (viewIndex == null) {
                viewIndex = createViewIndex(graph);
            }
            return viewIndex;
        } finally {
            unlock();
        }
    }

    protected IndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }
        IndexImpl viewIndex = new IndexImpl<T>(columnStore);
        ColumnImpl[] columns = columnStore.toArray();
        viewIndex.addAllColumns(columns);
        viewIndexes.put(graph.getView(), viewIndex);

        indexView(graph);

        return viewIndex;
    }

    protected void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        lock();
        try {
            IndexImpl<T> index = viewIndexes.remove(graph.getView());
            if (index != null) {
                index.destroy();
            }
        } finally {
            unlock();
        }
    }

    public Object set(Column column, Object oldValue, Object valueParam, T element) {
        lock();
        Object value = valueParam;
        try {
            value = mainIndex.set(column, oldValue, value, element);

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, IndexImpl<T>> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    boolean inView = element instanceof Node ? graph.contains((Node) element) : graph
                            .contains((Edge) element);
                    if (inView) {
                        entry.getValue().set(column, oldValue, value, element);
                    }
                }
            }

            return value;
        } finally {
            unlock();
        }
    }

    public void clear(T element) {
        AbstractElementImpl elementImpl = (AbstractElementImpl) element;

        lock();
        try {
            final int length = columnStore.length;
            final ColumnImpl[] cols = columnStore.columns;
            for (int i = 0; i < length; i++) {
                Column c = cols[i];
                if (c != null && c.isIndexed() && elementImpl.attributes.length > c.getIndex()) {
                    Object value = elementImpl.attributes[c.getIndex()];
                    mainIndex.remove(c, value, element);
                    for (Entry<GraphView, IndexImpl<T>> entry : viewIndexes.entrySet()) {
                        GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                        DirectedSubgraph graph = graphView.getDirectedGraph();
                        boolean inView = element instanceof Node ? graph.contains((Node) element) : graph
                                .contains((Edge) element);
                        if (inView) {
                            entry.getValue().remove(c, value, element);
                        }
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void index(T element) {
        AbstractElementImpl elementImpl = (AbstractElementImpl) element;
        lock();
        try {
            ensureAttributeArrayLength(elementImpl, columnStore.length);

            final int length = columnStore.length;
            final ColumnImpl[] cols = columnStore.columns;
            for (int i = 0; i < length; i++) {
                Column c = cols[i];
                if (c != null && c.isIndexed()) {
                    Object value = elementImpl.attributes[c.getIndex()];
                    value = mainIndex.put(c, value, element);
                    elementImpl.attributes[c.getIndex()] = value;
                }
            }
        } finally {
            unlock();
        }
    }

    public void indexView(Graph graph) {
        IndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex != null) {
            graph.readLock();
            try {
                Iterator<T> iterator = null;
                if (columnStore.elementType.equals(Node.class)) {
                    iterator = (Iterator<T>) graph.getNodes().iterator();
                } else if (columnStore.elementType.equals(Edge.class)) {
                    iterator = (Iterator<T>) graph.getEdges().iterator();
                }

                if (iterator != null) {
                    while (iterator.hasNext()) {
                        AbstractElementImpl element = (AbstractElementImpl) iterator.next();
                        ensureAttributeArrayLength(element, columnStore.length);

                        final ColumnImpl[] cols = columnStore.columns;
                        synchronized (element) {
                            int length = columnStore.length;
                            for (int i = 0; i < length; i++) {
                                Column c = cols[i];
                                if (c != null && c.isIndexed()) {
                                    Object value = element.attributes[c.getIndex()];
                                    viewIndex.put(c, value, element);
                                }
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
        AbstractElementImpl elementImpl = (AbstractElementImpl) element;
        lock();
        try {
            IndexImpl<T> index = viewIndexes.get(view);
            if (index != null) {
                final int length = columnStore.length;
                final ColumnImpl[] cols = columnStore.columns;
                for (int i = 0; i < length; i++) {
                    Column c = cols[i];
                    if (c != null && c.isIndexed()) {
                        Object value = elementImpl.attributes[c.getIndex()];
                        index.put(c, value, element);
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void clearInView(T element, GraphView view) {
        AbstractElementImpl elementImpl = (AbstractElementImpl) element;
        lock();
        try {
            IndexImpl<T> index = viewIndexes.get(view);
            if (index != null) {
                final int length = columnStore.length;
                final ColumnImpl[] cols = columnStore.columns;
                for (int i = 0; i < length; i++) {
                    Column c = cols[i];
                    if (c != null && c.isIndexed()) {
                        Object value = elementImpl.attributes[c.getIndex()];
                        index.remove(c, value, element);
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void clear(GraphView view) {
        lock();
        try {
            IndexImpl<T> index = viewIndexes.get(view);
            if (index != null) {
                index.clear();
            }
        } finally {
            unlock();
        }
    }

    public void clear() {
        lock();
        try {
            mainIndex.clear();
            for (IndexImpl index : viewIndexes.values()) {
                index.clear();
            }
        } finally {
            unlock();
        }
    }

    private void ensureAttributeArrayLength(AbstractElementImpl element, int size) {
        synchronized (element) {
            final Object[] attributes = element.attributes;
            if (size > attributes.length) {
                Object[] newArray = new Object[size];
                System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                element.attributes = newArray;
            }
        }

    }

    private void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    private void unlock() {
        if (lock != null) {
            lock.unlock();
        }
    }
}
