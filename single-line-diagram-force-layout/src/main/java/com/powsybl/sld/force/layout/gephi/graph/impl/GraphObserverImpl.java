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
import com.powsybl.sld.force.layout.gephi.graph.api.GraphDiff;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphObserver;
import com.powsybl.sld.force.layout.gephi.graph.api.NodeIterable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.util.Collections;

public class GraphObserverImpl implements GraphObserver {

    // Store and graph
    protected final GraphStore graphStore;
    protected final Graph graph;
    protected final GraphVersion graphVersion;
    // Config
    protected final boolean withDiff;
    // Version
    protected int nodeVersion = Integer.MIN_VALUE;
    protected int edgeVersion = Integer.MIN_VALUE;
    protected boolean destroyed;
    protected boolean newObserver = true;
    // Cache
    protected GraphDiffImpl graphDiff;
    protected NodeImpl[] nodeCache;
    protected EdgeImpl[] edgeCache;

    public GraphObserverImpl(GraphStore store, GraphVersion graphVersion, Graph graph, boolean withDiff) {
        this.graphStore = store;
        this.graphVersion = graphVersion;
        this.graph = graph;
        this.withDiff = withDiff;
        if (withDiff) {
            readLock();
            initCache();
            readUnlock();
        }
        this.nodeVersion = graphVersion.nodeVersion;
        this.edgeVersion = graphVersion.edgeVersion;
    }

    @Override
    public boolean hasGraphChanged() {
        if (!destroyed) {
            readLock();
            try {
                if (nodeVersion < graphVersion.nodeVersion || edgeVersion < graphVersion.edgeVersion) {
                    if (withDiff) {
                        refreshDiff();
                    }
                    nodeVersion = graphVersion.nodeVersion;
                    edgeVersion = graphVersion.edgeVersion;
                    return true;
                }
            } finally {
                readUnlock();
                newObserver = false;
            }
        }
        return false;
    }

    @Override
    public GraphDiff getDiff() {
        if (!withDiff) {
            throw new RuntimeException("This observer doesn't compute diffs, set diff setting to true");
        }
        if (graphDiff == null) {
            throw new IllegalStateException(
                    "The hasGraphChanged() method should be called first and getDiff() only once then");
        }
        GraphDiff diff = graphDiff;
        graphDiff = null;
        return diff;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    private void initCache() {
        int maxNodeStoreId = graphStore.nodeStore.maxStoreId();
        nodeCache = new NodeImpl[maxNodeStoreId];
        for (Node n : graph.getNodes()) {
            NodeImpl nImpl = (NodeImpl) n;
            nodeCache[nImpl.storeId] = nImpl;
        }
        int maxEdgeStoreId = graphStore.edgeStore.maxStoreId();
        edgeCache = new EdgeImpl[maxEdgeStoreId];
        for (Edge e : graph.getEdges()) {
            EdgeImpl eImpl = (EdgeImpl) e;
            edgeCache[eImpl.storeId] = eImpl;
        }
    }

    protected void refreshDiff() {
        graphDiff = new GraphDiffImpl();

        if (nodeVersion < graphVersion.nodeVersion) {
            int maxStoreId = graphStore.nodeStore.maxStoreId();

            for (Node n : nodeCache) {
                NodeImpl nImpl = (NodeImpl) n;
                if (nImpl != null && !graph.contains(nImpl)) {
                    graphDiff.removedNodes.add(nImpl);
                }
            }
            if (maxStoreId > nodeCache.length || maxStoreId < nodeCache.length) {
                NodeImpl[] newCache = new NodeImpl[maxStoreId];
                System.arraycopy(nodeCache, 0, newCache, 0, maxStoreId > nodeCache.length ? nodeCache.length
                        : maxStoreId);
                nodeCache = newCache;
            }
            for (Node n : graph.getNodes()) {
                NodeImpl nImpl = (NodeImpl) n;
                int storeId = nImpl.storeId;
                NodeImpl cachedNode = nodeCache[storeId];
                if (cachedNode == null || cachedNode != nImpl) {
                    graphDiff.addedNodes.add(nImpl);
                    nodeCache[storeId] = nImpl;
                }
            }
        }

        if (edgeVersion < graphVersion.edgeVersion) {
            int maxStoreId = graphStore.edgeStore.maxStoreId();

            for (Edge e : edgeCache) {
                EdgeImpl eImpl = (EdgeImpl) e;
                if (eImpl != null && !graph.contains(eImpl)) {
                    graphDiff.removedEdges.add(eImpl);
                }
            }
            if (maxStoreId > edgeCache.length || maxStoreId < edgeCache.length) {
                EdgeImpl[] newCache = new EdgeImpl[maxStoreId];
                System.arraycopy(edgeCache, 0, newCache, 0, maxStoreId > edgeCache.length ? edgeCache.length
                        : maxStoreId);
                edgeCache = newCache;
            }
            for (Edge e : graph.getEdges()) {
                EdgeImpl eImpl = (EdgeImpl) e;
                int storeId = eImpl.storeId;
                EdgeImpl cachedEdge = edgeCache[storeId];
                if (cachedEdge == null || cachedEdge != eImpl) {
                    graphDiff.addedEdges.add(eImpl);
                    edgeCache[storeId] = eImpl;
                }

            }
        }

    }

    protected void resetNodeVersion() {
        nodeVersion = Integer.MIN_VALUE;
    }

    protected void resetEdgeVersion() {
        edgeVersion = Integer.MIN_VALUE;
    }

    protected final class GraphDiffImpl implements GraphDiff {

        protected final ObjectList<Node> addedNodes;
        protected final ObjectList<Node> removedNodes;
        protected final ObjectList<Edge> addedEdges;
        protected final ObjectList<Edge> removedEdges;

        public GraphDiffImpl() {
            addedNodes = new ObjectArrayList<Node>();
            removedNodes = new ObjectArrayList<Node>();
            addedEdges = new ObjectArrayList<Edge>();
            removedEdges = new ObjectArrayList<Edge>();
        }

        @Override
        public NodeIterable getAddedNodes() {
            if (!addedNodes.isEmpty()) {
                return graphStore.getNodeIterableWrapper(Collections.unmodifiableList(addedNodes).iterator(), false);
            }
            return NodeIterable.EMPTY;
        }

        @Override
        public NodeIterable getRemovedNodes() {
            if (!removedNodes.isEmpty()) {
                return graphStore.getNodeIterableWrapper(Collections.unmodifiableList(removedNodes).iterator(), false);
            }
            return NodeIterable.EMPTY;
        }

        @Override
        public EdgeIterable getAddedEdges() {
            if (!addedEdges.isEmpty()) {
                return graphStore.getEdgeIterableWrapper(Collections.unmodifiableList(addedEdges).iterator(), false);
            }
            return EdgeIterable.EMPTY;
        }

        @Override
        public EdgeIterable getRemovedEdges() {
            if (!removedEdges.isEmpty()) {
                return graphStore.getEdgeIterableWrapper(Collections.unmodifiableList(removedEdges).iterator(), false);
            }
            return EdgeIterable.EMPTY;
        }
    }

    @Override
    public void destroy() {
        checkNotDestroyed();

        graphStore.graphModel.destroyGraphObserver(this);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public boolean isNew() {
        return !destroyed && newObserver;
    }

    public void destroyObserver() {
        checkNotDestroyed();

        nodeCache = null;
        edgeCache = null;
        destroyed = true;
    }

    private void checkNotDestroyed() {
        if (destroyed) {
            throw new RuntimeException("This observer has already been destroyed");
        }
    }

    private void readLock() {
        graphStore.autoReadLock();
    }

    private void readUnlock() {
        graphStore.autoReadUnlock();
    }
}
