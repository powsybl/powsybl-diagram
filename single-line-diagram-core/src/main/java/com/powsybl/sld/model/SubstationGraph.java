/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class builds the connectivity among the voltageLevels of a substation
 * buildSubstationGraph establishes the List of nodes, edges
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class SubstationGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubstationGraph.class);

    private String substationId;

    private final List<Graph> nodes = new ArrayList<>();

    private final List<TwtEdge> edges = new ArrayList<>();

    private final Map<String, Graph> nodesById = new HashMap<>();

    /**
     * Constructor
     */
    private SubstationGraph(String id) {
        this.substationId = Objects.requireNonNull(id);
    }

    public static SubstationGraph create(String id) {
        Objects.requireNonNull(id);
        SubstationGraph g = new SubstationGraph(id);
        return g;
    }

    public void addNode(Graph node) {
        nodes.add(node);
        nodesById.put(node.getVoltageLevelId(), node);
    }

    public Graph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public void addEdge(Node n1, Node n2) {
        TwtEdge sl = new TwtEdge(n1, n2);
        edges.add(sl);
    }

    public List<Graph> getNodes() {
        return new ArrayList<>(nodes);
    }

    public List<TwtEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    public boolean graphAdjacents(Graph g1, Graph g2) {
        int nbNodes = nodes.size();
        for (int i = 0; i < nbNodes; i++) {
            if (nodes.get(i) == g1 && i < (nbNodes - 1) && nodes.get(i + 1) == g2) {
                return true;
            }
        }
        return false;
    }

    public String getSubstationId() {
        return substationId;
    }
}
