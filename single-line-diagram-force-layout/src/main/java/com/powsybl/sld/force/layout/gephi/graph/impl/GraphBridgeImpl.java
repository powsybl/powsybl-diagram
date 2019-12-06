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
import com.powsybl.sld.force.layout.gephi.graph.api.Configuration;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphFactory;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;
import com.powsybl.sld.force.layout.gephi.graph.api.TextProperties;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeRepresentation;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphBridge;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphBridgeImpl implements GraphBridge {

    private final GraphStore store;

    public GraphBridgeImpl(GraphStore store) {
        this.store = store;
    }

    @Override
    public void copyNodes(Node[] nodes) {
        if (nodes.length == 0) {
            return;
        }
        GraphStore sourceStore = null;

        // Verify nodes and add to id set
        Set<Integer> nodeIds = new IntOpenHashSet();
        for (Node node : nodes) {
            NodeImpl nodeImpl = verifyNode(node);
            sourceStore = nodeImpl.graphStore;
            nodeIds.add(nodeImpl.storeId);
        }

        // Verify compatibility
        verifyCompatibility(sourceStore);

        // Verify edges and add to edges list
        Set<Integer> edgeTypeIds = new IntOpenHashSet();
        List<EdgeImpl> edges = new ArrayList<EdgeImpl>();
        for (Node node : nodes) {
            for (Edge edge : sourceStore.getEdges(node)) {
                Node oppositeNode = sourceStore.getOpposite(node, edge);
                if (store.getNode(oppositeNode.getId()) != null || nodeIds.contains(oppositeNode.getStoreId())) {
                    EdgeImpl edgeImpl = verifyEdge(edge);
                    if (edgeImpl.isSelfLoop() || oppositeNode.getStoreId() > node.getStoreId()) {
                        edges.add(edgeImpl);

                        if (edgeImpl.getType() != EdgeTypeStore.NULL_LABEL) {
                            edgeTypeIds.add(edgeImpl.getType());
                        }
                    }
                }
            }
        }

        // Copy edge labels
        EdgeTypeStore sourceEdgeTypeStore = sourceStore.edgeTypeStore;
        for (Integer edgeId : edgeTypeIds) {
            Object label = sourceEdgeTypeStore.getLabel(edgeId);
            store.edgeTypeStore.addType(label, edgeId);
        }

        // Copy node columns
        TableImpl<Node> nodeTable = store.nodeTable;
        copyColumns(sourceStore.nodeTable, nodeTable);

        // Copy edge columns
        TableImpl<Edge> edgeTable = store.edgeTable;
        copyColumns(sourceStore.edgeTable, edgeTable);

        // Copy nodes
        GraphFactory factory = store.factory;
        for (Node node : nodes) {
            if (store.getNode(node.getId()) == null) {
                Node nodeCopy = factory.newNode(node.getId());

                // Properties
                copyNodeProperties(node, nodeCopy);

                // Text properties
                copyTextProperties(node.getTextProperties(), nodeCopy.getTextProperties());

                // Attributes
                copyAttributes(sourceStore.nodeTable, nodeTable, node, nodeCopy);

                // Add
                store.addNode(nodeCopy);
            }
        }

        // Copy edges
        for (EdgeImpl edge : edges) {
            if (store.getEdge(edge.getId()) == null) {
                Node source = store.getNode(edge.getSource().getId());
                Node target = store.getNode(edge.getTarget().getId());

                Edge edgeCopy = factory.newEdge(edge.getId(), source, target, edge.getType(), 0.0, edge.isDirected());

                // Weight
                copyEdgeWeight(edge, edgeCopy);

                // Properties
                copyEdgeProperties(edge, edgeCopy);

                // Text properties
                copyTextProperties(edge.getTextProperties(), edgeCopy.getTextProperties());

                // Attributes
                copyAttributes(sourceStore.edgeTable, edgeTable, edge, edgeCopy);

                // Add
                store.addEdge(edgeCopy);
            }
        }
    }

    private void copyEdgeWeight(EdgeImpl edge, Edge edgeCopy) {
        if (edge.hasDynamicWeight()) {
            TimeRepresentation tr = edge.graphStore.configuration.getTimeRepresentation();
            if (tr.equals(TimeRepresentation.INTERVAL)) {
                for (Map.Entry<Interval, Double> entry : edge.getWeights()) {
                    edgeCopy.setWeight(entry.getValue(), entry.getKey());
                }
            } else if (tr.equals(TimeRepresentation.TIMESTAMP)) {
                for (Map.Entry<Double, Double> entry : edge.getWeights()) {
                    edgeCopy.setWeight(entry.getValue(), entry.getKey());
                }
            }
        } else {
            edgeCopy.setWeight(edge.getWeight());
        }
    }

    private void copyNodeProperties(Node node, Node nodeCopy) {
        nodeCopy.setPosition(node.x(), node.y(), node.z());
        nodeCopy.setColor(node.getColor());
        nodeCopy.setFixed(node.isFixed());
        nodeCopy.setLabel(node.getLabel());
        nodeCopy.setSize(node.size());
    }

    private void copyEdgeProperties(Edge edge, Edge edgeCopy) {
        edgeCopy.setColor(edge.getColor());
        edgeCopy.setLabel(edge.getLabel());
    }

    private void copyTextProperties(TextProperties text, TextProperties textCopy) {
        textCopy.setColor(text.getColor());
        textCopy.setSize(text.getSize());
        textCopy.setVisible(text.isVisible());
    }

    private void copyColumns(TableImpl sourceTable, TableImpl destTable) {
        for (Column col : sourceTable.toArray()) {
            if (!col.isProperty() && !destTable.hasColumn(col.getId())) {
                destTable.addColumn(col.getId(), col.getTitle(), col.getTypeClass(), col.getOrigin(), col
                        .getDefaultValue(), col.isIndexed());
            }
        }
    }

    private void copyAttributes(TableImpl sourceTable, TableImpl destTable, Element element, Element elementCopy) {
        TimeRepresentation tr = sourceTable.store.configuration.getTimeRepresentation();
        for (Column col : sourceTable.toArray()) {
            if (!col.isProperty()) {
                Column colCopy = destTable.getColumn(col.getId());
                if (col.isDynamic() && tr.equals(TimeRepresentation.TIMESTAMP)) {
                    for (Map.Entry<Double, Object> entry : element.getAttributes(col)) {
                        Double key = entry.getKey();
                        Object value = entry.getValue();
                        elementCopy.setAttribute(colCopy, value, key);
                    }
                } else if (col.isDynamic() && tr.equals(TimeRepresentation.INTERVAL)) {
                    for (Map.Entry<Interval, Object> entry : element.getAttributes(col)) {
                        Interval key = entry.getKey();
                        Object value = entry.getValue();
                        elementCopy.setAttribute(colCopy, value, key);
                    }
                } else {
                    Object attribute = element.getAttribute(col);
                    elementCopy.setAttribute(colCopy, attribute);
                }
            }
        }
    }

    private NodeImpl verifyNode(Node node) {
        NodeImpl nodeImpl = (NodeImpl) node;
        verifyElement(nodeImpl);
        return nodeImpl;
    }

    private EdgeImpl verifyEdge(Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        verifyElement(edgeImpl);
        EdgeImpl existingEdge = store.getEdge(edge.getId());
        if (existingEdge != null && (!existingEdge.getSource().getId().equals(edge.getSource().getId()) || !existingEdge
                .getTarget().getId().equals(edge.getTarget().getId()))) {
            throw new RuntimeException("An edge with a similar id '" + edge.getId() + "' already exists");
        }

        return edgeImpl;
    }

    private void verifyElement(AbstractElementImpl elementImpl) {
        if (elementImpl.getStoreId() < 0) {
            throw new RuntimeException("The element '" + elementImpl.getId() + "' doesn't belong to any store");
        }
    }

    private void verifyCompatibility(GraphStore sourceStore) {
        // Verify configuration
        Configuration destConfig = store.configuration;
        Configuration sourceConfig = sourceStore.configuration;
        if (!destConfig.equals(sourceConfig)) {
            throw new RuntimeException("The configurations don't match");
        }

        // Verify node table
        TableImpl<Node> destNodeTable = store.nodeTable;
        for (Column sourceCol : sourceStore.nodeTable) {
            if (!sourceCol.isProperty()) {
                Column destColumn = destNodeTable.getColumn(sourceCol.getId());
                if (destColumn != null && !destColumn.getTypeClass().equals(sourceCol.getTypeClass())) {
                    throw new RuntimeException(
                            "A node column '" + destColumn.getId() + "' already exists with a different type");

                }
            }
        }

        // Verify edge table
        TableImpl<Edge> destEdgeTable = store.edgeTable;
        for (Column sourceCol : sourceStore.edgeTable) {
            if (!sourceCol.isProperty()) {
                Column destColumn = destEdgeTable.getColumn(sourceCol.getId());
                if (destColumn != null && !destColumn.getTypeClass().equals(sourceCol.getTypeClass())) {
                    throw new RuntimeException(
                            "An edge column '" + destColumn.getId() + "' already exists with a different type");

                }
            }
        }
    }
}
