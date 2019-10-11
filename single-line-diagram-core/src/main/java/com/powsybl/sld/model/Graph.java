/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DefaultTopologyVisitor;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.postprocessor.GraphBuildPostProcessor;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.library.ComponentTypeName.INDUCTOR;
import static com.powsybl.sld.library.ComponentTypeName.LINE;

/**
 * This class builds the connectivity among the elements of a voltageLevel
 * buildGraphAndDetectCell establishes the List of nodes, edges and nodeBuses
 * cells is built by the PatternCellDetector Class
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Graph {

    private static final Logger LOGGER = LoggerFactory.getLogger(Graph.class);

    private static final ServiceLoaderCache<GraphBuildPostProcessor> POST_PROCESSOR_LOADER = new ServiceLoaderCache<>(GraphBuildPostProcessor.class);

    private VoltageLevel voltageLevel;

    private final boolean useName;

    private final List<Node> nodes = new ArrayList<>();

    private final List<Edge> edges = new ArrayList<>();

    private final SortedSet<Cell> cells = new TreeSet<>(
            Comparator.comparingInt(Cell::getNumber)); // cells sorted to avoid randomness

    private final Map<Node.NodeType, List<Node>> nodesByType = new EnumMap<>(Node.NodeType.class);

    private final Map<String, Node> nodesById = new HashMap<>();

    private Position maxBusStructuralPosition = new Position(0, 0);

    private Map<Integer, Map<Integer, BusNode>> vPosToHPosToNodeBus;

    private int cellCounter = 0;

    private double x = 0;
    private double y = 0;

    private final boolean forVoltageLevelDiagram;  // true if voltageLevel diagram
                                                   // false if substation diagram

    private final boolean showInductorFor3WT;

    /**
     * Constructor
     */
    public Graph(VoltageLevel voltageLevel, boolean useName, boolean forVoltageLevelDiagram, boolean showInductorFor3WT) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
        this.useName = useName;
        this.forVoltageLevelDiagram = forVoltageLevelDiagram;
        this.showInductorFor3WT = showInductorFor3WT;
    }

    public boolean isUseName() {
        return useName;
    }

    public static Graph create(VoltageLevel vl) {
        return create(vl, false, true, false);
    }

    public static Graph create(VoltageLevel vl, boolean useName, boolean forVoltageLevelDiagram, boolean showInductorFor3WT) {
        Objects.requireNonNull(vl);
        Graph g = new Graph(vl, useName, forVoltageLevelDiagram, showInductorFor3WT);
        g.buildGraph(vl);
        return g;
    }

    int getNextCellIndex() {
        return cellCounter++;
    }

    private abstract class AbstractGraphBuilder extends DefaultTopologyVisitor {

        protected abstract void addFeeder(FeederNode node, Terminal terminal);

        @Override
        public void visitLoad(Load load) {
            addFeeder(FeederNode.create(Graph.this, load), load.getTerminal());
        }

        @Override
        public void visitGenerator(Generator generator) {
            addFeeder(FeederNode.create(Graph.this, generator), generator.getTerminal());
        }

        @Override
        public void visitShuntCompensator(ShuntCompensator sc) {
            addFeeder(FeederNode.create(Graph.this, sc), sc.getTerminal());
        }

        @Override
        public void visitDanglingLine(DanglingLine danglingLine) {
            addFeeder(FeederNode.create(Graph.this, danglingLine), danglingLine.getTerminal());
        }

        @Override
        public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
            addFeeder(FeederNode.create(Graph.this, converterStation), converterStation.getTerminal());
        }

        @Override
        public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
            addFeeder(FeederNode.create(Graph.this, staticVarCompensator), staticVarCompensator.getTerminal());
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer,
                                                TwoWindingsTransformer.Side side) {
            addFeeder(Feeder2WTNode.create(Graph.this, transformer, side), transformer.getTerminal(side));
        }

        @Override
        public void visitLine(Line line, Line.Side side) {
            addFeeder(FeederLineNode.create(Graph.this, line, side), line.getTerminal(side));
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
                                                  ThreeWindingsTransformer.Side side) {
            addFeeder(Feeder3WTNode.create(Graph.this, transformer, side), transformer.getTerminal(side));
        }
    }

    private class NodeBreakerGraphBuilder extends AbstractGraphBuilder {

        private final Map<Integer, Node> nodesByNumber;

        NodeBreakerGraphBuilder(Map<Integer, Node> nodesByNumber) {
            this.nodesByNumber = Objects.requireNonNull(nodesByNumber);
        }

        public ConnectablePosition.Feeder getFeeder(Terminal terminal) {
            Connectable connectable = terminal.getConnectable();
            ConnectablePosition position = (ConnectablePosition) connectable.getExtension(ConnectablePosition.class);
            if (position == null) {
                return null;
            }
            if (connectable instanceof Injection) {
                return position.getFeeder();
            } else if (connectable instanceof Branch) {
                Branch branch = (Branch) connectable;
                if (branch.getTerminal1() == terminal) {
                    return position.getFeeder1();
                } else if (branch.getTerminal2() == terminal) {
                    return position.getFeeder2();
                } else {
                    throw new AssertionError();
                }
            } else if (connectable instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) connectable;
                if (twt.getLeg1().getTerminal() == terminal) {
                    return position.getFeeder1();
                } else if (twt.getLeg2().getTerminal() == terminal) {
                    return position.getFeeder2();
                } else if (twt.getLeg3().getTerminal() == terminal) {
                    return position.getFeeder3();
                } else {
                    throw new AssertionError();
                }
            } else {
                throw new AssertionError();
            }
        }

        protected void addFeeder(FeederNode node, Terminal terminal) {
            ConnectablePosition.Feeder feeder = getFeeder(terminal);
            if (feeder != null) {
                node.setOrder(feeder.getOrder());
                node.setLabel(feeder.getName());
                node.setDirection(BusCell.Direction.valueOf(feeder.getDirection().toString()));
            }
            nodesByNumber.put(terminal.getNodeBreakerView().getNode(), node);
            addNode(node);
        }

        @Override
        public void visitBusbarSection(BusbarSection busbarSection) {
            BusbarSectionPosition extension = busbarSection.getExtension(BusbarSectionPosition.class);
            BusNode node = BusNode.create(Graph.this, busbarSection);
            if (extension != null) {
                node.setStructuralPosition(new Position(extension.getSectionIndex(), extension.getBusbarIndex())
                        .setHSpan(1));
            }
            nodesByNumber.put(busbarSection.getTerminal().getNodeBreakerView().getNode(), node);
            addNode(node);
        }
    }

    private class BusBreakerGraphBuilder extends AbstractGraphBuilder {

        private final Map<String, Node> nodesByBusId;

        private int order = 1;

        BusBreakerGraphBuilder(Map<String, Node> nodesByBusId) {
            this.nodesByBusId = Objects.requireNonNull(nodesByBusId);
        }

        protected void addFeeder(FeederNode node, Terminal terminal) {
            node.setOrder(order++);
            node.setDirection(order % 2 == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
            addNode(node);
            SwitchNode nodeSwitch = SwitchNode.create(Graph.this, terminal);
            addNode(nodeSwitch);
            String busId = terminal.getBusBreakerView().getConnectableBus().getId();
            addEdge(nodesByBusId.get(busId), nodeSwitch);
            addEdge(nodeSwitch, node);
        }
    }

    private void buildBusBreakerGraph(VoltageLevel vl) {
        Map<String, Node> nodesByBusId = new HashMap<>();

        int v = 1;
        for (Bus b : vl.getBusBreakerView().getBuses()) {
            BusNode busNode = BusNode.create(this, b);
            nodesByBusId.put(b.getId(), busNode);
            busNode.setStructuralPosition(new Position(1, v++));
            addNode(busNode);
        }

        // visit equipments
        vl.visitEquipments(new BusBreakerGraphBuilder(nodesByBusId));
    }

    private void buildNodeBreakerGraph(VoltageLevel vl) {
        Map<Integer, Node> nodesByNumber = new HashMap<>();

        // visit equipments
        vl.visitEquipments(new NodeBreakerGraphBuilder(nodesByNumber));

        // switches
        for (Switch sw : vl.getNodeBreakerView().getSwitches()) {
            SwitchNode n = SwitchNode.create(Graph.this, sw);

            int node1 = vl.getNodeBreakerView().getNode1(sw.getId());
            int node2 = vl.getNodeBreakerView().getNode2(sw.getId());

            ensureNodeExists(node1, nodesByNumber);
            ensureNodeExists(node2, nodesByNumber);

            addEdge(nodesByNumber.get(node1), n);
            addEdge(n, nodesByNumber.get(node2));
            addNode(n);
        }

        // internal connections
        vl.getNodeBreakerView().getInternalConnectionStream().forEach(internalConnection -> {
            int node1 = internalConnection.getNode1();
            int node2 = internalConnection.getNode2();

            ensureNodeExists(node1, nodesByNumber);
            ensureNodeExists(node2, nodesByNumber);

            addEdge(nodesByNumber.get(node1), nodesByNumber.get(node2));
        });
    }

    private void buildGraph(VoltageLevel vl) {
        LOGGER.info("Building '{}' graph...", vl.getId());

        switch (vl.getTopologyKind()) {
            case BUS_BREAKER:
                buildBusBreakerGraph(vl);
                break;
            case NODE_BREAKER:
                buildNodeBreakerGraph(vl);
                break;
            default:
                throw new AssertionError("Unknown topology kind: " + vl.getTopologyKind());
        }

        LOGGER.info("{} nodes, {} edges", nodes.size(), edges.size());

        constructCellForThreeWindingsTransformer();

        handleGraphPostProcessors();

        handleConnectedComponents();
    }

    public void removeUnnecessaryFictitiousNodes() {
        List<Node> fictitiousNodesToRemove = nodes.stream()
                .filter(node -> node.getType() == Node.NodeType.FICTITIOUS)
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

    public void removeFictitiousSwitchNodes() {
        List<Node> fictitiousSwithcNodesToRemove = nodes.stream()
                .filter(node -> node.getType() == Node.NodeType.SWITCH)
                .filter(this::isFictitiousSwitchNode)
                .filter(node -> node.getAdjacentNodes().size() == 2)
                .collect(Collectors.toList());
        for (Node n : fictitiousSwithcNodesToRemove) {
            Node node1 = n.getAdjacentNodes().get(0);
            Node node2 = n.getAdjacentNodes().get(1);
            LOGGER.info("Remove fictitious switch node between {} and {}", node1.getId(), node2.getId());
            removeNode(n);
            addEdge(node1, node2);
        }
    }

    private boolean isFictitiousSwitchNode(Node node) {
        Switch sw = TopologyKind.NODE_BREAKER.equals(voltageLevel.getTopologyKind()) ?
                voltageLevel.getNodeBreakerView().getSwitch(node.getId()) :
                voltageLevel.getBusBreakerView().getSwitch(node.getId());
        return sw == null || sw.isFictitious();
    }

    private void ensureNodeExists(int n, Map<Integer, Node> nodesByNumber) {
        if (!nodesByNumber.containsKey(n)) {
            FictitiousNode node = new FictitiousNode(Graph.this, "" + n);
            nodesByNumber.put(n, node);
            addNode(node);
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
            Cell cell = node.getCell();
            if (cell != null) {
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

    private UndirectedGraph<Node, Edge> toJgrapht() {
        UndirectedGraph<Node, Edge> graph = new Pseudograph<>(Edge.class);
        for (Node node : nodes) {
            graph.addVertex(node);
        }
        for (Edge edge : edges) {
            graph.addEdge(edge.getNode1(), edge.getNode2(), edge);
        }
        return graph;
    }

    /**
     * Check if the graph is connected or not
     *
     * @return true if connected, false otherwise
     */
    private void handleConnectedComponents() {
        List<Set<Node>> connectedSets = new ConnectivityInspector<>(toJgrapht()).connectedSets();
        if (connectedSets.size() != 1) {
            LOGGER.warn("{} connected components found", connectedSets.size());
            connectedSets.stream()
                    .sorted(Comparator.comparingInt(Set::size))
                    .map(setNodes -> setNodes.stream().map(Node::getId).collect(Collectors.toSet()))
                    .forEach(strings -> LOGGER.warn("   - {}", strings));
        }
        connectedSets.forEach(this::ensureOneBusInConnectedComponent);
    }

    private void ensureOneBusInConnectedComponent(Set<Node> nodes) {
        if (nodes.stream().anyMatch(node -> node.getType() == Node.NodeType.BUS)) {
            return;
        }
        FictitiousNode biggestFn = nodes.stream()
                .filter(node -> node.getType() == Node.NodeType.FICTITIOUS)
                .sorted(Comparator.<Node>comparingInt(node -> node.getAdjacentEdges().size())
                        .reversed()
                        .thenComparing(Node::getId)) // for stable fictitious node selection, also sort on id
                .map(FictitiousNode.class::cast)
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Empty node set"));
        BusNode bn = BusNode.createFictitious(this, biggestFn.getId() + "FictitiousBus");
        addNode(bn);
        substitueNode(biggestFn, bn);
    }

    public void addNode(Node node) {
        nodes.add(node);
        nodesByType.computeIfAbsent(node.getType(), nodeType -> new ArrayList<>()).add(node);
        nodesById.put(node.getId(), node);
    }

    private void removeNode(Node node) {
        nodes.remove(node);
        nodesByType.computeIfAbsent(node.getType(), nodeType -> new ArrayList<>()).remove(node);
        nodesById.remove(node.getId());
        for (Edge edge : new ArrayList<>(node.getAdjacentEdges())) {
            removeEdge(edge);
        }
    }

    public Node getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    /**
     * Add an edge between the two nodes
     *
     * @param n1 first node
     * @param n2 second node
     */
    public void addEdge(Node n1, Node n2) {
        Edge edge = new Edge(n1, n2);
        edges.add(edge);
        n1.addAdjacentEdge(edge);
        n2.addAdjacentEdge(edge);
    }

    /**
     * Remove an edge between two nodes
     *
     * @param n1 first node
     * @param n2 second node
     */
    void removeEdge(Node n1, Node n2) {
        for (Edge edge : edges) {
            if ((edge.getNode1().equals(n1) && edge.getNode2().equals(n2))
                    || (edge.getNode1().equals(n2) && edge.getNode2().equals(n1))) {
                removeEdge(edge);
                return;
            }
        }
    }

    void removeEdge(Edge edge) {
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
            List<Node> nodesToVisit = node.getAdjacentNodes()
                    .stream()
                    .filter(nodesIn::contains)
                    .collect(Collectors.toList());
            for (Node n : nodesToVisit) {
                rIdentifyConnexComponent(n, nodesIn, connexComponent);
            }
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

    public List<String> signatureSortedCellsContent() {
        return cells.stream().map(Cell::getFullId).sorted().collect(Collectors.toList());
    }

    public boolean compareCellDetection(Graph graph) {
        return signatureSortedCellsContent().equals(graph.signatureSortedCellsContent());
    }

    public Position getMaxBusStructuralPosition() {
        return maxBusStructuralPosition;
    }

    public void setMaxBusPosition() {
        List<Integer> h = new ArrayList<>();
        List<Integer> v = new ArrayList<>();
        getNodeBuses().forEach(nodeBus -> {
            v.add(nodeBus.getStructuralPosition().getV());
            h.add(nodeBus.getStructuralPosition().getH());
        });
        if (h.isEmpty() || v.isEmpty()) {
            return;
        }
        maxBusStructuralPosition.setH(Collections.max(h));
        maxBusStructuralPosition.setV(Collections.max(v));
    }

    public Stream<BusCell> getBusCells() {
        return cells.stream()
                .filter(cell -> cell instanceof BusCell && !((BusCell) cell).getPrimaryLegBlocks().isEmpty())
                .map(BusCell.class::cast);
    }

    private void buildVPosToHposToNodeBus() {
        vPosToHPosToNodeBus = new HashMap<>();
        getNodeBuses()
                .forEach(nodeBus -> {
                    int vPos = nodeBus.getStructuralPosition().getV();
                    int hPos = nodeBus.getStructuralPosition().getH();
                    vPosToHPosToNodeBus.putIfAbsent(vPos, new HashMap<>());
                    vPosToHPosToNodeBus.get(vPos).put(hPos, nodeBus);
                });
    }

    public void extendFeederWithMultipleSwitches() {
        List<Node> nodesToAdd = new ArrayList<>();
        for (Node n : nodes) {
            if (n instanceof FeederNode && n.getAdjacentNodes().size() > 1) {
                // Create a new fictitious node
                FictitiousNode nf = new FictitiousNode(Graph.this, n.getId() + "Fictif");
                nodesToAdd.add(nf);
                // Create all new edges and remove old ones
                List<Node> oldNeighboor = new ArrayList<>(n.getAdjacentNodes());
                for (Node neighboor : oldNeighboor) {
                    addEdge(nf, neighboor);
                    removeEdge(n, neighboor);
                }
                addEdge(n, nf);
            }
        }
        nodes.addAll(nodesToAdd);
    }

    //add a fictitious node between 2 switches or between a switch and a feeder
    //when one switch is connected to a bus
    public void extendFirstOutsideNode() {
        getNodeBuses().stream()
                .flatMap(node -> node.getAdjacentNodes().stream())
                .filter(node -> node.getType() == Node.NodeType.SWITCH)
                .forEach(nodeSwitch ->
                        nodeSwitch.getAdjacentNodes().stream()
                                .filter(node -> node.getType() == Node.NodeType.SWITCH ||
                                        node.getType() == Node.NodeType.FEEDER)
                                .forEach(node -> {
                                    removeEdge(node, nodeSwitch);
                                    FictitiousNode newNode = new FictitiousNode(Graph.this, nodeSwitch.getId() + "Fictif");
                                    addNode(newNode);
                                    addEdge(node, newNode);
                                    addEdge(nodeSwitch, newNode);
                                }));
    }

    //the first element shouldn't be a Breaker
    public void extendBreakerConnectedToBus() {
        getNodeBuses().forEach(nodeBus -> nodeBus.getAdjacentNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.SWITCH
                        && ((SwitchNode) node).getKind() != SwitchKind.DISCONNECTOR)
                .forEach(nodeSwitch -> addDoubleNode(nodeBus, nodeSwitch, "")));
    }

    //the first element shouldn't be a Feeder
    public void extendFeederConnectedToBus() {
        getNodeBuses().forEach(nodeBus -> nodeBus.getAdjacentNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.FEEDER)
                .forEach(feeder -> addDoubleNode(nodeBus, feeder, "")));
    }

    public void extendSwitchBetweenBus(SwitchNode nodeSwitch) {
        List<Node> copyAdj = new ArrayList<>(nodeSwitch.getAdjacentNodes());
        addDoubleNode((BusNode) copyAdj.get(0), nodeSwitch, "0");
        addDoubleNode((BusNode) copyAdj.get(1), nodeSwitch, "1");
    }

    private void addDoubleNode(BusNode busNode, Node node, String suffix) {
        removeEdge(busNode, node);
        SwitchNode fNodeToBus = SwitchNode.createFictitious(Graph.this, node.getId() + "fSwitch" + suffix, node.isOpen());
        addNode(fNodeToBus);
        FictitiousNode fNodeToSw = new FictitiousNode(Graph.this, node.getId() + "fNode" + suffix);
        addNode(fNodeToSw);
        addEdge(busNode, fNodeToBus);
        addEdge(fNodeToBus, fNodeToSw);
        addEdge(fNodeToSw, node);
    }

    private void substitueNode(Node nodeOrigin, Node newNode) {
        while (!nodeOrigin.getAdjacentEdges().isEmpty()) {
            Edge edge = nodeOrigin.getAdjacentEdges().get(0);
            Node node1 = edge.getNode1() == nodeOrigin ? newNode : edge.getNode1();
            Node node2 = edge.getNode2() == nodeOrigin ? newNode : edge.getNode2();
            addEdge(node1, node2);
            removeEdge(edge);
        }
        removeNode(nodeOrigin);
    }

    public void substituteFictitiousNodesMirroringBusNodes() {
        getNodeBuses().forEach(busNode -> {
            List<Node> adjs = busNode.getAdjacentNodes();
            if (adjs.size() == 1 && adjs.get(0).getType() == Node.NodeType.FICTITIOUS) {
                Node adj = adjs.get(0);
                removeEdge(adj, busNode);
                substitueNode(adj, busNode);
            }
        });
    }

    public void substituteSingularFictitiousByFeederNode() {
        getNodes().stream()
                .filter(n -> n.getType() == Node.NodeType.FICTITIOUS && n.getAdjacentEdges().size() == 1)
                .forEach(n -> {
                    FeederNode feederNode = FeederNode.createFictitious(this, n.getId());
                    addNode(feederNode);
                    substitueNode(n, feederNode);
                });
    }

    public BusNode getVHNodeBus(int v, int h) {
        if (vPosToHPosToNodeBus == null) {
            buildVPosToHposToNodeBus();
        }
        if (!vPosToHPosToNodeBus.containsKey(v)) {
            return null;
        }
        if (!vPosToHPosToNodeBus.get(v).containsKey(h)) {
            return null;
        }
        return vPosToHPosToNodeBus.get(v).get(h);
    }

    public void addCell(Cell c) {
        cells.add(c);
    }

    public void removeCell(Cell c) {
        cells.remove(c);
    }

    public List<BusNode> getNodeBuses() {
        return nodesByType.computeIfAbsent(Node.NodeType.BUS, nodeType -> new ArrayList<>())
                .stream()
                .map(BusNode.class::cast)
                .collect(Collectors.toList());
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    public Set<Cell> getCells() {
        return new TreeSet<>(cells);
    }

    public VoltageLevel getVoltageLevel() {
        return voltageLevel;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void writeJson(Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        try (JsonGenerator generator = new JsonFactory()
                .createGenerator(writer)
                .useDefaultPrettyPrinter()) {
            generator.writeStartArray();
            for (Cell cell : cells) {
                cell.writeJson(generator);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private class InfosInductor3WT {
        private final String id;    // id of the inductor
        private final String name;  // name of the inductor
        private final ThreeWindingsTransformer.Side side;  // side of the 3WT where the inductor is found

        InfosInductor3WT(String id, String name, ThreeWindingsTransformer.Side side) {
            this.id = id;
            this.name = name;
            this.side = side;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public ThreeWindingsTransformer.Side getSide() {
            return side;
        }
    }

    /*
     * Finding the inductor, if present, in the tertiary voltage level of a 3 windings transformer
     */
    private InfosInductor3WT findInductorOf3WT(Feeder3WTNode node) {
        final AtomicReference<InfosInductor3WT> infos = new AtomicReference<>();

        ThreeWindingsTransformer transformer = node.getTransformer();

        transformer.getTerminals().stream()
                .filter(t -> transformer.getSide(t) != ThreeWindingsTransformer.Side.ONE)
                .forEach(t -> {
                    VoltageLevel v = t.getVoltageLevel();
                    if (v != node.getGraph().getVoltageLevel()) {
                        v.getShuntCompensatorStream().forEach(s -> {
                            Bus connectableBusInductor = s.getTerminal().getBusView().getConnectableBus();
                            Bus connectableBus3WT = t.getBusView().getConnectableBus();
                            if (connectableBusInductor == connectableBus3WT &&
                                    s.getbPerSection() < 0 && infos.get() == null) {  // inductor found
                                infos.set(new InfosInductor3WT(s.getId(), s.getName(), transformer.getSide(t)));
                            }
                        });
                    }
                });

        return infos.get();
    }

    private void constructCellForThreeWindingsTransformer() {
        getNodes().stream()
                .filter(n -> n instanceof Feeder3WTNode)
                .forEach(n -> {
                    Feeder3WTNode n3WT = (Feeder3WTNode) n;

                    // Create a new fictitious node
                    Fictitious3WTNode nf = new Fictitious3WTNode(this, n3WT.getLabel() + "_fictif", n3WT.getTransformer());
                    addNode(nf);

                    FeederNode nfeeder1 = null;
                    FeederNode nfeeder2 = null;

                    InfosInductor3WT infosInductor = null;
                    if (showInductorFor3WT && forVoltageLevelDiagram) {
                        // finding the inductor in the tertiary voltage level
                        infosInductor = findInductorOf3WT(n3WT);
                    }

                    if (infosInductor != null) {
                        // We are in a voltage level diagram AND
                        // We want to show (if we are not in the tertiary voltage level),
                        // the inductor present in the tertiary voltage level of the 3 windings transformer,

                        // We represent the 3 windings transformer like a double feeder cell with :
                        // . one winding to the secondary voltage level
                        // . one feeder for the inductor present in the tertiary voltage level

                        if (infosInductor.getSide() == ThreeWindingsTransformer.Side.TWO) {
                            // Create a feeder for the inductor
                            String idFeeder1 = infosInductor.getId() + "_" + n3WT.getTransformer().getId();
                            String nameFeeder1 = infosInductor.getName();
                            nfeeder1 = Feeder2WTNode.create(Graph.this, idFeeder1, nameFeeder1, n3WT.getVL2());
                            nfeeder1.setComponentType(INDUCTOR);
                        } else {
                            // Create a feeder for the winding to the secondary voltage level
                            String idFeeder1 = n3WT.getId() + "_" + n3WT.getId2();
                            String nameFeeder1 = n3WT.getName() + "_" + n3WT.getName2();
                            nfeeder1 = Feeder2WTNode.create(Graph.this, idFeeder1, nameFeeder1, n3WT.getVL2());
                            nfeeder1.setComponentType(LINE);
                        }
                        nfeeder1.setOrder(n3WT.getOrder());
                        nfeeder1.setDirection(n3WT.getDirection());
                        addNode(nfeeder1);

                        if (infosInductor.getSide() == ThreeWindingsTransformer.Side.THREE) {
                            // Create a feeder for the inductor
                            String idFeeder2 = infosInductor.getId() + "_" + n3WT.getTransformer().getId();
                            String nameFeeder2 = infosInductor.getName();
                            nfeeder2 = Feeder2WTNode.create(Graph.this, idFeeder2, nameFeeder2, n3WT.getVL3());
                            nfeeder2.setComponentType(INDUCTOR);
                        } else {
                            // Create a feeder for the winding to the tertiary voltage level
                            String idFeeder2 = n3WT.getId() + "_" + n3WT.getId3();
                            String nameFeeder2 = n3WT.getName() + "_" + n3WT.getName3();
                            nfeeder2 = Feeder2WTNode.create(Graph.this, idFeeder2, nameFeeder2, n3WT.getVL3());
                            nfeeder2.setComponentType(LINE);
                        }
                        nfeeder2.setOrder(n3WT.getOrder() + 1);
                        nfeeder2.setDirection(n3WT.getDirection());
                        addNode(nfeeder2);
                    } else {
                        // We represent the 3 windings transformer like a double feeder cell with :
                        // . one winding to the first other voltage level
                        // . one winding to the second other voltage level

                        // Create a feeder for the winding to the first other voltage level
                        String idFeeder1 = n3WT.getId() + "_" + n3WT.getId2();
                        String nameFeeder1 = n3WT.getName() + "_" + n3WT.getName2();
                        nfeeder1 = Feeder2WTNode.create(Graph.this, idFeeder1, nameFeeder1, n3WT.getVL2());
                        nfeeder1.setComponentType(LINE);
                        nfeeder1.setOrder(n3WT.getOrder());
                        nfeeder1.setDirection(n3WT.getDirection());
                        addNode(nfeeder1);

                        // Create a feeder for the winding to the second other voltage level
                        String idFeeder2 = n3WT.getId() + "_" + n3WT.getId3();
                        String nameFeeder2 = n3WT.getName() + "_" + n3WT.getName3();
                        nfeeder2 = Feeder2WTNode.create(Graph.this, idFeeder2, nameFeeder2, n3WT.getVL3());
                        nfeeder2.setComponentType(LINE);
                        nfeeder2.setOrder(n3WT.getOrder() + 1);
                        nfeeder2.setDirection(n3WT.getDirection());
                        addNode(nfeeder2);
                    }

                    // Replacement of the old 3WT feeder node by the new fictitious node
                    substitueNode(n3WT, nf);

                    // Add edges between the new fictitious node and the new feeder nodes
                    addEdge(nf, nfeeder1);
                    addEdge(nf, nfeeder2);
                });
    }

    /**
       Discover and apply postprocessor plugins to add custom nodes
     **/
    private void handleGraphPostProcessors() {
        List<GraphBuildPostProcessor> listPostProcessors = POST_PROCESSOR_LOADER.getServices();
        for (GraphBuildPostProcessor gbp : listPostProcessors) {
            LOGGER.info("Graph post-processor id '{}' : Adding custom node in graph '{}'",
                    gbp.getId(), voltageLevel.getId());
            gbp.addNode(this);
        }
    }
}
