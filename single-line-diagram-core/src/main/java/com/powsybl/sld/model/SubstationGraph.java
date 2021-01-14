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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class builds the connectivity among the voltageLevels of a substation
 * buildSubstationGraph establishes the List of nodes, edges
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class SubstationGraph extends AbstractGraph {

    private String substationId;

    private final List<Graph> nodes = new ArrayList<>();

    private final Map<String, Graph> nodesById = new HashMap<>();

    /**
     * Constructor
     */
    private SubstationGraph(String id) {
        this.substationId = Objects.requireNonNull(id);
    }

    public static SubstationGraph create(String id) {
        Objects.requireNonNull(id);
        return new SubstationGraph(id);
    }

    public void addNode(Graph node) {
        nodes.add(node);
        nodesById.put(node.getId(), node);
    }

    public Graph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public TwtEdge addEdge(Node node1, Node node2) {
        return addTwtEdge(node1, node2);
    }

    @Override
    public Graph getVLGraph(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return nodes.stream().filter(g -> voltageLevelId.equals(g.getVoltageLevelInfos().getId())).findFirst().orElse(null);
    }

    public List<Graph> getNodes() {
        return new ArrayList<>(nodes);
    }

    public Stream<Graph> getNodeStream() {
        return getNodes().stream();
    }

    public List<Edge> getEdges() {
        return Stream.concat(lineEdges.stream(), twtEdges.stream()).collect(Collectors.toList());
    }

    public boolean graphAdjacents(Graph g1, Graph g2) {
        if (g1 == g2) {
            return true;
        } else {
            int nbNodes = nodes.size();
            for (int i = 0; i < nbNodes; i++) {
                if (nodes.get(i) == g1 && i < (nbNodes - 1) && nodes.get(i + 1) == g2) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSubstationId() {
        return substationId;
    }

    @Override
    public String getId() {
        return getSubstationId();
    }

    public void writeJson(Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("substationId", substationId);
        generator.writeArrayFieldStart("voltageLevels");
        for (Graph graph : nodes) {
            graph.setGenerateCoordsInJson(generateCoordsInJson);
            graph.writeJson(generator);
        }
        generator.writeEndArray();

        super.writeJson(generator);

        generator.writeEndObject();
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
}
