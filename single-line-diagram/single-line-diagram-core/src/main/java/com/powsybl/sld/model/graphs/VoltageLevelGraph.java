/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.cells.*;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.Node.NodeType;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.util.GraphTraversal;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

/**
 * This class builds the connectivity among the elements of a voltageLevel
 * buildGraphAndDetectCell establishes the List of nodes, edges and nodeBuses
 * cells is built by the PatternCellDetector Class
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class VoltageLevelGraph extends AbstractBaseGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelGraph.class);

    private final VoltageLevelInfos voltageLevelInfos;

    private final List<Node> nodes = new ArrayList<>();

    private final List<Edge> edges = new ArrayList<>();

    private final SortedSet<Cell> cells = new TreeSet<>(
            Comparator.comparingInt(Cell::getNumber)); // cells sorted to avoid randomness

    private final Map<Node.NodeType, List<Node>> nodesByType = new EnumMap<>(Node.NodeType.class);

    private final Map<String, Node> nodesById = new HashMap<>();

    private final Map<Node, Cell> nodeToCell = new HashMap<>();

    private int maxHorizontalBusPosition = 0;
    private int maxVerticalBusPosition = 0;

    private int cellCounter = 0;

    private final Point coord = new Point(0, 0);

    private final boolean forVoltageLevelDiagram;  // true if voltageLevel diagram
    // false if substation diagram

    // by direction, max calculated height of the extern cells
    // If no extern cell found taking into account intern cells too
    // (filled and used only when using the adapt cell height to content option)
    private Map<Direction, Double> maxCellHeight = new EnumMap<>(Direction.class);

    public VoltageLevelGraph(VoltageLevelInfos voltageLevelInfos, Graph parentGraph) {
        super(parentGraph);
        this.voltageLevelInfos = Objects.requireNonNull(voltageLevelInfos);
        this.forVoltageLevelDiagram = parentGraph == null;
    }

    @Override
    public String getId() {
        return voltageLevelInfos.getId();
    }

    public boolean isForVoltageLevelDiagram() {
        return forVoltageLevelDiagram;
    }

    public int getNextCellNumber() {
        return cellCounter++;
    }

    public void removeUnnecessaryConnectivityNodes() {
        List<Node> fictitiousNodesToRemove = nodes.stream()
                .filter(ConnectivityNode.class::isInstance)
                .collect(Collectors.toList());
        for (Node n : fictitiousNodesToRemove) {
            if (n.getAdjacentEdges().size() == 2) {
                List<Node> adjNodes = n.getAdjacentNodes();
                Node node1 = adjNodes.get(0);
                Node node2 = adjNodes.get(1);
                LOGGER.info("Remove fictitious node {} between {} and {}", n.getId(), node1.getId(), node2.getId());
                removeNode(n);
                addEdge(node1, node2);
            } else {
                LOGGER.info("Working on fictitious node {} with {} adjacent nodes", n.getId(), n.getAdjacentNodes().size());
                Node busNode = n.getAdjacentNodes().stream().filter(node -> node.getType() == Node.NodeType.BUS).findFirst().orElse(null);
                if (busNode != null) {
                    n.getAdjacentNodes().stream().filter(node -> !node.equals(busNode)).forEach(node -> {
                        LOGGER.info("Connecting {} to {}", node.getId(), busNode.getId());
                        addEdge(node, busNode);
                    });
                    LOGGER.info("Remove fictitious node {}", n.getId());
                    removeNode(n);
                } else {
                    LOGGER.warn("Cannot remove fictitious node {} because there are no adjacent BUS nodes", n.getId());
                }
            }
        }
    }

    public void removeFictitiousSwitchNode() {
        List<SwitchNode> fictitiousSwitchNodesToRemove = this.getNodes().stream()
                .filter(SwitchNode.class::isInstance)
                .map(SwitchNode.class::cast)
                .filter(SwitchNode::isFictitious)
                .filter(node -> node.getAdjacentNodes().size() == 2)
                .collect(Collectors.toList());
        for (SwitchNode n : fictitiousSwitchNodesToRemove) {
            Node node1 = n.getAdjacentNodes().get(0);
            Node node2 = n.getAdjacentNodes().get(1);
            LOGGER.info("Remove fictitious switch node {} between {} and {}", n.getName(), node1.getId(), node2.getId());
            this.removeNode(n);
            this.addEdge(node1, node2);
        }
    }

    public void logCellDetectionStatus() {
        Set<Cell> cellsLog = new HashSet<>();
        Map<Cell.CellType, Integer> cellCountByType = new EnumMap<>(Cell.CellType.class);
        for (Cell.CellType cellType : Cell.CellType.values()) {
            cellCountByType.put(cellType, 0);
        }
        int remainingNodeCount = 0;
        Map<Node.NodeType, Integer> remainingNodeCountByType = new EnumMap<>(Node.NodeType.class);
        for (Node.NodeType nodeType : Node.NodeType.values()) {
            remainingNodeCountByType.put(nodeType, 0);
        }
        for (Node node : nodes) {
            Optional<Cell> oCell = getCell(node);
            if (oCell.isPresent()) {
                Cell cell = oCell.get();
                if (cellsLog.add(cell)) {
                    cellCountByType.put(cell.getType(), cellCountByType.get(cell.getType()) + 1);
                }
            } else {
                remainingNodeCount++;
                remainingNodeCountByType.put(node.getType(), remainingNodeCountByType.get(node.getType()) + 1);
            }
        }
        if (cellsLog.isEmpty()) {
            LOGGER.warn("No cell detected");
        } else {
            LOGGER.info("{} cells detected ({})", cellsLog.size(), cellCountByType);
        }
        if (remainingNodeCount > 0) {
            LOGGER.warn("{}/{} nodes not associated to a cell ({})",
                    remainingNodeCount, nodes.size(), remainingNodeCountByType);
        }
    }

    public org.jgrapht.Graph<Node, Edge> toJgrapht() {
        org.jgrapht.Graph<Node, Edge> graph = new Pseudograph<>(Edge.class);
        for (Node node : nodes) {
            graph.addVertex(node);
        }
        for (Edge edge : edges) {
            graph.addEdge(edge.getNode1(), edge.getNode2(), edge);
        }
        return graph;
    }

    public void addNode(Node node) {
        if (nodes.contains(node)) {
            throw new AssertionError("The node cannot be added, it is already in the graph");
        }
        super.addNode(this, node);
        nodes.add(node);
        nodesByType.computeIfAbsent(node.getType(), nodeType -> new ArrayList<>()).add(node);
        nodesById.put(node.getId(), node);
    }

    @Override
    public void removeNode(Node node) {
        nodes.remove(node);
        super.removeNode(node);
        nodesByType.computeIfAbsent(node.getType(), nodeType -> new ArrayList<>()).remove(node);
        nodesById.remove(node.getId(), node);
        for (Edge edge : new ArrayList<>(node.getAdjacentEdges())) {
            removeEdge(edge);
        }
    }

    public Node getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    @Override
    public VoltageLevelGraph getVoltageLevel(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return voltageLevelId.equals(voltageLevelInfos.getId()) ? this : null;
    }

    @Override
    public List<VoltageLevelGraph> getVoltageLevels() {
        return Collections.singletonList(this);
    }

    @Override
    public Stream<VoltageLevelGraph> getVoltageLevelStream() {
        return Stream.of(this);
    }

    @Override
    public Stream<Node> getAllNodesStream() {
        return nodes.stream();
    }

    /**
     * Add an edge between the two nodes
     *
     * @param n1 first node
     * @param n2 second node
     */
    public Edge addEdge(Node n1, Node n2) {
        Edge edge = new Edge(n1, n2);
        edges.add(edge);
        n1.addAdjacentEdge(edge);
        n2.addAdjacentEdge(edge);
        return edge;
    }

    /**
     * Remove an edge between two nodes
     *
     * @param n1 first node
     * @param n2 second node
     * @return the removed edge
     */
    private Edge removeEdge(Node n1, Node n2) {
        for (Edge edge : n1.getAdjacentEdges()) {
            if (edge.getNode1().equals(n2) || edge.getNode2().equals(n2)) {
                removeEdge(edge);
                return edge;
            }
        }
        throw new PowsyblException("Cannot remove edge, the two node are not connected");
    }

    private void removeEdge(Edge edge) {
        edge.getNode1().removeAdjacentEdge(edge);
        edge.getNode2().removeAdjacentEdge(edge);
        edges.remove(edge);
    }

    /**
     * Resolve when one EQ is connected with 2 switchs component
     */
    private void rIdentifyConnexComponent(Node node, List<Node> nodesIn, List<Node> connexComponent) {
        if (!connexComponent.contains(node)) {
            connexComponent.add(node);
            node.getAdjacentNodes()
                    .stream()
                    .filter(nodesIn::contains)
                    .forEach(n -> rIdentifyConnexComponent(n, nodesIn, connexComponent));
        }
    }

    public List<List<Node>> getConnexComponents(List<Node> nodesIn) {
        List<Node> nodesToHandle = new ArrayList<>(nodesIn);
        List<List<Node>> result = new ArrayList<>();
        while (!nodesToHandle.isEmpty()) {
            Node n = nodesToHandle.get(0);
            List<Node> connexComponent = new ArrayList<>();
            rIdentifyConnexComponent(n, nodesIn, connexComponent);
            nodesToHandle.removeAll(connexComponent);
            result.add(connexComponent);
        }
        return result;
    }

    public void setMaxBusPosition() {
        List<Integer> h = new ArrayList<>();
        List<Integer> v = new ArrayList<>();
        getNodeBuses().forEach(nodeBus -> {
            v.add(nodeBus.getBusbarIndex());
            h.add(nodeBus.getSectionIndex());
        });
        if (h.isEmpty() || v.isEmpty()) {
            return;
        }
        setMaxHorizontalBusPosition(Collections.max(h));
        setMaxVerticalBusPosition(Collections.max(v));
    }

    public void insertBusConnections(Predicate<Node> nodesOnBus) {
        getNodeBuses().forEach(busNode -> insertBusConnections(busNode, nodesOnBus));
    }

    private void insertBusConnections(BusNode busNode, Predicate<Node> nodesOnBus) {
        busNode.getAdjacentNodes().stream()
                .filter(node -> !nodesOnBus.test(node))
                .forEach(node -> insertBusConnection(busNode, node));
    }

    private void insertBusConnection(BusNode busNode, Node nodeConnectedToBusNode) {
        Node fNodeToBus = NodeFactory.createBusConnection(this, busNode.getId() + "_" + nodeConnectedToBusNode.getId());
        insertNode(busNode, fNodeToBus, nodeConnectedToBusNode);
    }

    private void insertNode(Node nodeA, Node nodeToInsert, Node nodeB) {
        Edge removedEdge = removeEdge(nodeA, nodeB);
        if (removedEdge.getNode1() == nodeA) {
            addEdge(nodeA, nodeToInsert);
            addEdge(nodeToInsert, nodeB);
        } else {
            addEdge(nodeB, nodeToInsert);
            addEdge(nodeToInsert, nodeA);
        }

        if (nodeA.isOverloaded() || nodeB.isOverloaded()) {
            nodeToInsert.setInOverload(true);
        }
    }

    public void insertHookNodesAtBuses() {
        getNodeBuses().forEach(this::insertHookNodesAtBuses);
    }

    private void insertHookNodesAtBuses(BusNode busNode) {
        busNode.getAdjacentNodes()
                .forEach(nodeOnBus -> nodeOnBus.getAdjacentNodes().stream()
                        .filter(n -> n.getType() != NodeType.BUS)
                        .filter(n -> !(n.getType() == NodeType.INTERNAL && !(n instanceof EquipmentNode)))
                        .forEach(n -> insertBusHookNode(nodeOnBus, n)));
    }

    private void insertBusHookNode(Node nodeOnBus, Node node) {
        // Create hook node
        Node fStackNode = NodeFactory.createConnectivityNode(this, nodeOnBus.getId() + "-" + node.getId());

        // Update edges
        if (node.getType() == NodeType.FEEDER) {
            // The feeder node might have several adjacent nodes (feeder fork for instance):
            // for display reasons we want the stack node to carry those adjacent nodes.
            transferEdges(node, fStackNode);
            addEdge(fStackNode, node);
        } else {
            insertNode(nodeOnBus, fStackNode, node);
        }
    }

    /**
     * Insert fictitious node(s) before feeders in order for the feeder to be properly displayed:
     * feeders need at least one inserted fictitious node to have enough space to display the feeder arrows.
     */
    public void insertHookNodesAtFeeders() {
        // Each feeder node needs a fictitious node to have enough place for the feeder infos (arrows)
        // FeederNode linked to Middle3WTNode do not need any fictitious node inserted, because of the fictitious Middle3WTNode
        List<Node> feederNodes = nodesByType.computeIfAbsent(Node.NodeType.FEEDER, nodeType -> new ArrayList<>());
        feederNodes.stream()
                .filter(feederNode -> !isInternal3wtFeederNode((FeederNode) feederNode))
                .forEach(this::insertFeederHookNode);
    }

    private boolean isInternal3wtFeederNode(FeederNode feederNode) {
        return feederNode.getFeeder().getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG
                && feederNode.getAdjacentNodes().get(0).getComponentType().equals(ComponentTypeName.THREE_WINDINGS_TRANSFORMER);
    }

    private void insertFeederHookNode(Node feederNode) {
        // Create a new hook node to insert before feeder node
        Node hookNode = NodeFactory.createConnectivityNode(this, feederNode.getId());

        List<Node> adjacentNodes = feederNode.getAdjacentNodes();
        if (adjacentNodes.size() == 1) {
            // Update edges: create the 2 new ones and remove the old one
            Node singleNeighbor = adjacentNodes.get(0);
            insertNode(singleNeighbor, hookNode, feederNode);
        } else {
            // Create an extra fork node, otherwise the hook-node is a node with several neighbors (fork node)
            Node forkNode = NodeFactory.createConnectivityNode(this, feederNode.getId() + "_fork");

            // Transfer the neighbours of the feeder node to that new forkNode
            transferEdges(feederNode, forkNode);

            addEdge(forkNode, hookNode);
            addEdge(hookNode, feederNode);
        }
    }

    public void extendBusesConnectedToBuses() {
        getNodeBuses().forEach(n1 ->
                n1.getAdjacentNodes().stream()
                        .filter(BusNode.class::isInstance)
                        .forEach(n2 -> extendBusConnectedToBus(n1, (BusNode) n2)));
    }

    private void extendBusConnectedToBus(BusNode n1, BusNode n2) {
        removeEdge(n1, n2);
        String busToBusId = n1.getId() + "-" + n2.getId();
        Node cNode1 = NodeFactory.createConnectivityNode(this, busToBusId + "_1");
        Node cNode2 = NodeFactory.createConnectivityNode(this, busToBusId + "_2");
        addEdge(n1, cNode1);
        addEdge(cNode1, cNode2);
        addEdge(n2, cNode2);
    }

    public ConnectivityNode insertConnectivityNode(Node node1, Node node2, String id) {
        ConnectivityNode iNode = NodeFactory.createConnectivityNode(this, id);
        insertNode(node1, iNode, node2);
        return iNode;
    }

    /**
     * Substitute a node with another node already in the graph.
     *
     * @param nodeOrigin: node which will be substituted
     * @param newNode:    node which will substitute the first one
     */
    public void substituteNode(Node nodeOrigin, Node newNode) {
        transferEdges(nodeOrigin, newNode);
        removeNode(nodeOrigin);
    }

    private void transferEdges(Node nodeOrigin, Node newNode) {
        transferEdges(nodeOrigin, newNode, new ArrayList<>(nodeOrigin.getAdjacentEdges()));
    }

    public void transferEdges(Node nodeOrigin, Node newNode, List<Edge> edgesToTransfer) {
        if (!nodesById.containsKey(newNode.getId())) {
            throw new PowsyblException("New node [" + newNode.getId() + "] is not in current voltage level graph");
        }
        for (Edge edge : edgesToTransfer) {
            if (!edge.getNodes().contains(nodeOrigin)) {
                throw new PowsyblException("Edge to transfer not in adjacent edges of given node");
            }
            Node node1 = edge.getNode1() == nodeOrigin ? newNode : edge.getNode1();
            Node node2 = edge.getNode2() == nodeOrigin ? newNode : edge.getNode2();
            addEdge(node1, node2);
            removeEdge(edge);
        }
    }

    public void substituteFictitiousNodesMirroringBusNodes() {
        getNodeBuses().forEach(busNode -> {
            List<Node> adjs = busNode.getAdjacentNodes();
            if (adjs.size() == 1 && adjs.get(0).getType() == Node.NodeType.INTERNAL) {
                Node adj = adjs.get(0);
                removeEdge(adj, busNode);
                substituteNode(adj, busNode);
            }
        });
    }

    public void substituteSingularFictitiousByFeederNode() {
        getNodes().stream()
                .filter(n -> n.getType() == Node.NodeType.INTERNAL && n.getAdjacentEdges().size() == 1)
                .forEach(n -> substituteNode(n, NodeFactory.createFictitiousFeederNode(this, n.getId(), Orientation.UP)));
    }

    public void addCell(Cell c) {
        cells.add(c);
        List<Node> cellNodes = c.getNodes();
        if (c.getType() == Cell.CellType.SHUNT) {
            cellNodes.stream().skip(1).limit(((long) cellNodes.size()) - 2) // skip first and last nodes that are part of the ExternalNodes
                    .forEach(n -> nodeToCell.put(n, c));
        } else {
            cellNodes.stream().filter(n -> n.getType() != NodeType.BUS)
                    .forEach(n -> nodeToCell.put(n, c));
        }
    }

    public void removeCell(Cell c) {
        cells.remove(c);
        c.getNodes().forEach(n -> nodeToCell.remove(n, c));
    }

    public List<BusNode> getNodeBuses() {
        return nodesByType.computeIfAbsent(Node.NodeType.BUS, nodeType -> new ArrayList<>())
                .stream()
                .map(BusNode.class::cast)
                .collect(Collectors.toList());
    }

    public List<FeederNode> getFeederNodes() {
        return getFeederNodeStream().collect(Collectors.toList());
    }

    private Stream<FeederNode> getFeederNodeStream() {
        return nodesByType.computeIfAbsent(NodeType.FEEDER, nodeType -> new ArrayList<>())
                .stream()
                .map(FeederNode.class::cast);
    }

    private Stream<FeederNode> getFeederNodeStream(FeederType feederType) {
        return getFeederNodeStream().filter(feederNode -> feederNode.getFeeder().getFeederType() == feederType);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    public Stream<ConnectivityNode> getConnectivityNodeStream() {
        return nodesByType.computeIfAbsent(NodeType.INTERNAL, nodeType -> new ArrayList<>())
                .stream()
                .filter(ConnectivityNode.class::isInstance)
                .map(ConnectivityNode.class::cast);
    }

    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    public Set<Node> getNodeSet() {
        return new LinkedHashSet<>(nodes);
    }

    public Set<Edge> getEdgeSet() {
        return new LinkedHashSet<>(edges);
    }

    public Stream<Cell> getCellStream() {
        return cells.stream();
    }

    public Stream<BusCell> getBusCellStream() {
        return cells.stream().filter(BusCell.class::isInstance).map(BusCell.class::cast);
    }

    public List<BusCell> getBusCells() {
        return getBusCellStream().collect(Collectors.toList());
    }

    public Stream<InternCell> getInternCellStream() {
        return cells.stream().filter(InternCell.class::isInstance).map(InternCell.class::cast);
    }

    public Stream<ExternCell> getExternCellStream() {
        return cells.stream().filter(ExternCell.class::isInstance).map(ExternCell.class::cast);
    }

    public Stream<ArchCell> getArchCellStream() {
        return cells.stream().filter(ArchCell.class::isInstance).map(ArchCell.class::cast);
    }

    public Stream<ShuntCell> getShuntCellStream() {
        return cells.stream().filter(ShuntCell.class::isInstance).map(ShuntCell.class::cast);
    }

    @Override
    public Optional<Cell> getCell(Node node) {
        return Optional.ofNullable(nodeToCell.get(node));
    }

    public VoltageLevelInfos getVoltageLevelInfos() {
        return voltageLevelInfos;
    }

    public Point getCoord() {
        return coord;
    }

    public void setCoord(double x, double y) {
        coord.setX(x);
        coord.setY(y);
    }

    public double getX() {
        return coord.getX();
    }

    public double getY() {
        return coord.getY();
    }

    public boolean isPositionNodeBusesCalculated() {
        return getNodeBuses().stream().allMatch(n -> n.getPosition().get(H) != -1 && n.getPosition().get(V) != -1);
    }

    @Override
    public void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStartObject();

        generator.writeFieldName("voltageLevelInfos");
        voltageLevelInfos.writeJsonContent(generator);

        if (includeCoordinates) {
            generator.writeNumberField("x", getX());
            generator.writeNumberField("y", getY());
        }

        generator.writeArrayFieldStart("nodes");
        Iterator<Node> nodesIt = nodes.stream().sorted(Comparator.comparing(Node::getId)).iterator();
        while (nodesIt.hasNext()) {
            nodesIt.next().writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();

        generator.writeArrayFieldStart("cells");
        for (Cell cell : cells) {
            cell.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();

        generator.writeArrayFieldStart("edges");
        for (Edge edge : edges) {
            edge.writeJson(generator);
        }
        generator.writeEndArray();

        writeBranchFields(generator, includeCoordinates);

        generator.writeEndObject();
    }

    public int getMaxH() {
        return getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(H) + nodeBus.getPosition().getSpan(H))
                .max().orElse(0);
    }

    public int getMaxV() {
        return getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(V) + nodeBus.getPosition().getSpan(V))
                .max().orElse(0);
    }

    public Double getExternCellHeight(Direction direction) {
        if (maxCellHeight.isEmpty() || direction == Direction.MIDDLE || direction == Direction.UNDEFINED) {
            return 0.;
        }
        return maxCellHeight.get(direction);
    }

    public void setMaxCellHeight(Map<Direction, Double> maxCellHeight) {
        this.maxCellHeight = maxCellHeight;
    }

    public int getMaxHorizontalBusPosition() {
        return maxHorizontalBusPosition;
    }

    public void setMaxHorizontalBusPosition(int maxHorizontalBusPosition) {
        this.maxHorizontalBusPosition = maxHorizontalBusPosition;
    }

    public int getMaxVerticalBusPosition() {
        return maxVerticalBusPosition;
    }

    public void setMaxVerticalBusPosition(int maxVerticalBusPosition) {
        this.maxVerticalBusPosition = maxVerticalBusPosition;
    }

    public double getFirstBusY() {
        return getExternCellHeight(Direction.TOP);
    }

    public double getLastBusY(double verticalSpaceBus) {
        return getFirstBusY() + (getMaxVerticalBusPosition() - 1) * verticalSpaceBus;
    }

    public double getInnerHeight(double verticalSpaceBus) {
        return getExternCellHeight(Direction.TOP) + verticalSpaceBus * getMaxV() + getExternCellHeight(Direction.BOTTOM);
    }

    public void substituteNodesMirroringGroundDisconnectionComponent() {
        List<GroundDisconnection> groundDisconnections = getFeederNodeStream(FeederType.GROUND)
                .map(this::getGroundDisconnection)
                .filter(Objects::nonNull)
                .toList();
        for (GroundDisconnection gd : groundDisconnections) {
            gd.nodes().forEach(this::removeNode);
            substituteNode(gd.forkNode(), NodeFactory.createGroundDisconnectionNode(this, gd.disconnector(), gd.ground()));
        }
    }

    private GroundDisconnection getGroundDisconnection(FeederNode groundFeederNode) {
        Set<Node> setFromGround = new LinkedHashSet<>();
        AtomicReference<SwitchNode> aSwitch = new AtomicReference<>();
        boolean groundDisconnectionSetIdentified = GraphTraversal.run(groundFeederNode,
                node -> node.getAdjacentEdges().size() > 2, // traversal ends successfully when encountering a fork node, which will be replaced by the ground disconnection component
                node -> node.getType() == NodeType.BUS || node.getType() == NodeType.FEEDER // traversal fails and stops when encountering a bus, a feeder, or more than one switch
                        || node.getType() == NodeType.SWITCH && aSwitch.getAndSet((SwitchNode) node) != null,
                setFromGround,
                Collections.emptySet());
        if (groundDisconnectionSetIdentified && aSwitch.get() != null && aSwitch.get().getKind() == SwitchNode.SwitchKind.DISCONNECTOR) {
            List<Node> list = setFromGround.stream().toList(); // the list contains the fork node which should not be removed but substituted
            return new GroundDisconnection(list.subList(0, list.size() - 1), groundFeederNode, aSwitch.get(), list.get(list.size() - 1));
        }
        return null;
    }

    public void substituteInternalMiddle2wtByEquipmentNodes() {
        for (FeederNode feederNode : getFeederNodeStream(FeederType.TWO_WINDINGS_TRANSFORMER_LEG).toList()) {
            if (nodesById.get(feederNode.getId()) == feederNode) { // check if not already removed
                substituteInternalMiddle2wtByEquipmentNode(feederNode);
            }
        }
    }

    private void substituteInternalMiddle2wtByEquipmentNode(FeederNode feederNode) {
        MiddleTwtNode middleNode = getMultiTermNodes().stream()
                .filter(m2wtn -> m2wtn.getAdjacentNodes().stream().anyMatch(n -> n == feederNode))
                .findFirst().orElse(null);
        Node otherSideNode = Optional.ofNullable(middleNode)
                .flatMap(m2wtn -> m2wtn.getAdjacentNodes().stream().filter(n -> n != feederNode).findFirst())
                .orElse(null);
        if (middleNode instanceof Middle2WTNode middle2WTNode
                && otherSideNode instanceof FeederNode otherSideFeederNode
                && otherSideFeederNode.getFeeder() instanceof FeederTwLeg otherSideFeederTwLeg
                && otherSideFeederTwLeg.getOwnVoltageLevelInfos().getId().equals(voltageLevelInfos.getId())) {

            ConnectivityNode connectivityNodeA = NodeFactory.createConnectivityNode(this, feederNode.getId(), ComponentTypeName.NODE);
            ConnectivityNode connectivityNodeB = NodeFactory.createConnectivityNode(this, otherSideNode.getId(), ComponentTypeName.NODE);

            // substitute nodes in voltage level
            substituteNode(feederNode, connectivityNodeA);
            substituteNode(otherSideNode, connectivityNodeB);

            // substitute the node which was "outside" the voltage level (multiTermNode / snake line) by a node inside the voltage level
            ConnectivityNode connectivityNode1 = otherSideFeederTwLeg.getSide() == NodeSide.ONE ? connectivityNodeB : connectivityNodeA;
            ConnectivityNode connectivityNode2 = otherSideFeederTwLeg.getSide() == NodeSide.ONE ? connectivityNodeA : connectivityNodeB;
            NodeFactory.createInternal2WTNode(this,
                    middle2WTNode.getId(), middle2WTNode.getName(), middle2WTNode.getEquipmentId(),
                    connectivityNode1, connectivityNode2, middle2WTNode.getComponentType());
            multiTermNodes.remove(middleNode);
            twtEdges.removeAll(middleNode.getAdjacentEdges());
        }
    }

    public void insertNodeNextTo(Node nodeToInsert, Node adjacentNode) {
        List<Node> neighbours = adjacentNode.getAdjacentNodes();
        if (neighbours.isEmpty()) {
            addEdge(nodeToInsert, adjacentNode);
        } else {
            insertNode(adjacentNode, nodeToInsert, neighbours.get(0));
        }
    }

    private record GroundDisconnection(List<Node> nodes, FeederNode ground, SwitchNode disconnector, Node forkNode) {
    }
}
