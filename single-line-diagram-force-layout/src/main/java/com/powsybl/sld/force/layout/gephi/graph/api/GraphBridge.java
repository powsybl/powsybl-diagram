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
 * Helper that helps transfer elements from another graph store.
 * <p>
 * This bridge can be used to insert elements that belong to another graph in
 * this graph store. It operates a deep copy so the destination elements are
 * independent from the source and have exactly the same properties and
 * attributes.
 */
public interface GraphBridge {

    /**
     * Copy the given nodes to the current graph store.
     * <p>
     * The <code>nodes</code> typically belong to another graph store. If nodes
     * already exists in the current graph they will be ignored.
     * <p>
     * All edges attached to <code>nodes</code> will be copied as well if their
     * source and target exists in this graph store.
     * <p>
     * This operation takes care of copying attribute columns and values, edge
     * type labels and element properties.
     * <p>
     * Beware that the source's configuration should match this graph store
     * configuration.
     *
     * @param nodes nodes to copy
     */
    public void copyNodes(Node[] nodes);
}
