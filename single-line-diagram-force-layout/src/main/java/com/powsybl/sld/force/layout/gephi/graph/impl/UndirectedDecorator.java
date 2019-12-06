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

import com.powsybl.sld.force.layout.gephi.graph.api.EdgeIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphModel;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphView;
import com.powsybl.sld.force.layout.gephi.graph.api.UndirectedSubgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;
import com.powsybl.sld.force.layout.gephi.graph.api.NodeIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.Subgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.UndirectedGraph;

import java.util.Collection;
import java.util.Set;

public class UndirectedDecorator implements UndirectedGraph, UndirectedSubgraph {

    protected final GraphStore store;

    public UndirectedDecorator(GraphStore store) {
        this.store = store;
    }

    @Override
    public boolean addEdge(Edge edge) {
        if (edge.isDirected()) {
            throw new IllegalArgumentException("Can't add a directed edge to an undirected graph");
        }
        return store.addEdge(edge);
    }

    @Override
    public boolean addNode(Node node) {
        return store.addNode(node);
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        for (Edge edge : edges) {
            if (edge.isDirected()) {
                throw new IllegalArgumentException("Can't add a directed edge to an undirected graph");
            }
        }
        return store.addAllEdges(edges);
    }

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        return store.addAllNodes(nodes);
    }

    @Override
    public boolean removeEdge(Edge edge) {
        return store.removeEdge(edge);
    }

    @Override
    public boolean removeNode(Node node) {
        return store.removeNode(node);
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        return store.removeAllEdges(edges);
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        return store.removeAllNodes(nodes);
    }

    @Override
    public boolean contains(Node node) {
        return store.contains(node);
    }

    @Override
    public boolean contains(Edge edge) {
        return store.contains(edge);
    }

    @Override
    public Node getNode(Object id) {
        return store.getNode(id);
    }

    @Override
    public boolean hasNode(final Object id) {
        return store.hasNode(id);
    }

    @Override
    public Edge getEdge(Object id) {
        return store.getEdge(id);
    }

    @Override
    public boolean hasEdge(final Object id) {
        return store.hasEdge(id);
    }

    @Override
    public Edge getEdge(Node node1, Node node2) {
        readLock();
        try {
            return store.edgeStore.get(node1, node2, true);
        } finally {
            readUnlock();
        }
    }

    @Override
    public EdgeIterable getEdges(Node node1, Node node2) {
        return store.getEdgeIterableWrapper(store.edgeStore.edgesUndirectedIterator(node1, node2));
    }

    @Override
    public Edge getEdge(Node node1, Node node2, int type) {
        readLock();
        try {
            return store.edgeStore.get(node1, node2, type, true);
        } finally {
            readUnlock();
        }
    }

    @Override
    public EdgeIterable getEdges(Node node1, Node node2, int type) {
        return store.getEdgeIterableWrapper(store.edgeStore.edgesUndirectedIterator(node1, node2, type));
    }

    @Override
    public NodeIterable getNodes() {
        return store.getNodes();
    }

    @Override
    public EdgeIterable getEdges() {
        return store.getEdgeIterableWrapper(store.edgeStore.iteratorUndirected());
    }

    @Override
    public EdgeIterable getSelfLoops() {
        return store.getEdgeIterableWrapper(store.edgeStore.iteratorSelfLoop());
    }

    @Override
    public NodeIterable getNeighbors(Node node) {
        return store.getNodeIterableWrapper(store.edgeStore.neighborIterator(node));
    }

    @Override
    public NodeIterable getNeighbors(Node node, int type) {
        return store.getNodeIterableWrapper(store.edgeStore.neighborIterator(node, type));
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        return store.getEdgeIterableWrapper(store.edgeStore.edgeUndirectedIterator(node));
    }

    @Override
    public EdgeIterable getEdges(Node node, int type) {
        return store.getEdgeIterableWrapper(store.edgeStore.edgeUndirectedIterator(node, type));
    }

    @Override
    public int getNodeCount() {
        return store.getNodeCount();
    }

    @Override
    public int getEdgeCount() {
        return store.edgeStore.undirectedSize();
    }

    @Override
    public int getEdgeCount(int type) {
        store.autoReadLock();
        try {
            if (store.edgeTypeStore.contains(type)) {
                return store.edgeStore.undirectedSize(type);
            }
            return 0;
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Node getOpposite(Node node, Edge edge) {
        return store.getOpposite(node, edge);
    }

    @Override
    public int getDegree(Node node) {
        return store.getUndirectedDegree(node);
    }

    @Override
    public boolean isSelfLoop(Edge edge) {
        return store.isSelfLoop(edge);
    }

    @Override
    public boolean isDirected(Edge edge) {
        return false;
    }

    @Override
    public boolean isAdjacent(Node node1, Node node2) {
        return store.isAdjacent(node1, node2);
    }

    @Override
    public boolean isAdjacent(Node node1, Node node2, int type) {
        return store.isAdjacent(node1, node2, type);
    }

    @Override
    public boolean isIncident(Edge edge1, Edge edge2) {
        return store.isIncident(edge1, edge2);
    }

    @Override
    public boolean isIncident(Node node, Edge edge) {
        return store.isIncident(node, edge);
    }

    @Override
    public void clearEdges(Node node) {
        store.clearEdges(node);
    }

    @Override
    public void clearEdges(Node node, int type) {
        store.clearEdges(node, type);
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void clearEdges() {
        store.clearEdges();
    }

    @Override
    public Object getAttribute(String key) {
        return store.attributes.getValue(key);
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        return store.attributes.getValue(key, timestamp);
    }

    @Override
    public Object getAttribute(String key, Interval interval) {
        return store.attributes.getValue(key, interval);
    }

    @Override
    public Set<String> getAttributeKeys() {
        return store.attributes.getKeys();
    }

    @Override
    public void setAttribute(String key, Object value) {
        store.attributes.setValue(key, value);
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        store.attributes.setValue(key, value, timestamp);
    }

    @Override
    public void setAttribute(String key, Object value, Interval interval) {
        store.attributes.setValue(key, value, interval);
    }

    @Override
    public void removeAttribute(String key) {
        store.attributes.removeValue(key);
    }

    @Override
    public void removeAttribute(String key, Interval interval) {
        store.attributes.removeValue(key, interval);
    }

    @Override
    public void removeAttribute(String key, double timestamp) {
        store.attributes.removeValue(key, timestamp);
    }

    @Override
    public GraphView getView() {
        return store.mainGraphView;
    }

    @Override
    public void readLock() {
        store.autoReadLock();
    }

    @Override
    public void readUnlock() {
        store.autoReadUnlock();
    }

    @Override
    public void readUnlockAll() {
        store.autoReadUnlockAll();
    }

    @Override
    public void writeLock() {
        store.autoWriteLock();
    }

    @Override
    public void writeUnlock() {
        store.autoWriteUnlock();
    }

    @Override
    public GraphModel getModel() {
        return store.graphModel;
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public boolean isMixed() {
        return false;
    }

    @Override
    public boolean isUndirected() {
        return true;
    }

    @Override
    public void union(Subgraph subGraph) {
        throw new UnsupportedOperationException("Not supported yet for the main view.");
    }

    @Override
    public void intersection(Subgraph subGraph) {
        throw new UnsupportedOperationException("Not supported yet for the main view.");
    }

    @Override
    public void fill() {
        throw new UnsupportedOperationException("Not supported yet for the main view.");
    }

    @Override
    public void not() {
        throw new UnsupportedOperationException("Not supported yet for the main view.");
    }

    @Override
    public Graph getRootGraph() {
        return this;
    }
}
