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
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphFactory;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.util.concurrent.atomic.AtomicInteger;

public class GraphFactoryImpl implements GraphFactory {

    protected enum AssignConfiguration {
        STRING, INTEGER, DISABLED
    }

    protected final AtomicInteger nodeIds = new AtomicInteger();
    protected final AtomicInteger edgeIds = new AtomicInteger();
    // Config
    protected AssignConfiguration nodeAssignConfiguration;
    protected AssignConfiguration edgeAssignConfiguration;
    // Store
    protected final GraphStore store;

    public GraphFactoryImpl(GraphStore store) {
        this.store = store;
        this.nodeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getNodeIdType()));
        this.edgeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getEdgeIdType()));
    }

    @Override
    public Edge newEdge(Node source, Node target) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, EdgeTypeStore.NULL_LABEL,
                GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT, true);
    }

    @Override
    public Edge newEdge(Node source, Node target, boolean directed) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, EdgeTypeStore.NULL_LABEL,
                GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT, directed);
    }

    @Override
    public Edge newEdge(Node source, Node target, int type, boolean directed) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, type,
                GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT, directed);
    }

    @Override
    public Edge newEdge(Node source, Node target, int type, double weight, boolean directed) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, type, weight, directed);
    }

    @Override
    public Edge newEdge(Object id, Node source, Node target, int type, double weight, boolean directed) {
        EdgeImpl res = new EdgeImpl(id, store, (NodeImpl) source, (NodeImpl) target, type, weight, directed);
        switch (edgeAssignConfiguration) {
            case INTEGER:
                Integer idInt = (Integer) id;
                if (idInt >= edgeIds.get()) {
                    edgeIds.set(idInt + 1);
                }
                break;
            case STRING:
                String idStr = (String) id;
                if (isNumeric(idStr)) {
                    Integer idStrParsed = Integer.parseInt(idStr);
                    if (idStrParsed >= edgeIds.get()) {
                        edgeIds.set(idStrParsed + 1);
                    }
                }
                break;
        }
        return res;
    }

    @Override
    public Node newNode() {
        return new NodeImpl(nextNodeId(), store);
    }

    @Override
    public Node newNode(Object id) {
        NodeImpl res = new NodeImpl(id, store);
        switch (nodeAssignConfiguration) {
            case INTEGER:
                Integer idInt = (Integer) id;
                if (idInt >= nodeIds.get()) {
                    nodeIds.set(idInt + 1);
                }
                break;
            case STRING:
                String idStr = (String) id;
                if (isNumeric(idStr)) {
                    Integer idStrParsed = Integer.parseInt(idStr);
                    if (idStrParsed >= nodeIds.get()) {
                        nodeIds.set(idStrParsed + 1);
                    }
                }
                break;
        }
        return res;
    }

    private Object nextNodeId() {
        switch (nodeAssignConfiguration) {
            case INTEGER:
                return nodeIds.getAndIncrement();
            case STRING:
                return String.valueOf(nodeIds.getAndIncrement());
            case DISABLED:
            default:
                throw new UnsupportedOperationException(
                        "Automatic node ids assignement isn't available for this type: '" + store.configuration
                                .getNodeIdType().getName() + "'");
        }
    }

    private Object nextEdgeId() {
        switch (edgeAssignConfiguration) {
            case INTEGER:
                return edgeIds.getAndIncrement();
            case STRING:
                return String.valueOf(edgeIds.getAndIncrement());
            case DISABLED:
            default:
                throw new UnsupportedOperationException(
                        "Automatic edge ids assignement isn't available for this type: '" + store.configuration
                                .getEdgeIdType().getName() + "'");
        }
    }

    protected int getNodeCounter() {
        return nodeIds.get();
    }

    protected int getEdgeCounter() {
        return edgeIds.get();
    }

    protected void setNodeCounter(int count) {
        nodeIds.set(count);
    }

    protected void setEdgeCounter(int count) {
        edgeIds.set(count);
    }

    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        char[] data = str.toCharArray();
        if (data.length <= 0 || data.length > 9) {
            return false;
        }
        int index = 0;
        if (data[0] == '-' && data.length > 1) {
            index = 1;
        }
        for (; index < data.length; index++) {
            if (data[index] < '0' || data[index] > '9') {
                return false;
            }
        }
        return true;
    }

    public int deepHashCode() {
        int hash = 3;
        Integer node = this.nodeIds.get();
        Integer edge = this.edgeIds.get();
        hash = 59 * hash + node.hashCode();
        hash = 59 * hash + edge.hashCode();
        return hash;
    }

    public boolean deepEquals(GraphFactoryImpl obj) {
        if (obj == null) {
            return false;
        }
        Integer node = this.nodeIds.get();
        Integer edge = this.edgeIds.get();
        Integer otherNode = obj.nodeIds.get();
        Integer otherEdge = obj.edgeIds.get();
        if (this.nodeIds != obj.nodeIds && (!node.equals(otherNode))) {
            return false;
        }
        if (this.edgeIds != obj.edgeIds && (!edge.equals(otherEdge))) {
            return false;
        }
        return true;
    }

    public void resetConfiguration() {
        this.nodeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getNodeIdType()));
        this.edgeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getEdgeIdType()));
    }

    protected final AssignConfiguration getAssignConfiguration(Class type) {
        if (type.equals(Integer.class)) {
            return AssignConfiguration.INTEGER;
        } else if (type.equals(String.class)) {
            return AssignConfiguration.STRING;
        } else {
            return AssignConfiguration.DISABLED;
        }
    }
}
