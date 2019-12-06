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

/**
 * Directed graph.
 * <p>
 * This interface has additional methods specific to directed graphs compared to
 * the <em>Graph</em> interface it inherits from.
 */
public interface DirectedGraph extends Graph {

    /**
     * Gets the edge adjacent to source and target.
     *
     * @param source the source node
     * @param target the target node
     * @return the adjacent edge, or null if not found
     */
    @Override
    public Edge getEdge(Node source, Node target);

    /**
     * Gets the edge adjacent to source and target with an edge of the given
     * type.
     *
     * @param source the source node
     * @param target the target node
     * @param type the edge type
     * @return the adjacent edge, or null if not found
     */
    @Override
    public Edge getEdge(Node source, Node target, int type);

    /**
     * Returns true if source and target are adjacent.
     *
     * @param source the source node
     * @param target the target node
     * @return true of adjacent, false otherwise
     */
    @Override
    public boolean isAdjacent(Node source, Node target);

    /**
     * Returns true if source and target are adjacent with an edge of the given
     * type.
     *
     * @param source the source node
     * @param target the target node
     * @param type the edge type
     * @return true if adjacent, false otherwise
     */
    @Override
    public boolean isAdjacent(Node source, Node target, int type);

    /**
     * Gets the node's predecessors.
     * <p>
     * A node predecessor is a node connected by an incoming edge.
     *
     * @param node the node to get predecessors
     * @return an iterable on <em>node</em>'s predecessors
     */
    public NodeIterable getPredecessors(Node node);

    /**
     * Gets the node's predecessors through a specific edge type.
     * <p>
     * A node predecessor is a node connected by an incoming edge.
     *
     * @param node the node to get predecessors
     * @param type the edge type
     * @return an iterable on <em>node</em>'s predecessors
     */
    public NodeIterable getPredecessors(Node node, int type);

    /**
     * Gets the node's successors.
     * <p>
     * A node successor is a node connected by an outgoing edge.
     *
     * @param node the node to get successors
     * @return an iterable on <em>node</em>'s successors
     */
    public NodeIterable getSuccessors(Node node);

    /**
     * Gets the node's successors through a specific edge type.
     * <p>
     * A node successor is a node connected by an outgoing edge.
     *
     * @param node the node to get successors
     * @param type the edge type
     * @return an iterable on <em>node</em>'s successors
     */
    public NodeIterable getSuccessors(Node node, int type);

    /**
     * Gets the node's incoming edges.
     *
     * @param node the node to get incoming edges
     * @return an iterable on <em>node</em>'s incoming edges
     */
    public EdgeIterable getInEdges(Node node);

    /**
     * Gets the node's incoming edges for the given type.
     *
     * @param node the node to get incoming edges
     * @param type the edge type
     * @return an iterable on <em>node</em>'s incoming edges
     */
    public EdgeIterable getInEdges(Node node, int type);

    /**
     * Gets the node's outgoing edges.
     *
     * @param node the node to get outgoing edges
     * @return an iterable on <em>node</em>'s outgoing edges
     */
    public EdgeIterable getOutEdges(Node node);

    /**
     * Gets the node's incoming edges for the given type.
     *
     * @param node the node to get incoming edges
     * @param type the edge type
     * @return an iterable on <em>node</em>'s incoming edges
     */
    public EdgeIterable getOutEdges(Node node, int type);

    /**
     * Gets the edge in the other direction of the given edge.
     * <p>
     * This takes in account the edge type so only edges of the same type can be
     * mutual.
     *
     * @param edge the edge to get the mutual edge
     * @return the mutual edge, or null
     */
    public Edge getMutualEdge(Edge edge);

    /**
     * Gets the edge's in-degree.
     *
     * @param node the node to get the in-degree from
     * @return the in-degree number
     */
    public int getInDegree(Node node);

    /**
     * Gets the edge's out-degree.
     *
     * @param node the node to get the out-degree from
     * @return the out-degree number
     */
    public int getOutDegree(Node node);
}
