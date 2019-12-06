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

import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Column;
import com.powsybl.sld.force.layout.gephi.graph.api.ColumnIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.Configuration;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;
import it.unimi.dsi.fastutil.shorts.ShortSortedSet;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ColumnStore<T extends Element> implements ColumnIterable {

    // Config
    protected static final int MAX_SIZE = 65534;
    // Const
    protected static final int NULL_ID = -1;
    protected static final short NULL_SHORT = Short.MIN_VALUE;
    // Configuration
    protected final GraphStore graphStore;
    protected final Configuration configuration;
    // Element
    protected final Class<T> elementType;
    // Columns
    protected final Object2ShortMap<String> idMap;
    protected final ColumnImpl[] columns;
    protected final ShortSortedSet garbageQueue;
    // Index
    protected final IndexStore<T> indexStore;
    // Version
    protected final List<TableObserverImpl> observers;
    // Locking (optional)
    protected final TableLock lock;
    // Variables
    protected int length;

    public ColumnStore(Class<T> elementType, boolean indexed) {
        this(null, elementType, indexed);
    }

    public ColumnStore(GraphStore graphStore, Class<T> elementType, boolean indexed) {
        if (MAX_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE + 1) {
            throw new RuntimeException("Column Store size can't exceed 65534");
        }
        this.graphStore = graphStore;
        this.configuration = graphStore != null ? graphStore.configuration : new Configuration();
        this.lock = GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? new TableLock() : null;
        this.garbageQueue = new ShortRBTreeSet();
        this.idMap = new Object2ShortOpenHashMap<String>(MAX_SIZE);
        this.columns = new ColumnImpl[MAX_SIZE];
        this.elementType = elementType;
        this.indexStore = indexed ? new IndexStore<T>(this) : null;
        idMap.defaultReturnValue(NULL_SHORT);
        this.observers = GraphStoreConfiguration.ENABLE_OBSERVERS ? new ArrayList<TableObserverImpl>() : null;
    }

    private void updateConfiguration(Column changedColumn) {
        String columnId = changedColumn.getId();
        if (Edge.class.equals(elementType)) {
            if (columnId.equals(GraphStoreConfiguration.EDGE_WEIGHT_COLUMN_ID)) {
                if (hasColumn(columnId)) {
                    Class edgeWeightColumnClass = getColumn(columnId).getTypeClass();
                    configuration.setEdgeWeightType(edgeWeightColumnClass);
                    configuration.setEdgeWeightColumn(true);
                } else {
                    configuration.setEdgeWeightColumn(false);
                }
            } else if (columnId.equals(GraphStoreConfiguration.ELEMENT_ID_COLUMN_ID)) {
                configuration.setEdgeIdType(changedColumn.getTypeClass());
            }
        } else if (Node.class.equals(elementType)) {
            if (columnId.equals(GraphStoreConfiguration.ELEMENT_ID_COLUMN_ID)) {
                configuration.setNodeIdType(changedColumn.getTypeClass());
            }
        }
    }

    public void addColumn(final Column column) {
        checkNonNullColumnObject(column);
        checkIndexStatus(column);

        lock();
        try {
            final ColumnImpl columnImpl = (ColumnImpl) column;
            short id = idMap.getShort(columnImpl.getId());
            if (id == NULL_SHORT) {
                if (!garbageQueue.isEmpty()) {
                    id = garbageQueue.firstShort();
                    garbageQueue.remove(id);
                } else {
                    id = intToShort(length);
                    if (length >= MAX_SIZE) {
                        throw new RuntimeException("Maximum number of columns reached at " + MAX_SIZE);
                    }
                    length++;
                }
                idMap.put(column.getId(), id);
                int intIndex = shortToInt(id);
                columnImpl.setStoreId(intIndex);
                columns[intIndex] = columnImpl;
                if (indexStore != null) {
                    indexStore.addColumn(columnImpl);
                }
                updateConfiguration(column);
            } else {
                throw new IllegalArgumentException("The column already exist");
            }
        } finally {
            unlock();
        }
    }

    public void removeColumn(final Column column) {
        checkNonNullColumnObject(column);

        lock();
        graphWriteLock();
        try {
            final ColumnImpl columnImpl = (ColumnImpl) column;

            // Clean attributes
            if (graphStore != null && columnImpl.table != null) {
                if (AttributeUtils.isNodeColumn(columnImpl)) {
                    for (Node n : graphStore.nodeStore) {
                        Object[] attributes = ((NodeImpl) n).attributes;
                        if (attributes.length > columnImpl.getIndex()) {
                            attributes[columnImpl.getIndex()] = null;
                        }
                    }
                } else {
                    for (Edge e : graphStore.edgeStore) {
                        Object[] attributes = ((EdgeImpl) e).attributes;
                        if (attributes.length > columnImpl.getIndex()) {
                            attributes[columnImpl.getIndex()] = null;
                        }
                    }
                }
            }

            short id = idMap.removeShort(column.getId());
            if (id == NULL_SHORT) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            garbageQueue.add(id);

            int intId = shortToInt(id);
            columns[intId] = null;
            if (indexStore != null) {
                indexStore.removeColumn((ColumnImpl) column);
            }
            columnImpl.setStoreId(NULL_ID);
            updateConfiguration(column);
        } finally {
            graphWriteUnlock();
            unlock();
        }
    }

    public void removeColumn(final String key) {
        checkNonNullObject(key);
        lock();
        try {
            removeColumn(getColumn(key));
        } finally {
            unlock();
        }
    }

    public int getColumnIndex(final String key) {
        checkNonNullObject(key);
        lock();
        try {
            short id = idMap.getShort(key.toLowerCase());
            if (id == NULL_SHORT) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            return shortToInt(id);
        } finally {
            unlock();
        }
    }

    public Column getColumnByIndex(final int index) {
        lock();
        try {
            if (index < 0 || index >= columns.length) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            ColumnImpl a = columns[index];
            if (a == null) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            return a;
        } finally {
            unlock();
        }
    }

    public Column getColumn(final String key) {
        checkNonNullObject(key);
        lock();
        try {
            short id = idMap.getShort(key.toLowerCase());
            if (id == NULL_SHORT) {
                return null;
            }
            return columns[shortToInt(id)];
        } finally {
            unlock();
        }
    }

    public boolean hasColumn(String key) {
        checkNonNullObject(key);
        lock();
        try {
            return idMap.containsKey(key.toLowerCase());
        } finally {
            unlock();
        }
    }

    @Override
    public Iterator<Column> iterator() {
        return new ColumnStoreIterator();
    }

    @Override
    public ColumnImpl[] toArray() {
        lock();
        try {
            ColumnImpl[] cols = new ColumnImpl[size()];
            int j = 0;
            for (int i = 0; i < length; i++) {
                ColumnImpl c = columns[i];
                if (c != null) {
                    cols[j++] = c;
                }
            }
            return cols;
        } finally {
            unlock();
        }
    }

    @Override
    public List<Column> toList() {
        lock();
        try {
            List<Column> cols = new ArrayList<Column>(size());
            for (int i = 0; i < length; i++) {
                ColumnImpl c = columns[i];
                if (c != null) {
                    cols.add(c);
                }
            }
            return cols;
        } finally {
            unlock();
        }
    }

    @Override
    public void doBreak() {
        unlock();
    }

    public Set<String> getColumnKeys() {
        lock();
        try {
            return new ObjectOpenHashSet<String>(idMap.keySet());
        } finally {
            unlock();
        }
    }

    public void clear() {
        lock();
        try {
            // Clean attributes
            if (graphStore != null) {
                List<Column> cols = toList();
                int[] indices = new int[cols.size()];
                for (int i = 0; i < indices.length; i++) {
                    indices[i] = cols.get(i).getIndex();
                }
                if (graphStore.nodeTable.store == this) {
                    for (Node n : graphStore.nodeStore) {
                        Object[] atts = ((NodeImpl) n).attributes;
                        for (int i = 0; i < indices.length; i++) {
                            atts[indices[i]] = null;
                        }
                    }
                } else {
                    for (Edge e : graphStore.edgeStore) {
                        Object[] atts = ((EdgeImpl) e).attributes;
                        for (int i = 0; i < indices.length; i++) {
                            atts[indices[i]] = null;
                        }
                    }
                }
            }

            garbageQueue.clear();
            idMap.clear();
            length = 0;
            Arrays.fill(columns, null);
            if (indexStore != null) {
                indexStore.clear();
            }
        } finally {
            unlock();
        }
    }

    public int size() {
        return length - garbageQueue.size();
    }

    protected TableObserverImpl createTableObserver(TableImpl table, boolean withDiff) {
        if (observers != null) {
            lock();
            try {
                TableObserverImpl observer = new TableObserverImpl(table, withDiff);
                observers.add(observer);

                return observer;
            } finally {
                unlock();
            }
        }
        return null;
    }

    protected void destroyTablesObserver(TableObserverImpl observer) {
        if (observers != null) {
            lock();
            try {
                observers.remove(observer);
                observer.destroyObserver();
            } finally {
                unlock();
            }
        }
    }

    short intToShort(final int id) {
        return (short) (id + Short.MIN_VALUE + 1);
    }

    int shortToInt(final short id) {
        return id - Short.MIN_VALUE - 1;
    }

    void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    void unlock() {
        if (lock != null) {
            lock.unlock();
        }
    }

    void graphWriteLock() {
        if (graphStore != null) {
            graphStore.autoWriteLock();
        }
    }

    void graphWriteUnlock() {
        if (graphStore != null) {
            graphStore.autoWriteUnlock();
        }
    }

    void checkNonNullObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    void checkNonNullColumnObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof ColumnImpl)) {
            throw new ClassCastException("Must be ColumnImpl object");
        }
    }

    void checkIndexStatus(final Column column) {
        if (indexStore == null && column.isIndexed()) {
            throw new IllegalArgumentException("Can't add an indexed column to a non indexed store");
        }
    }

    private final class ColumnStoreIterator implements Iterator<Column> {

        private int index;
        private ColumnImpl pointer;

        public ColumnStoreIterator() {
            lock();
        }

        @Override
        public boolean hasNext() {
            while (index < length && (pointer = columns[index++]) == null) {
            }
            if (pointer == null) {
                unlock();
                return false;
            }
            return true;
        }

        @Override
        public ColumnImpl next() {
            ColumnImpl c = pointer;
            pointer = null;
            return c;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    public boolean deepEquals(ColumnStore<T> obj) {
        if (obj == null) {
            return false;
        }
        if (this.elementType != obj.elementType && (this.elementType == null || !this.elementType
                .equals(obj.elementType))) {
            return false;
        }
        Iterator<Column> itr1 = this.iterator();
        Iterator<Column> itr2 = obj.iterator();
        while (itr1.hasNext()) {
            if (!itr2.hasNext()) {
                return false;
            }
            Column c1 = itr1.next();
            Column c2 = itr2.next();
            if (!c1.equals(c2)) {
                return false;
            }
        }
        return true;
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 11 * hash + (this.elementType != null ? this.elementType.hashCode() : 0);
        ColumnStoreIterator itr = new ColumnStoreIterator();
        while (itr.hasNext()) {
            hash = 11 * hash + itr.next().deepHashCode();
        }
        // TODO what about timestampmap
        return hash;
    }
}
