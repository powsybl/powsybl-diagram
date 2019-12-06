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

import cern.colt.bitvector.BitVector;
import cern.colt.bitvector.QuickBitVector;
import com.powsybl.sld.force.layout.gephi.graph.api.Column;
import com.powsybl.sld.force.layout.gephi.graph.api.EdgeIterable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.ColumnDiff;
import com.powsybl.sld.force.layout.gephi.graph.api.ColumnObserver;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;
import com.powsybl.sld.force.layout.gephi.graph.api.NodeIterable;

public class ColumnObserverImpl implements ColumnObserver {

    // Store and column
    protected final GraphStore graphStore;
    protected final ColumnImpl column;
    // Version
    protected int version = Integer.MIN_VALUE;
    protected boolean destroyed;
    // Config
    protected final boolean withDiff;
    protected BitVector bitVector;
    // Cache
    protected AbstractColumnDiffImpl columnDiff;

    public ColumnObserverImpl(GraphStore store, ColumnImpl column, boolean withDiff) {
        this.column = column;
        this.graphStore = store;
        this.version = column.version.version.get();
        this.withDiff = withDiff;
    }

    @Override
    public synchronized boolean hasColumnChanged() {
        if (!destroyed) {
            readLock();
            try {
                int v = column.version.version.get();
                boolean changed = v != version;
                version = v;
                if (withDiff && changed) {
                    refreshDiff();
                }
                return changed;
            } finally {
                readUnlock();
            }
        }
        return false;
    }

    @Override
    public ColumnDiff getDiff() {
        if (!withDiff) {
            throw new RuntimeException("This observer doesn't compute diffs, set diff setting to true");
        }
        if (columnDiff == null) {
            throw new IllegalStateException(
                    "The hasGraphChanged() method should be called first and getDiff() only once then");
        }
        ColumnDiff diff = columnDiff;
        columnDiff = null;
        return diff;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public void destroy() {
        column.destroyColumnObserver(this);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    protected void destroyObserver() {
        destroyed = true;
        columnDiff = null;
        bitVector = null;
    }

    private void refreshDiff() {
        boolean node = AttributeUtils.isNodeColumn(column);
        columnDiff = node ? new NodeColumnDiffImpl() : new EdgeColumnDiffImpl();

        int size = bitVector.size();

        for (int i = 0; i < size; i++) {
            boolean t = bitVector.get(i);
            if (t && node) {
                if (graphStore.nodeStore.isValidIndex(i)) {
                    Node n = graphStore.nodeStore.get(i);
                    if (n.getStoreId() == i) {
                        columnDiff.addElement(n);
                    }
                }
            } else if (t && !node) {
                if (graphStore.edgeStore.isValidIndex(i)) {
                    Edge e = graphStore.edgeStore.get(i);
                    if (e.getStoreId() == i) {
                        columnDiff.addElement(e);
                    }
                }
            }
        }
        bitVector.clear();
    }

    protected void setElement(AbstractElementImpl element) {
        int storeId = element.getStoreId();
        ensureVectorSize(element);
        bitVector.set(storeId);
    }

    protected abstract class AbstractColumnDiffImpl<K extends Element> implements ColumnDiff {

        protected final ObjectList<K> touchedElements;

        public AbstractColumnDiffImpl() {
            this.touchedElements = new ObjectArrayList<K>();
        }

        protected void addElement(K element) {
            touchedElements.add(element);
        }
    }

    protected final class NodeColumnDiffImpl extends AbstractColumnDiffImpl<Node> {

        @Override
        public NodeIterable getTouchedElements() {
            if (!touchedElements.isEmpty()) {
                return graphStore.getNodeIterableWrapper(touchedElements.iterator(), false);

            }
            return NodeIterable.NodeIterableEmpty.EMPTY;
        }
    }

    protected final class EdgeColumnDiffImpl extends AbstractColumnDiffImpl<Edge> {

        @Override
        public EdgeIterable getTouchedElements() {
            if (!touchedElements.isEmpty()) {
                return graphStore.getEdgeIterableWrapper(touchedElements.iterator(), false);

            }
            return EdgeIterable.EdgeIterableEmpty.EMPTY;
        }
    }

    private void ensureVectorSize(AbstractElementImpl element) {
        int sid = element.getStoreId();
        if (bitVector == null) {
            bitVector = new BitVector(sid + 1);
        } else if (sid >= bitVector.size()) {
            int newSize = Math
                    .min(Math.max(sid + 1, (int) (sid * GraphStoreConfiguration.COLUMNDIFF_GROWING_FACTOR)), Integer.MAX_VALUE);
            bitVector = growBitVector(bitVector, newSize);
        }
    }

    private BitVector growBitVector(BitVector bitVector, int size) {
        long[] elements = bitVector.elements();
        long[] newElements = QuickBitVector.makeBitVector(size, 1);
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        return new BitVector(newElements, size);
    }

    private void readLock() {
        graphStore.autoReadLock();
    }

    private void readUnlock() {
        graphStore.autoReadUnlock();
    }
}
