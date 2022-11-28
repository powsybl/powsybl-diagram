/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.WeightedPseudograph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Graph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, BusNode> busNodes = new LinkedHashMap<>();
    private final Map<String, Edge> edges = new LinkedHashMap<>();
    private double minX = 0;
    private double minY = 0;
    private double maxX = 0;
    private double maxY = 0;

    private final org.jgrapht.Graph<Node, Edge> voltageLevelGraph = new WeightedPseudograph<>(Edge.class);
    private final org.jgrapht.Graph<Node, Edge> busGraph = new Pseudograph<>(Edge.class);
    private final Map<TextEdge, Pair<VoltageLevelNode, TextNode>> textEdges = new LinkedHashMap<>();

    private static final String DIAGRAM_ID_SUFFIX_FOR_TEXT_NODE = "-textnode";
    private static final String DIAGRAM_ID_SUFFIX_FOR_TEXT_EDGE = "-textedge";

    public void addNode(Node node) {
        Objects.requireNonNull(node);
        nodes.put(node.getEquipmentId(), node);
        voltageLevelGraph.addVertex(node);
        if (node instanceof VoltageLevelNode) {
            ((VoltageLevelNode) node).getBusNodeStream().forEach(b -> {
                busGraph.addVertex(b);
                busNodes.put(b.getEquipmentId(), b);
            });
        }
        if (node instanceof ThreeWtNode || node instanceof BoundaryNode) {
            busGraph.addVertex(node);
        }
    }

    public void addTextNode(VoltageLevelNode vlNode) {
        Objects.requireNonNull(vlNode);
        addEdge(vlNode,
                new TextNode(vlNode.getDiagramId() + DIAGRAM_ID_SUFFIX_FOR_TEXT_NODE),
                new TextEdge(vlNode.getDiagramId() + DIAGRAM_ID_SUFFIX_FOR_TEXT_EDGE));
    }

    public void addEdge(VoltageLevelNode node1, BusNode busNode1,
                        VoltageLevelNode node2, BusNode busNode2, BranchEdge edge) {
        addVoltageLevelsEdge(node1, node2, edge);
        addBusesEdge(busNode1, busNode2, edge);
    }

    public void addEdge(VoltageLevelNode vlNode, BusNode busNode, ThreeWtNode tNode, ThreeWtEdge edge) {
        addVoltageLevelsEdge(vlNode, tNode, edge);
        addBusesEdge(busNode, tNode, edge);
    }

    public void addEdge(VoltageLevelNode vlNode, BusNode busNode, BoundaryNode boundaryNode, DanglingLineEdge edge) {
        addVoltageLevelsEdge(vlNode, boundaryNode, edge);
        addBusesEdge(busNode, boundaryNode, edge);
    }

    public void addEdge(VoltageLevelNode vlNode, TextNode textNode, TextEdge edge) {
        Objects.requireNonNull(vlNode);
        Objects.requireNonNull(textNode);
        Objects.requireNonNull(edge);
        textEdges.put(edge, Pair.of(vlNode, textNode));
    }

    private void addVoltageLevelsEdge(Node node1, Node node2, Edge edge) {
        Objects.requireNonNull(node1);
        Objects.requireNonNull(node2);
        Objects.requireNonNull(edge);
        edges.put(edge.getEquipmentId(), edge);
        voltageLevelGraph.addEdge(node1, node2, edge);
    }

    private void addBusesEdge(BusNode node1, Node node2, Edge edge) {
        Objects.requireNonNull(node1);
        Objects.requireNonNull(node2);
        Objects.requireNonNull(edge);
        if (node1 == BusNode.UNKNOWN || node2 == BusNode.UNKNOWN) {
            busGraph.addVertex(BusNode.UNKNOWN);
        }
        busGraph.addEdge(node1, node2, edge);
    }

    public Stream<BusNode> getBusNodesStream() {
        return busNodes.values().stream();
    }

    public Stream<Node> getNodesStream() {
        return voltageLevelGraph.vertexSet().stream();
    }

    public Stream<VoltageLevelNode> getVoltageLevelNodesStream() {
        return nodes.values().stream().filter(VoltageLevelNode.class::isInstance).map(VoltageLevelNode.class::cast);
    }

    public Stream<ThreeWtNode> getThreeWtNodesStream() {
        return nodes.values().stream().filter(ThreeWtNode.class::isInstance).map(ThreeWtNode.class::cast);
    }

    public Stream<BoundaryNode> getBoundaryNodesStream() {
        return nodes.values().stream().filter(BoundaryNode.class::isInstance).map(BoundaryNode.class::cast);
    }

    public Stream<TextNode> getTextNodesStream() {
        return textEdges.values().stream().map(Pair::getSecond);
    }

    public Collection<Pair<VoltageLevelNode, TextNode>> getVoltageLevelTextPairs() {
        return Collections.unmodifiableCollection(textEdges.values());
    }

    public Stream<Edge> getEdgesStream() {
        return edges.values().stream();
    }

    public Collection<Edge> getEdges() {
        return Collections.unmodifiableCollection(voltageLevelGraph.edgeSet());
    }

    public Stream<Edge> getEdgeStream(Node node) {
        return voltageLevelGraph.edgesOf(node).stream();
    }

    public Stream<ThreeWtEdge> getThreeWtEdgeStream(ThreeWtNode node) {
        return voltageLevelGraph.edgesOf(node).stream().filter(ThreeWtEdge.class::isInstance).map(ThreeWtEdge.class::cast);
    }

    public Stream<BranchEdge> getBranchEdgeStream(Node node) {
        return getEdgeStream(node)
                .filter(BranchEdge.class::isInstance)
                .map(BranchEdge.class::cast);
    }

    public Collection<Edge> getBusEdges(BusNode busNode) {
        return busGraph.edgesOf(busNode);
    }

    public Stream<BranchEdge> getBranchEdgeStream() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(BranchEdge.class::isInstance)
                .map(BranchEdge.class::cast);
    }

    public List<BranchEdge> getBranchEdges() {
        return getBranchEdgeStream().collect(Collectors.toList());
    }

    public Stream<TextEdge> getTextEdgesStream() {
        return textEdges.keySet().stream();
    }

    public List<TextEdge> getTextEdges() {
        return getTextEdgesStream().collect(Collectors.toList());
    }

    public Map<TextEdge, Pair<VoltageLevelNode, TextNode>> getTextEdgesMap() {
        return Collections.unmodifiableMap(textEdges);
    }

    public Stream<BranchEdge> getNonMultiBranchEdgesStream() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(BranchEdge.class::isInstance)
                .map(BranchEdge.class::cast)
                .filter(e -> voltageLevelGraph.getAllEdges(voltageLevelGraph.getEdgeSource(e), voltageLevelGraph.getEdgeTarget(e)).size() == 1);
    }

    public Stream<List<BranchEdge>> getMultiBranchEdgesStream() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(e -> !isLoop(e))
                .map(e -> voltageLevelGraph.getAllEdges(voltageLevelGraph.getEdgeSource(e), voltageLevelGraph.getEdgeTarget(e)))
                .filter(e -> e.size() > 1)
                .distinct()
                .map(e -> e.stream().filter(BranchEdge.class::isInstance).map(BranchEdge.class::cast).collect(Collectors.toList()))
                .filter(e -> e.size() > 1);
    }

    public Map<VoltageLevelNode, List<BranchEdge>> getLoopBranchEdgesMap() {
        return voltageLevelGraph.vertexSet().stream()
                .map(n -> voltageLevelGraph.getAllEdges(n, n).stream()
                        .filter(BranchEdge.class::isInstance).map(BranchEdge.class::cast)
                        .collect(Collectors.toList()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toMap(l -> getVoltageLevelNode1(l.get(0)), l -> l));
    }

    public Stream<ThreeWtEdge> getThreeWtEdgesStream() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(ThreeWtEdge.class::isInstance)
                .map(ThreeWtEdge.class::cast);
    }

    public List<ThreeWtEdge> getThreeWtEdges() {
        return getThreeWtEdgesStream().collect(Collectors.toList());
    }

    public Stream<DanglingLineEdge> getDanglingLineEdgesStream() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(DanglingLineEdge.class::isInstance)
                .map(DanglingLineEdge.class::cast);
    }

    public List<DanglingLineEdge> getDanglingLineEdges() {
        return getDanglingLineEdgesStream().collect(Collectors.toList());
    }

    public Optional<Node> getNode(String equipmentId) {
        return Optional.ofNullable(nodes.get(equipmentId));
    }

    public Optional<VoltageLevelNode> getVoltageLevelNode(String voltageLevelId) {
        return getNode(voltageLevelId).filter(VoltageLevelNode.class::isInstance).map(VoltageLevelNode.class::cast);
    }

    public VoltageLevelNode getVoltageLevelNode(TextEdge textEdge) {
        return textEdges.get(textEdge).getFirst();
    }

    public BusNode getBusNode(String busId) {
        return busNodes.get(busId);
    }

    public org.jgrapht.Graph<Node, Edge> getJgraphtGraph(boolean includeTextNodes) {
        if (includeTextNodes) {
            org.jgrapht.Graph<Node, Edge> graphWithTextNodes = new WeightedPseudograph<>(Edge.class);
            voltageLevelGraph.vertexSet().forEach(graphWithTextNodes::addVertex);
            voltageLevelGraph.edgeSet().forEach(e -> graphWithTextNodes.addEdge(voltageLevelGraph.getEdgeSource(e), voltageLevelGraph.getEdgeTarget(e), e));
            textEdges.values().forEach(nodePair -> graphWithTextNodes.addVertex(nodePair.getSecond()));
            textEdges.forEach((edge, nodePair) -> {
                graphWithTextNodes.addEdge(nodePair.getFirst(), nodePair.getSecond(), edge);
                graphWithTextNodes.setEdgeWeight(edge, 1);
            });
            return graphWithTextNodes;
        } else {
            return voltageLevelGraph;
        }
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setDimensions(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public Node getNode1(Edge edge) {
        return voltageLevelGraph.getEdgeSource(edge);
    }

    public Node getNode2(Edge edge) {
        return voltageLevelGraph.getEdgeTarget(edge);
    }

    public VoltageLevelNode getVoltageLevelNode(BranchEdge edge, BranchEdge.Side side) {
        return side == BranchEdge.Side.ONE ? getVoltageLevelNode1(edge) : getVoltageLevelNode2(edge);
    }

    public VoltageLevelNode getVoltageLevelNode1(BranchEdge edge) {
        return (VoltageLevelNode) voltageLevelGraph.getEdgeSource(edge);
    }

    public VoltageLevelNode getVoltageLevelNode2(BranchEdge edge) {
        return (VoltageLevelNode) voltageLevelGraph.getEdgeTarget(edge);
    }

    public VoltageLevelNode getVoltageLevelNode(ThreeWtEdge edge) {
        return (VoltageLevelNode) voltageLevelGraph.getEdgeSource(edge);
    }

    public VoltageLevelNode getNetworkNode(DanglingLineEdge edge) {
        return (VoltageLevelNode) voltageLevelGraph.getEdgeSource(edge);
    }

    public BoundaryNode getBoundaryNode(DanglingLineEdge edge) {
        return (BoundaryNode) voltageLevelGraph.getEdgeTarget(edge);
    }

    public ThreeWtNode getThreeWtNode(ThreeWtEdge edge) {
        return (ThreeWtNode) voltageLevelGraph.getEdgeTarget(edge);
    }

    public BusNode getBusGraphNode(BranchEdge edge, BranchEdge.Side side) {
        return (BusNode) (side == BranchEdge.Side.ONE ? getBusGraphNode1(edge) : getBusGraphNode2(edge));
    }

    public BusNode getBusGraphNode(ThreeWtEdge edge) {
        return (BusNode) getBusGraphNode1(edge);
    }

    public Node getBusGraphNode1(Edge edge) {
        return busGraph.getEdgeSource(edge);
    }

    public Node getBusGraphNode2(Edge edge) {
        return busGraph.getEdgeTarget(edge);
    }

    public boolean containsEdge(String equipmentId) {
        return edges.containsKey(equipmentId);
    }

    public boolean containsNode(String equipmentId) {
        return nodes.containsKey(equipmentId);
    }

    public boolean isLoop(Edge edge) {
        return getNode1(edge) == getNode2(edge);
    }

    public Map<String, Point> getNodePositions() {
        return getVoltageLevelNodesStream()
                .filter(VoltageLevelNode::isVisible)
                .collect(Collectors.toMap(
                        VoltageLevelNode::getEquipmentId,
                        VoltageLevelNode::getPosition
                ));
    }
}
