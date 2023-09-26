/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.build.GraphBuilder;
import com.powsybl.nad.model.*;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkGraphBuilder implements GraphBuilder {

    private final Network network;
    private final IdProvider idProvider;
    private final Predicate<VoltageLevel> voltageLevelFilter;

    public NetworkGraphBuilder(Network network, Predicate<VoltageLevel> voltageLevelFilter, IdProvider idProvider) {
        this.network = Objects.requireNonNull(network);
        this.voltageLevelFilter = voltageLevelFilter;
        this.idProvider = Objects.requireNonNull(idProvider);
    }

    public NetworkGraphBuilder(Network network, Predicate<VoltageLevel> voltageLevelFilter) {
        this(network, voltageLevelFilter, new IntIdProvider());
    }

    public NetworkGraphBuilder(Network network) {
        this(network, VoltageLevelFilter.NO_FILTER, new IntIdProvider());
    }

    @Override
    public Graph buildGraph() {
        Graph graph = new Graph();
        List<VoltageLevel> voltageLevelsVisible = getVoltageLevels();
        List<VoltageLevel> voltageLevelsInvisible = VoltageLevelFilter.getNextDepthVoltageLevels(network, voltageLevelsVisible)
                .stream()
                .sorted(Comparator.comparing(VoltageLevel::getId))
                .toList();
        voltageLevelsVisible.forEach(vl -> addVoltageLevelGraphNode(vl, graph, true));
        voltageLevelsInvisible.forEach(vl -> addVoltageLevelGraphNode(vl, graph, false));
        voltageLevelsVisible.forEach(vl -> addGraphEdges(vl, graph));
        return graph;
    }

    public List<VoltageLevel> getVoltageLevels() {
        return network.getVoltageLevelStream()
                .filter(voltageLevelFilter)
                .sorted(Comparator.comparing(VoltageLevel::getId))
                .toList();
    }

    public List<VoltageLevel> getVisibleVoltageLevels(int depth) {
        List<String> voltageLevelIds = getVoltageLevels().stream()
                .map(VoltageLevel::getId)
                .sorted()
                .toList();
        VoltageLevelFilter filter = VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth);
        return network.getVoltageLevelStream()
                .filter(filter)
                .sorted(Comparator.comparing(VoltageLevel::getId))
                .toList();
    }

    public List<String> getVisibleVoltageLevelIds(int depth) {
        return getVisibleVoltageLevels(depth).stream()
                .map(VoltageLevel::getId)
                .sorted()
                .toList();
    }

    private VoltageLevelNode addVoltageLevelGraphNode(VoltageLevel vl, Graph graph, boolean visible) {
        VoltageLevelNode vlNode = new VoltageLevelNode(idProvider.createId(vl), vl.getId(), vl.getNameOrId(), vl.isFictitious(), visible);
        vl.getBusView().getBusStream()
                .map(bus -> new BusNode(idProvider.createId(bus), bus.getId()))
                .forEach(vlNode::addBusNode);
        graph.addNode(vlNode);
        if (visible) {
            graph.addTextNode(vlNode);
        }
        return vlNode;
    }

    private void addGraphEdges(VoltageLevel vl, Graph graph) {
        vl.getLineStream().forEach(l -> visitLine(vl, l, graph));
        vl.getTwoWindingsTransformerStream().forEach(twt -> visitTwoWindingsTransformer(vl, twt, graph));
        vl.getThreeWindingsTransformerStream().forEach(thwt -> visitThreeWindingsTransformer(vl, thwt, graph));
        vl.getDanglingLineStream().forEach(dl -> visitDanglingLine(dl, graph));
        vl.getConnectableStream(HvdcConverterStation.class).forEach(hvdc -> visitHvdcConverterStation(hvdc, graph));
    }

    private void visitLine(VoltageLevel vl, Line line, Graph graph) {
        addEdge(graph, line, vl, BranchEdge.LINE_EDGE);
    }

    private void visitTwoWindingsTransformer(VoltageLevel vl, TwoWindingsTransformer twt, Graph graph) {
        addEdge(graph, twt, vl, twt.hasPhaseTapChanger() ? BranchEdge.PST_EDGE : BranchEdge.TWO_WT_EDGE);
    }

    private void visitThreeWindingsTransformer(VoltageLevel vl, ThreeWindingsTransformer thwt, Graph graph) {
        // check if the transformer was not already added (at the other sides of the transformer)
        if (graph.containsNode(thwt.getId())) {
            return;
        }

        ThreeWtNode tn = new ThreeWtNode(idProvider.createId(thwt), thwt.getId(), thwt.getNameOrId());
        graph.addNode(tn);

        ThreeWindingsTransformer.Side side;
        if (thwt.getLeg1().getTerminal().getVoltageLevel() == vl) {
            side = ThreeWindingsTransformer.Side.ONE;
        } else if (thwt.getLeg2().getTerminal().getVoltageLevel() == vl) {
            side = ThreeWindingsTransformer.Side.TWO;
        } else {
            side = ThreeWindingsTransformer.Side.THREE;
        }

        for (ThreeWindingsTransformer.Side s : getSidesArray(side)) {
            addThreeWtEdge(graph, thwt, tn, s);
        }
    }

    private void visitDanglingLine(DanglingLine dl, Graph graph) {
        if (!dl.isPaired()) {
            BoundaryNode boundaryNode = new BoundaryNode(idProvider.createId(dl), dl.getId(), dl.getNameOrId());
            BusNode boundaryBusNode = new BoundaryBusNode(idProvider.createId(dl), dl.getId());
            boundaryNode.addBusNode(boundaryBusNode);
            graph.addNode(boundaryNode);
            addEdge(graph, dl, boundaryNode, boundaryBusNode);
        } else {
            dl.getTieLine().ifPresent(tieLine -> visitTieLine(tieLine, dl, graph));
        }
    }

    private void visitTieLine(TieLine tieLine, DanglingLine dl, Graph graph) {
        addEdge(graph, tieLine, dl.getTerminal().getVoltageLevel(), BranchEdge.TIE_LINE_EDGE);
    }

    private void visitHvdcConverterStation(HvdcConverterStation<?> converterStation, Graph graph) {
        // check if the hvdc line was not already added (at the other side of the line)
        HvdcLine hvdcLine = converterStation.getHvdcLine();
        if (graph.containsEdge(hvdcLine.getId())) {
            return;
        }

        HvdcLine.Side otherSide = (hvdcLine.getConverterStation1().getId().equals(converterStation.getId()))
                ? HvdcLine.Side.TWO : HvdcLine.Side.ONE;

        Terminal terminal = converterStation.getTerminal();
        Terminal otherSideTerminal = hvdcLine.getConverterStation(otherSide).getTerminal();

        addEdge(graph, terminal, otherSideTerminal, hvdcLine, BranchEdge.HVDC_LINE_EDGE, otherSide == HvdcLine.Side.ONE);
    }

    private void addEdge(Graph graph, Branch<?> branch, VoltageLevel vl, String edgeType) {
        Branch.Side side = branch.getTerminal(Branch.Side.ONE).getVoltageLevel() == vl ? Branch.Side.ONE : Branch.Side.TWO;
        // check if the edge was not already added (at the other side of the transformer)
        if (graph.containsEdge(branch.getId())) {
            return;
        }

        Terminal terminalA = branch.getTerminal(side);
        Terminal terminalB = branch.getTerminal(IidmUtils.getOpposite(side));

        addEdge(graph, terminalA, terminalB, branch, edgeType, side == Branch.Side.TWO);
    }

    private void addEdge(Graph graph, Terminal terminalA, Terminal terminalB, Identifiable<?> identifiable, String edgeType, boolean terminalsInReversedOrder) {
        VoltageLevelNode vlNodeA = getVoltageLevelNode(graph, terminalA);
        VoltageLevelNode vlNodeB = getVoltageLevelNode(graph, terminalB);

        BusNode busNodeA = getBusNode(graph, terminalA);
        BusNode busNodeB = getBusNode(graph, terminalB);

        BranchEdge edge = new BranchEdge(idProvider.createId(identifiable), identifiable.getId(), identifiable.getNameOrId(), edgeType);
        if (!terminalsInReversedOrder) {
            graph.addEdge(vlNodeA, busNodeA, vlNodeB, busNodeB, edge);
        } else {
            graph.addEdge(vlNodeB, busNodeB, vlNodeA, busNodeA, edge);
        }
    }

    private void addThreeWtEdge(Graph graph, ThreeWindingsTransformer twt, ThreeWtNode tn, ThreeWindingsTransformer.Side side) {
        Terminal terminal = twt.getTerminal(side);
        VoltageLevelNode vlNode = getVoltageLevelNode(graph, terminal);
        ThreeWtEdge edge = new ThreeWtEdge(idProvider.createId(IidmUtils.get3wtLeg(twt, side)),
                twt.getId(), twt.getNameOrId(), IidmUtils.getThreeWtEdgeSideFromIidmSide(side), vlNode.isVisible());
        graph.addEdge(vlNode, getBusNode(graph, terminal), tn, edge);
    }

    private void addEdge(Graph graph, DanglingLine dl, BoundaryNode boundaryVlNode, BusNode boundaryBusNode) {
        Terminal terminal = dl.getTerminal();
        VoltageLevelNode vlNode = getVoltageLevelNode(graph, terminal);
        BranchEdge edge = new BranchEdge(idProvider.createId(dl),
                dl.getId(), dl.getNameOrId(), BranchEdge.DANGLING_LINE_EDGE);
        graph.addEdge(vlNode, getBusNode(graph, terminal), boundaryVlNode, boundaryBusNode, edge);
    }

    private BusNode getBusNode(Graph graph, Terminal terminal) {
        Bus connectableBusA = terminal.getBusView().getConnectableBus();
        if (connectableBusA == null) {
            graph.getVoltageLevelNode(terminal.getVoltageLevel().getId()).ifPresent(vlNode -> vlNode.setHasUnknownBusNode(true));
            return BusNode.UNKNOWN;
        }
        return graph.getBusNode(connectableBusA.getId());
    }

    private VoltageLevelNode getVoltageLevelNode(Graph graph, Terminal terminal) {
        return graph.getVoltageLevelNode(terminal.getVoltageLevel().getId())
                .orElseThrow(() -> new PowsyblException("Cannot add edge, corresponding voltage level is unknown: '" + terminal.getVoltageLevel().getId() + "'"));
    }

    private ThreeWindingsTransformer.Side[] getSidesArray(ThreeWindingsTransformer.Side sideA) {
        ThreeWindingsTransformer.Side sideB;
        ThreeWindingsTransformer.Side sideC;
        if (sideA == ThreeWindingsTransformer.Side.ONE) {
            sideB = ThreeWindingsTransformer.Side.TWO;
            sideC = ThreeWindingsTransformer.Side.THREE;
        } else if (sideA == ThreeWindingsTransformer.Side.TWO) {
            sideB = ThreeWindingsTransformer.Side.ONE;
            sideC = ThreeWindingsTransformer.Side.THREE;
        } else {
            sideB = ThreeWindingsTransformer.Side.ONE;
            sideC = ThreeWindingsTransformer.Side.TWO;
        }
        return new ThreeWindingsTransformer.Side[] {sideA, sideB, sideC};
    }
}
