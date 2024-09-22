/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public abstract class AbstractNode implements Node {

    private NodeType type;

    private final String id;

    private final String componentType;

    private final boolean fictitious;

    private final Point position = new Point(-1, -1);

    private final List<Edge> adjacentEdges = new ArrayList<>();

    private String label;

    private Integer order;

    private Direction direction = Direction.UNDEFINED;

    private Orientation orientation;

    private boolean isLimitExceeded;

    /**
     * Constructor
     */
    protected AbstractNode(NodeType type, String id, String componentType, boolean fictitious) {
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(id);
        this.componentType = Objects.requireNonNull(componentType);
        this.fictitious = fictitious;
        setOrientation(defaultOrientation());
    }

    @Override
    public String getComponentType() {
        return componentType;
    }

    @Override
    public boolean isFictitious() {
        return fictitious;
    }

    @Override
    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public List<Node> getAdjacentNodes() {
        return adjacentEdges.stream()
                .map(edge -> edge.getOppositeNode(this))
                .toList();
    }

    @Override
    public List<Edge> getAdjacentEdges() {
        return adjacentEdges;
    }

    @Override
    public void addAdjacentEdge(Edge e) {
        adjacentEdges.add(e);
    }

    @Override
    public void removeAdjacentEdge(Edge e) {
        adjacentEdges.remove(e);
    }

    @Override
    public Point getCoordinates() {
        return position;
    }

    @Override
    public void setCoordinates(Point coord) {
        position.setCoordinates(coord);
    }

    @Override
    public void setCoordinates(double x, double y) {
        position.setCoordinates(x, y);
    }

    /**
     * Get abscissa within current voltage level
     * @return abscissa within voltage level
     */
    @Override
    public double getX() {
        return position.getX();
    }

    /**
     * Get ordinate within current voltage level
     * @return ordinate within voltage level
     */
    @Override
    public double getY() {
        return position.getY();
    }

    @Override
    public void setX(double x) {
        position.setX(x);
    }

    @Override
    public void setY(double y) {
        position.setY(y);
    }

    @Override
    public NodeType getType() {
        return this.type;
    }

    @Override
    public Optional<Integer> getOrder() {
        return Optional.ofNullable(order);
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void removeOrder() {
        this.order = null;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(Orientation orientation) {
        this.orientation = Objects.requireNonNullElse(orientation, defaultOrientation());
    }

    @Override
    public void setOrientationFromBlock(Orientation blockOrientation, List<Node> blockNodes) {
        setOrientation(blockOrientation);
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
    @Override
    public boolean checkNodeSimilarity(Node n) {
        return this.equals(n)
                || similarToAFeederNode(this) && similarToAFeederNode(n)
                || this instanceof BusNode && n instanceof BusNode;
    }

    @Override
    public boolean similarToAFeederNode(Node n) {
        return n instanceof FeederNode
                || n.getType() == NodeType.INTERNAL && n.getAdjacentEdges().size() == 1;
    }

    @Override
    public int getCardinality(VoltageLevelGraph vlGraph) {
        return getAdjacentNodes().size();
    }

    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStringField("type", type.name());
        generator.writeStringField("id", id);
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
        return type + " " + id;
    }
}
