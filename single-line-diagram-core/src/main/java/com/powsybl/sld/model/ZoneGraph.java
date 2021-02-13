/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.*;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class ZoneGraph implements LineGraph {

    private List<String> zone;
    private List<SubstationGraph> nodes = new ArrayList<>();
    private List<LineEdge> edges = new ArrayList<>();
    private Map<String, SubstationGraph> nodesById = new HashMap<>();
    private Map<String, LineEdge> edgesById = new HashMap<>();

    private ZoneGraph(List<String> zone) {
        this.zone = Objects.requireNonNull(zone);
    }

    public static ZoneGraph create(List<String> zone) {
        return new ZoneGraph(zone);
    }

    private boolean generateCoordsInJson = true;

    @Override
    public String getId() {
        return String.join("_", zone);
    }

    public void addNode(SubstationGraph sGraph) {
        nodes.add(sGraph);
        nodesById.put(sGraph.getSubstationId(), sGraph);
    }

    public void addEdge(String lineId, Node node1, Node node2) {
        LineEdge edge = new LineEdge(lineId, node1, node2);
        edges.add(edge);
        edgesById.put(lineId, edge);
    }

    @Override
    public LineEdge addLineEdge(String lineId, Node n1, Node n2) {
        addEdge(lineId, n1, n2);
        return edgesById.get(lineId);
    }

    public List<String> getZone() {
        return zone;
    }

    public List<SubstationGraph> getNodes() {
        return nodes;
    }

    public List<LineEdge> getEdges() {
        return edges;
    }

    @Override
    public List<LineEdge> getLineEdges() {
        return getEdges();
    }

    @Override
    public VoltageLevelGraph getVLGraph(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return nodes.stream().flatMap(SubstationGraph::getNodeStream).filter(g -> voltageLevelId.equals(g.getVoltageLevelInfos().getId())).findFirst().orElse(null);
    }

    public SubstationGraph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public LineEdge getEdge(String lineId) {
        Objects.requireNonNull(lineId);
        return edgesById.get(lineId);
    }

    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        try (JsonGenerator generator = new JsonFactory()
                .createGenerator(writer)
                .useDefaultPrettyPrinter()) {
            writeJson(generator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeArrayFieldStart("substations");
        for (SubstationGraph substationGraph : nodes) {
            substationGraph.setGenerateCoordsInJson(generateCoordsInJson);
            substationGraph.writeJson(generator);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("lineEdges");
        for (LineEdge edge : edges) {
            edge.writeJson(generator, generateCoordsInJson);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    public void setGenerateCoordsInJson(boolean generateCoordsInJson) {
        this.generateCoordsInJson = generateCoordsInJson;
    }

}
