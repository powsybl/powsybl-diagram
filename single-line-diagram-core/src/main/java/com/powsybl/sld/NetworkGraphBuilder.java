/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.model.*;
import com.powsybl.sld.postprocessor.GraphBuildPostProcessor;
import org.jgrapht.alg.ConnectivityInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.ComponentTypeName.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NetworkGraphBuilder implements GraphBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkGraphBuilder.class);

    private static final ServiceLoaderCache<GraphBuildPostProcessor> POST_PROCESSOR_LOADER = new ServiceLoaderCache<>(GraphBuildPostProcessor.class);

    private final Network network;  // IIDM network

    public NetworkGraphBuilder(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    public Graph buildVoltageLevelGraph(String id, boolean useName, boolean forVoltageLevelDiagram) {
        // get the voltageLevel from id
        VoltageLevel vl = network.getVoltageLevel(id);
        if (vl == null) {
            throw new PowsyblException("Voltage level '" + id + "' not found !!");
        }

        // build the graph from the voltage level
        Graph graph = Graph.create(new VoltageLevelInfos(vl.getId(), vl.getName(), vl.getNominalV()), useName, forVoltageLevelDiagram);
        buildGraph(graph, vl);

        return graph;
    }

    public SubstationGraph buildSubstationGraph(String id, boolean useName) {
        // get the substation from id
        Substation substation = network.getSubstation(id);
        if (substation == null) {
            throw new PowsyblException("Substation '" + id + "' not found !!");
        }

        // build the substation graph from the substation
        SubstationGraph graph = SubstationGraph.create(substation.getId());
        buildSubstationGraph(graph, substation, useName);

        return graph;
    }

    private void buildGraph(Graph graph, VoltageLevel vl) {
        LOGGER.info("Building '{}' graph...", vl.getId());

        switch (vl.getTopologyKind()) {
            case BUS_BREAKER:
                buildBusBreakerGraph(graph, vl);
                break;
            case NODE_BREAKER:
                buildNodeBreakerGraph(graph, vl);
                break;
            default:
                throw new AssertionError("Unknown topology kind: " + vl.getTopologyKind());
        }

        LOGGER.info("{} nodes, {} edges", graph.getNodes().size(), graph.getEdges().size());

        handleGraphPostProcessors(graph);

        handleConnectedComponents(graph);
    }

    private abstract static class AbstractGraphBuilder extends DefaultTopologyVisitor {

        protected final Graph graph;

        protected AbstractGraphBuilder(Graph graph) {
            this.graph = graph;
        }

        protected abstract void addFeeder(FeederNode node, Terminal terminal);

        protected abstract void add3wtFeeder(Middle3wtNode middleNode, Feeder3wtLegNode firstOtherLegNode,
                                             Feeder3wtLegNode secondOtherLegNode, Terminal terminal);

        private FeederNode createFeederLineNode(Graph graph, Line line, Branch.Side side) {
            Objects.requireNonNull(graph);
            Objects.requireNonNull(line);

            String id = line.getId() + "_" + side.name();
            String name = line.getName();
            String equipmentId = line.getId();
            FeederWithSideNode.Side s = FeederWithSideNode.Side.valueOf(side.name());
            Branch.Side otherSide = side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
            VoltageLevel vlOtherSide = line.getTerminal(otherSide).getVoltageLevel();
            return FeederLineNode.create(graph, id, name, equipmentId, s, new VoltageLevelInfos(vlOtherSide.getId(), vlOtherSide.getName(), vlOtherSide.getNominalV()));
        }

        private FeederNode createFeederNode(Graph graph, Injection injection) {
            Objects.requireNonNull(graph);
            Objects.requireNonNull(injection);
            switch (injection.getType()) {
                case GENERATOR:
                    return FeederInjectionNode.createGenerator(graph, injection.getId(), injection.getName());
                case LOAD:
                    return FeederInjectionNode.createLoad(graph, injection.getId(), injection.getName());
                case HVDC_CONVERTER_STATION:
                    return FeederInjectionNode.createVscConverterStation(graph, injection.getId(), injection.getName());
                case STATIC_VAR_COMPENSATOR:
                    return FeederInjectionNode.createStaticVarCompensator(graph, injection.getId(), injection.getName());
                case SHUNT_COMPENSATOR:
                    return ((ShuntCompensator) injection).getbPerSection() >= 0 ? FeederInjectionNode.createCapacitor(graph, injection.getId(), injection.getName())
                                                                                : FeederInjectionNode.createInductor(graph, injection.getId(), injection.getName());
                case DANGLING_LINE:
                    return FeederInjectionNode.createDanglingLine(graph, injection.getId(), injection.getName());
                default:
                    throw new IllegalStateException();
            }
        }

        private FeederNode createFeeder2wtNode(Graph graph,
                                               TwoWindingsTransformer branch,
                                               Branch.Side side) {
            Objects.requireNonNull(graph);
            Objects.requireNonNull(branch);

            String id = branch.getId() + "_" + side.name();
            String name = branch.getName();
            String equipmentId = branch.getId();

            if (graph.isForVoltageLevelDiagram()) {
                Branch.Side otherSide = side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
                VoltageLevel vlOtherSide = branch.getTerminal(otherSide).getVoltageLevel();
                VoltageLevelInfos otherSideVoltageLevelInfos = new VoltageLevelInfos(vlOtherSide.getId(), vlOtherSide.getName(), vlOtherSide.getNominalV());
                if (branch.getPhaseTapChanger() == null) {
                    return Feeder2wtNode.create(graph, id, name, equipmentId, FeederWithSideNode.Side.valueOf(side.name()), otherSideVoltageLevelInfos);
                } else {
                    return Feeder2wtNode.createWithPhaseShifter(graph, id, name, equipmentId, FeederWithSideNode.Side.valueOf(side.name()), otherSideVoltageLevelInfos);
                }
            } else {
                return Feeder2wtLegNode.create(graph, id, name, equipmentId, FeederWithSideNode.Side.valueOf(side.name()));
            }
        }

        protected SwitchNode createSwitchNodeFromTerminal(Graph graph, Terminal terminal) {
            Objects.requireNonNull(graph);
            Objects.requireNonNull(terminal);
            Bus bus = terminal.getBusBreakerView().getConnectableBus();
            String id = bus.getId() + "_" + terminal.getConnectable().getId();
            String name = bus.getName() + "_" + terminal.getConnectable().getName();
            return new SwitchNode(id, name, DISCONNECTOR, false, graph, SwitchNode.SwitchKind.DISCONNECTOR, !terminal.isConnected());
        }

        @Override
        public void visitLoad(Load load) {
            addFeeder(createFeederNode(graph, load), load.getTerminal());
        }

        @Override
        public void visitGenerator(Generator generator) {
            addFeeder(createFeederNode(graph, generator), generator.getTerminal());
        }

        @Override
        public void visitShuntCompensator(ShuntCompensator sc) {
            addFeeder(createFeederNode(graph, sc), sc.getTerminal());
        }

        @Override
        public void visitDanglingLine(DanglingLine danglingLine) {
            addFeeder(createFeederNode(graph, danglingLine), danglingLine.getTerminal());
        }

        @Override
        public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
            addFeeder(createFeederNode(graph, converterStation), converterStation.getTerminal());
        }

        @Override
        public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
            addFeeder(createFeederNode(graph, staticVarCompensator), staticVarCompensator.getTerminal());
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer,
                                                Branch.Side side) {
            addFeeder(createFeeder2wtNode(graph, transformer, side), transformer.getTerminal(side));
        }

        @Override
        public void visitLine(Line line, Branch.Side side) {
            addFeeder(createFeederLineNode(graph, line, side), line.getTerminal(side));
        }

        private static VoltageLevelInfos createVoltageLevelInfos(ThreeWindingsTransformer.Leg leg) {
            VoltageLevel vl = leg.getTerminal().getVoltageLevel();
            return new VoltageLevelInfos(vl.getId(), vl.getName(), vl.getNominalV());
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
                                                  ThreeWindingsTransformer.Side side) {

            if (graph.isForVoltageLevelDiagram()) {
                // in a voltageLevel diagram we represent 3 windings transformers by a double feeder cell:
                //   - a transformer middle node at double feeder fork
                //   - a feeder for first other leg
                //   - a feeder for second other leg

                Map<FeederWithSideNode.Side, VoltageLevelInfos> voltageLevelInfosBySide
                        = ImmutableMap.of(FeederWithSideNode.Side.ONE, createVoltageLevelInfos(transformer.getLeg1()),
                                          FeederWithSideNode.Side.TWO, createVoltageLevelInfos(transformer.getLeg2()),
                                          FeederWithSideNode.Side.THREE, createVoltageLevelInfos(transformer.getLeg3()));

                // create middle node
                Middle3wtNode middleNode = new Middle3wtNode(graph, transformer.getId() + "_fictif",
                                                             voltageLevelInfosBySide.get(FeederWithSideNode.Side.ONE),
                                                             voltageLevelInfosBySide.get(FeederWithSideNode.Side.TWO),
                                                             voltageLevelInfosBySide.get(FeederWithSideNode.Side.THREE));

                FeederWithSideNode.Side firstOtherLegSide;
                FeederWithSideNode.Side secondOtherLegSide;
                switch (side) {
                    case ONE:
                        firstOtherLegSide = FeederWithSideNode.Side.TWO;
                        secondOtherLegSide = FeederWithSideNode.Side.THREE;
                        break;
                    case TWO:
                        firstOtherLegSide = FeederWithSideNode.Side.ONE;
                        secondOtherLegSide = FeederWithSideNode.Side.THREE;
                        break;
                    case THREE:
                        firstOtherLegSide = FeederWithSideNode.Side.ONE;
                        secondOtherLegSide = FeederWithSideNode.Side.TWO;
                        break;
                    default:
                        throw new IllegalStateException();
                }

                // create first other leg feeder node
                String firstOtherLegNodeId = transformer.getId() + "_" + firstOtherLegSide.name();
                Feeder3wtLegNode firstOtherLegNode = Feeder3wtLegNode.create(graph, firstOtherLegNodeId, transformer.getName(), transformer.getId(), firstOtherLegSide);

                // create second other leg feeder node
                String secondOtherLegNodeId = transformer.getId() + "_" + secondOtherLegSide.name();
                Feeder3wtLegNode secondOtherLegNode = Feeder3wtLegNode.create(graph, secondOtherLegNodeId, transformer.getName(), transformer.getId(), secondOtherLegSide);

                add3wtFeeder(middleNode, firstOtherLegNode, secondOtherLegNode, transformer.getTerminal(side));
            } else {
                // in substation diagram, we only represent the leg node

                String id = transformer.getId() + "_" + side.name();
                Feeder3wtLegNode legNode = Feeder3wtLegNode.create(graph, id, transformer.getName(), transformer.getId(), FeederWithSideNode.Side.valueOf(side.name()));

                addFeeder(legNode, transformer.getTerminal(side));
            }
        }
    }

    private class NodeBreakerGraphBuilder extends AbstractGraphBuilder {

        private final Map<Integer, Node> nodesByNumber;

        NodeBreakerGraphBuilder(Graph graph, Map<Integer, Node> nodesByNumber) {
            super(graph);
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
            graph.addNode(node);
        }

        @Override
        protected void add3wtFeeder(Middle3wtNode middleNode, Feeder3wtLegNode firstOtherLegNode, Feeder3wtLegNode secondOtherLegNode, Terminal terminal) {
            ConnectablePosition.Feeder feeder = getFeeder(terminal);
            if (feeder != null) {
                BusCell.Direction direction = BusCell.Direction.valueOf(feeder.getDirection().toString());

                firstOtherLegNode.setOrder(feeder.getOrder());
                firstOtherLegNode.setLabel(feeder.getName());
                firstOtherLegNode.setDirection(direction);

                secondOtherLegNode.setOrder(feeder.getOrder() + 1);
                secondOtherLegNode.setLabel(feeder.getName());
                secondOtherLegNode.setDirection(direction);
            }

            nodesByNumber.put(terminal.getNodeBreakerView().getNode(), middleNode);
            graph.addNode(middleNode);
            graph.addNode(firstOtherLegNode);
            graph.addNode(secondOtherLegNode);

            // add edges between the middle node and other leg nodes
            graph.addEdge(middleNode, firstOtherLegNode);
            graph.addEdge(middleNode, secondOtherLegNode);
        }

        @Override
        public void visitBusbarSection(BusbarSection busbarSection) {
            BusbarSectionPosition extension = busbarSection.getExtension(BusbarSectionPosition.class);
            BusNode node = BusNode.create(graph, busbarSection.getId(), busbarSection.getName());
            if (extension != null) {
                node.setStructuralPosition(new Position(extension.getSectionIndex(), extension.getBusbarIndex())
                        .setHSpan(1));
            }
            nodesByNumber.put(busbarSection.getTerminal().getNodeBreakerView().getNode(), node);
            graph.addNode(node);
        }
    }

    private class BusBreakerGraphBuilder extends AbstractGraphBuilder {

        private final Map<String, Node> nodesByBusId;

        private int order = 1;

        BusBreakerGraphBuilder(Graph graph, Map<String, Node> nodesByBusId) {
            super(graph);
            this.nodesByBusId = Objects.requireNonNull(nodesByBusId);
        }

        private void connectToBus(Node node, Terminal terminal) {
            SwitchNode nodeSwitch = createSwitchNodeFromTerminal(graph, terminal);
            graph.addNode(nodeSwitch);
            String busId = terminal.getBusBreakerView().getConnectableBus().getId();
            graph.addEdge(nodesByBusId.get(busId), nodeSwitch);
            graph.addEdge(nodeSwitch, node);
        }

        protected void addFeeder(FeederNode node, Terminal terminal) {
            node.setOrder(order++);
            node.setDirection(order % 2 == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
            graph.addNode(node);
            connectToBus(node, terminal);
        }

        @Override
        protected void add3wtFeeder(Middle3wtNode middleNode, Feeder3wtLegNode firstOtherLegNode, Feeder3wtLegNode secondOtherLegNode, Terminal terminal) {
            BusCell.Direction direction = order % 2 == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM;

            firstOtherLegNode.setOrder(order++);
            firstOtherLegNode.setDirection(direction);

            secondOtherLegNode.setOrder(order++);
            secondOtherLegNode.setDirection(direction);

            graph.addNode(middleNode);
            graph.addNode(firstOtherLegNode);
            graph.addNode(secondOtherLegNode);

            // add edges between the middle node and other leg nodes
            graph.addEdge(middleNode, firstOtherLegNode);
            graph.addEdge(middleNode, secondOtherLegNode);

            connectToBus(middleNode, terminal);
        }
    }

    private void buildBusBreakerGraph(Graph graph, VoltageLevel vl) {
        Map<String, Node> nodesByBusId = new HashMap<>();

        int v = 1;
        for (Bus b : vl.getBusBreakerView().getBuses()) {
            BusNode busNode = BusNode.create(graph, b.getId(), b.getName());
            nodesByBusId.put(b.getId(), busNode);
            busNode.setStructuralPosition(new Position(1, v++));
            graph.addNode(busNode);
        }

        // visit equipments
        vl.visitEquipments(new BusBreakerGraphBuilder(graph, nodesByBusId));

        // switches
        for (Switch sw : vl.getBusBreakerView().getSwitches()) {
            SwitchNode n = createSwitchNodeFromSwitch(graph, sw);

            Bus bus1 = vl.getBusBreakerView().getBus1(sw.getId());
            Bus bus2 = vl.getBusBreakerView().getBus2(sw.getId());

            graph.addNode(n);
            graph.addEdge(nodesByBusId.get(bus1.getId()), n);
            graph.addEdge(n, nodesByBusId.get(bus2.getId()));
        }
    }

    private void buildNodeBreakerGraph(Graph graph, VoltageLevel vl) {
        Map<Integer, Node> nodesByNumber = new HashMap<>();

        // visit equipments
        vl.visitEquipments(new NodeBreakerGraphBuilder(graph, nodesByNumber));

        // switches
        for (Switch sw : vl.getNodeBreakerView().getSwitches()) {
            SwitchNode n = createSwitchNodeFromSwitch(graph, sw);

            int node1 = vl.getNodeBreakerView().getNode1(sw.getId());
            int node2 = vl.getNodeBreakerView().getNode2(sw.getId());

            ensureNodeExists(graph, node1, nodesByNumber);
            ensureNodeExists(graph, node2, nodesByNumber);

            graph.addEdge(nodesByNumber.get(node1), n);
            graph.addEdge(n, nodesByNumber.get(node2));
            graph.addNode(n);
        }

        // internal connections
        vl.getNodeBreakerView().getInternalConnectionStream().forEach(internalConnection -> {
            int node1 = internalConnection.getNode1();
            int node2 = internalConnection.getNode2();

            ensureNodeExists(graph, node1, nodesByNumber);
            ensureNodeExists(graph, node2, nodesByNumber);

            graph.addEdge(nodesByNumber.get(node1), nodesByNumber.get(node2));
        });
    }

    private void ensureNodeExists(Graph graph, int n, Map<Integer, Node> nodesByNumber) {
        if (!nodesByNumber.containsKey(n)) {
            FictitiousNode node = new FictitiousNode(graph, "" + n);
            nodesByNumber.put(n, node);
            graph.addNode(node);
        }
    }

    /**
     * Check if the graph is connected or not
     *
     * @return true if connected, false otherwise
     */
    private void handleConnectedComponents(Graph graph) {
        List<Set<Node>> connectedSets = new ConnectivityInspector<>(graph.toJgrapht()).connectedSets();
        if (connectedSets.size() != 1) {
            LOGGER.warn("{} connected components found", connectedSets.size());
            connectedSets.stream()
                    .sorted(Comparator.comparingInt(Set::size))
                    .map(setNodes -> setNodes.stream().map(Node::getId).collect(Collectors.toSet()))
                    .forEach(strings -> LOGGER.warn("   - {}", strings));
        }
        connectedSets.forEach(s -> ensureOneBusInConnectedComponent(graph, s));
    }

    private void ensureOneBusInConnectedComponent(Graph graph, Set<Node> nodes) {
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
        BusNode bn = BusNode.createFictitious(graph, biggestFn.getId() + "FictitiousBus");
        graph.addNode(bn);
        graph.substitueNode(biggestFn, bn);
    }

    /**
     * Discover and apply postprocessor plugins to add custom nodes
     **/
    private void handleGraphPostProcessors(Graph graph) {
        List<GraphBuildPostProcessor> listPostProcessors = POST_PROCESSOR_LOADER.getServices();
        for (GraphBuildPostProcessor gbp : listPostProcessors) {
            LOGGER.info("Graph post-processor id '{}' : Adding custom node in graph '{}'",
                    gbp.getId(), graph.getVoltageLevelInfos().getId());
            gbp.addNode(graph, network);
        }
    }

    private void buildSubstationGraph(SubstationGraph graph, Substation substation, boolean useName) {
        // building the graph for each voltageLevel (ordered by descending voltageLevel nominalV)
        substation.getVoltageLevelStream()
                .sorted(Comparator.comparing(VoltageLevel::getNominalV)
                        .reversed()).forEach(v -> {
                            Graph vlGraph = Graph.create(new VoltageLevelInfos(v.getId(), v.getName(), v.getNominalV()), useName, false);
                            buildGraph(vlGraph, v);
                            graph.addNode(vlGraph);
                        });

        LOGGER.info("Number of node : {} ", graph.getNodes().size());

        // Creation of snake lines for transformers between the voltage levels in the substation diagram
        addSnakeLines(graph, substation);
    }

    private void addSnakeLines(SubstationGraph graph, Substation substation) {
        // Two windings transformer
        //
        for (TwoWindingsTransformer transfo : substation.getTwoWindingsTransformers()) {
            Terminal t1 = transfo.getTerminal1();
            Terminal t2 = transfo.getTerminal2();

            String id1 = transfo.getId() + "_" + transfo.getSide(t1).name();
            String id2 = transfo.getId() + "_" + transfo.getSide(t2).name();

            Graph g1 = graph.getNode(t1.getVoltageLevel().getId());
            Graph g2 = graph.getNode(t2.getVoltageLevel().getId());

            Node n1 = g1.getNode(id1);
            Node n2 = g2.getNode(id2);

            graph.addEdge(TWO_WINDINGS_TRANSFORMER, n1, n2);
        }

        // Three windings transformer
        //
        for (ThreeWindingsTransformer transfo : substation.getThreeWindingsTransformers()) {
            Terminal t1 = transfo.getLeg1().getTerminal();
            Terminal t2 = transfo.getLeg2().getTerminal();
            Terminal t3 = transfo.getLeg3().getTerminal();

            String id1 = transfo.getId() + "_" + transfo.getSide(t1).name();
            String id2 = transfo.getId() + "_" + transfo.getSide(t2).name();
            String id3 = transfo.getId() + "_" + transfo.getSide(t3).name();

            Graph g1 = graph.getNode(t1.getVoltageLevel().getId());
            Graph g2 = graph.getNode(t2.getVoltageLevel().getId());
            Graph g3 = graph.getNode(t3.getVoltageLevel().getId());

            Node n1 = g1.getNode(id1);
            Node n2 = g2.getNode(id2);
            Node n3 = g3.getNode(id3);

            graph.addEdge(THREE_WINDINGS_TRANSFORMER, n1, n2, n3);
        }
    }

    private SwitchNode createSwitchNodeFromSwitch(Graph graph, Switch aSwitch) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(aSwitch);
        String componentType;
        switch (aSwitch.getKind()) {
            case BREAKER:
                componentType = BREAKER;
                break;
            case DISCONNECTOR:
                componentType = DISCONNECTOR;
                break;
            case LOAD_BREAK_SWITCH:
                componentType = LOAD_BREAK_SWITCH;
                break;
            default:
                throw new AssertionError();
        }
        SwitchNode.SwitchKind sk = SwitchNode.SwitchKind.valueOf(aSwitch.getKind().name());
        return new SwitchNode(aSwitch.getId(), aSwitch.getName(), componentType, false, graph, sk, aSwitch.isOpen());
    }

    @Override
    public ZoneGraph buildZoneGraph(List<String> substationIds, boolean useName) {
        Objects.requireNonNull(substationIds);

        List<Substation> zone = substationIds.stream().map(substationId -> {
            Substation substation = network.getSubstation(substationId);
            if (substation == null) {
                throw new PowsyblException("Substation '" + substationId + "' not in network " + network.getId());
            }
            return substation;
        }).collect(Collectors.toList());

        ZoneGraph graph = ZoneGraph.create(substationIds);
        buildZoneGraph(graph, zone, useName);

        return graph;
    }

    private void buildZoneGraph(ZoneGraph graph, List<Substation> zone, boolean useName) {
        if (zone.isEmpty()) {
            LOGGER.warn("No substations in the zone: skipping graph building");
            return;
        }
        // add nodes -> substation graphs
        GraphBuilder graphBuilder = new NetworkGraphBuilder(network);
        zone.forEach(substation -> {
            LOGGER.info("Adding substation {} to zone graph", substation.getId());
            SubstationGraph sGraph = graphBuilder.buildSubstationGraph(substation.getId(), useName);
            graph.addNode(sGraph);
        });
        // add edges -> lines
        List<String> lines = new ArrayList<>();
        zone.forEach(substation ->
            substation.getVoltageLevelStream().forEach(voltageLevel ->
                voltageLevel.getConnectableStream(Line.class).forEach(line -> {
                    if (!lines.contains(line.getId())) {
                        String nodeId1 = line.getId() + "_" + Branch.Side.ONE;
                        String nodeId2 = line.getId() + "_" + Branch.Side.TWO;
                        String voltageLevelId1 = line.getTerminal1().getVoltageLevel().getId();
                        String voltageLevelId2 = line.getTerminal2().getVoltageLevel().getId();
                        String substationId1 = line.getTerminal1().getVoltageLevel().getSubstation().getId();
                        String substationId2 = line.getTerminal2().getVoltageLevel().getSubstation().getId();
                        SubstationGraph sGraph1 = graph.getNode(substationId1);
                        SubstationGraph sGraph2 = graph.getNode(substationId2);
                        if (sGraph1 != null && sGraph2 != null) {
                            Graph vlGraph1 = sGraph1.getNode(voltageLevelId1);
                            Graph vlGraph2 = sGraph2.getNode(voltageLevelId2);
                            Node node1 = vlGraph1.getNode(nodeId1);
                            Node node2 = vlGraph2.getNode(nodeId2);
                            LOGGER.info("Adding line {} to zone graph", line.getId());
                            graph.addEdge(line.getId(), node1, node2);
                            lines.add(line.getId());
                        }
                    }
                })
            )
        );
    }

}
