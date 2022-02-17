/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Node {

    public enum NodeType {
        BUS,
        FEEDER,
        FICTITIOUS,
        SWITCH,
        SHUNT,
        OTHER
    }

    protected final VoltageLevelGraph voltageLevelGraph;

    private NodeType type;

    private final String id;

    private final String name;

    private final String equipmentId;

    private final String componentType;

    private final boolean fictitious;

    private final Point position = new Point(-1, -1);

    private Cell cell;

    private Double rotationAngle;

    private boolean open = false;

    private final List<Edge> adjacentEdges = new ArrayList<>();

    private String label;

    private Integer order;

    private BusCell.Direction direction = BusCell.Direction.UNDEFINED;

    private Orientation orientation;

    /**
     * Constructor
     */
    protected Node(NodeType type, String id, String name, String equipmentId, String componentType, boolean fictitious, VoltageLevelGraph graph) {
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.equipmentId = equipmentId;
        this.componentType = Objects.requireNonNull(componentType);
        this.fictitious = fictitious;
        // graph can be null here : for example, in a substation diagram, a fictitious node is created outside
        // any graph, in order to link the different windings together
        this.voltageLevelGraph = graph;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public VoltageLevelGraph getVoltageLevelGraph() {
        return voltageLevelGraph;
    }

    public String getComponentType() {
        return componentType;
    }

    public Double getRotationAngle() {
        return rotationAngle;
    }

    public boolean isFictitious() {
        return fictitious;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
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

    public Point getDiagramCoordinates() {
        return voltageLevelGraph != null ? position.getShiftedPoint(voltageLevelGraph.getCoord()) : position;
    }

    public Point getCoordinates() {
        return position;
    }

    public void setCoordinates(Point coord) {
        position.setCoordinates(coord);
    }

    public void setCoordinates(double x, double y) {
        position.setCoordinates(x, y);
    }

    /**
     * Get abscissa within current voltage level
     * @return abscissa within voltage level
     */
    public double getX() {
        return position.getX();
    }

    /**
     * Get ordinate within current voltage level
     * @return ordinate within voltage level
     */
    public double getY() {
        return position.getY();
    }

    /**
     * Get abscissa within the diagram
     * @return abscissa within diagram
     */
    public double getDiagramX() {
        return voltageLevelGraph != null ? position.getX() + voltageLevelGraph.getX() : position.getX();
    }

    /**
     * Get ordinate within the diagram
     * @return ordinate within diagram
     */
    public double getDiagramY() {
        return voltageLevelGraph != null ? position.getY() + voltageLevelGraph.getY() : position.getY();
    }

    public void setX(double x) {
        position.setX(x);
    }

    public void setY(double y) {
        position.setY(y);
    }

    public NodeType getType() {
        return this.type;
    }

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

    public Optional<Integer> getOrder() {
        return Optional.ofNullable(order);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void removeOrder() {
        this.order = null;
    }

    public BusCell.Direction getDirection() {
        return direction;
    }

    public void setDirection(BusCell.Direction direction) {
        this.direction = direction;
        if (orientation == null || orientation.isHorizontal()) {
            this.orientation = direction.toOrientation();
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
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

    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStringField("type", type.name());
        generator.writeStringField("id", id);
        if (name != null) {
            generator.writeStringField("name", name);
        }
        if (equipmentId != null) {
            generator.writeStringField("equipmentId", equipmentId);
        }
        generator.writeStringField("componentType", componentType);
        generator.writeBooleanField("fictitious", fictitious);
        if (includeCoordinates) {
            generator.writeNumberField("x", getX());
            generator.writeNumberField("y", getY());
        }
        if (rotationAngle != null) {
            generator.writeNumberField("rotationAngle", rotationAngle);
        }
        generator.writeBooleanField("open", open);
        if (label != null) {
            generator.writeStringField("label", label);
        }
    }

    public void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStartObject();
        writeJsonContent(generator, includeCoordinates);
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return type + " " + name + " " + id;
    }

    public void resetCoords() {
        setCoordinates(-1, -1);
    }

    public void shiftY(double yShift) {
        position.shiftY(yShift);
    }

    /**
     * Get voltage level infos for this node. By default it is the voltage level infos of the graph but it
     * could be override in case of node that represents an external voltage level.
     */
    public VoltageLevelInfos getVoltageLevelInfos() {
        if (voltageLevelGraph != null) {
            return voltageLevelGraph.getVoltageLevelInfos();
        }
        return null;
    }
}
