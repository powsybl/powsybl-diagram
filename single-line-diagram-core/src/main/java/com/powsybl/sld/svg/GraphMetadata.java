/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.*;
import com.powsybl.sld.model.BusCell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class GraphMetadata implements AnchorPointProvider {

    public static class NodeMetadata {

        private final String id;

        private String componentType;

        private Double rotationAngle;

        private final boolean open;

        private final String vId;

        private final String nextVId;

        private final BusCell.Direction direction;

        private final boolean vLabel;

        private final String equipmentId;

        private final List<NodeLabelMetadata> labels;

        @JsonCreator
        public NodeMetadata(@JsonProperty("id") String id,
                            @JsonProperty("vid") String vId,
                            @JsonProperty("nextVId") String nextVId,
                            @JsonProperty("componentType") String componentType,
                            @JsonProperty("rotationAngle") Double rotationAngle,
                            @JsonProperty("open") boolean open,
                            @JsonProperty("direction") BusCell.Direction direction,
                            @JsonProperty("vlabel") boolean vLabel,
                            @JsonProperty("equipmentId") String equipmentId,
                            @JsonProperty("labels") List<NodeLabelMetadata> labels) {
            this.id = Objects.requireNonNull(id);
            this.vId = Objects.requireNonNull(vId);
            this.nextVId = nextVId;
            this.componentType = componentType;
            this.rotationAngle = rotationAngle;
            this.open = Objects.requireNonNull(open);
            this.direction = direction;
            this.vLabel = vLabel;
            this.equipmentId = equipmentId;
            this.labels = Objects.requireNonNull(labels);
        }

        public String getId() {
            return id;
        }

        public String getVId() {
            return vId;
        }

        public String getNextVId() {
            return nextVId;
        }

        public String getComponentType() {
            return componentType;
        }

        public Double getRotationAngle() {
            return rotationAngle;
        }

        public boolean isOpen() {
            return open;
        }

        public BusCell.Direction getDirection() {
            return direction;
        }

        public boolean isVLabel() {
            return vLabel;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public List<NodeLabelMetadata> getLabels() {
            return labels;
        }
    }

    public static class NodeLabelMetadata {

        private final String id;

        private final String userId;

        private final String positionName;

        @JsonCreator
        public NodeLabelMetadata(@JsonProperty("id") String id,
                                 @JsonProperty("userId") String userId,
                                 @JsonProperty("positionName") String positionName) {
            this.id = Objects.requireNonNull(id);
            this.userId = userId;
            this.positionName = Objects.requireNonNull(positionName);
        }

        public String getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public String getPositionName() {
            return positionName;
        }
    }

    public static class WireMetadata {

        private final String id;

        private final String nodeId1;

        private final String nodeId2;

        private final boolean straight;

        private final boolean snakeLine;

        @JsonCreator
        public WireMetadata(@JsonProperty("id") String id,
                            @JsonProperty("nodeId1") String nodeId1,
                            @JsonProperty("nodeId2") String nodeId2,
                            @JsonProperty("straight") boolean straight,
                            @JsonProperty("snakeline") boolean snakeline) {
            this.id = Objects.requireNonNull(id);
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
            this.straight = straight;
            this.snakeLine = snakeline;
        }

        public String getId() {
            return id;
        }

        public String getNodeId1() {
            return nodeId1;
        }

        public String getNodeId2() {
            return nodeId2;
        }

        public boolean isStraight() {
            return straight;
        }

        public boolean isSnakeLine() {
            return snakeLine;
        }
    }

    public static class LineMetadata {

        private final String id;

        private final String nodeId1;

        private final String nodeId2;

        @JsonCreator
        public LineMetadata(@JsonProperty("id") String id,
                            @JsonProperty("nodeId1") String nodeId1,
                            @JsonProperty("nodeId2") String nodeId2) {
            this.id = Objects.requireNonNull(id);
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
        }

        public String getId() {
            return id;
        }

        public String getNodeId1() {
            return nodeId1;
        }

        public String getNodeId2() {
            return nodeId2;
        }
    }

    public static class FeederInfoMetadata {

        private final String id;

        private final String feederNodeId;

        private final String userId;

        private final DiagramLabelProvider.Direction arrowDirection;

        private final String leftLabel;

        private final String rightLabel;

        @JsonCreator
        public FeederInfoMetadata(@JsonProperty("id") String id, @JsonProperty("feederNodeId") String feederNodeId, @JsonProperty("userId") String userId,
                                  @JsonProperty("arrowDirection") DiagramLabelProvider.Direction arrowDirection, @JsonProperty("leftLabel") String leftLabel, @JsonProperty("rightLabel") String rightLabel) {
            this.id = Objects.requireNonNull(id);
            this.feederNodeId = Objects.requireNonNull(feederNodeId);
            this.userId = userId;
            this.arrowDirection = arrowDirection;
            this.leftLabel = leftLabel;
            this.rightLabel = rightLabel;
        }

        public String getId() {
            return id;
        }

        public String getFeederNodeId() {
            return feederNodeId;
        }

        public String getUserId() {
            return userId;
        }

        public DiagramLabelProvider.Direction getArrowDirection() {
            return arrowDirection;
        }

        public String getLeftLabel() {
            return leftLabel;
        }

        public String getRightLabel() {
            return rightLabel;
        }
    }

    public static class ElectricalNodeInfoMetadata {

        private final String id;

        @JsonCreator
        public ElectricalNodeInfoMetadata(@JsonProperty("id") String id) {
            this.id = Objects.requireNonNull(id);
        }

        public String getId() {
            return id;
        }
    }

    private final Map<String, Component> componentByType = new HashMap<>();

    private final Map<String, Component> componentById = new HashMap<>();

    private final Map<String, NodeMetadata> nodeMetadataMap = new HashMap<>();

    private final Map<String, WireMetadata> wireMetadataMap = new HashMap<>();

    private final Map<String, LineMetadata> lineMetadataMap = new HashMap<>();

    private final LayoutParameters layoutParameters;

    private final Map<String, FeederInfoMetadata> feederInfoMetadataMap = new HashMap<>();

    private final Map<String, ElectricalNodeInfoMetadata> electricalNodeInfoMetadataMap = new HashMap<>();

    public GraphMetadata() {
        this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new LayoutParameters());
    }

    @JsonCreator
    public GraphMetadata(@JsonProperty("components") List<Component> componentList,
                         @JsonProperty("nodes") List<NodeMetadata> nodeMetadataList,
                         @JsonProperty("wires") List<WireMetadata> wireMetadataList,
                         @JsonProperty("lines") List<LineMetadata> lineMetadataList,
                         @JsonProperty("feederInfos") List<FeederInfoMetadata> feederInfoMetadataList,
                         @JsonProperty("electricalNodeInfos") List<ElectricalNodeInfoMetadata> electricalNodeInfoMetadataMap,
                         @JsonProperty("layoutParams") LayoutParameters layoutParams) {
        for (Component component : componentList) {
            addComponent(component);
        }
        for (NodeMetadata nodeMetadata : nodeMetadataList) {
            addNodeMetadata(nodeMetadata);
        }
        for (WireMetadata wireMetadata : wireMetadataList) {
            addWireMetadata(wireMetadata);
        }
        for (LineMetadata lineMetadata : lineMetadataList) {
            addLineMetadata(lineMetadata);
        }
        for (FeederInfoMetadata feederInfoMetadata : feederInfoMetadataList) {
            addFeederInfoMetadata(feederInfoMetadata);
        }
        for (ElectricalNodeInfoMetadata electricalNodeInfoMetadata : electricalNodeInfoMetadataMap) {
            addElectricalNodeInfoMetadata(electricalNodeInfoMetadata);
        }
        layoutParameters = layoutParams;
    }

    public static GraphMetadata parseJson(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return parseJson(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static GraphMetadata parseJson(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            return objectMapper.readValue(inputStream, GraphMetadata.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static GraphMetadata parseJson(Reader reader) {
        Objects.requireNonNull(reader);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            return objectMapper.readValue(reader, GraphMetadata.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Path file) {
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(writer, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void addComponent(Component component) {
        Objects.requireNonNull(component);
        componentByType.put(component.getType(), component);
        if (component.getId() != null) {
            componentById.put(component.getId(), component);
        }
    }

    public Component getComponentMetadata(String componentType) {
        return componentType != null ? componentByType.get(componentType) : null;
    }

    @Override
    public List<AnchorPoint> getAnchorPoints(String type, String id) {
        Component component = null;
        if (id != null) {
            component = componentById.get(id);
        }
        if (component == null) {
            component = getComponentMetadata(type);
        }
        return component != null ? component.getAnchorPoints()
                                         : Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
    }

    @JsonProperty("components")
    public List<Component> getComponentMetadata() {
        return ImmutableList.copyOf(componentByType.values());
    }

    public void addNodeMetadata(NodeMetadata metadata) {
        Objects.requireNonNull(metadata);
        nodeMetadataMap.put(metadata.getId(), metadata);
    }

    public NodeMetadata getNodeMetadata(String id) {
        Objects.requireNonNull(id);
        return nodeMetadataMap.get(id);
    }

    @JsonProperty("nodes")
    public List<NodeMetadata> getNodeMetadata() {
        return ImmutableList.copyOf(nodeMetadataMap.values());
    }

    public void addWireMetadata(WireMetadata metadata) {
        Objects.requireNonNull(metadata);
        wireMetadataMap.put(metadata.getId(), metadata);
    }

    public WireMetadata getWireMetadata(String id) {
        Objects.requireNonNull(id);
        return wireMetadataMap.get(id);
    }

    @JsonProperty("wires")
    public List<WireMetadata> getWireMetadata() {
        return ImmutableList.copyOf(wireMetadataMap.values());
    }

    public void addLineMetadata(LineMetadata metadata) {
        Objects.requireNonNull(metadata);
        lineMetadataMap.put(metadata.getId(), metadata);
    }

    public LineMetadata getLineMetadata(String id) {
        Objects.requireNonNull(id);
        return lineMetadataMap.get(id);
    }

    @JsonProperty("lines")
    public List<LineMetadata> getLineMetadata() {
        return ImmutableList.copyOf(lineMetadataMap.values());
    }

    public void addFeederInfoMetadata(FeederInfoMetadata metadata) {
        Objects.requireNonNull(metadata);
        feederInfoMetadataMap.put(metadata.getId(), metadata);
    }

    public FeederInfoMetadata getFeederInfoMetadata(String id) {
        Objects.requireNonNull(id);
        return feederInfoMetadataMap.get(id);
    }

    @JsonProperty("feederInfos")
    public List<FeederInfoMetadata> getFeederInfoMetadata() {
        return ImmutableList.copyOf(feederInfoMetadataMap.values());
    }

    public void addElectricalNodeInfoMetadata(ElectricalNodeInfoMetadata metadata) {
        Objects.requireNonNull(metadata);
        electricalNodeInfoMetadataMap.put(metadata.getId(), metadata);
    }

    public ElectricalNodeInfoMetadata getElectricalNodeInfoMetadata(String id) {
        Objects.requireNonNull(id);
        return electricalNodeInfoMetadataMap.get(id);
    }

    @JsonProperty("electricalNodeInfos")
    public List<ElectricalNodeInfoMetadata> getElectricalNodeInfoMetadata() {
        return ImmutableList.copyOf(electricalNodeInfoMetadataMap.values());
    }

    @JsonProperty("layoutParams")
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;

    }
}
