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

import com.powsybl.sld.force.layout.gephi.graph.api.DirectedSubgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.EdgeIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphModel;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphView;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;
import com.powsybl.sld.force.layout.gephi.graph.api.NodeIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.Subgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.UndirectedSubgraph;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class GraphViewDecorator implements DirectedSubgraph, UndirectedSubgraph {

    protected final boolean undirected;
    protected final GraphViewImpl view;
    protected final GraphStore graphStore;

    public GraphViewDecorator(GraphStore graphStore, GraphViewImpl view, boolean undirected) {
        this.graphStore = graphStore;
        this.view = view;
        this.undirected = undirected;
    }

    @Override
    public Edge getEdge(Node node1, Node node2) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(node1, node2, undirected);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public EdgeIterable getEdges(Node node1, Node node2) {
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore
                .getAll(node1, node2, undirected)));
    }

    @Override
    public Edge getEdge(Node node1, Node node2, int type) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(node1, node2, type, undirected);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public EdgeIterable getEdges(Node node1, Node node2, int type) {
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore
                .getAll(node1, node2, type, undirected)));
    }

    @Override
    public Edge getMutualEdge(Edge e) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.getMutualEdge(e);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public NodeIterable getPredecessors(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeInIterator(node))));
    }

    @Override
    public NodeIterable getPredecessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeInIterator(node, type))));
    }

    @Override
    public NodeIterable getSuccessors(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeOutIterator(node))));
    }

    @Override
    public NodeIterable getSuccessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeOutIterator(node, type))));
    }

    @Override
    public EdgeIterable getInEdges(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node)));
    }

    @Override
    public EdgeIterable getInEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node, type)));
    }

    @Override
    public EdgeIterable getOutEdges(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node)));
    }

    @Override
    public EdgeIterable getOutEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore
                .getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node, type)));
    }

    @Override
    public boolean isAdjacent(Node source, Node target) {
        checkValidInViewNodeObject(source);
        checkValidInViewNodeObject(target);
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(source, target, undirected);
            return edge != null && view.containsEdge(edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean isAdjacent(Node source, Node target, int type) {
        checkValidInViewNodeObject(source);
        checkValidInViewNodeObject(target);
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(source, target, type, undirected);
            return edge != null && view.containsEdge(edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean addEdge(Edge edge) {
        checkValidEdgeObject(edge);
        graphStore.autoWriteLock();
        try {
            return view.addEdge(edge);
        } finally {
            graphStore.autoWriteUnlock();
        }

    }

    @Override
    public boolean addNode(Node node) {
        checkValidNodeObject(node);
        graphStore.autoWriteLock();
        try {
            return view.addNode(node);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        graphStore.autoWriteLock();
        try {
            return view.addAllEdges(edges);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            return view.addAllNodes(nodes);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeEdge(Edge edge) {
        checkValidEdgeObject(edge);
        graphStore.autoWriteLock();
        try {
            return view.removeEdge(edge);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeNode(Node node) {
        checkValidNodeObject(node);
        graphStore.autoWriteLock();
        try {
            return view.removeNode(node);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        graphStore.autoWriteLock();
        try {
            return view.removeEdgeAll(edges);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            return view.removeNodeAll(nodes);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean contains(Node node) {
        checkValidNodeObject(node);
        graphStore.autoReadLock();
        try {
            return view.containsNode((NodeImpl) node);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean contains(Edge edge) {
        checkValidEdgeObject(edge);
        graphStore.autoReadLock();
        try {
            return view.containsEdge((EdgeImpl) edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public Node getNode(Object id) {
        graphStore.autoReadLock();
        try {
            NodeImpl node = graphStore.getNode(id);
            if (node != null && view.containsNode(node)) {
                return node;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean hasNode(final Object id) {
        return getNode(id) != null;
    }

    @Override
    public Edge getEdge(Object id) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.getEdge(id);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean hasEdge(final Object id) {
        return getEdge(id) != null;
    }

    @Override
    public NodeIterable getNodes() {
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.nodeStore.iterator()));
    }

    @Override
    public EdgeIterable getEdges() {
        if (undirected) {
            return graphStore.getEdgeIterableWrapper(new UndirectedEdgeViewIterator(graphStore.edgeStore.iterator()));
        } else {
            return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.iterator()));
        }
    }

    @Override
    public EdgeIterable getSelfLoops() {
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.iteratorSelfLoop()));
    }

    @Override
    public NodeIterable getNeighbors(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new UndirectedEdgeViewIterator(
                graphStore.edgeStore.edgeIterator(node))));
    }

    @Override
    public NodeIterable getNeighbors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new UndirectedEdgeViewIterator(
                graphStore.edgeStore.edgeIterator(node, type))));
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        checkValidInViewNodeObject(node);
        if (undirected) {
            return graphStore.getEdgeIterableWrapper(new UndirectedEdgeViewIterator(graphStore.edgeStore
                    .edgeIterator(node)));
        } else {
            return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeIterator(node)));
        }
    }

    @Override
    public EdgeIterable getEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        if (undirected) {
            return graphStore.getEdgeIterableWrapper(new UndirectedEdgeViewIterator(graphStore.edgeStore
                    .edgeIterator(node, type)));
        } else {
            return graphStore
                    .getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeIterator(node, type)));
        }

    }

    @Override
    public int getNodeCount() {
        return view.getNodeCount();
    }

    @Override
    public int getEdgeCount() {
        if (undirected) {
            return view.getUndirectedEdgeCount();
        } else {
            return view.getEdgeCount();
        }
    }

    @Override
    public int getEdgeCount(int type) {
        if (undirected) {
            return view.getUndirectedEdgeCount(type);
        } else {
            return view.getEdgeCount(type);
        }
    }

    @Override
    public Node getOpposite(Node node, Edge edge) {
        checkValidInViewNodeObject(node);
        checkValidInViewEdgeObject(edge);

        return graphStore.getOpposite(node, edge);
    }

    @Override
    public int getDegree(Node node) {
        if (undirected) {
            int count = 0;
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                if (view.containsEdge(edge) && !isUndirectedToIgnore(edge)) {
                    count++;
                    if (edge.isSelfLoop()) {
                        count++;
                    }
                }
            }
            return count;
        } else {
            int count = 0;
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                if (view.containsEdge(edge)) {
                    count++;
                    if (edge.isSelfLoop()) {
                        count++;
                    }
                }
            }
            return count;
        }
    }

    @Override
    public int getInDegree(Node node) {
        int count = 0;
        EdgeStore.EdgeInIterator itr = graphStore.edgeStore.edgeInIterator(node);
        while (itr.hasNext()) {
            if (view.containsEdge(itr.next())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getOutDegree(Node node) {
        int count = 0;
        EdgeStore.EdgeOutIterator itr = graphStore.edgeStore.edgeOutIterator(node);
        while (itr.hasNext()) {
            if (view.containsEdge(itr.next())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isSelfLoop(Edge edge) {
        return edge.isSelfLoop();
    }

    @Override
    public boolean isDirected(Edge edge) {
        return edge.isDirected();
    }

    @Override
    public boolean isIncident(Edge edge1, Edge edge2) {
        graphStore.autoReadLock();
        try {
            checkValidInViewEdgeObject(edge1);
            checkValidInViewEdgeObject(edge2);

            return graphStore.edgeStore.isIncident((EdgeImpl) edge1, (EdgeImpl) edge2);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean isIncident(final Node node, final Edge edge) {
        graphStore.autoReadLock();
        try {
            checkValidInViewNodeObject(node);
            checkValidInViewEdgeObject(edge);

            return graphStore.edgeStore.isIncident((NodeImpl) node, (EdgeImpl) edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public void clearEdges(Node node) {
        graphStore.autoWriteLock();
        try {
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            for (; itr.hasNext();) {
                EdgeImpl edge = itr.next();
                view.removeEdge(edge);
            }
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void clearEdges(Node node, int type) {
        graphStore.autoWriteLock();
        try {
            EdgeStore.EdgeTypeInOutIterator itr = graphStore.edgeStore.edgeIterator(node, type);
            for (; itr.hasNext();) {
                EdgeImpl edge = itr.next();
                view.removeEdge(edge);
            }
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public void clearEdges() {
        view.clearEdges();
    }

    @Override
    public Object getAttribute(String key) {
        return view.attributes.getValue(key);
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        return view.attributes.getValue(key, timestamp);
    }

    @Override
    public Object getAttribute(String key, Interval interval) {
        return view.attributes.getValue(key, interval);
    }

    @Override
    public Set<String> getAttributeKeys() {
        return view.attributes.getKeys();
    }

    @Override
    public void setAttribute(String key, Object value) {
        view.attributes.setValue(key, value);
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        view.attributes.setValue(key, value, timestamp);
    }

    @Override
    public void setAttribute(String key, Object value, Interval interval) {
        view.attributes.setValue(key, value, interval);
    }

    @Override
    public void removeAttribute(String key) {
        view.attributes.removeValue(key);
    }

    @Override
    public void removeAttribute(String key, double timestamp) {
        view.attributes.removeValue(key, timestamp);
    }

    @Override
    public void removeAttribute(String key, Interval interval) {
        view.attributes.removeValue(key, interval);
    }

    @Override
    public GraphModel getModel() {
        return graphStore.graphModel;
    }

    @Override
    public boolean isDirected() {
        return graphStore.isDirected();
    }

    @Override
    public boolean isUndirected() {
        return graphStore.isUndirected();
    }

    @Override
    public boolean isMixed() {
        return graphStore.isMixed();
    }

    @Override
    public void readLock() {
        graphStore.lock.readLock();
    }

    @Override
    public void readUnlock() {
        graphStore.lock.readUnlock();
    }

    @Override
    public void readUnlockAll() {
        graphStore.lock.readUnlockAll();
    }

    @Override
    public void writeLock() {
        graphStore.lock.writeLock();
    }

    @Override
    public void writeUnlock() {
        graphStore.lock.writeUnlock();
    }

    @Override
    public GraphView getView() {
        return view;
    }

    @Override
    public void fill() {
        graphStore.autoWriteLock();
        try {
            view.fill();
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void union(Subgraph subGraph) {
        checkValidViewObject(subGraph.getView());

        graphStore.autoWriteLock();
        try {
            view.union((GraphViewImpl) subGraph.getView());
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void intersection(Subgraph subGraph) {
        checkValidViewObject(subGraph.getView());

        graphStore.autoWriteLock();
        try {
            view.intersection((GraphViewImpl) subGraph.getView());
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void not() {
        graphStore.autoWriteLock();
        try {
            view.not();
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public Graph getRootGraph() {
        return graphStore;
    }

    void checkWriteLock() {
        if (graphStore.lock != null) {
            graphStore.lock.checkHoldWriteLock();
        }
    }

    void checkValidNodeObject(final Node n) {
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

    void checkValidInViewNodeObject(final Node n) {
        checkValidNodeObject(n);

        if (!view.containsNode((NodeImpl) n)) {
            throw new RuntimeException("Node doesn't belong to this view");
        }
    }

    void checkValidEdgeObject(final Edge n) {
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

    void checkValidInViewEdgeObject(final Edge e) {
        checkValidEdgeObject(e);

        if (!view.containsEdge((EdgeImpl) e)) {
            throw new RuntimeException("Edge doesn't belong to this view");
        }
    }

    void checkValidViewObject(final GraphView view) {
        if (view == null) {
            throw new NullPointerException();
        }
        if (!(view instanceof GraphViewImpl)) {
            throw new ClassCastException("Object must be a GraphViewImpl object");
        }
        if (((GraphViewImpl) view).graphStore != graphStore) {
            throw new RuntimeException("The view doesn't belong to this store");
        }
    }

    boolean isUndirectedToIgnore(final EdgeImpl edge) {
        if (edge.isMutual() && edge.source.storeId < edge.target.storeId) {
            if (view.containsEdge(graphStore.edgeStore.get(edge.target, edge.source, edge.type, false))) {
                return true;
            }
        }
        return false;
    }

    protected final class NodeViewIterator implements Iterator<Node> {

        private final Iterator<Node> nodeIterator;
        private NodeImpl pointer;

        public NodeViewIterator(Iterator<Node> nodeIterator) {
            this.nodeIterator = nodeIterator;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!nodeIterator.hasNext()) {
                    return false;
                }
                pointer = (NodeImpl) nodeIterator.next();
                if (!view.containsNode(pointer)) {
                    pointer = null;
                }
            }
            return true;
        }

        @Override
        public Node next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            removeNode(pointer);
        }
    }

    protected final class EdgeViewIterator implements Iterator<Edge> {

        private final Iterator<Edge> edgeIterator;
        private EdgeImpl pointer;

        public EdgeViewIterator(Iterator<Edge> edgeIterator) {
            this.edgeIterator = edgeIterator;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!edgeIterator.hasNext()) {
                    return false;
                }
                pointer = (EdgeImpl) edgeIterator.next();
                if (!view.containsEdge(pointer)) {
                    pointer = null;
                }
            }
            return true;
        }

        @Override
        public Edge next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            removeEdge(pointer);
        }
    }

    protected final class UndirectedEdgeViewIterator implements Iterator<Edge> {

        protected final Iterator<Edge> itr;
        protected EdgeImpl pointer;

        public UndirectedEdgeViewIterator(Iterator<Edge> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null || !view.containsEdge(pointer) || isUndirectedToIgnore(pointer)) {
                if (!itr.hasNext()) {
                    return false;
                }
                pointer = (EdgeImpl) itr.next();
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            return pointer;
        }

        @Override
        public void remove() {
            itr.remove();
        }
    }

    protected class NeighborsIterator implements Iterator<Node> {

        protected final NodeImpl node;
        protected final Iterator<Edge> itr;

        public NeighborsIterator(NodeImpl node, Iterator<Edge> itr) {
            this.node = node;
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Node next() {
            Edge e = itr.next();
            return e.getSource() == node ? e.getTarget() : e.getSource();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for this iterator");
        }
    }
}
