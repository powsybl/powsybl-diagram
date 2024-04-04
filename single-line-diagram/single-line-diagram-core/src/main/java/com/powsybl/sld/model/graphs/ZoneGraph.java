/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Node;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class ZoneGraph extends AbstractBaseGraph {

    private final List<String> zone;
    private final List<SubstationGraph> substations = new ArrayList<>();
    private final Map<String, SubstationGraph> substationsById = new HashMap<>();
    private final Map<String, BranchEdge> edgesById = new HashMap<>();

    protected ZoneGraph(List<String> zone) {
        super(null);
        this.zone = Objects.requireNonNull(zone);
    }

    public static ZoneGraph create(List<String> zone) {
        return new ZoneGraph(zone);
    }

    @Override
    public String getId() {
        return String.join("_", zone);
    }

    public void addSubstation(SubstationGraph sGraph) {
        substations.add(sGraph);
        substationsById.put(sGraph.getSubstationId(), sGraph);
    }

    @Override
    public BranchEdge addLineEdge(String lineId, Node node1, Node node2) {
        BranchEdge edge = super.addLineEdge(lineId, node1, node2);
        edgesById.put(lineId, edge);
        return edge;
    }

    public List<String> getZone() {
        return zone;
    }

    public List<SubstationGraph> getSubstations() {
        return substations;
    }

    public Optional<SubstationGraph> getSubstationGraph(Node node) {
        VoltageLevelGraph vlGraph = getVoltageLevelGraph(node);
        return getSubstations().stream().filter(s -> s.getVoltageLevels().contains(vlGraph)).findFirst();
    }

    @Override
    public VoltageLevelGraph getVoltageLevel(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return getVoltageLevelStream().filter(g -> voltageLevelId.equals(g.getVoltageLevelInfos().getId())).findFirst().orElse(null);
    }

    @Override
    public Stream<VoltageLevelGraph> getVoltageLevelStream() {
        return substations.stream().flatMap(SubstationGraph::getVoltageLevelStream);
    }

    @Override
    public List<VoltageLevelGraph> getVoltageLevels() {
        return getVoltageLevelStream().toList();
    }

    @Override
    public Stream<Node> getAllNodesStream() {
        return getVoltageLevelStream().flatMap(Graph::getAllNodesStream);
    }

    public SubstationGraph getSubstationGraph(String id) {
        Objects.requireNonNull(id);
        return substationsById.get(id);
    }

    public BranchEdge getLineEdge(String lineId) {
        Objects.requireNonNull(lineId);
        return edgesById.get(lineId);
    }

    public void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStartObject();
        generator.writeArrayFieldStart("substations");
        for (SubstationGraph substationGraph : substations) {
            substationGraph.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("lineEdges");
        for (BranchEdge edge : getLineEdges()) {
            edge.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

}
