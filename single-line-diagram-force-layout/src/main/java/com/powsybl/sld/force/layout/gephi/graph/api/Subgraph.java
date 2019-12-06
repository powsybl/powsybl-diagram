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
package com.powsybl.sld.force.layout.gephi.graph.api;

import java.util.Collection;

/**
 * A subgraph is a subset of a graph based on a graph view.
 * <p>
 * A subgraph has the same or less elements compared to the graph it's based on.
 * This interface inherits from <em>Graph</em> and all read operations behave in
 * a similar fashion. For instance, calling <em>getNodes</em> will return only
 * nodes in this subgraph. However, write operations such as <em>addNode</em> or
 * <em>removeNode</em> are used to control which elements are part of the view.
 *
 */
public interface Subgraph extends Graph {

    /**
     * Gets the view associated with this subgraph.
     *
     * @return the graph view
     */
    @Override
    public GraphView getView();

    /**
     * Return the root graph this subgraph is based on.
     *
     * @return the root graph
     */
    public Graph getRootGraph();

    /**
     * Adds a node to this subgraph.
     * <p>
     * The node should be part of the root graph.
     *
     * @param node the node to add
     * @return true if the node has been added, false otherwise
     */
    @Override
    public boolean addNode(Node node);

    /**
     * Adds a collection of nodes to this subgraph.
     * <p>
     * The nodes should be part of the root graph.
     *
     * @param nodes the nodes to add
     * @return true if at least a node has been added, false otherwise
     */
    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes);

    /**
     * Adds an edge to this subgraph.
     * <p>
     * The edge should be part of the root graph.
     *
     * @param edge the edge to add
     * @return true if the edge has been added, false otherwise
     */
    @Override
    public boolean addEdge(Edge edge);

    /**
     * Adds a collection of edges to this subgraph.
     * <p>
     * The edges should be part of the root graph.
     *
     * @param edges the edges to add
     * @return true if at least an edge has been added, false otherwise
     */
    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges);

    /**
     * Removes a node from this subgraph.
     * <p>
     * The node should be part of the root graph.
     *
     * @param node the node to remove
     * @return true if removed, false otherwise
     */
    @Override
    public boolean removeNode(Node node);

    /**
     * Removes a collection of nodes from this subgraph.
     * <p>
     * The nodes should be part of the root graph.
     *
     * @param nodes the nodes to remove
     * @return true if at least a node is removed, false otherwise
     */
    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes);

    /**
     * Removes an edge from this subgraph.
     * <p>
     * The edge should be part of the root graph.
     *
     * @param edge the edge to remove
     * @return true if removed, false otherwise
     */
    @Override
    public boolean removeEdge(Edge edge);

    /**
     * Removes a collection of edges from this subgraph.
     * <p>
     * The edges should be part of the root graph.
     *
     * @param edges the edges to remove
     * @return true if at least an edge has been removed, false otherwise
     */
    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges);

    /**
     * Fills the subgraph so all elements in the graph are in the subgraph.
     */
    public void fill();

    /**
     * Unions the given subgraph with this sugbgraph.
     * <p>
     * The given subgraph remains unchanged.
     *
     * @param subGraph the subgraph to do the union with
     */
    public void union(Subgraph subGraph);

    /**
     * Intersects the given subgraph with this sugbgraph.
     * <p>
     * The given subgraph remains unchanged.
     *
     * @param subGraph the subgraph to do the intersection with
     */
    public void intersection(Subgraph subGraph);

    /**
     * Inverse this subgraph so all elements in the graph are removed and all
     * elements not in the graph are added.
     */
    public void not();
}
