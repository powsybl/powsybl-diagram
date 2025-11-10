/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.diagram.metadata.AbstractMetadata;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.layout.TextPosition;
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.SvgEdgeInfo;
import com.powsybl.nad.svg.SvgParameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class DiagramMetadata extends AbstractMetadata {

    private final LayoutParameters layoutParameters;
    private final SvgParameters svgParameters;
    private final List<BusNodeMetadata> busNodesMetadata = new ArrayList<>();
    private final List<NodeMetadata> nodesMetadata = new ArrayList<>();
    private final List<EdgeMetadata> edgesMetadata = new ArrayList<>();
    private final List<TextNodeMetadata> textNodesMetadata = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY) // only serializes it non-empty
    @JsonSetter(nulls = Nulls.AS_EMPTY) // if missing when deserializing creates an empty array
    private final List<InjectionMetadata> injectionsMetadata = new ArrayList<>();

    public DiagramMetadata(LayoutParameters layoutParameters, SvgParameters svgParameters) {
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        this.svgParameters = Objects.requireNonNull(svgParameters);
    }

    @JsonCreator
    public DiagramMetadata(@JsonProperty("layoutParameters") LayoutParameters layoutParameters,
                           @JsonProperty("svgParameters") SvgParameters svgParameters,
                           @JsonProperty("busNodes") List<BusNodeMetadata> busNodesMetadata,
                           @JsonProperty("nodes") List<NodeMetadata> nodesMetadata,
                           @JsonProperty("injections") List<InjectionMetadata> injectionsMetadata,
                           @JsonProperty("edges") List<EdgeMetadata> edgesMetadata,
                           @JsonProperty("textNodes") List<TextNodeMetadata> textNodesMetadata) {
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.busNodesMetadata.addAll(busNodesMetadata);
        this.nodesMetadata.addAll(nodesMetadata);
        this.injectionsMetadata.addAll(injectionsMetadata);
        this.edgesMetadata.addAll(edgesMetadata);
        this.textNodesMetadata.addAll(textNodesMetadata);
    }

    @JsonProperty("busNodes")
    public List<BusNodeMetadata> getBusNodesMetadata() {
        return busNodesMetadata;
    }

    @JsonProperty("nodes")
    public List<NodeMetadata> getNodesMetadata() {
        return nodesMetadata;
    }

    @JsonProperty("injections")
    public List<InjectionMetadata> getInjectionsMetadata() {
        return injectionsMetadata;
    }

    @JsonProperty("edges")
    public List<EdgeMetadata> getEdgesMetadata() {
        return edgesMetadata;
    }

    @JsonProperty("textNodes")
    public List<TextNodeMetadata> getTextNodesMetadata() {
        return textNodesMetadata;
    }

    @JsonProperty("layoutParameters")
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @JsonProperty("svgParameters")
    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public DiagramMetadata addMetadata(Graph graph) {
        graph.getVoltageLevelNodesStream().forEach(vlNode -> vlNode.getBusNodeStream().forEach(busNode -> busNodesMetadata.add(new BusNodeMetadata(
                getPrefixedId(busNode.getSvgId()),
                busNode.getEquipmentId(),
                busNode.getRingIndex(),
                getPrefixedId(vlNode.getSvgId()),
                busNode.getLegend()))));
        graph.getNodesStream().forEach(node -> nodesMetadata.add(createNodeMetadata(node)));
        graph.getVoltageLevelNodesStream().forEach(
                vlNode -> vlNode.getBusNodeStream().forEach(
                        busNode -> busNode.getInjections().forEach(
                                injection -> injectionsMetadata.add(new InjectionMetadata(
                                        injection.getSvgId(),
                                        injection.getEquipmentId(),
                                        injection.getComponentType(),
                                        busNode.getSvgId(),
                                        vlNode.getSvgId(),
                                        injection.getSvgEdgeInfo().map(DiagramMetadata::createEdgeInfoMetadata).orElse(null))))));
        graph.getBranchEdgeStream().forEach(edge -> edgesMetadata.add(new EdgeMetadata(
                getPrefixedId(edge.getSvgId()),
                edge.getEquipmentId(),
                getPrefixedId(graph.getNode1(edge).getSvgId()),
                getPrefixedId(graph.getNode2(edge).getSvgId()),
                getPrefixedId(graph.getBusGraphNode1(edge).getSvgId()),
                getPrefixedId(graph.getBusGraphNode2(edge).getSvgId()),
                edge.getType(),
                edge.getLabel(),
                edge.getSvgEdgeInfo(BranchEdge.Side.ONE).map(DiagramMetadata::createEdgeInfoMetadata).orElse(null),
                edge.getSvgEdgeInfo(BranchEdge.Side.TWO).map(DiagramMetadata::createEdgeInfoMetadata).orElse(null))));
        graph.getThreeWtEdgesStream().forEach(edge -> {
            String threeWtNodeSvgId = graph.getThreeWtNode(edge).getSvgId();
            edgesMetadata.add(new EdgeMetadata(
                    getPrefixedId(edge.getSvgId()),
                    edge.getEquipmentId(),
                    getPrefixedId(graph.getVoltageLevelNode(edge).getSvgId()),
                    getPrefixedId(threeWtNodeSvgId),
                    getPrefixedId(graph.getBusGraphNode(edge).getSvgId()),
                    getPrefixedId(threeWtNodeSvgId),
                    edge.getType(),
                    null,
                    edge.getSvgEdgeInfo().map(DiagramMetadata::createEdgeInfoMetadata).orElse(null),
                    null));
        });
        graph.getVoltageLevelTextPairs().forEach(textPair -> textNodesMetadata.add(new TextNodeMetadata(
                getPrefixedId(textPair.getSecond().getSvgId()),
                textPair.getFirst().getEquipmentId(),
                getPrefixedId(textPair.getFirst().getSvgId()),
                round(textPair.getSecond().getX() - textPair.getFirst().getX()),
                round(textPair.getSecond().getY() - textPair.getFirst().getY()),
                round(textPair.getSecond().getEdgeConnection().getX() - textPair.getFirst().getX()),
                round(textPair.getSecond().getEdgeConnection().getY() - textPair.getFirst().getY()))));
        return this;
    }

    private NodeMetadata createNodeMetadata(Node node) {
        if (node instanceof VoltageLevelNode vlNode) {
            return new NodeMetadata(
                    getPrefixedId(node.getSvgId()),
                    node.getEquipmentId(),
                    vlNode.getBusNodesCount(),
                    round(node.getX()),
                    round(node.getY()),
                    node.isFictitious(),
                    vlNode.getLegendSvgId(),
                    vlNode.getLegendEdgeSvgId(),
                    vlNode.getLegendHeader(),
                    vlNode.getLegendFooter());
        } else {
            return new NodeMetadata(
                    getPrefixedId(node.getSvgId()),
                    node.getEquipmentId(),
                    0,
                    round(node.getX()),
                    round(node.getY()),
                    node.isFictitious(),
                    null,
                    null,
                    null,
                    null);
        }
    }

    private static EdgeInfoMetadata createEdgeInfoMetadata(SvgEdgeInfo svgEdgeInfo) {
        EdgeInfo edgeInfo = svgEdgeInfo.edgeInfo();
        return new EdgeInfoMetadata(svgEdgeInfo.svgId(),
                edgeInfo.getInfoType(),
                edgeInfo.getDirection().map(Enum::name).orElse(null),
                edgeInfo.getInternalLabel().orElse(null),
                edgeInfo.getExternalLabel().orElse(null));
    }

    private String getPrefixedId(String id) {
        return svgParameters.getSvgPrefix() + id;
    }

    private double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    public static DiagramMetadata parseJson(Path file) {
        Objects.requireNonNull(file);
        try (Reader reader = Files.newBufferedReader(file)) {
            return parseJson(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static DiagramMetadata parseJson(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            return objectMapper.readValue(inputStream, DiagramMetadata.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static DiagramMetadata parseJson(Reader reader) {
        Objects.requireNonNull(reader);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            return objectMapper.readValue(reader, DiagramMetadata.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @JsonIgnore
    public Map<String, Point> getFixedPositions() {
        return nodesMetadata.stream().collect(Collectors.toMap(NodeMetadata::getEquipmentId, NodeMetadata::getPosition));
    }

    @JsonIgnore
    public Map<String, TextPosition> getFixedTextPositions() {
        return textNodesMetadata.stream().collect(Collectors.toMap(TextNodeMetadata::getEquipmentId, TextNodeMetadata::getTextPosition));
    }
}
