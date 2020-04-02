/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Node implements BaseNode {

    public enum NodeType {
        BUS,
        FEEDER,
        FICTITIOUS,
        SWITCH,
        SHUNT,
        OTHER
    }

    protected final Graph graph;

    private NodeType type;

    private final String id;

    private final String name;

    private final String equipmentId;

    private String componentType;

    private final boolean fictitious;

    private double x = -1;
    private double y = -1;
    private List<Double> xs = new ArrayList<>();
    private List<Double> ys = new ArrayList<>();

    private boolean xPriority = false;
    private boolean yPriority = false;

    private double initY = -1;  // y value before shifting the feeders height, if asked

    private Cell cell;

    private Double rotationAngle;

    private boolean open = false;

    private final List<Edge> adjacentEdges = new ArrayList<>();

    private String label;

    /**
     * Constructor
     */
    protected Node(NodeType type, String id, String name, String equipmentId, String componentType, boolean fictitious, Graph graph) {
        this.type = Objects.requireNonNull(type);
        this.name = name;
        this.equipmentId = equipmentId;
        this.componentType = Objects.requireNonNull(componentType);
        this.fictitious = fictitious;
        // graph can be null here : for example, in a substation diagram, a fictitious node is created outside
        // any graph, in order to link the different windings together
        this.graph = graph;
        // for unicity purpose (in substation diagram), we prefix the id of the fictitious node with the voltageLevel id and "_"
        String tmpId = Objects.requireNonNull(id);
        if (type == NodeType.FICTITIOUS &&
                graph != null &&
                !StringUtils.startsWith(tmpId, "FICT_" + this.graph.getVoltageLevelInfos().getId() + "_")) {
            this.id = "FICT_" + graph.getVoltageLevelInfos().getId() + "_" + tmpId;
        } else {
            this.id = tmpId;
        }
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Graph getGraph() {
        return graph;
    }

    @Override
    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    @Override
    public Double getRotationAngle() {
        return rotationAngle;
    }

    public boolean isFictitious() {
        return fictitious;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getLabel() {
        if (label != null) {
            return label;
        } else {
            return graph.isUseName() ? name : equipmentId;
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Node> getAdjacentNodes() {
        return adjacentEdges.stream()
                .map(edge -> edge.getNode1() == Node.this ? edge.getNode2() : edge.getNode1())
                .collect(Collectors.toList());
    }

    public List<Edge> getAdjacentEdges() {
        return adjacentEdges;
    }

    public void addAdjacentEdge(Edge e) {
        adjacentEdges.add(e);
    }

    void removeAdjacentEdge(Edge e) {
        adjacentEdges.remove(e);
    }

    public Stream<Node> getListNodeAdjInCell(Cell cell) {
        return getAdjacentNodes().stream().filter(n -> cell.getNodes().contains(n));
    }

    @Override
    public double getX() {
        return x;
    }

    public void setX(double x) {
        setX(x, false, true);
    }

    public void setX(double x, boolean xPriority) {
        setX(x, xPriority, true);
    }

    public void setX(double x, boolean xPriority, boolean addXGraph) {
        double xNode = x;
        if (addXGraph) {
            xNode += graph.getX();
        }

        if (!this.xPriority && xPriority) {
            xs.clear();
            this.xPriority = true;
        }
        if (this.xPriority == xPriority) {
            this.x = xNode;
            xs.add(xNode);
        }
    }

    @Override
    public double getY() {
        return y;
    }

    public void setY(double y) {
        setY(y, false, true);
    }

    public void setY(double y, boolean yPriority) {
        setY(y, yPriority, true);
    }

    public void setY(double y, boolean yPriority, boolean addYGraph) {
        double yNode = y;
        if (addYGraph) {
            yNode += graph.getY();
        }

        if (!this.yPriority && yPriority) {
            ys.clear();
            this.yPriority = true;
        }
        if (this.yPriority == yPriority) {
            this.y = yNode;
            ys.add(yNode);
        }
    }

    public double getInitY() {
        return initY;
    }

    public void setInitY(double initY) {
        this.initY = initY;
    }

    public NodeType getType() {
        return this.type;
    }

    @Override
    public boolean isRotated() {
        return rotationAngle != null;
    }

    public void setRotationAngle(Double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Check similarity with another node
     *
     * @param n the node to compare with
     * @return true IF the both are the same OR they are both Busbar OR they are both EQ(but not Busbar);
     * false otherwise
     **/
    public boolean checkNodeSimilarity(Node n) {
        return this.equals(n)
                || ((similarToAFeederNode(this)) && (similarToAFeederNode(n)))
                || ((this instanceof BusNode) && (n instanceof BusNode));
    }

    public boolean similarToAFeederNode(Node n) {
        return (n instanceof FeederNode)
                || (n.getType() == NodeType.FICTITIOUS && n.adjacentEdges.size() == 1);
    }

    public void finalizeCoord() {
        x = xs.stream().mapToDouble(d -> d).average().orElse(0);
        y = ys.stream().mapToDouble(d -> d).average().orElse(0);
    }

    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStringField("type", type.name());
        generator.writeStringField("id", id);
        if (name != null) {
            generator.writeStringField("name", name);
        }
        generator.writeStringField("equipmentId", equipmentId);
        generator.writeStringField("componentType", componentType);
        generator.writeBooleanField("fictitious", fictitious);
        if (graph.isGenerateCoordsInJson()) {
            generator.writeNumberField("x", x);
            generator.writeNumberField("y", y);
        }
        if (rotationAngle != null) {
            generator.writeNumberField("rotationAngle", rotationAngle);
        }
        generator.writeBooleanField("open", open);
        if (label != null) {
            generator.writeStringField("label", label);
        }
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        writeJsonContent(generator);
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return "Node(id='" + getId() + "' name='" + name + "', type= " + type + ")";
    }

    public void resetCoords() {
        x = -1;
        y = -1;
        xPriority = false;
        yPriority = false;
        initY = -1;
        xs.clear();
        ys.clear();
    }

    public void shiftY(double yShift) {
        y += yShift;
    }

    public VoltageLevelInfos getVoltageLevelInfos() {
        return graph != null ? graph.getVoltageLevelInfos() : null;
    }
}
