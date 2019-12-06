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
 * View on the graph.
 * <p>
 * Each graph can have views and use these views to obtain subgraphs. A view is
 * a filter on the main graph structure where some nodes and/or edges are
 * missing.
 * <p>
 * The graph model has a main view, which is always 100% of nodes and edges.
 * Users can then create views and modify them by enabling/disabling elements.
 * Views can only have elements which are in the model. As a consequence, if a
 * element is removed from the graph it's also removed from all the views. By
 * default, the view is empty.
 * <p>
 * The main benefits of views is the ability to obtain a <code>Subgraph</code>
 * object from it. Users can call the
 * {@link GraphModel#getGraph(GraphView) } method and obtain
 * a subgraph backed by the view. Update operations such as add or remove on
 * this graph are in-fact modifying the view rather than the model. Indeed,
 * adding a node to a view is enabling this node in the view. Similarly for
 * removal.
 * <p>
 * Views can apply on nodes only, edges only or both. This is configured when
 * the view is created. Nodes-only view let the system automatically control the
 * set of edges. Enabling a node in the view will automatically enable all it's
 * edges if the opposite nodes are also in the view.
 *
 * @see GraphModel
 */
public interface GraphView {

    /**
     * Gets the graph model this view belongs to.
     *
     * @return the graph model
     */
    public GraphModel getGraphModel();

    /**
     * Returns true if this view is the main view.
     *
     * @return true if main view, false otherwise
     */
    public boolean isMainView();

    /**
     * Returns true if this view supports node filtering.
     *
     * @return true if node view, false otherwise
     */
    public boolean isNodeView();

    /**
     * Returns true if this view supports edge filtering.
     *
     * @return true if edge view, false otherwise
     */
    public boolean isEdgeView();

    /**
     * Gets the time interval for this view.
     * <p>
     * If no interval is set, it returns a [-inf, +inf] interval.
     *
     * @return the time interval, or [-inf, +inf] if not set
     */
    public Interval getTimeInterval();

    /**
     * Returns true if this view has been destroyed.
     *
     * @return true if destroyed, false otherwise
     */
    public boolean isDestroyed();
}
