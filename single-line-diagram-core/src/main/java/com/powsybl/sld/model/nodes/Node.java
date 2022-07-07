/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.Direction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private NodeType type;

    private final String id;

    private final String name;

    private final String equipmentId;

    private final String componentType;

    private final boolean fictitious;

    private final Point position = new Point(-1, -1);

    private final List<Edge> adjacentEdges = new ArrayList<>();

    private String label;

    private Integer order;

    private Direction direction = Direction.UNDEFINED;

    private Orientation orientation;

    private boolean canConnectBus = false;

    /**
     * Constructor
     */
    public Node(NodeType type, String id, String name, String equipmentId, String componentType, boolean fictitious) {
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.equipmentId = equipmentId;
        this.componentType = Objects.requireNonNull(componentType);
        this.fictitious = fictitious;
        setOrientation(defaultOrientation());
    }

    public String getComponentType() {
        return componentType;
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

    public void removeAdjacentEdge(Edge e) {
        adjacentEdges.remove(e);
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

    public void setX(double x) {
        position.setX(x);
    }

    public void setY(double y) {
        position.setY(y);
    }

    public NodeType getType() {
        return this.type;
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

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    //TODO: add isBusConnector in writeJsonContent
    public boolean isCanConnectBus() {
        return canConnectBus;
    }

    public void setCanConnectBus(boolean isBusConnector) {
        this.canConnectBus = isBusConnector;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = Objects.requireNonNullElse(orientation, defaultOrientation());
    }

    protected Orientation defaultOrientation() {
        return Orientation.UP;
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
        if (orientation != defaultOrientation()) {
            generator.writeStringField("orientation", orientation.name());
        }
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
}
