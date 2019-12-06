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
import java.util.Set;

/**
 * Graph interface.
 */
public interface Graph {

    /**
     * Adds an edge to this graph.
     *
     * @param edge the edge to add
     * @return true if the edge has been added, false if it already exists
     */
    public boolean addEdge(Edge edge);

    /**
     * Adds a node to this graph.
     *
     * @param node the node to add
     * @return true if the node has been added, false if it already exists
     */
    public boolean addNode(Node node);

    /**
     * Adds all edges in the collection to this graph.
     *
     * @param edges the edge collection
     * @return true if at least one edge has been added, false otherwise
     */
    public boolean addAllEdges(Collection<? extends Edge> edges);

    /**
     * Adds all nodes in the collection to this graph.
     *
     * @param nodes the node collection
     * @return true if at least one node has been added, false otherwise
     */
    public boolean addAllNodes(Collection<? extends Node> nodes);

    /**
     * Removes an edge from this graph.
     *
     * @param edge the edge to remove
     * @return true if the edge was removed, false if it didn't exist
     */
    public boolean removeEdge(Edge edge);

    /**
     * Removes a node from this graph.
     * <p>
     * All edges attached to the node will be removed as well.
     *
     * @param node the node to remove
     * @return true if the node was removed, false if it didn't exist
     */
    public boolean removeNode(Node node);

    /**
     * Removes all edges in the collection from this graph.
     *
     * @param edges the edge collection
     * @return true if at least one edge has been removed, false otherwise
     */
    public boolean removeAllEdges(Collection<? extends Edge> edges);

    /**
     * Removes all nodes in the collection from this graph.
     *
     * @param nodes the node collection
     * @return true if at least one node has been removed, false otherwise
     */
    public boolean removeAllNodes(Collection<? extends Node> nodes);

    /**
     * Returns true if <em>node</em> is contained in this graph.
     *
     * @param node the node to test
     * @return true if this graph contains <em>node</em>, false otherwise
     */
    public boolean contains(Node node);

    /**
     * Returns true if <em>edge</em> is contained in this graph.
     *
     * @param edge the edge to test
     * @return true if this graph contains <em>edge</em>, false otherwise
     */
    public boolean contains(Edge edge);

    /**
     * Gets a node given its identifier.
     *
     * @param id the node id
     * @return the node, or null if not found
     */
    public Node getNode(Object id);

    /**
     * Returns true if a node with <em>id</em> as identifier exists.
     *
     * @param id node id
     * @return true if a node exists, false otherwise
     */
    public boolean hasNode(Object id);

    /**
     * Gets an edge by its identifier.
     *
     * @param id the edge id
     * @return the edge, or null if not found
     */
    public Edge getEdge(Object id);

    /**
     * Returns true if an edge with <em>id</em> as identifier exists.
     *
     * @param id edge id
     * @return true if an edge exists, false otherwise
     */
    public boolean hasEdge(Object id);

    /**
     * Gets the edge adjacent to node1 and node2.
     * <p>
     * If multiple parallel edges exist it returns the first edge.
     *
     * @param node1 first node
     * @param node2 second node
     * @return adjacent edge, or null if not found
     */
    public Edge getEdge(Node node1, Node node2);

    /**
     * Get the edges adjacent to node1 and node2.
     * <p>
     * If there aren't any parallel edges only one edge will be returned.
     *
     * @param node1 first node
     * @param node2 second node
     * @return adjacent edges
     */
    public EdgeIterable getEdges(Node node1, Node node2);

    /**
     * Gets the edge adjacent to node1 and node2 and from the given type.
     * <p>
     * If multiple parallel edges exist it returns the first edge.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @param type the edge type
     * @return the adjacent edge, or null if not found
     */
    public Edge getEdge(Node node1, Node node2, int type);

    /**
     * Gets the edges adjacent to node1 and node 2 and from the given type.
     * <p>
     * If there aren't any parallel edges only one edge will be returned.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @param type the edge type
     * @return the adjacent edges, or an empty iterator if not found
     */
    public EdgeIterable getEdges(Node node1, Node node2, int type);

    /**
     * Gets all the nodes in the graph.
     *
     * @return a node iterable over all nodes
     */
    public NodeIterable getNodes();

    /**
     * Gets all the edges in the graph.
     *
     * @return an edge iterable over all edges
     */
    public EdgeIterable getEdges();

    /**
     * Gets all the self-loop edges in the graph.
     *
     * @return an edge iterable over all self-loops
     */
    public EdgeIterable getSelfLoops();

    /**
     * Gets all neighbors of a given node.
     *
     * @param node the node to get neighbors
     * @return a node iterable over the neighbors
     */
    public NodeIterable getNeighbors(Node node);

    /**
     * Gets all neighbors of a given node connected through the given edge type.
     *
     * @param node the node to get neighbors
     * @param type the edge type
     * @return a node iterable over the neigbors
     */
    public NodeIterable getNeighbors(Node node, int type);

    /**
     * Gets all edges incident to a given node.
     *
     * @param node the node to get edges from
     * @return an edge iterable of all edges connected to the node
     */
    public EdgeIterable getEdges(Node node);

    /**
     * Gets all edges incident to a given node with the given edge type.
     *
     * @param node the node to get edges from
     * @param type the edge type
     * @return an edge iterable of the edges connected to the node
     */
    public EdgeIterable getEdges(Node node, int type);

    /**
     * Gets the number of nodes in the graph.
     *
     * @return the node count
     */
    public int getNodeCount();

    /**
     * Gets the number of edges in the graph.
     *
     * @return the edge count
     */
    public int getEdgeCount();

    /**
     * Gets the number of edges of the given type in the graph.
     *
     * @param type the edge type
     * @return the edge count for the given type
     */
    public int getEdgeCount(int type);

    /**
     * Gets the node at the opposite end of the given edge.
     *
     * @param node the node to get the opposite
     * @param edge the edge connected to both nodes
     * @return the opposite node
     */
    public Node getOpposite(Node node, Edge edge);

    /**
     * Gets the node degree.
     *
     * @param node the node
     * @return the degree
     */
    public int getDegree(Node node);

    /**
     * Returns true if the given edge is a self-loop.
     *
     * @param edge the edge to test
     * @return true if self-loop, false otherwise
     */
    public boolean isSelfLoop(Edge edge);

    /**
     * Returns true if the given edge is directed.
     *
     * @param edge the edge to test
     * @return true if directed, false otherwise
     */
    public boolean isDirected(Edge edge);

    /**
     * Returns true if node1 and node2 are adjacent.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return true if node1 is adjacent to node2, false otherwise
     */
    public boolean isAdjacent(Node node1, Node node2);

    /**
     * Returns true if node1 and node2 are adjacent with an edge of the given
     * type.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @param type the edge type
     * @return true if node1 and node2 are adjacent with an edge og the given
     *         type, false otherwise
     */
    public boolean isAdjacent(Node node1, Node node2, int type);

    /**
     * Returns true if edge1 and edge2 are incident.
     *
     * @param edge1 the first edge
     * @param edge2 the second edge
     * @return true if edge1 is incident to edge2, false otherwise
     */
    public boolean isIncident(Edge edge1, Edge edge2);

    /**
     * Returns true if the node and the edge are incident.
     *
     * @param node the node
     * @param edge the edge
     * @return true if the node and edge are incident, false otherwise
     */
    public boolean isIncident(Node node, Edge edge);

    /**
     * Clears the edges incident to the given node.
     *
     * @param node the node to clear edges from
     */
    public void clearEdges(Node node);

    /**
     * Clears the edges of the given type incident to the given node.
     *
     * @param node the node to clear edges from
     * @param type the edge type
     */
    public void clearEdges(Node node, int type);

    /**
     * Clears all edges and all nodes in the graph
     */
    public void clear();

    /**
     * Clears all edges in the graph
     */
    public void clearEdges();

    /**
     * Gets the graph view associated to this graph.
     *
     * @return the graph view
     */
    public GraphView getView();

    /**
     * Gets the attribute value for the given key.
     *
     * @param key the key
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String key);

    /**
     * Gets the attribute for the given key and timestamp
     *
     * @param key the key
     * @param timestamp the timestamp
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String key, double timestamp);

    /**
     * Gets the attribute for the given key and interval
     *
     * @param key the key
     * @param interval the interval
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String key, Interval interval);

    /**
     * Sets the attribute value for the given key.
     *
     * @param key the key
     * @param value the value
     */
    public void setAttribute(String key, Object value);

    /**
     * Removes the attribute for the given key.
     *
     * @param key key
     */
    public void removeAttribute(String key);

    /**
     * Sets the attribute value for the given key and timestamp.
     *
     * @param key the key
     * @param value the value
     * @param timestamp the timestamp
     */
    public void setAttribute(String key, Object value, double timestamp);

    /**
     * Sets the attribute value for the given key and interval.
     *
     * @param key the key
     * @param value the value
     * @param interval the interval
     */
    public void setAttribute(String key, Object value, Interval interval);

    /**
     * Removes the attribute for the given key and timestamp.
     *
     * @param key key
     * @param timestamp timestamp
     */
    public void removeAttribute(String key, double timestamp);

    /**
     * Removes the attribute for the given key and interval.
     *
     * @param key key
     * @param interval interval
     */
    public void removeAttribute(String key, Interval interval);

    /**
     * Gets all attribute keys.
     *
     * @return a set of all attribute keys
     */
    public Set<String> getAttributeKeys();

    /**
     * Returns the model this graph belongs to.
     *
     * @return graph model
     */
    public GraphModel getModel();

    /**
     * Returns true if this graph is directed.
     *
     * @return true if directed, false otherwise
     */
    public boolean isDirected();

    /**
     * Returns true if this graph is undirected.
     *
     * @return true if undirected, false otherwise
     */
    public boolean isUndirected();

    /**
     * Returns true if this graph is mixed (both directed and undirected edges).
     *
     * @return true if mixed, false otherwise
     */
    public boolean isMixed();

    /**
     * Opens a read lock for the current thread.
     */
    public void readLock();

    /**
     * Closes a read lock for the current thread.
     */
    public void readUnlock();

    /**
     * Closes all read locks for the current thread.
     */
    public void readUnlockAll();

    /**
     * Opens a write lock for the current thread.
     */
    public void writeLock();

    /**
     * Closes a write lock for the current thread.
     */
    public void writeUnlock();
}
