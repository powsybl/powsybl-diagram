/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * This class builds the connectivity among the voltageLevels of a substation
 * buildSubstationGraph establishes the List of nodes, edges
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class SubstationGraph {

    private String substationId;

    private final List<Graph> nodes = new ArrayList<>();

    private List<TwtEdge> edges = new ArrayList<>();

    private final Map<String, Graph> nodesById = new HashMap<>();

    private List<Node> multiTermNodes = new ArrayList<>();

    private boolean generateCoordsInJson = true;

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
        nodesById.put(node.getVoltageLevelInfos().getId(), node);
    }

    public Graph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public TwtEdge addEdge(Node node1, Node node2) {
        TwtEdge sl = new TwtEdge(node1, node2);
        edges.add(sl);
        return sl;
    }

    public List<Graph> getNodes() {
        return new ArrayList<>(nodes);
    }

    public List<TwtEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    public void setEdges(List<TwtEdge> edges) {
        this.edges = edges;
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

    public void addMultiTermNode(Node node) {
        multiTermNodes.add(node);
    }

    public List<Node> getMultiTermNodes() {
        return multiTermNodes;
    }

    public void writeJson(Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        try (JsonGenerator generator = new JsonFactory()
                .createGenerator(writer)
                .useDefaultPrettyPrinter()) {
            generator.writeStartArray();
            for (Graph graph : nodes) {
                graph.setGenerateCoordsInJson(generateCoordsInJson);
                graph.writeJson(generator);
            }

            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void setGenerateCoordsInJson(boolean generateCoordsInJson) {
        this.generateCoordsInJson = generateCoordsInJson;
    }
}
