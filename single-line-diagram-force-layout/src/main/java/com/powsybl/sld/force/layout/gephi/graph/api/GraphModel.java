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

import com.powsybl.sld.force.layout.gephi.graph.impl.GraphModelImpl;
import org.joda.time.DateTimeZone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Graph API's entry point.
 * <p>
 * <code>GraphModel</code> is the entry point for this API and provide methods
 * to create, access and modify graphs. It supports the most common graph
 * paradigms and a complete support for graphs over time as well.
 * <ul>
 * <li>Directed: Edges can have a direction. Graphs can be directed, undirected
 * or mixed.
 * <li>Weighted: Edges can have a weight.
 * <li>Self-loops: Nodes can have self-loops.
 * <li>Labelled edges: Edges can have a label.
 * <li>Properties: Each element in the graph can have properties associated to
 * it.
 * </ul>
 * <p>
 * New instances can be obtained via the embedded factory:
 *
 * <pre>
 * GraphModel model = GraphModel.Factory.newInstance();
 * </pre>
 *
 * This API revolves around a set of simple concepts. A <code>GraphModel</code>
 * encapsulate all elements and metadata associated with a graph structure. In
 * other words its a single graph but it also contains configuration, indices,
 * views and other less important services such as observers.
 * <p>
 * Then, <code>GraphModel</code> gives access to the <code>Graph</code>
 * interface, which focuses only on the graph structure and provide methods to
 * add, remove, get and iterate nodes and edges.
 * <p>
 * The <code>Graph</code> contains nodes and edges, which both implement the
 * <code>Element</code> interface. This <code>Element</code> interface gives
 * access to methods that manipulate the attributes associated to nodes and
 * edges.
 * <p>
 * Any number of attributes can be associated to elements but are managed
 * through the <code>Table</code> and <code>Column</code> interfaces. A
 * <code>GraphModel</code> gives access by default to a node and edge table. A
 * <code>Table</code> is simply a list of columns, which each has a unique
 * identifier and a type (e.g. integer). Attribute values can only be associated
 * with elements for existing columns.
 * <p>
 * Attributes are automatically indexed and information such as the number of
 * elements with a particular value can be obtained from the <code>Index</code>
 * interface.
 * <p>
 * Finally, this API supports the concept of graph views. A view is a mask on
 * the graph structure and represents a subgraph. The user controls the set of
 * nodes and edges in the view by obtaining a <code>Subgraph</code> for a
 * specific <code>GraphView</code>. Views can directly be created and destroyed
 * from this model.
 * <p>
 * Elements should be created through the {@link #factory() } method.
 * <p>
 * For performance reasons, edge labels are internally represented as integers
 * and the mapping between arbitrary labels is managed through the
 * {@link #addEdgeType(Object)
 * } and
 * {@link #getEdgeType(Object) } methods. By default, edges have a
 * <code>null</code> label, which is internally represented as zero.
 *
 * @see Graph
 * @see Element
 * @see Table
 * @see Column
 * @see Index
 * @see GraphView
 */
public interface GraphModel {

    /**
     * Utility to create new graph model instances.
     */
    public static class Factory {

        /**
         * Returns a new instance with default configuration.
         *
         * @return new instance
         */
        public static GraphModel newInstance() {
            return new GraphModelImpl();
        }

        /**
         * Returns a new instance with the given configuration.
         *
         * @param config configuration
         * @return new instance
         */
        public static GraphModel newInstance(Configuration config) {
            return new GraphModelImpl(config);
        }
    }

    /**
     * Serialization utility to read/write graph models from/to input/output.
     */
    public static class Serialization {

        /**
         * Read the <code>input</code> and return the read graph model.
         *
         * @param input data input to read from
         * @return new graph model
         * @throws IOException if an io error occurs
         */
        public static GraphModel read(DataInput input) throws IOException {
            try {
                com.powsybl.sld.force.layout.gephi.graph.impl.Serialization s = new com.powsybl.sld.force.layout.gephi.graph.impl.Serialization();
                return s.deserializeGraphModel(input);
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

        /**
         * Read the <code>input</code> and return the read graph model without
         * an explicit version header in the input. To be used with old
         * graphstore serialized data prior to version 0.4 (first, that added
         * the version header).
         *
         * @param input data input to read from
         * @param graphStoreVersion Forced version to use
         * @return new graph model
         * @throws IOException if an io error occurs
         */
        public static GraphModel readWithoutVersionHeader(DataInput input, float graphStoreVersion) throws IOException {
            try {
                com.powsybl.sld.force.layout.gephi.graph.impl.Serialization s = new com.powsybl.sld.force.layout.gephi.graph.impl.Serialization();
                return s.deserializeGraphModelWithoutVersionPrefix(input, graphStoreVersion);
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

        /**
         * Write <code>graphModel</code> to <code>output</code>.
         *
         * @param output data output to write to
         * @param graphModel graph model to write
         * @throws IOException if an io error occurs
         */
        public static void write(DataOutput output, GraphModel graphModel) throws IOException {
            com.powsybl.sld.force.layout.gephi.graph.impl.Serialization s = new com.powsybl.sld.force.layout.gephi.graph.impl.Serialization();
            s.serializeGraphModel(output, (GraphModelImpl) graphModel);
        }
    }

    /**
     * Returns the graph factory.
     *
     * @return graph factory
     */
    public GraphFactory factory();

    /**
     * Returns the graph bridge.
     *
     * @return graph bridge
     */
    public GraphBridge bridge();

    /**
     * Gets the full graph.
     *
     * @return graph
     */
    public Graph getGraph();

    /**
     * Get the visible graph.
     * <p>
     * The visible graph may be the full graph (default) or a graph view.
     *
     * @return visible graph
     */
    public Graph getGraphVisible();

    /**
     * Gets the graph for the given graph view.
     *
     * @param view graph view
     * @return graph for this view
     */
    public Subgraph getGraph(GraphView view);

    /**
     * Gets the full graph with the directed interface.
     *
     * @return directed graph
     */
    public DirectedGraph getDirectedGraph();

    /**
     * Gets the visible graph with the directed interface.
     *
     * @return visible graph
     */
    public DirectedGraph getDirectedGraphVisible();

    /**
     * Gets the full graph with the undirected interface.
     *
     * @return undirected graph
     */
    public UndirectedGraph getUndirectedGraph();

    /**
     * Gets the visible graph with the undirected interface.
     *
     * @return visible graph
     */
    public UndirectedGraph getUndirectedGraphVisible();

    /**
     * Gets the directed graph for the given graph view.
     *
     * @param view graph view
     * @return directed graph for this view
     */
    public DirectedSubgraph getDirectedGraph(GraphView view);

    /**
     * Gets the undirected graph for the given graph view.
     *
     * @param view graph view
     * @return undirected graph for this view
     */
    public UndirectedSubgraph getUndirectedGraph(GraphView view);

    /**
     * Gets the visible view.
     *
     * @return visible view
     */
    public GraphView getVisibleView();

    /**
     * Sets the visible view.
     * <p>
     * If <em>view</em> is null, it restores the main view.
     *
     * @param view view
     */
    public void setVisibleView(GraphView view);

    /**
     * Adds a new edge type and returns the integer identifier.
     * <p>
     * If the type already exists, it returns the existing identifier.
     *
     * @param label edge type label
     * @return newly created edge type identifier.
     */
    public int addEdgeType(Object label);

    /**
     * Gets the edge type for the given label.
     *
     * @param label edge label
     * @return edge type identifier, or -1 if not found
     */
    public int getEdgeType(Object label);

    /**
     * Gets the edge label associated with the given type.
     *
     * @param id edge type
     * @return edge label
     */
    public Object getEdgeTypeLabel(int id);

    /**
     * Returns the number of different edge types.
     *
     * @return edge type count
     */
    public int getEdgeTypeCount();

    /**
     * Returns the edge types.
     *
     * @return edge types
     */
    public int[] getEdgeTypes();

    /**
     * Returns the edge type labels.
     *
     * @return edge type labels
     */
    public Object[] getEdgeTypeLabels();

    /**
     * Returns true if the graph is directed.
     *
     * @return true if directed, false otherwise
     */
    public boolean isDirected();

    /**
     * Returns true if the graph is undirected.
     *
     * @return true if undirected, false otherwise
     */
    public boolean isUndirected();

    /**
     * Returns true if the graph is mixed (both directed and undirected edges).
     *
     * @return true if mixed, false otherwise
     */
    public boolean isMixed();

    /**
     * Returns true if the graph is dynamic.
     *
     * @return true if dynamic, false otherwise
     */
    public boolean isDynamic();

    /**
     * Returns true if the graph is multi-graph (multiple types of edges).
     *
     * @return true if multi-graph, false otherwise
     */
    public boolean isMultiGraph();

    /**
     * Creates a new graph view.
     *
     * @return newly created graph view
     */
    public GraphView createView();

    /**
     * Creates a new graph view.
     * <p>
     * The node and edge parameters allows to restrict the view filtering to
     * only nodes or only edges. By default, the view applies to both nodes and
     * edges.
     *
     * @param node true to enable node view, false otherwise
     * @param edge true to enable edge view, false otherwise
     * @return newly created graph view
     */
    public GraphView createView(boolean node, boolean edge);

    /**
     * Creates a new graph view based on an existing view.
     *
     * @param view view to copy
     * @return newly created graph view
     */
    public GraphView copyView(GraphView view);

    /**
     * Creates a new graph based on an existing view.
     * <p>
     * The node and edge parameters allows to restrict the view filtering to
     * only nodes or only edges. By default, the view applies to both nodes and
     * edges.
     *
     * @param view view to copy
     * @param node true to enable node view, false otherwise
     * @param edge true to enable edge view, false otherwise
     * @return newly created graph view
     */
    public GraphView copyView(GraphView view, boolean node, boolean edge);

    /**
     * Destroys the given view.
     *
     * @param view view to destroy
     */
    public void destroyView(GraphView view);

    /**
     * Sets the given time interval to the view.
     * <p>
     * Each view can be configured with a time interval to filter a graph over
     * time.
     *
     * @param view the view to configure
     * @param interval the time interval
     */
    public void setTimeInterval(GraphView view, Interval interval);

    /**
     * Returns the <b>node</b> table. Contains all the columns associated to
     * node elements.
     * <p>
     * A <code>GraphModel</code> always has <b>node</b> and <b>edge</b> tables
     * by default.
     *
     * @return node table, contains node columns
     */
    public Table getNodeTable();

    /**
     * Returns the <b>edge</b> table. Contains all the columns associated to
     * edge elements.
     * <p>
     * A <code>GraphModel</code> always has <b>node</b> and <b>edge</b> tables
     * by default.
     *
     * @return edge table, contains edge columns
     */
    public Table getEdgeTable();

    /**
     * Gets the node index.
     *
     * @return node index
     */
    public Index<Node> getNodeIndex();

    /**
     * Gets the node index for the given graph view.
     *
     * @param view the view to get the index from
     * @return node index
     */
    public Index<Node> getNodeIndex(GraphView view);

    /**
     * Gets the edge index.
     *
     * @return edge index
     */
    public Index<Edge> getEdgeIndex();

    /**
     * Gets the edge index for the given graph view.
     *
     * @param view the view to get the index from
     * @return edge index
     */
    public Index<Edge> getEdgeIndex(GraphView view);

    /**
     * Gets the node time index.
     *
     * @return node time index
     */
    public TimeIndex<Node> getNodeTimeIndex();

    /**
     * Gets the node time index for the given view.
     *
     * @param view the view to get the index from
     * @return node time index
     */
    public TimeIndex<Node> getNodeTimeIndex(GraphView view);

    /**
     * Gets the edge time index.
     *
     * @return edge timestamp index
     */
    public TimeIndex<Edge> getEdgeTimeIndex();

    /**
     * Gets the edge time index for the given view.
     *
     * @param view view to get the index from
     * @return edge timestamp index
     */
    public TimeIndex<Edge> getEdgeTimeIndex(GraphView view);

    /**
     * Gets the time bounds.
     * <p>
     * The time bounds is an interval made of the minimum and maximum time
     * observed in the entire graph.
     *
     * @return time bounds
     */
    public Interval getTimeBounds();

    /**
     * Gets the time bounds for the visible graph.
     * <p>
     * The time bounds is an interval made of the minimum and maximum time
     * observed in the entire graph.
     *
     * @return time bounds
     */
    public Interval getTimeBoundsVisible();

    /**
     * Gets the time bounds for the given graph view.
     * <p>
     * The time bounds is an interval made of the minimum and maximum time
     * observed in the entire graph.
     *
     * @param view the graph view
     * @return time bounds
     */
    public Interval getTimeBounds(GraphView view);

    /**
     * Creates and returns a new graph observer.
     *
     * @param graph the graph to observe
     * @param withGraphDiff true to include graph difference feature, false
     *        otherwise
     * @return newly created graph observer
     */
    public GraphObserver createGraphObserver(Graph graph, boolean withGraphDiff);

    /**
     * Returns the time format used to display time.
     *
     * @return time format
     */
    public TimeFormat getTimeFormat();

    /**
     * Sets the time format used to display time.
     *
     * @param timeFormat time format
     */
    public void setTimeFormat(TimeFormat timeFormat);

    /**
     * Returns the time zone used to display time.
     *
     * @return time zone
     */
    public DateTimeZone getTimeZone();

    /**
     * Sets the time zone used to display time.
     *
     * @param timeZone time zone
     */
    public void setTimeZone(DateTimeZone timeZone);

    /**
     * Returns the current configuration.
     *
     * @return configuration
     */
    public Configuration getConfiguration();

    /**
     * Sets a new configuration for this graph model.
     * <p>
     * Note that this method only works if the graph model is empty.
     *
     * @param configuration new configuration
     * @throws IllegalStateException if the graph model isn't empty
     */
    public void setConfiguration(Configuration configuration);

    /**
     * Returns the maximum store id number nodes have in this model.
     * <p>
     * Each node has a unique store identifier which can be retrieved from
     * {@link Node#getStoreId() }. This maximum number can help design algorithms
     * thar rely on storing nodes in a array. Note that not all consecutive ids
     * may be assigned.
     *
     * @return maximum node store id
     */
    public int getMaxNodeStoreId();

    /**
     * Returns the maximum store id number edges have in this model.
     * <p>
     * Each edge has a unique store identifier which can be retrieved from
     * {@link Edge#getStoreId() }. This maximum number can help design algorithms
     * thar rely on storing edges in a array. Note that not all consecutive ids
     * may be assigned.
     *
     * @return maximum edge store id
     */
    public int getMaxEdgeStoreId();
}
