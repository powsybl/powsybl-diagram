/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.Feeder3WTNode;
import com.powsybl.sld.model.FeederLineNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.FictitiousNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Position;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.postprocessor.GraphBuildPostProcessor;
import org.jgrapht.alg.ConnectivityInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.CAPACITOR;
import static com.powsybl.sld.library.ComponentTypeName.DANGLING_LINE;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.GENERATOR;
import static com.powsybl.sld.library.ComponentTypeName.INDUCTOR;
import static com.powsybl.sld.library.ComponentTypeName.LINE;
import static com.powsybl.sld.library.ComponentTypeName.LOAD;
import static com.powsybl.sld.library.ComponentTypeName.LOAD_BREAK_SWITCH;
import static com.powsybl.sld.library.ComponentTypeName.PHASE_SHIFT_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.STATIC_VAR_COMPENSATOR;
import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.VSC_CONVERTER_STATION;

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

    public Graph buildVoltageLevelGraph(String id, boolean useName,
                                        boolean forVoltageLevelDiagram, boolean showInductorFor3WT) {
        // get the voltageLevel from id
        VoltageLevel vl = network.getVoltageLevel(id);
        if (vl == null) {
            throw new PowsyblException("Voltage level '" + id + "' not found !!");
        }

        // build the graph from the voltage level
        Graph graph = Graph.create(vl.getId(), vl.getName(), vl.getNominalV(), useName,
                forVoltageLevelDiagram, showInductorFor3WT);
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

        constructCellForThreeWindingsTransformer(graph);

        handleGraphPostProcessors(graph);

        handleConnectedComponents(graph);
    }

    private abstract class AbstractGraphBuilder extends DefaultTopologyVisitor {

        protected final Graph graph;

        public AbstractGraphBuilder(Graph graph) {
            this.graph = graph;
        }

        protected abstract void addFeeder(FeederNode node, Terminal terminal);

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
                                                TwoWindingsTransformer.Side side) {
            addFeeder(createFeeder2WTNode(graph, transformer, side), transformer.getTerminal(side));
        }

        @Override
        public void visitLine(Line line, Line.Side side) {
            addFeeder(createFeederLineNode(graph, line, side), line.getTerminal(side));
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
                                                  ThreeWindingsTransformer.Side side) {
            addFeeder(createFeeder3WTNode(graph, transformer, side), transformer.getTerminal(side));
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

        protected void addFeeder(FeederNode node, Terminal terminal) {
            node.setOrder(order++);
            node.setDirection(order % 2 == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
            graph.addNode(node);
            SwitchNode nodeSwitch = createSwitchNodeFromTerminal(graph, terminal);
            graph.addNode(nodeSwitch);
            String busId = terminal.getBusBreakerView().getConnectableBus().getId();
            graph.addEdge(nodesByBusId.get(busId), nodeSwitch);
            graph.addEdge(nodeSwitch, node);
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
    private InfosInductor3WT findInductorOf3WT(Feeder3WTNode node, ThreeWindingsTransformer transformer) {
        final AtomicReference<InfosInductor3WT> infos = new AtomicReference<>();

        transformer.getTerminals().stream()
                .filter(t -> transformer.getSide(t) != ThreeWindingsTransformer.Side.ONE)
                .forEach(t -> {
                    VoltageLevel v = t.getVoltageLevel();
                    if (!v.getId().equals(node.getGraph().getVoltageLevelId())) {
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

    private VoltageLevel getVL2Side(Feeder3WTNode node, ThreeWindingsTransformer transformer) {
        VoltageLevel vl = null;
        switch (node.getSide()) {
            case ONE: vl = transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel(); break;
            case TWO: case THREE: vl = transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel(); break;
        }
        return vl;
    }

    private VoltageLevel getVL3Side(Feeder3WTNode node, ThreeWindingsTransformer transformer) {
        VoltageLevel vl = null;
        switch (node.getSide()) {
            case ONE: case TWO: vl = transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel(); break;
            case THREE: vl = transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel(); break;
        }
        return vl;
    }

    private void constructCellForThreeWindingsTransformer(Graph graph) {
        graph.getNodes().stream()
                .filter(n -> n instanceof Feeder3WTNode)
                .forEach(n -> {
                    Feeder3WTNode n3WT = (Feeder3WTNode) n;
                    ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(n3WT.getTransformerId());

                    // Create a new fictitious node
                    Fictitious3WTNode nf = new Fictitious3WTNode(graph, n3WT.getLabel() + "_fictif", n3WT.getTransformerId());
                    graph.addNode(nf);

                    FeederNode nfeeder1 = null;
                    FeederNode nfeeder2 = null;

                    InfosInductor3WT infosInductor = null;
                    if (graph.isShowInductorFor3WT() && graph.isForVoltageLevelDiagram()) {
                        // finding the inductor in the tertiary voltage level
                        infosInductor = findInductorOf3WT(n3WT, transformer);
                    }

                    VoltageLevel vl2Side = getVL2Side(n3WT, transformer);
                    VoltageLevel vl3Side = getVL3Side(n3WT, transformer);

                    if (infosInductor != null) {
                        // We are in a voltage level diagram AND
                        // We want to show (if we are not in the tertiary voltage level),
                        // the inductor present in the tertiary voltage level of the 3 windings transformer,

                        // We represent the 3 windings transformer like a double feeder cell with :
                        // . one winding to the secondary voltage level
                        // . one feeder for the inductor present in the tertiary voltage level

                        if (infosInductor.getSide() == ThreeWindingsTransformer.Side.TWO) {
                            // Create a feeder for the inductor
                            String idFeeder1 = infosInductor.getId();
                            String nameFeeder1 = infosInductor.getName();
                            nfeeder1 = Feeder2WTNode.create(graph, idFeeder1, nameFeeder1, vl2Side.getId(), vl2Side.getNominalV());
                            nfeeder1.setComponentType(INDUCTOR);
                        } else {
                            // Create a feeder for the winding to the secondary voltage level
                            String idFeeder1 = n3WT.getId() + "_" + n3WT.getId2();
                            String nameFeeder1 = n3WT.getName() + "_" + n3WT.getId2();
                            nfeeder1 = Feeder2WTNode.create(graph, idFeeder1, nameFeeder1, vl2Side.getId(), vl2Side.getNominalV());
                            nfeeder1.setComponentType(LINE);
                        }
                        nfeeder1.setOrder(n3WT.getOrder());
                        nfeeder1.setDirection(n3WT.getDirection());
                        graph.addNode(nfeeder1);

                        if (infosInductor.getSide() == ThreeWindingsTransformer.Side.THREE) {
                            // Create a feeder for the inductor
                            String idFeeder2 = infosInductor.getId();
                            String nameFeeder2 = infosInductor.getName();
                            nfeeder2 = Feeder2WTNode.create(graph, idFeeder2, nameFeeder2, vl3Side.getId(), vl3Side.getNominalV());
                            nfeeder2.setComponentType(INDUCTOR);
                        } else {
                            // Create a feeder for the winding to the tertiary voltage level
                            String idFeeder2 = n3WT.getId() + "_" + n3WT.getId3();
                            String nameFeeder2 = n3WT.getName() + "_" + n3WT.getId3();
                            nfeeder2 = Feeder2WTNode.create(graph, idFeeder2, nameFeeder2, vl3Side.getId(), vl3Side.getNominalV());
                            nfeeder2.setComponentType(LINE);
                        }
                        nfeeder2.setOrder(n3WT.getOrder() + 1);
                        nfeeder2.setDirection(n3WT.getDirection());
                        graph.addNode(nfeeder2);
                    } else {
                        // We represent the 3 windings transformer like a double feeder cell with :
                        // . one winding to the first other voltage level
                        // . one winding to the second other voltage level

                        // Create a feeder for the winding to the first other voltage level
                        String idFeeder1 = n3WT.getId() + "_" + n3WT.getId2();
                        String nameFeeder1 = n3WT.getName() + "_" + n3WT.getId2();
                        nfeeder1 = Feeder2WTNode.create(graph, idFeeder1, nameFeeder1, vl2Side.getId(), vl2Side.getNominalV());
                        nfeeder1.setComponentType(LINE);
                        nfeeder1.setOrder(n3WT.getOrder());
                        nfeeder1.setDirection(n3WT.getDirection());
                        graph.addNode(nfeeder1);

                        // Create a feeder for the winding to the second other voltage level
                        String idFeeder2 = n3WT.getId() + "_" + n3WT.getId3();
                        String nameFeeder2 = n3WT.getName() + "_" + n3WT.getId3();
                        nfeeder2 = Feeder2WTNode.create(graph, idFeeder2, nameFeeder2, vl3Side.getId(), vl3Side.getNominalV());
                        nfeeder2.setComponentType(LINE);
                        nfeeder2.setOrder(n3WT.getOrder() + 1);
                        nfeeder2.setDirection(n3WT.getDirection());
                        graph.addNode(nfeeder2);
                    }

                    // Replacement of the old 3WT feeder node by the new fictitious node
                    graph.substitueNode(n3WT, nf);

                    // Add edges between the new fictitious node and the new feeder nodes
                    graph.addEdge(nf, nfeeder1);
                    graph.addEdge(nf, nfeeder2);
                });
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
                    gbp.getId(), graph.getVoltageLevelId());
            gbp.addNode(graph);
        }
    }

    private void buildSubstationGraph(SubstationGraph graph, Substation substation, boolean useName) {
        // building the graph for each voltageLevel (ordered by descending voltageLevel nominalV)
        substation.getVoltageLevelStream()
                .sorted(Comparator.comparing(VoltageLevel::getNominalV)
                        .reversed()).forEach(v -> {
                            Graph vlGraph = Graph.create(v.getId(), v.getName(), v.getNominalV(),
                                    useName, false, false);
                            buildGraph(vlGraph, v);
                            graph.addNode(vlGraph);
                        });

        LOGGER.info("Number of node : {} ", graph.getNodes().size());

        // Creation of snake lines for transformers between the voltage levels
        // in the substation diagram
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

            VoltageLevel v1 = t1.getVoltageLevel();
            VoltageLevel v2 = t2.getVoltageLevel();

            Graph g1 = graph.getNode(v1.getId());
            Graph g2 = graph.getNode(v2.getId());

            Node n1 = g1.getNode(id1);
            Node n2 = g2.getNode(id2);

            graph.addEdge(n1, n2);
        }

        // Three windings transformer
        //
        for (ThreeWindingsTransformer transfo : substation.getThreeWindingsTransformers()) {
            Terminal t1 = transfo.getLeg1().getTerminal();
            Terminal t2 = transfo.getLeg2().getTerminal();
            Terminal t3 = transfo.getLeg3().getTerminal();

            String id12 = transfo.getId() + "_" + transfo.getSide(t1).name() + "_" + transfo.getSide(t2).name();
            String id13 = transfo.getId() + "_" + transfo.getSide(t1).name() + "_" + transfo.getSide(t3).name();

            String id21 = transfo.getId() + "_" + transfo.getSide(t2).name() + "_" + transfo.getSide(t1).name();
            String id23 = transfo.getId() + "_" + transfo.getSide(t2).name() + "_" + transfo.getSide(t3).name();

            String id31 = transfo.getId() + "_" + transfo.getSide(t3).name() + "_" + transfo.getSide(t1).name();
            String id32 = transfo.getId() + "_" + transfo.getSide(t3).name() + "_" + transfo.getSide(t2).name();

            VoltageLevel v1 = t1.getVoltageLevel();
            VoltageLevel v2 = t2.getVoltageLevel();
            VoltageLevel v3 = t3.getVoltageLevel();

            Graph g1 = graph.getNode(v1.getId());
            Graph g2 = graph.getNode(v2.getId());
            Graph g3 = graph.getNode(v3.getId());

            Node n12 = g1.getNode(id12);
            Node n13 = g1.getNode(id13);

            Node n21 = g2.getNode(id21);
            Node n23 = g2.getNode(id23);

            Node n31 = g3.getNode(id31);
            Node n32 = g3.getNode(id32);

            graph.addEdge(n12, n21);
            graph.addEdge(n13, n31);
            graph.addEdge(n23, n32);
        }
    }

    private FeederNode createFeederLineNode(Graph graph, Line line, Branch.Side side) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(line);

        String id = line.getId() + "_" + side.name();
        String name = line.getName() + "_" + side.name();
        Branch.Side otherSide = side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        VoltageLevel vlOtherSide = line.getTerminal(otherSide).getVoltageLevel();
        return new FeederLineNode(id, name, LINE, false, graph, vlOtherSide.getId(), vlOtherSide.getNominalV());
    }

    private FeederNode createFeederNode(Graph graph, Injection injection) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(injection);
        String componentType;
        switch (injection.getType()) {
            case GENERATOR:
                componentType = GENERATOR;
                break;
            case LOAD:
                componentType = LOAD;
                break;
            case HVDC_CONVERTER_STATION:
                componentType = VSC_CONVERTER_STATION;
                break;
            case STATIC_VAR_COMPENSATOR:
                componentType = STATIC_VAR_COMPENSATOR;
                break;
            case SHUNT_COMPENSATOR:
                componentType = ((ShuntCompensator) injection).getbPerSection() >= 0 ? CAPACITOR : INDUCTOR;
                break;
            case DANGLING_LINE:
                componentType = DANGLING_LINE;
                break;
            default:
                throw new AssertionError();
        }
        return new FeederNode(injection.getId(), injection.getName(), componentType, false, graph);
    }

    private FeederNode createFeeder2WTNode(Graph graph,
                                           TwoWindingsTransformer branch,
                                           TwoWindingsTransformer.Side side) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(branch);
        String componentType;

        if (branch.getPhaseTapChanger() == null) {
            componentType = TWO_WINDINGS_TRANSFORMER;
        } else {
            componentType = PHASE_SHIFT_TRANSFORMER;
        }

        String id = branch.getId() + "_" + side.name();
        String name = branch.getName() + "_" + side.name();
        TwoWindingsTransformer.Side otherSide = side == TwoWindingsTransformer.Side.ONE
                ? TwoWindingsTransformer.Side.TWO
                : TwoWindingsTransformer.Side.ONE;
        VoltageLevel vlOtherSide = branch.getTerminal(otherSide).getVoltageLevel();
        return new Feeder2WTNode(id, name, componentType, false, graph,
                vlOtherSide.getId(), vlOtherSide.getNominalV());
    }

    private SwitchNode createSwitchNodeFromTerminal(Graph graph, Terminal terminal) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(terminal);
        Bus bus = terminal.getBusBreakerView().getConnectableBus();
        String id = bus.getId() + "_" + terminal.getConnectable().getId();
        String name = bus.getName() + "_" + terminal.getConnectable().getName();
        return new SwitchNode(id, name, DISCONNECTOR, false, graph, SwitchNode.SwitchKind.DISCONNECTOR, !terminal.isConnected());
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

    private Feeder3WTNode createFeeder3WTNode(Graph graph,
                                              ThreeWindingsTransformer twt,
                                              ThreeWindingsTransformer.Side side) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(twt);
        Objects.requireNonNull(side);
        String id = twt.getId() + "_" + side.name();
        String name = twt.getName() + "_" + side.name();
        Feeder3WTNode.Side s = Feeder3WTNode.Side.valueOf(side.name());
        return new Feeder3WTNode(id, name, THREE_WINDINGS_TRANSFORMER, false, graph, twt.getId(), s);
    }
}
