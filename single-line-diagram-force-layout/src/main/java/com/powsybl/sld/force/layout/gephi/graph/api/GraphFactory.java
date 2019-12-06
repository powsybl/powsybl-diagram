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
 * Factory for nodes and edges.
 * <p>
 * All new nodes and edges are created by this factory.
 * <p>
 * Both nodes and edges have unique identifiers. If not provided, a unique id
 * will be automatically assigned to the elements.
 */
public interface GraphFactory {

    /**
     * Creates and returns a directed edge between source and target.
     *
     * @param source the source node
     * @param target the target node
     * @return the new edge
     */
    public Edge newEdge(Node source, Node target);

    /**
     * Creates and returns an edge between source and target.
     *
     * @param source the source node
     * @param target the target node
     * @param directed true if directed, false if undirected
     * @return the new edge
     */
    public Edge newEdge(Node source, Node target, boolean directed);

    /**
     * Creates and returns an edge between source and target.
     *
     * @param source the source node
     * @param target the target node
     * @param type the edge type
     * @param directed true if directed, false if undirected
     * @return the new edge
     */
    public Edge newEdge(Node source, Node target, int type, boolean directed);

    /**
     * Creates and returns an edge between source and target.
     *
     * @param source the source node
     * @param target the target node
     * @param type the edge type
     * @param weight the edge weight
     * @param directed true if directed, false if undirected
     * @return the new edge
     */
    public Edge newEdge(Node source, Node target, int type, double weight, boolean directed);

    /**
     * Creates and returns an edge between source and target.
     *
     * @param id the edge id
     * @param source the source node
     * @param target the target node
     * @param type the edge type
     * @param weight the edge weight
     * @param directed true if directed, false if undirected
     * @return the new edge
     */
    public Edge newEdge(Object id, Node source, Node target, int type, double weight, boolean directed);

    /**
     * Creates and returns a node.
     *
     * @return the new node
     */
    public Node newNode();

    /**
     * Creates and returns a node.
     *
     * @param id the node id
     * @return the new node
     */
    public Node newNode(Object id);
}
