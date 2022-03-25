/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.cells.BusCell;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Node;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractGraph implements Graph {

    private boolean coordinatesSerialized = true;
    private double width;
    private double height;
    private final List<BranchEdge> lineEdges = new ArrayList<>();
    private final Map<Node, VoltageLevelGraph> nodeToVlGraph;

    AbstractGraph(Graph parentGraph) {
        if (parentGraph instanceof VoltageLevelGraph) {
            throw new PowsyblException("a voltageLevelGraph can not be a parent Graph");
        }
        this.nodeToVlGraph = parentGraph == null ? new HashMap<>() : parentGraph.getNodeToVlGraph();
    }

    @Override
    public BranchEdge addLineEdge(String lineId, Node node1, Node node2) {
        BranchEdge edge = new BranchEdge(lineId, node1, node2);
        lineEdges.add(edge);
        return edge;
    }

    @Override
    public List<BranchEdge> getLineEdges() {
        return new ArrayList<>(lineEdges);
    }

    @Override
    public void setCoordinatesSerialized(boolean coordinatesSerialized) {
        this.coordinatesSerialized = coordinatesSerialized;
    }

    @Override
    public void writeJson(Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        try (JsonGenerator generator = new JsonFactory()
            .createGenerator(writer)
            .useDefaultPrettyPrinter()) {
            writeJson(generator, coordinatesSerialized);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Map<Node, VoltageLevelGraph> getNodeToVlGraph() {
        return nodeToVlGraph;
    }

    @Override
    public VoltageLevelGraph getVoltageLevelGraph(Node node) {
        return nodeToVlGraph.get(node);
    }

    @Override
    public Optional<Cell> getCell(Node node) {
        VoltageLevelGraph vlGraph = getVoltageLevelGraph(node);
        return vlGraph == null ? Optional.empty() : vlGraph.getCell(node);
    }

    @Override
    public Direction getDirection(Node node) {
        return getCell(node).filter(BusCell.class::isInstance).map(c -> ((BusCell) c).getDirection()).orElse(Direction.UNDEFINED);
    }

    @Override
    public void addNode(VoltageLevelGraph vlGraph, Node node) {
        nodeToVlGraph.put(node, vlGraph);
    }

    @Override
    public void removeNode(Node node) {
        nodeToVlGraph.remove(node);
    }

    protected abstract void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException;

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }
}
