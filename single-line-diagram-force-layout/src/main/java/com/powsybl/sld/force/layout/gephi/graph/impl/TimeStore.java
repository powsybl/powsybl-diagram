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

import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeRepresentation;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;

public class TimeStore {

    protected final GraphStore graphStore;
    // Lock (optional
    protected final GraphLock lock;
    // Store
    protected AbstractTimeIndexStore nodeIndexStore;
    protected AbstractTimeIndexStore edgeIndexStore;

    public TimeStore(GraphStore store, GraphLock graphLock, boolean indexed) {
        lock = graphLock;
        graphStore = store;

        TimeRepresentation timeRepresentation = GraphStoreConfiguration.DEFAULT_TIME_REPRESENTATION;
        if (store != null) {
            timeRepresentation = store.configuration.getTimeRepresentation();
        }
        if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
            nodeIndexStore = new IntervalIndexStore<Node>(Node.class, lock, indexed);
            edgeIndexStore = new IntervalIndexStore<Edge>(Edge.class, lock, indexed);
        } else {
            nodeIndexStore = new TimestampIndexStore<Node>(Node.class, lock, indexed);
            edgeIndexStore = new TimestampIndexStore<Edge>(Edge.class, lock, indexed);
        }
    }

    protected void resetConfiguration() {
        if (graphStore != null) {
            if (graphStore.configuration.getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
                nodeIndexStore = new IntervalIndexStore<Node>(Node.class, lock, nodeIndexStore.hasIndex());
                edgeIndexStore = new IntervalIndexStore<Edge>(Edge.class, lock, edgeIndexStore.hasIndex());
            } else {
                nodeIndexStore = new TimestampIndexStore<Node>(Node.class, lock, nodeIndexStore.hasIndex());
                edgeIndexStore = new TimestampIndexStore<Edge>(Edge.class, lock, edgeIndexStore.hasIndex());
            }
        }
    }

    public double getMin(Graph graph) {
        if (nodeIndexStore == null || edgeIndexStore == null) {
            // TODO: Manual calculation
            return Double.NEGATIVE_INFINITY;
        }
        double nodeMin = nodeIndexStore.getIndex(graph).getMinTimestamp();
        double edgeMin = edgeIndexStore.getIndex(graph).getMinTimestamp();
        if (Double.isInfinite(nodeMin)) {
            return edgeMin;
        }
        if (Double.isInfinite(edgeMin)) {
            return nodeMin;
        }
        return Math.min(nodeMin, edgeMin);
    }

    public double getMax(Graph graph) {
        if (nodeIndexStore == null || edgeIndexStore == null) {
            // TODO: Manual calculation
            return Double.POSITIVE_INFINITY;
        }
        double nodeMax = nodeIndexStore.getIndex(graph).getMaxTimestamp();
        double edgeMax = edgeIndexStore.getIndex(graph).getMaxTimestamp();
        if (Double.isInfinite(nodeMax)) {
            return edgeMax;
        }
        if (Double.isInfinite(edgeMax)) {
            return nodeMax;
        }
        return Math.max(nodeMax, edgeMax);
    }

    public boolean isEmpty() {
        return nodeIndexStore.size() == 0 && edgeIndexStore.size() == 0;
    }

    public void clear() {
        nodeIndexStore.clear();
        edgeIndexStore.clear();
    }

    public void clearEdges() {
        edgeIndexStore.clear();
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 79 * hash + (this.nodeIndexStore != null ? this.nodeIndexStore.deepHashCode() : 0);
        hash = 79 * hash + (this.edgeIndexStore != null ? this.edgeIndexStore.deepHashCode() : 0);
        return hash;
    }

    public boolean deepEquals(TimeStore obj) {
        if (obj == null) {
            return false;
        }
        if (this.nodeIndexStore != obj.nodeIndexStore && (this.nodeIndexStore == null || !this.nodeIndexStore
                .deepEquals(obj.nodeIndexStore))) {
            return false;
        }
        if (this.edgeIndexStore != obj.edgeIndexStore && (this.edgeIndexStore == null || !this.edgeIndexStore
                .deepEquals(obj.edgeIndexStore))) {
            return false;
        }
        return true;
    }
}
