/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.MiddleTwtNode;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.graph.Pseudograph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class builds the connectivity among the voltageLevels of a substation
 * buildSubstationGraph establishes the List of nodes, edges
 *
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class SubstationGraph extends AbstractBaseGraph {

    private final List<VoltageLevelGraph> voltageLevels = new ArrayList<>();
    private final Map<String, VoltageLevelGraph> voltageLevelsById = new HashMap<>();
    private final String substationId;

    /**
     * Constructor
     */
    protected SubstationGraph(String id, Graph parentGraph) {
        super(parentGraph);
        this.substationId = Objects.requireNonNull(id);
    }

    public static SubstationGraph create(String id, Graph parentGraph) {
        Objects.requireNonNull(id);
        return new SubstationGraph(id, parentGraph);
    }

    public static SubstationGraph create(String id) {
        Objects.requireNonNull(id);
        return new SubstationGraph(id, null);
    }

    public void addVoltageLevel(VoltageLevelGraph node) {
        voltageLevels.add(node);
        voltageLevelsById.put(node.getId(), node);
    }

    @Override
    public VoltageLevelGraph getVoltageLevel(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return voltageLevelsById.get(voltageLevelId);
    }

    @Override
    public List<VoltageLevelGraph> getVoltageLevels() {
        return Collections.unmodifiableList(voltageLevels);
    }

    @Override
    public Stream<VoltageLevelGraph> getVoltageLevelStream() {
        return voltageLevels.stream();
    }

    @Override
    public Stream<Node> getAllNodesStream() {
        return voltageLevels.stream().flatMap(g -> g.getNodes().stream());
    }

    public List<BranchEdge> getEdges() {
        return Stream.concat(getLineEdges().stream(), twtEdges.stream()).collect(Collectors.toList());
    }

    public boolean graphAdjacents(VoltageLevelGraph g1, VoltageLevelGraph g2) {
        if (g1 == g2) {
            return true;
        } else {
            int nbNodes = voltageLevels.size();
            for (int i = 0; i < nbNodes - 1; i++) {
                if (voltageLevels.get(i) == g1 && voltageLevels.get(i + 1) == g2) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSubstationId() {
        return substationId;
    }

    public org.jgrapht.Graph<VoltageLevelGraph, Object> toJgrapht() {
        org.jgrapht.Graph<VoltageLevelGraph, Object> graph = new Pseudograph<>(Object.class);

        for (VoltageLevelGraph voltageLevelGraph : getVoltageLevels()) {
            graph.addVertex(voltageLevelGraph);
        }

        for (MiddleTwtNode multiNode : getMultiTermNodes()) {
            // the multiTermNode itself is not in the graph created
            // edges are added between its adjacent nodes
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();

            graph.addEdge(getVoltageLevelGraph(adjacentNodes.get(0)), getVoltageLevelGraph(adjacentNodes.get(1)));

            if (adjacentNodes.size() == 3) {
                graph.addEdge(getVoltageLevelGraph(adjacentNodes.get(0)), getVoltageLevelGraph(adjacentNodes.get(2)));
                graph.addEdge(getVoltageLevelGraph(adjacentNodes.get(1)), getVoltageLevelGraph(adjacentNodes.get(2)));
            }
        }

        return graph;
    }

    @Override
    public String getId() {
        return getSubstationId();
    }

    @Override
    public void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("substationId", substationId);
        generator.writeArrayFieldStart("voltageLevels");
        for (VoltageLevelGraph graph : voltageLevels) {
            graph.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();

        writeBranchFields(generator, includeCoordinates);

        generator.writeEndObject();
    }
}
