/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.diagram.util.IidmUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.postprocessor.GraphBuildPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.SldComponentTypeName.*;
import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static com.powsybl.sld.model.coordinate.Direction.UNDEFINED;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
 */
public class NetworkGraphBuilder implements GraphBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkGraphBuilder.class);

    private static final ServiceLoaderCache<GraphBuildPostProcessor> POST_PROCESSOR_LOADER = new ServiceLoaderCache<>(GraphBuildPostProcessor.class);

    private final Network network;  // IIDM network

    public NetworkGraphBuilder(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    private static boolean isInternalToVoltageLevel(Branch<?> branch) {
        return branch.getTerminal1().getVoltageLevel().getId().equals(branch.getTerminal2().getVoltageLevel().getId());
    }

    private static boolean isNotInternalToVoltageLevel(Branch<?> branch) {
        return !isInternalToVoltageLevel(branch);
    }

    private static boolean isInternalToVoltageLevel(ThreeWindingsTransformer transformer) {
        return transformer.getLeg1().getTerminal().getVoltageLevel().getId().equals(transformer.getLeg2().getTerminal().getVoltageLevel().getId())
                && transformer.getLeg2().getTerminal().getVoltageLevel().getId().equals(transformer.getLeg3().getTerminal().getVoltageLevel().getId());
    }

    private static boolean isNotInternalToVoltageLevel(ThreeWindingsTransformer transformer) {
        return !isInternalToVoltageLevel(transformer);
    }

    private static boolean isInternalToSubstation(Branch<?> branch) {
        Optional<Substation> substation1 = branch.getTerminal1().getVoltageLevel().getSubstation();
        Optional<Substation> substation2 = branch.getTerminal2().getVoltageLevel().getSubstation();
        return substation1.isPresent() && substation2.isPresent() && substation1.get() == substation2.get();
    }

    private static boolean isNotInternalToSubstation(Branch<?> branch) {
        return !isInternalToSubstation(branch);
    }

    @Override
    public VoltageLevelGraph buildVoltageLevelGraph(String id, Graph parentGraph) {
        // get the voltageLevel from id
        VoltageLevel vl = network.getVoltageLevel(id);
        if (vl == null) {
            throw new PowsyblException("Voltage level '" + id + "' not found !!");
        }

        // build the graph from the voltage level
        VoltageLevelGraph graph = new VoltageLevelGraph(new VoltageLevelInfos(vl.getId(), vl.getNameOrId(), vl.getNominalV()), parentGraph);
        buildGraph(graph, vl);

        return graph;
    }

    @Override
    public VoltageLevelGraph buildVoltageLevelGraph(String id) {
        return buildVoltageLevelGraph(id, null);
    }

    private void buildGraph(VoltageLevelGraph graph, VoltageLevel vl) {
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

        // Add snake edges in the same voltage level
        addBranchEdges(graph, vl);

        LOGGER.info("{} nodes, {} edges", graph.getNodes().size(), graph.getEdges().size());

        handleGraphPostProcessors(graph);
    }

    private void addBranchEdges(VoltageLevelGraph graph, VoltageLevel vl) {
        // Lines
        vl.getLineStream()
            .filter(NetworkGraphBuilder::isInternalToVoltageLevel)
            .sorted(Comparator.comparing(Line::getId))
            .forEach(line -> addBranchEdge(graph, line));

        // Two windings transformers
        vl.getTwoWindingsTransformerStream()
            .filter(NetworkGraphBuilder::isInternalToVoltageLevel)
            .sorted(Comparator.comparing(TwoWindingsTransformer::getId))
            .forEach(twoWindingsTransformer -> add2wtEdge(graph, twoWindingsTransformer));

        // Three windings transformers
        vl.getThreeWindingsTransformerStream()
            .filter(NetworkGraphBuilder::isInternalToVoltageLevel)
            .sorted(Comparator.comparing(ThreeWindingsTransformer::getId))
            .forEach(threeWindingsTransformer -> add3wtEdge(graph, threeWindingsTransformer));

        // Dangling lines
        vl.getDanglingLineStream()
            .map(DanglingLine::getTieLine)
            .flatMap(Optional::stream)
            .filter(NetworkGraphBuilder::isInternalToVoltageLevel)
            .distinct()
            .sorted(Comparator.comparing(TieLine::getId))
            .forEach(tieLine -> addBranchEdge(graph, tieLine));
    }

    @Override
    public SubstationGraph buildSubstationGraph(String id, ZoneGraph parentGraph) {
        // get the substation from id
        Substation substation = network.getSubstation(id);
        if (substation == null) {
            throw new PowsyblException("Substation '" + id + "' not found !!");
        }

        // build the substation graph from the substation
        SubstationGraph graph = SubstationGraph.create(substation.getId(), parentGraph);
        buildSubstationGraph(graph, substation);

        return graph;
    }

    @Override
    public SubstationGraph buildSubstationGraph(String id) {
        return buildSubstationGraph(id, null);
    }

    private void buildSubstationGraph(SubstationGraph graph, Substation substation) {
        // building the graph for each voltageLevel (ordered by descending voltageLevel nominalV)
        substation.getVoltageLevelStream()
                .sorted(Comparator.comparing(VoltageLevel::getNominalV)
                        .reversed())
                .forEach(v -> {
                    VoltageLevelGraph vlGraph = new VoltageLevelGraph(new VoltageLevelInfos(v.getId(), v.getNameOrId(), v.getNominalV()), graph);
                    buildGraph(vlGraph, v);
                    graph.addVoltageLevel(vlGraph);
                });

        // Add snake edges between different voltageLevels in the same substation
        addSnakeEdges(graph, substation);

        LOGGER.info("Number of voltage levels: {} ", graph.getVoltageLevels().size());
    }

    private void addSnakeEdges(SubstationGraph graph, Substation substation) {
        // Lines
        substation.getVoltageLevelStream()
            .flatMap(VoltageLevel::getLineStream)
            .filter(NetworkGraphBuilder::isInternalToSubstation)
            .filter(NetworkGraphBuilder::isNotInternalToVoltageLevel)
            .distinct()
            .sorted(Comparator.comparing(Line::getId))
            .forEach(line -> addBranchEdge(graph, line));

        // Two windings transformers
        substation.getVoltageLevelStream()
            .flatMap(VoltageLevel::getTwoWindingsTransformerStream)
            .filter(NetworkGraphBuilder::isNotInternalToVoltageLevel)
            .distinct()
            .sorted(Comparator.comparing(TwoWindingsTransformer::getId))
            .forEach(twoWindingsTransformer -> add2wtEdge(graph, twoWindingsTransformer));

        // Three windings transformers
        substation.getVoltageLevelStream()
            .flatMap(VoltageLevel::getThreeWindingsTransformerStream)
            .filter(NetworkGraphBuilder::isNotInternalToVoltageLevel)
            .distinct()
            .sorted(Comparator.comparing(ThreeWindingsTransformer::getId))
            .forEach(threeWindingsTransformer -> add3wtEdge(graph, threeWindingsTransformer));

        // Tie lines
        substation.getVoltageLevelStream()
            .flatMap(VoltageLevel::getDanglingLineStream)
            .map(DanglingLine::getTieLine)
            .flatMap(Optional::stream)
            .filter(NetworkGraphBuilder::isInternalToSubstation)
            .filter(NetworkGraphBuilder::isNotInternalToVoltageLevel)
            .distinct()
            .sorted(Comparator.comparing(TieLine::getId))
            .forEach(tieLine -> addBranchEdge(graph, tieLine));
    }

    private abstract static class AbstractGraphBuilder extends DefaultTopologyVisitor {

        protected final VoltageLevelGraph graph;

        protected AbstractGraphBuilder(VoltageLevelGraph graph) {
            this.graph = graph;
        }

        protected abstract void addTerminalNode(Node node, Terminal terminal);

        protected abstract void add3wtFeeder(Middle3WTNode middleNode, FeederNode firstOtherLegNode,
                                             FeederNode secondOtherLegNode, Terminal terminal);

        private FeederNode createFeederLineNode(VoltageLevelGraph graph, Line line, TwoSides side) {
            return createFeederBranchNode(graph, line, side, LINE);
        }

        private FeederNode createFeederTieLineNode(VoltageLevelGraph graph, TieLine tieLine, TwoSides side) {
            return createFeederBranchNode(graph, tieLine, side, TIE_LINE);
        }

        private FeederNode createFeederBranchNode(VoltageLevelGraph graph, Branch<?> branch, TwoSides side, String componentTypeName) {
            String nodeId = branch.getId() + "_" + side.name();
            String equipmentNameOrId = branch.getNameOrId();
            String equipmentId;
            equipmentId = branch.getId();
            NodeSide s = NodeSide.valueOf(side.name());
            TwoSides otherSide = side == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
            VoltageLevel vlOtherSide = branch.getTerminal(otherSide).getVoltageLevel();
            return NodeFactory.createFeederBranchNode(graph, nodeId, equipmentNameOrId, equipmentId, componentTypeName, s,
                    new VoltageLevelInfos(vlOtherSide.getId(), vlOtherSide.getNameOrId(), vlOtherSide.getNominalV()));
        }

        private FeederNode createFeederVscNode(VoltageLevelGraph graph, HvdcConverterStation<?> hvdcStation) {
            // An injection node is created if only one side of the station in the network
            return hvdcStation.getOtherConverterStation()
                    .map(otherStation -> otherStation.getTerminal().getVoltageLevel())
                    .map(otherVl -> new VoltageLevelInfos(otherVl.getId(), otherVl.getNameOrId(), otherVl.getNominalV()))
                    .map(otherVlInfo -> NodeFactory.createVscConverterStation(graph, hvdcStation.getId(), hvdcStation.getNameOrId(), hvdcStation.getHvdcLine().getId(),
                            hvdcStation.getHvdcLine().getConverterStation1() == hvdcStation ? NodeSide.ONE : NodeSide.TWO, otherVlInfo))
                    .orElseGet(() -> NodeFactory.createVscConverterStationInjection(graph, hvdcStation.getId(), hvdcStation.getNameOrId()));
        }

        private FeederNode createFeederLccNode(VoltageLevelGraph graph, HvdcConverterStation<?> hvdcStation) {
            // An injection node is created if only one side of the station in the network
            return hvdcStation.getOtherConverterStation()
                    .map(otherStation -> otherStation.getTerminal().getVoltageLevel())
                    .map(otherVl -> new VoltageLevelInfos(otherVl.getId(), otherVl.getNameOrId(), otherVl.getNominalV()))
                    .map(otherVlInfo -> NodeFactory.createLccConverterStation(graph, hvdcStation.getId(), hvdcStation.getNameOrId(), hvdcStation.getHvdcLine().getId(),
                            hvdcStation.getHvdcLine().getConverterStation1() == hvdcStation ? NodeSide.ONE : NodeSide.TWO, otherVlInfo))
                    .orElseGet(() -> NodeFactory.createLccConverterStationInjection(graph, hvdcStation.getId(), hvdcStation.getNameOrId()));
        }

        private FeederNode createFeeder2wtNode(VoltageLevelGraph graph, TwoWindingsTransformer branch, TwoSides side) {
            String id = branch.getId() + "_" + side.name();
            String name = branch.getNameOrId();
            String equipmentId = branch.getId();
            TwoSides otherSide = side == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
            VoltageLevel vlOtherSide = branch.getTerminal(otherSide).getVoltageLevel();
            VoltageLevelInfos otherSideVoltageLevelInfos = new VoltageLevelInfos(vlOtherSide.getId(), vlOtherSide.getNameOrId(), vlOtherSide.getNominalV());

            if (graph.isForVoltageLevelDiagram() && isNotInternalToVoltageLevel(branch)) {
                if (!branch.hasPhaseTapChanger()) {
                    return NodeFactory.createFeeder2WTNode(graph, id, name, equipmentId, NodeSide.valueOf(side.name()), otherSideVoltageLevelInfos);
                } else {
                    return NodeFactory.createFeeder2WTNodeWithPhaseShifter(graph, id, name, equipmentId, NodeSide.valueOf(side.name()), otherSideVoltageLevelInfos);
                }
            } else {
                if (!branch.hasPhaseTapChanger()) {
                    return NodeFactory.createFeeder2WTLegNode(graph, id, name, equipmentId, NodeSide.valueOf(side.name()));
                } else {
                    return NodeFactory.createFeeder2WTLegNodeWithPhaseShifter(graph, id, name, equipmentId, NodeSide.valueOf(side.name()));
                }
            }
        }

        private void addFeeder3wtNode(VoltageLevelGraph graph,
                                      ThreeWindingsTransformer transformer,
                                      ThreeSides side) {
            if (graph.isForVoltageLevelDiagram() && isNotInternalToVoltageLevel(transformer)) {
                // in a voltageLevel diagram we represent 3 windings transformers by a double feeder cell:
                //   - a transformer middle node at double feeder fork
                //   - a feeder for first other leg
                //   - a feeder for second other leg

                Map<NodeSide, VoltageLevelInfos> voltageLevelInfosBySide
                        = Map.of(NodeSide.ONE, createVoltageLevelInfos(transformer.getLeg1().getTerminal()),
                        NodeSide.TWO, createVoltageLevelInfos(transformer.getLeg2().getTerminal()),
                        NodeSide.THREE, createVoltageLevelInfos(transformer.getLeg3().getTerminal()));

                NodeSide vlLegSide;
                NodeSide firstOtherLegSide;
                NodeSide secondOtherLegSide;
                switch (side) {
                    case ONE:
                        vlLegSide = NodeSide.ONE;
                        firstOtherLegSide = NodeSide.TWO;
                        secondOtherLegSide = NodeSide.THREE;
                        break;
                    case TWO:
                        vlLegSide = NodeSide.TWO;
                        firstOtherLegSide = NodeSide.ONE;
                        secondOtherLegSide = NodeSide.THREE;
                        break;
                    case THREE:
                        vlLegSide = NodeSide.THREE;
                        firstOtherLegSide = NodeSide.ONE;
                        secondOtherLegSide = NodeSide.TWO;
                        break;
                    default:
                        throw new IllegalStateException();
                }

                // create first other leg feeder node
                String firstOtherLegNodeId = transformer.getId() + "_" + firstOtherLegSide.name();
                FeederNode firstOtherLegNode = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(graph, firstOtherLegNodeId, transformer.getNameOrId(),
                        transformer.getId(), firstOtherLegSide, voltageLevelInfosBySide.get(firstOtherLegSide));

                // create second other leg feeder node
                String secondOtherLegNodeId = transformer.getId() + "_" + secondOtherLegSide.name();
                FeederNode secondOtherLegNode = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(graph, secondOtherLegNodeId, transformer.getNameOrId(),
                        transformer.getId(), secondOtherLegSide, voltageLevelInfosBySide.get(secondOtherLegSide));

                boolean hasPhaseTapChanger1 = transformer.getLeg1().hasPhaseTapChanger();
                boolean hasPhaseTapChanger2 = transformer.getLeg2().hasPhaseTapChanger();
                boolean hasPhaseTapChanger3 = transformer.getLeg3().hasPhaseTapChanger();
                // create middle node
                Middle3WTNode middleNode = NodeFactory.createMiddle3WTNode(graph, transformer.getId(), transformer.getNameOrId(),
                        vlLegSide, firstOtherLegNode, secondOtherLegNode,
                        voltageLevelInfosBySide.get(NodeSide.ONE),
                        voltageLevelInfosBySide.get(NodeSide.TWO),
                        voltageLevelInfosBySide.get(NodeSide.THREE),
                        hasPhaseTapChanger1, hasPhaseTapChanger2, hasPhaseTapChanger3);

                add3wtFeeder(middleNode, firstOtherLegNode, secondOtherLegNode, transformer.getTerminal(side));
            } else {
                // in substation diagram, we only represent the leg node within the voltage level (3wt node will be on the snake line)
                String id = transformer.getId() + "_" + side.name();
                FeederNode legNode = NodeFactory.createFeeder3WTLegNodeForSubstationDiagram(graph, id, transformer.getNameOrId(), transformer.getId(),
                        NodeSide.valueOf(side.name()));

                addTerminalNode(legNode, transformer.getTerminal(side));
            }
        }

        @Override
        public void visitLoad(Load load) {
            addTerminalNode(NodeFactory.createLoad(graph, load.getId(), load.getNameOrId()), load.getTerminal());
        }

        @Override
        public void visitGenerator(Generator generator) {
            addTerminalNode(NodeFactory.createGenerator(graph, generator.getId(), generator.getNameOrId()), generator.getTerminal());
        }

        @Override
        public void visitBattery(Battery battery) {
            addTerminalNode(NodeFactory.createBattery(graph, battery.getId(), battery.getNameOrId()), battery.getTerminal());
        }

        @Override
        public void visitShuntCompensator(ShuntCompensator sc) {
            FeederNode feederNode = IidmUtil.isCapacitor(sc)
                    ? NodeFactory.createCapacitor(graph, sc.getId(), sc.getNameOrId())
                    : NodeFactory.createInductor(graph, sc.getId(), sc.getNameOrId());
            addTerminalNode(feederNode, sc.getTerminal());
        }

        @Override
        public void visitDanglingLine(DanglingLine dl) {
            if (!dl.isPaired()) {
                addTerminalNode(NodeFactory.createDanglingLine(graph, dl.getId(), dl.getNameOrId()), dl.getTerminal());
            } else {
                dl.getTieLine().ifPresent(tieLine -> visitTieLine(tieLine, dl, graph));
            }
        }

        private void visitTieLine(TieLine tieLine, DanglingLine dl, Graph graph) {
            TwoSides side = tieLine.getSide(dl.getTerminal());
            Terminal terminal = dl.getTerminal();
            addTerminalNode(createFeederTieLineNode((VoltageLevelGraph) graph, tieLine, side), terminal);
        }

        @Override
        public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
            FeederNode node;
            switch (converterStation.getHvdcType()) {
                case LCC: node = createFeederLccNode(graph, converterStation); break;
                case VSC: node = createFeederVscNode(graph, converterStation); break;
                default: throw new AssertionError();
            }
            addTerminalNode(node, converterStation.getTerminal());
        }

        @Override
        public void visitStaticVarCompensator(StaticVarCompensator svc) {
            addTerminalNode(NodeFactory.createStaticVarCompensator(graph, svc.getId(), svc.getNameOrId()), svc.getTerminal());
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
            Node transformerNode = createFeeder2wtNode(graph, transformer, side);
            addTerminalNode(transformerNode, transformer.getTerminal(side));
        }

        @Override
        public void visitLine(Line line, TwoSides side) {
            addTerminalNode(createFeederLineNode(graph, line, side), line.getTerminal(side));
        }

        private static VoltageLevelInfos createVoltageLevelInfos(Terminal terminal) {
            VoltageLevel vl = terminal.getVoltageLevel();
            return new VoltageLevelInfos(vl.getId(), vl.getNameOrId(), vl.getNominalV());
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
                                                  ThreeSides side) {
            addFeeder3wtNode(graph, transformer, side);
        }

        @Override
        public void visitGround(Ground ground) {
            addTerminalNode(NodeFactory.createGround(graph, ground.getId(), ground.getNameOrId()), ground.getTerminal());
        }
    }

    public static class NodeBreakerGraphBuilder extends AbstractGraphBuilder {

        private final Map<Integer, Node> nodesByNumber;

        protected NodeBreakerGraphBuilder(VoltageLevelGraph graph, Map<Integer, Node> nodesByNumber) {
            super(graph);
            this.nodesByNumber = Objects.requireNonNull(nodesByNumber);
        }

        public ConnectablePosition.Feeder getFeeder(Terminal terminal) {
            Connectable<?> connectable = terminal.getConnectable();
            ConnectablePosition<?> position = (ConnectablePosition<?>) connectable.getExtension(ConnectablePosition.class);
            if (position == null) {
                return null;
            }
            if (connectable instanceof Injection) {
                return position.getFeeder();
            } else if (connectable instanceof Branch) {
                Branch<?> branch = (Branch<?>) connectable;
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

        protected void addTerminalNode(Node node, Terminal terminal) {
            ConnectablePosition.Feeder feeder = getFeeder(terminal);
            if (feeder != null) {
                feeder.getOrder().ifPresent(node::setOrder);
                feeder.getName().ifPresent(node::setLabel);
                Direction dir = Direction.valueOf(feeder.getDirection().toString());
                node.setDirection(dir == UNDEFINED ? TOP : dir);
            }
            nodesByNumber.put(terminal.getNodeBreakerView().getNode(), node);
        }

        @Override
        protected void add3wtFeeder(Middle3WTNode middleNode, FeederNode firstOtherLegNode, FeederNode secondOtherLegNode, Terminal terminal) {
            ConnectablePosition.Feeder feeder = getFeeder(terminal);
            if (feeder != null) {
                middleNode.setDirection(Direction.valueOf(feeder.getDirection().toString()));
                feeder.getOrder().ifPresent(order -> {
                    firstOtherLegNode.setOrder(order);
                    secondOtherLegNode.setOrder(order + 1);
                });
                feeder.getName().ifPresent(name -> {
                    firstOtherLegNode.setLabel(name);
                    secondOtherLegNode.setLabel(name);
                });
            }

            nodesByNumber.put(terminal.getNodeBreakerView().getNode(), middleNode);
        }

        @Override
        public void visitBusbarSection(BusbarSection busbarSection) {
            BusbarSectionPosition extension = busbarSection.getExtension(BusbarSectionPosition.class);
            BusNode node = NodeFactory.createBusNode(graph, busbarSection.getId(), busbarSection.getNameOrId());
            if (extension != null) {
                node.setBusBarIndexSectionIndex(extension.getBusbarIndex(), extension.getSectionIndex());
            }
            nodesByNumber.put(busbarSection.getTerminal().getNodeBreakerView().getNode(), node);
        }
    }

    public static class BusBreakerGraphBuilder extends AbstractGraphBuilder {

        private final Map<String, Node> nodesByBusId;

        private int order = 1;

        protected BusBreakerGraphBuilder(VoltageLevelGraph graph, Map<String, Node> nodesByBusId) {
            super(graph);
            this.nodesByBusId = Objects.requireNonNull(nodesByBusId);
        }

        private void connectToBus(Node node, Terminal terminal) {
            String busId = terminal.getBusBreakerView().getConnectableBus().getId();
            graph.addEdge(nodesByBusId.get(busId), node);
        }

        protected void addTerminalNode(Node node, Terminal terminal) {
            node.setOrder(order++);
            node.setDirection(order % 2 == 0 ? Direction.TOP : Direction.BOTTOM);
            connectToBus(node, terminal);
        }

        @Override
        protected void add3wtFeeder(Middle3WTNode middleNode, FeederNode firstOtherLegNode, FeederNode secondOtherLegNode, Terminal terminal) {
            Direction direction = order % 2 == 0 ? Direction.TOP : Direction.BOTTOM;

            firstOtherLegNode.setOrder(order++);
            firstOtherLegNode.setDirection(direction);

            secondOtherLegNode.setOrder(order++);
            secondOtherLegNode.setDirection(direction);

            connectToBus(middleNode, terminal);
        }
    }

    protected BusBreakerGraphBuilder createBusBreakerGraphBuilder(VoltageLevelGraph graph, Map<String, Node> nodesByBusId) {
        return new BusBreakerGraphBuilder(graph, nodesByBusId);
    }

    private void buildBusBreakerGraph(VoltageLevelGraph graph, VoltageLevel vl) {
        Map<String, Node> nodesByBusId = new HashMap<>();

        int v = 1;
        for (Bus b : vl.getBusBreakerView().getBuses()) {
            BusNode busNode = NodeFactory.createBusNode(graph, b.getId(), b.getNameOrId());
            nodesByBusId.put(b.getId(), busNode);
            busNode.setBusBarIndexSectionIndex(v++, 1);
        }

        // visit equipments
        vl.visitEquipments(createBusBreakerGraphBuilder(graph, nodesByBusId));

        // switches
        for (Switch sw : vl.getBusBreakerView().getSwitches()) {
            SwitchNode n = createSwitchNodeFromSwitch(graph, sw);

            Bus bus1 = vl.getBusBreakerView().getBus1(sw.getId());
            Bus bus2 = vl.getBusBreakerView().getBus2(sw.getId());
            graph.addEdge(nodesByBusId.get(bus1.getId()), n);
            graph.addEdge(n, nodesByBusId.get(bus2.getId()));
        }
    }

    protected NodeBreakerGraphBuilder createNodeBreakerGraphBuilder(VoltageLevelGraph graph, Map<Integer, Node> nodesByNumber) {
        return new NodeBreakerGraphBuilder(graph, nodesByNumber);
    }

    private void buildNodeBreakerGraph(VoltageLevelGraph graph, VoltageLevel vl) {
        Map<Integer, Node> nodesByNumber = new HashMap<>();

        // visit equipments
        NodeBreakerGraphBuilder builder = createNodeBreakerGraphBuilder(graph, nodesByNumber);
        vl.getLineStream().sorted(Comparator.comparing(Line::getId))
            .forEach(l -> builder.visitLine(l, (l.getTerminal1().getVoltageLevel() == vl ? l.getTerminal1().getSide() : l.getTerminal2().getSide()).toTwoSides()));
        vl.getTwoWindingsTransformerStream().sorted(Comparator.comparing(TwoWindingsTransformer::getId))
            .forEach(twt -> builder.visitTwoWindingsTransformer(twt, (twt.getTerminal1().getVoltageLevel() == vl ? twt.getTerminal1().getSide() : twt.getTerminal2().getSide()).toTwoSides()));
        vl.getThreeWindingsTransformerStream().sorted(Comparator.comparing(ThreeWindingsTransformer::getId))
            .forEach(thwt -> builder.visitThreeWindingsTransformer(thwt, thwt.getLegStream().filter(leg -> leg.getTerminal().getVoltageLevel() == vl).findFirst().orElseThrow().getSide()));
        vl.getDanglingLineStream().sorted(Comparator.comparing(DanglingLine::getId)).forEach(builder::visitDanglingLine);
        vl.getConnectableStream(HvdcConverterStation.class).sorted(Comparator.comparing(HvdcConverterStation::getId))
            .forEach(builder::visitHvdcConverterStation);
        vl.getLoadStream().sorted(Comparator.comparing(Load::getId)).forEach(builder::visitLoad);
        vl.getGeneratorStream().sorted(Comparator.comparing(Generator::getId)).forEach(builder::visitGenerator);
        vl.getBatteryStream().sorted(Comparator.comparing(Battery::getId)).forEach(builder::visitBattery);
        vl.getStaticVarCompensatorStream().sorted(Comparator.comparing(StaticVarCompensator::getId)).forEach(builder::visitStaticVarCompensator);
        vl.getGroundStream().sorted(Comparator.comparing(Ground::getId)).forEach(builder::visitGround);
//        vl.visitEquipments(createNodeBreakerGraphBuilder(graph, nodesByNumber));

        // switches
        for (Switch sw : vl.getNodeBreakerView().getSwitches()) {
            SwitchNode n = createSwitchNodeFromSwitch(graph, sw);

            int node1 = vl.getNodeBreakerView().getNode1(sw.getId());
            int node2 = vl.getNodeBreakerView().getNode2(sw.getId());

            ensureNodeExists(graph, node1, nodesByNumber);
            ensureNodeExists(graph, node2, nodesByNumber);

            graph.addEdge(nodesByNumber.get(node1), n);
            graph.addEdge(n, nodesByNumber.get(node2));
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

    private void ensureNodeExists(VoltageLevelGraph graph, int n, Map<Integer, Node> nodesByNumber) {
        nodesByNumber.computeIfAbsent(n, k -> NodeFactory.createConnectivityNode(graph, String.valueOf(k)));
    }

    /**
     * Discover and apply postprocessor plugins to add custom nodes
     **/
    private void handleGraphPostProcessors(VoltageLevelGraph graph) {
        List<GraphBuildPostProcessor> listPostProcessors = POST_PROCESSOR_LOADER.getServices();
        for (GraphBuildPostProcessor gbp : listPostProcessors) {
            LOGGER.info("Graph post-processor id '{}' : Adding custom node in graph '{}'",
                    gbp.getId(), graph.getVoltageLevelInfos().getId());
            gbp.addNode(graph, network);
        }
    }

    private void addBranchEdge(Graph graph, Branch<?> branch) {
        Terminal t1 = branch.getTerminal1();
        Terminal t2 = branch.getTerminal2();
        if (!addLineEdge(graph, branch.getId(),
            t1,
            t2,
            branch.getId() + "_" + branch.getSide(t1).name(),
            branch.getId() + "_" + branch.getSide(t2).name())) {
            LOGGER.warn("Failed to add branch edge for branch '{}'", branch.getId());
        }
    }

    private void addHvdcLineEdge(Graph graph, HvdcLine hvdcLine) {
        HvdcConverterStation<?> cvs1 = hvdcLine.getConverterStation1();
        HvdcConverterStation<?> cvs2 = hvdcLine.getConverterStation2();
        addLineEdge(graph, hvdcLine.getId(), cvs1.getTerminal(), cvs2.getTerminal(), cvs1.getId(), cvs2.getId());
    }

    private boolean addLineEdge(Graph graph, String lineId, Terminal t1, Terminal t2, String nodeId1, String nodeId2) {
        VoltageLevel vl1 = t1.getVoltageLevel();
        VoltageLevel vl2 = t2.getVoltageLevel();

        VoltageLevelGraph g1 = graph.getVoltageLevel(vl1.getId());
        VoltageLevelGraph g2 = graph.getVoltageLevel(vl2.getId());

        boolean isNotNull = g1 != null && g2 != null;
        if (isNotNull) {
            Node n1 = g1.getNode(nodeId1);
            Node n2 = g2.getNode(nodeId2);
            graph.addLineEdge(lineId, n1, n2);
        }
        return isNotNull;
    }

    private void add2wtEdge(BaseGraph graph, TwoWindingsTransformer twoWindingsTransformer) {
        Terminal t1 = twoWindingsTransformer.getTerminal1();
        Terminal t2 = twoWindingsTransformer.getTerminal2();

        String id1 = twoWindingsTransformer.getId() + "_" + twoWindingsTransformer.getSide(t1).name();
        String id2 = twoWindingsTransformer.getId() + "_" + twoWindingsTransformer.getSide(t2).name();

        VoltageLevel vl1 = t1.getVoltageLevel();
        VoltageLevel vl2 = t2.getVoltageLevel();

        VoltageLevelGraph g1 = graph.getVoltageLevel(vl1.getId());
        VoltageLevelGraph g2 = graph.getVoltageLevel(vl2.getId());

        Node n1 = g1.getNode(id1);
        Node n2 = g2.getNode(id2);

        // creation of the middle node and the edges linking the transformer leg nodes to this middle node
        VoltageLevelInfos voltageLevelInfos1 = new VoltageLevelInfos(vl1.getId(), vl1.getNameOrId(), vl1.getNominalV());
        VoltageLevelInfos voltageLevelInfos2 = new VoltageLevelInfos(vl2.getId(), vl2.getNameOrId(), vl2.getNominalV());

        NodeFactory.createMiddle2WTNode(graph, twoWindingsTransformer.getId(), twoWindingsTransformer.getNameOrId(),
                (FeederNode) n1, (FeederNode) n2, voltageLevelInfos1, voltageLevelInfos2,
            twoWindingsTransformer.hasPhaseTapChanger());
    }

    private void add3wtEdge(BaseGraph graph, ThreeWindingsTransformer threeWindingsTransformer) {
        List<FeederNode> feederNodes = threeWindingsTransformer.getLegStream().map(leg -> {
            String vlId = leg.getTerminal().getVoltageLevel().getId();
            String idLeg = threeWindingsTransformer.getId() + "_" + threeWindingsTransformer.getSide(leg.getTerminal()).name();
            return (FeederNode) graph.getVoltageLevel(vlId).getNode(idLeg);
        }).toList();

        NodeFactory.createMiddle3WTNode(graph, threeWindingsTransformer.getId(), threeWindingsTransformer.getNameOrId(),
                feederNodes.get(0), feederNodes.get(1), feederNodes.get(2));
    }

    private SwitchNode createSwitchNodeFromSwitch(VoltageLevelGraph graph, Switch aSwitch) {
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
        return NodeFactory.createSwitchNode(graph, aSwitch.getId(), aSwitch.getNameOrId(), componentType, aSwitch.isFictitious(), sk, aSwitch.isOpen());
    }

    @Override
    public ZoneGraph buildZoneGraph(List<String> substationIds) {
        Objects.requireNonNull(substationIds);

        List<Substation> zone = substationIds.stream().map(substationId -> {
            Substation substation = network.getSubstation(substationId);
            if (substation == null) {
                throw new PowsyblException("Substation '" + substationId + "' not in network " + network.getId());
            }
            return substation;
        }).collect(Collectors.toList());

        ZoneGraph graph = ZoneGraph.create(substationIds);
        buildZoneGraph(graph, zone);

        return graph;
    }

    private void buildZoneGraph(ZoneGraph zoneGraph, List<Substation> zone) {
        if (zone.isEmpty()) {
            LOGGER.warn("No substations in the zone: skipping graph building");
            return;
        }
        // add nodes -> substation graphs
        GraphBuilder graphBuilder = new NetworkGraphBuilder(network);
        zone.forEach(substation -> {
            LOGGER.info("Adding substation {} to zone graph", substation.getId());
            SubstationGraph sGraph = graphBuilder.buildSubstationGraph(substation.getId(), zoneGraph);
            zoneGraph.addSubstation(sGraph);
        });
        // add snake edges between different
        // - substations in the same zone
        zone.stream().flatMap(Substation::getVoltageLevelStream)
            .flatMap(VoltageLevel::getLineStream)
            .filter(NetworkGraphBuilder::isNotInternalToSubstation)
            .sorted(Comparator.comparing(Line::getId))
            .distinct()
            .forEach(line -> addBranchEdge(zoneGraph, line));

        // - hvdc lines in the same zone
        zone.stream().flatMap(Substation::getVoltageLevelStream)
            .flatMap(VoltageLevel::getVscConverterStationStream)
            .map(HvdcConverterStation::getHvdcLine)
            .distinct()
            .forEach(hvdcLine -> addHvdcLineEdge(zoneGraph, hvdcLine));

        // - tie lines in the same zone
        zone.stream().flatMap(Substation::getVoltageLevelStream)
            .flatMap(VoltageLevel::getDanglingLineStream)
            .map(DanglingLine::getTieLine)
            .flatMap(Optional::stream)
            .sorted(Comparator.comparing(TieLine::getId))
            .distinct()
            .forEach(tieLine -> addBranchEdge(zoneGraph, tieLine));
    }
}
