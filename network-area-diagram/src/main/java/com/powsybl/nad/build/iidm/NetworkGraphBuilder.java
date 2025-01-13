/**
 * Copyright (c) 2021-2025, RTE (http://www.rte-france.com)
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
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

    private VoltageLevelNode addVoltageLevelGraphNode(VoltageLevel vl, Graph graph, boolean visible) {
        VoltageLevelNode vlNode = new VoltageLevelNode(idProvider.createId(vl), vl.getId(), vl.getNameOrId(), vl.isFictitious(), visible);
        vl.getBusView().getBusStream()
                .map(bus -> new BusNode(idProvider.createId(bus), bus.getId()))
                .forEach(vlNode::addBusNode);
        graph.addNode(vlNode);
        if (visible) {
            graph.addTextNode(vlNode);
            graph.addProductionNode(vlNode);
            graph.addConsumptionNode(vlNode);
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

        ThreeSides side = Arrays.stream(ThreeSides.values())
                .filter(streamedSide -> thwt.getLeg(streamedSide).getTerminal().getVoltageLevel() == vl)
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        for (ThreeSides s : getSidesArray(side)) {
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

        TwoSides otherSide = (hvdcLine.getConverterStation1().getId().equals(converterStation.getId()))
                ? TwoSides.TWO : TwoSides.ONE;

        Terminal terminal = converterStation.getTerminal();
        Terminal otherSideTerminal = hvdcLine.getConverterStation(otherSide).getTerminal();

        addEdge(graph, terminal, otherSideTerminal, hvdcLine, BranchEdge.HVDC_LINE_EDGE, otherSide == TwoSides.ONE);
    }

    private void addEdge(Graph graph, Branch<?> branch, VoltageLevel vl, String edgeType) {
        TwoSides side = branch.getTerminal(TwoSides.ONE).getVoltageLevel() == vl ? TwoSides.ONE : TwoSides.TWO;
        // check if the edge was not already added (at the other side of the transformer)
        if (graph.containsEdge(branch.getId())) {
            return;
        }

        Terminal terminalA = branch.getTerminal(side);
        Terminal terminalB = branch.getTerminal(IidmUtils.getOpposite(side));

        addEdge(graph, terminalA, terminalB, branch, edgeType, side == TwoSides.TWO);
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

    private void addThreeWtEdge(Graph graph, ThreeWindingsTransformer twt, ThreeWtNode tn, ThreeSides side) {
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

    private ThreeSides[] getSidesArray(ThreeSides sideA) {
        return new ThreeSides[] {sideA, ThreeSides.valueOf(getNextSideNum(sideA.getNum(), 1)), ThreeSides.valueOf(getNextSideNum(sideA.getNum(), 2))};
    }

    private int getNextSideNum(int sideNum, int steps) {
        return (sideNum + steps + 2) % 3 + 1;
    }
}
