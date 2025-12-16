/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.diagram.metadata.AbstractMetadata;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.SldComponent;
import com.powsybl.sld.model.coordinate.Direction;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class GraphMetadata extends AbstractMetadata {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    // On some systems, the export order is determined by the order of the 1st encountered JsonCreator's attributes
    // and "unescapedId" is put in last place. But on other systems, the export order is determined by the getters' order
    // and "unescapedId" is put in 1st place, which leads to comparison errors in the unit tests.
    // To prevent this discrepancy, the order is manually fixed.
    @JsonPropertyOrder(value = {"unescapedId", "id", "vid", "nextVId", "componentType", "open", "direction", "vlabel", "equipmentId", "labels"})
    public static class NodeMetadata {

        private final String unescapedId;

        private final String id;

        private final String componentType;

        private final boolean open;

        private final String vId;

        private final String nextVId;

        private final Direction direction;

        private final boolean vLabel;

        private final String equipmentId;

        private final List<NodeLabelMetadata> labels;


        /**
         * @deprecated use {@link NodeMetadata#NodeMetadata(String, String, String, String, String, boolean, Direction, boolean, String, List)} instead.
         */
        @Deprecated(since = "4.0.0", forRemoval = true)
        public NodeMetadata(@JsonProperty("id") String escapedId,
                            @JsonProperty("vid") String vId,
                            @JsonProperty("nextVId") String nextVId,
                            @JsonProperty("componentType") String componentType,
                            @JsonProperty("open") boolean open,
                            @JsonProperty("direction") Direction direction,
                            @JsonProperty("vlabel") boolean vLabel,
                            @JsonProperty("equipmentId") String equipmentId,
                            @JsonProperty("labels") List<NodeLabelMetadata> labels) {
            this(null, escapedId, vId, nextVId, componentType, open, direction, vLabel, equipmentId, labels);
        }

        @JsonCreator
        public NodeMetadata(@JsonProperty("unescapedId") String unescapedId,
                            @JsonProperty("id") String escapedId,
                            @JsonProperty("vid") String vId,
                            @JsonProperty("nextVId") String nextVId,
                            @JsonProperty("componentType") String componentType,
                            @JsonProperty("open") boolean open,
                            @JsonProperty("direction") Direction direction,
                            @JsonProperty("vlabel") boolean vLabel,
                            @JsonProperty("equipmentId") String equipmentId,
                            @JsonProperty("labels") List<NodeLabelMetadata> labels) {
            this.unescapedId = unescapedId;
            this.id = Objects.requireNonNull(escapedId);
            this.vId = Objects.requireNonNull(vId);
            this.nextVId = nextVId;
            this.componentType = componentType;
            this.open = open;
            this.direction = direction;
            this.vLabel = vLabel;
            this.equipmentId = equipmentId;
            this.labels = Objects.requireNonNull(labels);
        }

        public String getUnescapedId() {
            return unescapedId;
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

        public boolean isOpen() {
            return open;
        }

        public Direction getDirection() {
            return direction;
        }

        @JsonProperty("direction")
        public Direction getNullableDirection() {
            return direction == Direction.UNDEFINED ? null : direction;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NodeLabelMetadata(String id, String positionName, String userDefinedId) {
        @JsonCreator
        public NodeLabelMetadata(@JsonProperty("id") String id,
                                 @JsonProperty("positionName") String positionName,
                                 @JsonProperty("userDefinedId") String userDefinedId) {
            this.id = Objects.requireNonNull(id);
            this.positionName = Objects.requireNonNull(positionName);
            this.userDefinedId = userDefinedId;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record WireMetadata(String id, String nodeId1, String nodeId2, boolean straight, boolean snakeLine) {
        @JsonCreator
        public WireMetadata(@JsonProperty("id") String id,
                            @JsonProperty("nodeId1") String nodeId1,
                            @JsonProperty("nodeId2") String nodeId2,
                            @JsonProperty("straight") boolean straight,
                            @JsonProperty("snakeLine") boolean snakeLine) {
            this.id = Objects.requireNonNull(id);
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
            this.straight = straight;
            this.snakeLine = snakeLine;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LineMetadata(String id, String nodeId1, String nodeId2) {

        @JsonCreator
        public LineMetadata(@JsonProperty("id") String id,
                            @JsonProperty("nodeId1") String nodeId1,
                            @JsonProperty("nodeId2") String nodeId2) {
            this.id = Objects.requireNonNull(id);
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FeederInfoMetadata(String id, String equipmentId, String side, String componentType,
                                     String userDefinedId) {

        @JsonCreator
        public FeederInfoMetadata(@JsonProperty("id") String id, @JsonProperty("equipmentId") String equipmentId, @JsonProperty("side") String side, @JsonProperty("componentType") String componentType, @JsonProperty("userDefinedId") String userDefinedId) {
            this.id = Objects.requireNonNull(id);
            this.equipmentId = Objects.requireNonNull(equipmentId);
            this.side = side;
            this.componentType = componentType;
            this.userDefinedId = userDefinedId;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BusInfoMetadata(String id, String busNodeId, String userDefinedId) {

        @JsonCreator
        public BusInfoMetadata(@JsonProperty("id") String id, @JsonProperty("busNodeId") String busNodeId, @JsonProperty("userDefinedId") String userDefinedId) {
            this.id = Objects.requireNonNull(id);
            this.busNodeId = Objects.requireNonNull(busNodeId);
            this.userDefinedId = userDefinedId;
        }
    }

    private final Map<String, SldComponent> componentByType = new HashMap<>();

    private final Map<String, NodeMetadata> nodeMetadataMap = new HashMap<>();

    private final Map<String, WireMetadata> wireMetadataMap = new HashMap<>();

    private final Map<String, LineMetadata> lineMetadataMap = new HashMap<>();

    private final LayoutParameters layoutParameters;
    private final SvgParameters svgParameters;

    private final Map<String, FeederInfoMetadata> feederInfoMetadataMap = new HashMap<>();

    private final Map<String, BusInfoMetadata> busInfoMetadataMap = new HashMap<>();

    public GraphMetadata(LayoutParameters layoutParameters, SvgParameters svgParameters) {
        this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), layoutParameters, svgParameters);
    }

    @JsonCreator
    public GraphMetadata(@JsonProperty("components") List<SldComponent> componentList,
                         @JsonProperty("nodes") List<NodeMetadata> nodeMetadataList,
                         @JsonProperty("wires") List<WireMetadata> wireMetadataList,
                         @JsonProperty("lines") List<LineMetadata> lineMetadataList,
                         @JsonProperty("feederInfos") List<FeederInfoMetadata> feederInfoMetadataList,
                         @JsonProperty("busInfos") List<BusInfoMetadata> busInfoMetadataList,
                         @JsonProperty("layoutParams") LayoutParameters layoutParams,
                         @JsonProperty("svgParams") SvgParameters svgParams) {
        for (SldComponent component : componentList) {
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
        for (BusInfoMetadata busInfoMetadata : busInfoMetadataList) {
            addBusInfoMetadata(busInfoMetadata);
        }
        layoutParameters = layoutParams;
        svgParameters = svgParams;
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

    public void addComponent(SldComponent component) {
        Objects.requireNonNull(component);
        componentByType.put(component.getType(), component);
    }

    public SldComponent getComponentMetadata(String componentType) {
        return componentType != null ? componentByType.get(componentType) : null;
    }

    public List<AnchorPoint> getAnchorPoints(String type) {
        SldComponent component = getComponentMetadata(type);
        return component != null ? component.getAnchorPoints()
                                         : Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
    }

    @JsonProperty("components")
    public List<SldComponent> getComponentMetadata() {
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
        wireMetadataMap.put(metadata.id(), metadata);
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
        lineMetadataMap.put(metadata.id(), metadata);
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
        feederInfoMetadataMap.put(metadata.id(), metadata);
    }

    public FeederInfoMetadata getFeederInfoMetadata(String id) {
        Objects.requireNonNull(id);
        return feederInfoMetadataMap.get(id);
    }

    @JsonProperty("feederInfos")
    public List<FeederInfoMetadata> getFeederInfoMetadata() {
        return ImmutableList.copyOf(feederInfoMetadataMap.values());
    }

    public void addBusInfoMetadata(BusInfoMetadata metadata) {
        Objects.requireNonNull(metadata);
        busInfoMetadataMap.put(metadata.id(), metadata);
    }

    public BusInfoMetadata getBusInfoMetadata(String id) {
        Objects.requireNonNull(id);
        return busInfoMetadataMap.get(id);
    }

    @JsonProperty("busInfos")
    public List<BusInfoMetadata> getBusInfoMetadata() {
        return ImmutableList.copyOf(busInfoMetadataMap.values());
    }

    @JsonProperty("layoutParams")
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;

    }

    @JsonProperty("svgParams")
    public SvgParameters getSvgParameters() {
        return svgParameters;

    }
}
