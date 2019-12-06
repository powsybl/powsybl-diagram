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
import com.powsybl.sld.force.layout.gephi.graph.api.UndirectedSubgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.DirectedSubgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphView;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class GraphViewImpl implements GraphView {

    // Data
    protected final GraphStore graphStore;
    protected final boolean nodeView;
    protected final boolean edgeView;
    protected final GraphAttributesImpl attributes;
    protected BitVector nodeBitVector;
    protected BitVector edgeBitVector;
    protected int storeId;
    // Version
    protected final GraphVersion version;
    protected final List<GraphObserverImpl> observers;
    // Decorators
    protected final GraphViewDecorator directedDecorator;
    protected final GraphViewDecorator undirectedDecorator;
    // Stats
    protected int nodeCount;
    protected int edgeCount;
    protected int[] typeCounts;
    protected int[] mutualEdgeTypeCounts;
    protected int mutualEdgesCount;
    // Dynamic
    protected Interval interval;

    public GraphViewImpl(final GraphStore store, boolean nodes, boolean edges) {
        this.graphStore = store;
        this.nodeView = nodes;
        this.edgeView = edges;
        this.attributes = new GraphAttributesImpl();
        if (nodes) {
            this.nodeBitVector = new BitVector(store.nodeStore.maxStoreId());
        } else {
            this.nodeBitVector = null;
        }
        this.edgeBitVector = new BitVector(store.edgeStore.maxStoreId());
        this.typeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        this.mutualEdgeTypeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];

        this.directedDecorator = new GraphViewDecorator(graphStore, this, false);
        this.undirectedDecorator = new GraphViewDecorator(graphStore, this, true);
        this.version = graphStore.version != null ? new GraphVersion(directedDecorator) : null;
        this.observers = graphStore.version != null ? new ArrayList<GraphObserverImpl>() : null;
        this.interval = Interval.INFINITY_INTERVAL;
    }

    public GraphViewImpl(final GraphViewImpl view, boolean nodes, boolean edges) {
        this.graphStore = view.graphStore;
        this.nodeView = nodes;
        this.edgeView = edges;
        this.attributes = new GraphAttributesImpl();
        if (nodes) {
            this.nodeBitVector = view.nodeBitVector.copy();
            this.nodeCount = view.nodeCount;
        } else {
            this.nodeBitVector = null;
        }
        this.edgeCount = view.edgeCount;
        this.edgeBitVector = view.edgeBitVector.copy();
        this.typeCounts = new int[view.typeCounts.length];
        System.arraycopy(view.typeCounts, 0, typeCounts, 0, view.typeCounts.length);
        this.mutualEdgeTypeCounts = new int[view.mutualEdgeTypeCounts.length];
        System.arraycopy(view.mutualEdgeTypeCounts, 0, mutualEdgeTypeCounts, 0, view.mutualEdgeTypeCounts.length);
        this.directedDecorator = new GraphViewDecorator(graphStore, this, false);
        this.undirectedDecorator = new GraphViewDecorator(graphStore, this, true);
        this.version = graphStore.version != null ? new GraphVersion(directedDecorator) : null;
        this.observers = graphStore.version != null ? new ArrayList<GraphObserverImpl>() : null;
        this.interval = view.interval;
    }

    protected DirectedSubgraph getDirectedGraph() {
        return directedDecorator;
    }

    protected UndirectedSubgraph getUndirectedGraph() {
        return undirectedDecorator;
    }

    public boolean addNode(final Node node) {
        checkNodeView();

        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (!isSet) {
            nodeBitVector.set(id);
            nodeCount++;
            incrementNodeVersion();

            IndexStore<Node> indexStore = graphStore.nodeTable.store.indexStore;
            if (indexStore != null) {
                indexStore.indexInView(nodeImpl, this);
            }
            AbstractTimeIndexStore timeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (timeIndexStore != null) {
                timeIndexStore.indexInView(nodeImpl, this);
            }

            if (nodeView && !edgeView) {
                // Add edges
                EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
                while (itr.hasNext()) {
                    EdgeImpl edge = itr.next();
                    NodeImpl opposite = edge.source == nodeImpl ? edge.target : edge.source;
                    if (nodeBitVector.get(opposite.getStoreId())) {
                        // Add edge
                        int edgeid = edge.storeId;
                        boolean edgeisSet = edgeBitVector.get(edgeid);
                        if (!edgeisSet) {

                            incrementEdgeVersion();

                            addEdge(edge);
                        }
                        // End
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean addAllNodes(final Collection<? extends Node> nodes) {
        checkNodeView();

        if (!nodes.isEmpty()) {
            Iterator<? extends Node> nodeItr = nodes.iterator();
            boolean changed = false;
            while (nodeItr.hasNext()) {
                Node node = nodeItr.next();
                checkValidNodeObject(node);
                if (addNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean addEdge(final Edge edge) {
        checkEdgeView();

        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (!isSet) {
            checkIncidentNodesExists(edgeImpl);

            addEdge(edgeImpl);
            return true;
        }
        return false;
    }

    public boolean addAllEdges(final Collection<? extends Edge> edges) {
        checkEdgeView();

        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                checkValidEdgeObject(edge);
                if (addEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean removeNode(final Node node) {
        checkNodeView();

        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (isSet) {
            nodeBitVector.clear(id);
            nodeCount--;
            incrementNodeVersion();

            IndexStore<Node> indexStore = graphStore.nodeTable.store.indexStore;
            if (indexStore != null) {
                indexStore.clearInView(nodeImpl, this);
            }
            AbstractTimeIndexStore timeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (timeIndexStore != null) {
                timeIndexStore.clearInView(nodeImpl, this);
            }

            // Remove edges
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edgeImpl = itr.next();

                int edgeId = edgeImpl.storeId;
                boolean edgeSet = edgeBitVector.get(edgeId);
                if (edgeSet) {
                    removeEdge(edgeImpl);
                }
            }
            return true;
        }
        return false;
    }

    public boolean removeNodeAll(final Collection<? extends Node> nodes) {
        if (!nodes.isEmpty()) {
            Iterator<? extends Node> nodeItr = nodes.iterator();
            boolean changed = false;
            while (nodeItr.hasNext()) {
                Node node = nodeItr.next();
                checkValidNodeObject(node);
                if (removeNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean removeEdge(final Edge edge) {
        checkEdgeView();

        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (isSet) {
            removeEdge(edgeImpl);

            return true;
        }
        return false;
    }

    public boolean removeEdgeAll(final Collection<? extends Edge> edges) {
        checkEdgeView();

        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                checkValidEdgeObject(edge);
                if (removeEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public void clear() {
        if (nodeCount > 0) {
            incrementNodeVersion();
        }
        if (edgeCount > 0) {
            incrementEdgeVersion();
        }
        if (nodeView) {
            nodeBitVector.clear();
        }
        edgeBitVector.clear();
        nodeCount = 0;
        edgeCount = 0;
        typeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgeTypeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgesCount = 0;

        if (nodeView) {
            IndexStore<Node> nodeIndexStore = graphStore.nodeTable.store.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.clear(this);
            }
            AbstractTimeIndexStore nodeTimeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (nodeTimeIndexStore != null) {
                nodeTimeIndexStore.clear(this);
            }
        }
        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.clear(this);
        }
        AbstractTimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.clear(this);
        }
    }

    public void clearEdges() {
        if (edgeCount > 0) {
            incrementEdgeVersion();
        }
        edgeBitVector.clear();
        edgeCount = 0;
        typeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgeTypeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgesCount = 0;

        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.clear(this);
        }
        AbstractTimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.clear(this);
        }
    }

    public void fill() {
        if (nodeView) {
            if (nodeCount > 0) {
                nodeBitVector = new BitVector(graphStore.nodeStore.maxStoreId());
            }
            nodeBitVector.not();
            this.nodeCount = graphStore.nodeStore.size();
        }
        if (edgeCount > 0) {
            edgeBitVector = new BitVector(graphStore.edgeStore.maxStoreId());
        }
        edgeBitVector.not();

        this.edgeCount = graphStore.edgeStore.size();
        int typeLength = graphStore.edgeStore.longDictionary.length;
        this.typeCounts = new int[typeLength];
        for (int i = 0; i < typeLength; i++) {
            int count = graphStore.edgeStore.longDictionary[i].size();
            this.typeCounts[i] = count;
        }
        this.mutualEdgeTypeCounts = new int[graphStore.edgeStore.mutualEdgesTypeSize.length];
        System.arraycopy(graphStore.edgeStore.mutualEdgesTypeSize, 0, this.mutualEdgeTypeCounts, 0, this.mutualEdgeTypeCounts.length);
        this.mutualEdgesCount = graphStore.edgeStore.mutualEdgesSize;

        if (edgeCount > 0) {
            incrementEdgeVersion();
        }
        if (nodeCount > 0) {
            incrementNodeVersion();
        }

        if (nodeView) {
            IndexStore<Node> nodeIndexStore = graphStore.nodeTable.store.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.indexView(directedDecorator);
            }
            AbstractTimeIndexStore nodeTimeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (nodeTimeIndexStore != null) {
                nodeTimeIndexStore.indexView(directedDecorator);
            }
        }
        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.indexView(directedDecorator);
        }
        AbstractTimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.indexView(directedDecorator);
        }
    }

    public boolean containsNode(final NodeImpl node) {
        if (!nodeView) {
            return true;
        }
        return nodeBitVector.get(node.storeId);
    }

    public boolean containsEdge(final EdgeImpl edge) {
        return edgeBitVector.get(edge.storeId);
    }

    public void intersection(final GraphViewImpl otherView) {
        BitVector nodeOtherBitVector = otherView.nodeBitVector;
        BitVector edgeOtherBitVector = otherView.edgeBitVector;

        if (nodeView) {
            int nodeSize = nodeBitVector.size();
            for (int i = 0; i < nodeSize; i++) {
                boolean t = nodeBitVector.get(i);
                boolean o = nodeOtherBitVector.get(i);
                if (t && !o) {
                    removeNode(getNode(i));
                }
            }
        }

        if (edgeView) {
            int edgeSize = edgeBitVector.size();
            for (int i = 0; i < edgeSize; i++) {
                boolean t = edgeBitVector.get(i);
                boolean o = edgeOtherBitVector.get(i);
                if (t && !o) {
                    removeEdge(getEdge(i));
                }
            }
        }
    }

    public void union(final GraphViewImpl otherView) {
        BitVector nodeOtherBitVector = otherView.nodeBitVector;
        BitVector edgeOtherBitVector = otherView.edgeBitVector;

        if (nodeView) {
            int nodeSize = nodeBitVector.size();
            for (int i = 0; i < nodeSize; i++) {
                boolean t = nodeBitVector.get(i);
                boolean o = nodeOtherBitVector.get(i);
                if (!t && o) {
                    addNode(getNode(i));
                }
            }
        }

        if (edgeView) {
            int edgeSize = edgeBitVector.size();
            for (int i = 0; i < edgeSize; i++) {
                boolean t = edgeBitVector.get(i);
                boolean o = edgeOtherBitVector.get(i);
                if (!t && o) {
                    addEdge(getEdge(i));
                }
            }
        }
    }

    public void not() {
        if (nodeView) {
            nodeBitVector.not();
            this.nodeCount = graphStore.nodeStore.size() - this.nodeCount;
        }
        edgeBitVector.not();

        this.edgeCount = graphStore.edgeStore.size() - this.edgeCount;
        for (int i = 0; i < typeCounts.length; i++) {
            this.typeCounts[i] = graphStore.edgeStore.longDictionary[i].size() - this.typeCounts[i];
        }
        for (int i = 0; i < mutualEdgeTypeCounts.length; i++) {
            this.mutualEdgeTypeCounts[i] = graphStore.edgeStore.mutualEdgesTypeSize[i] - this.mutualEdgeTypeCounts[i];
        }
        this.mutualEdgesCount = graphStore.edgeStore.mutualEdgesSize - this.mutualEdgesCount;

        if (nodeView) {
            incrementNodeVersion();
        }
        incrementEdgeVersion();

        if (nodeView) {
            for (Edge e : graphStore.edgeStore) {
                boolean t = edgeBitVector.get(e.getStoreId());
                if (t && (!nodeBitVector.get(e.getSource().getStoreId()) || !nodeBitVector.get(e.getTarget()
                        .getStoreId()))) {
                    removeEdge((EdgeImpl) e);
                }
            }
        }

        if (nodeView) {
            IndexStore<Node> nodeIndexStore = graphStore.nodeTable.store.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.clear(directedDecorator.view);
                nodeIndexStore.indexView(directedDecorator);
            }
            AbstractTimeIndexStore nodeTimeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (nodeTimeIndexStore != null) {
                nodeTimeIndexStore.clear(directedDecorator.view);
                nodeTimeIndexStore.indexView(directedDecorator);
            }
        }
        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.clear(directedDecorator.view);
            edgeIndexStore.indexView(directedDecorator);
        }
        AbstractTimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.clear(directedDecorator.view);
            edgeTimeIndexStore.indexView(directedDecorator);
        }
    }

    public void addEdgeInNodeView(EdgeImpl edge) {
        if (nodeBitVector.get(edge.source.getStoreId()) && nodeBitVector.get(edge.target.getStoreId())) {
            incrementEdgeVersion();

            addEdge(edge);
        }
    }

    public int getNodeCount() {
        if (nodeView) {
            return nodeCount;
        }
        return graphStore.nodeStore.size();
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getUndirectedEdgeCount() {
        return edgeCount - mutualEdgesCount;
    }

    public int getEdgeCount(int type) {
        if (type < 0 || type >= typeCounts.length) {
            throw new IllegalArgumentException("Incorrect type=" + type);
        }
        return typeCounts[type];
    }

    public int getUndirectedEdgeCount(int type) {
        if (type < 0 || type >= typeCounts.length) {
            throw new IllegalArgumentException("Incorrect type=" + type);
        }
        return typeCounts[type] - mutualEdgeTypeCounts[type];
    }

    @Override
    public GraphModelImpl getGraphModel() {
        return graphStore.graphModel;
    }

    @Override
    public boolean isMainView() {
        return false;
    }

    @Override
    public boolean isNodeView() {
        return nodeView;
    }

    @Override
    public boolean isEdgeView() {
        return edgeView;
    }

    public void setTimeInterval(Interval interval) {
        Interval intervalTmp = interval;
        if (intervalTmp == null) {
            intervalTmp = Interval.INFINITY_INTERVAL;
        }
        this.interval = intervalTmp;
    }

    @Override
    public Interval getTimeInterval() {
        return interval;
    }

    @Override
    public boolean isDestroyed() {
        return storeId == GraphViewStore.NULL_VIEW;
    }

    protected GraphObserverImpl createGraphObserver(Graph graph, boolean withDiff) {
        if (observers != null) {
            GraphObserverImpl observer = new GraphObserverImpl(graphStore, version, graph, withDiff);
            observers.add(observer);

            return observer;
        }
        return null;
    }

    protected void destroyGraphObserver(GraphObserverImpl observer) {
        if (observers != null) {
            observers.remove(observer);
            observer.destroyObserver();
        }
    }

    protected void destroyAllObservers() {
        if (observers != null) {
            for (GraphObserverImpl graphObserverImpl : observers) {
                graphObserverImpl.destroyObserver();
            }
            observers.clear();
        }
    }

    protected void ensureNodeVectorSize(NodeImpl node) {
        int sid = node.storeId;
        if (sid >= nodeBitVector.size()) {
            int newSize = Math
                    .min(Math.max(sid + 1, (int) (sid * GraphStoreConfiguration.VIEW_GROWING_FACTOR)), Integer.MAX_VALUE);
            nodeBitVector = growBitVector(nodeBitVector, newSize);
        }
    }

    private void ensureNodeVectorSize(int size) {
        if (size > nodeBitVector.size()) {
            nodeBitVector = growBitVector(nodeBitVector, size);
        }
    }

    private void ensureEdgeVectorSize(int size) {
        if (size > edgeBitVector.size()) {
            edgeBitVector = growBitVector(edgeBitVector, size);
        }
    }

    protected void ensureEdgeVectorSize(EdgeImpl edge) {
        int sid = edge.storeId;
        if (sid >= edgeBitVector.size()) {
            int newSize = Math
                    .min(Math.max(sid + 1, (int) (sid * GraphStoreConfiguration.VIEW_GROWING_FACTOR)), Integer.MAX_VALUE);
            edgeBitVector = growBitVector(edgeBitVector, newSize);
        }
    }

    private void addEdge(EdgeImpl edgeImpl) {
        incrementEdgeVersion();

        edgeBitVector.set(edgeImpl.storeId);
        edgeCount++;

        int type = edgeImpl.type;
        ensureTypeCountArrayCapacity(type);

        typeCounts[type]++;

        if (edgeImpl.isMutual() && edgeImpl.source.storeId < edgeImpl.target.storeId) {
            mutualEdgeTypeCounts[type]++;
            mutualEdgesCount++;
        }

        IndexStore<Edge> indexStore = graphStore.edgeTable.store.indexStore;
        if (indexStore != null) {
            indexStore.indexInView(edgeImpl, this);
        }
        AbstractTimeIndexStore timeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (timeIndexStore != null) {
            timeIndexStore.indexInView(edgeImpl, this);
        }
    }

    private void removeEdge(EdgeImpl edgeImpl) {
        incrementEdgeVersion();

        edgeBitVector.clear(edgeImpl.storeId);
        edgeCount--;
        typeCounts[edgeImpl.type]--;

        if (edgeImpl.isMutual() && edgeImpl.source.storeId < edgeImpl.target.storeId) {
            mutualEdgeTypeCounts[edgeImpl.type]--;
            mutualEdgesCount--;
        }

        IndexStore<Edge> indexStore = graphStore.edgeTable.store.indexStore;
        if (indexStore != null) {
            indexStore.clearInView(edgeImpl, this);
        }
    }

    private BitVector growBitVector(BitVector bitVector, int size) {
        long[] elements = bitVector.elements();
        long[] newElements = QuickBitVector.makeBitVector(size, 1);
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        return new BitVector(newElements, size);
    }

    private NodeImpl getNode(int id) {
        return graphStore.nodeStore.get(id);
    }

    private EdgeImpl getEdge(int id) {
        return graphStore.edgeStore.get(id);
    }

    private void ensureTypeCountArrayCapacity(int type) {
        if (type >= typeCounts.length) {
            int[] newArray = new int[type + 1];
            System.arraycopy(typeCounts, 0, newArray, 0, typeCounts.length);
            typeCounts = newArray;

            int[] newMutualArray = new int[type + 1];
            System.arraycopy(mutualEdgeTypeCounts, 0, newMutualArray, 0, mutualEdgeTypeCounts.length);
            mutualEdgeTypeCounts = newMutualArray;
        }
    }

    public int deepHashCode() {
        int hash = 5;
        hash = 17 * hash + (this.nodeView ? 1 : 0);
        hash = 17 * hash + (this.edgeView ? 1 : 0);
        hash = 11 * hash + (this.nodeBitVector != null ? this.nodeBitVector.hashCode() : 0);
        hash = 11 * hash + (this.edgeBitVector != null ? this.edgeBitVector.hashCode() : 0);
        hash = 11 * hash + this.nodeCount;
        hash = 11 * hash + this.edgeCount;
        hash = 11 * hash + Arrays.hashCode(this.typeCounts);
        hash = 11 * hash + Arrays.hashCode(this.mutualEdgeTypeCounts);
        hash = 11 * hash + this.mutualEdgesCount;
        hash = 11 * hash + (this.interval != null ? this.interval.hashCode() : 0);
        return hash;
    }

    public boolean deepEquals(GraphViewImpl obj) {
        if (obj == null) {
            return false;
        }
        if (this.nodeBitVector != obj.nodeBitVector && (this.nodeBitVector == null || !this.nodeBitVector
                .equals(obj.nodeBitVector))) {
            return false;
        }
        if (this.edgeBitVector != obj.edgeBitVector && (this.edgeBitVector == null || !this.edgeBitVector
                .equals(obj.edgeBitVector))) {
            return false;
        }
        if (this.nodeCount != obj.nodeCount) {
            return false;
        }
        if (this.edgeCount != obj.edgeCount) {
            return false;
        }
        if (this.nodeView != obj.nodeView) {
            return false;
        }
        if (this.edgeView != obj.edgeView) {
            return false;
        }
        if (!Arrays.equals(this.typeCounts, obj.typeCounts)) {
            return false;
        }
        if (!Arrays.equals(this.mutualEdgeTypeCounts, obj.mutualEdgeTypeCounts)) {
            return false;
        }
        if (this.mutualEdgesCount != obj.mutualEdgesCount) {
            return false;
        }
        if (this.interval != obj.interval && (this.interval == null || !this.interval.equals(obj.interval))) {
            return false;
        }
        return true;
    }

    private int incrementNodeVersion() {
        if (version != null) {
            return version.incrementAndGetNodeVersion();
        }
        return 0;
    }

    private int incrementEdgeVersion() {
        if (version != null) {
            return version.incrementAndGetEdgeVersion();
        }
        return 0;
    }

    private void checkNodeView() {
        if (!nodeView) {
            throw new RuntimeException("This method should only be used on a view with nodes enabled");
        }
    }

    private void checkEdgeView() {
        if (!edgeView) {
            throw new RuntimeException("This method should only be used on a view with edges enabled");
        }
    }

    private void checkIncidentNodesExists(final EdgeImpl e) {
        if (nodeView) {
            if (!nodeBitVector.get(e.source.storeId) || !nodeBitVector.get(e.target.storeId)) {
                throw new RuntimeException("Both source and target nodes need to be in the view");
            }
        }
    }

    private void checkValidEdgeObject(final Edge n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof EdgeImpl)) {
            throw new ClassCastException("Object must be a EdgeImpl object");
        }
        if (((EdgeImpl) n).storeId == EdgeStore.NULL_ID) {
            throw new IllegalArgumentException("Edge should belong to a store");
        }
    }

    private void checkValidNodeObject(final Node n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
        if (((NodeImpl) n).storeId == NodeStore.NULL_ID) {
            throw new IllegalArgumentException("Node should belong to a store");
        }
    }
}
