/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.util.IidmUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.nad.build.GraphBuilder;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.*;
import com.powsybl.nad.model.Injection;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgEdgeInfo;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class NetworkGraphBuilder implements GraphBuilder {

    private final Network network;
    private final Predicate<VoltageLevel> voltageLevelFilter;
    private final LabelProvider labelProvider;
    private final IdProvider idProvider;
    private final boolean injectionsAdded;

    public NetworkGraphBuilder(Network network, Predicate<VoltageLevel> voltageLevelFilter, LabelProvider labelProvider, LayoutParameters layoutParameters, IdProvider idProvider) {
        this.network = Objects.requireNonNull(network);
        this.voltageLevelFilter = voltageLevelFilter;
        this.labelProvider = labelProvider;
        this.idProvider = Objects.requireNonNull(idProvider);
        this.injectionsAdded = layoutParameters.isInjectionsAdded();
    }

    public NetworkGraphBuilder(Network network, Predicate<VoltageLevel> voltageLevelFilter, LayoutParameters layoutParameters) {
        this(network, voltageLevelFilter, new DefaultLabelProvider(network, new SvgParameters()), layoutParameters, new IntIdProvider());
    }

    public NetworkGraphBuilder(Network network, LayoutParameters layoutParameters) {
        this(network, VoltageLevelFilter.NO_FILTER, layoutParameters);
    }

    @Override
    public Graph buildGraph() {
        Graph graph = new Graph();
        List<VoltageLevel> voltageLevelsVisible = getVoltageLevels();
        List<VoltageLevel> voltageLevelsInvisible = VoltageLevelFilter.getNextDepthVoltageLevels(network, voltageLevelsVisible)
                .stream()
                .sorted(Comparator.comparing(VoltageLevel::getId))
                .toList();
        voltageLevelsVisible.forEach(vl -> addVoltageLevelGraphNode(vl, graph, true, injectionsAdded));
        voltageLevelsInvisible.forEach(vl -> addVoltageLevelGraphNode(vl, graph, false, false));
        voltageLevelsVisible.forEach(vl -> addGraphEdges(vl, graph));
        return graph;
    }

    public List<VoltageLevel> getVoltageLevels() {
        return network.getVoltageLevelStream()
                .filter(voltageLevelFilter)
                .sorted(Comparator.comparing(VoltageLevel::getId))
                .toList();
    }

    private VoltageLevelNode addVoltageLevelGraphNode(VoltageLevel vl, Graph graph, boolean visible, boolean injectionsAdded) {
        String vlNodeId = idProvider.createSvgId(vl);
        String vlLegendId = idProvider.createSvgId(vl);
        VoltageLevelNode vlNode = new VoltageLevelNode(vlNodeId, vl.getId(), vl.getNameOrId(), vl.isFictitious(), visible,
                vlLegendId, labelProvider);
        Map<String, List<Injection>> injectionsMap = new HashMap<>();
        if (injectionsAdded) {
            fillInjectionsMap(vl, graph, injectionsMap);
        }
        vl.getBusView().getBusStream()
                .map(bus -> new BusNode(idProvider.createSvgId(bus), bus.getId(),
                        injectionsMap.getOrDefault(bus.getId(), Collections.emptyList()),
                        labelProvider.getLegend(bus.getId())))
                .forEach(vlNode::addBusNode);
        graph.addNode(vlNode);
        if (visible) {
            graph.addTextNode(vlNode);
        }
        return vlNode;
    }

    private void fillInjectionsMap(VoltageLevel vl, Graph graph, Map<String, List<Injection>> injectionsMap) {
        vl.getGenerators().forEach(g -> addInjection(graph, g, injectionsMap));
        vl.getLoads().forEach(l -> addInjection(graph, l, injectionsMap));
        vl.getShuntCompensators().forEach(sc -> addInjection(graph, sc, injectionsMap));
        vl.getBatteries().forEach(b -> addInjection(graph, b, injectionsMap));
        vl.getStaticVarCompensators().forEach(svc -> addInjection(graph, svc, injectionsMap));
    }

    private void addInjection(Graph graph, com.powsybl.iidm.network.Injection<?> inj, Map<String, List<Injection>> injectionsMap) {
        injectionsMap.computeIfAbsent(inj.getTerminal().getBusView().getConnectableBus().getId(), k -> new ArrayList<>())
                .add(createInjectionFromIidm(graph, inj));
    }

    private Injection createInjectionFromIidm(Graph graph, com.powsybl.iidm.network.Injection<?> inj) {
        String diagramId = idProvider.createSvgId(inj);
        Injection.Type injectionType = getInjectionType(inj);

        SvgEdgeInfo svgEdgeInfo = labelProvider.getInjectionEdgeInfo(inj.getId())
                .map(ei -> new SvgEdgeInfo(idProvider.createSvgId(inj), ei))
                .orElse(null);

        Injection injDiagram = new Injection(diagramId, inj.getId(), inj.getNameOrId(), injectionType, svgEdgeInfo);
        graph.addInjection(injDiagram);
        return injDiagram;
    }

    private static Injection.Type getInjectionType(com.powsybl.iidm.network.Injection<?> inj) {
        return switch (inj.getType()) {
            case GENERATOR -> Injection.Type.GENERATOR;
            case BATTERY -> Injection.Type.BATTERY;
            case LOAD -> Injection.Type.LOAD;
            case SHUNT_COMPENSATOR -> getShuntCompensatorType((ShuntCompensator) inj);
            case STATIC_VAR_COMPENSATOR -> Injection.Type.STATIC_VAR_COMPENSATOR;
            default -> throw new AssertionError("Unexpected injection type: " + inj.getType());
        };
    }

    private static Injection.Type getShuntCompensatorType(ShuntCompensator shuntCompensator) {
        return IidmUtil.isCapacitor(shuntCompensator) ? Injection.Type.SHUNT_COMPENSATOR_CAPACITOR : Injection.Type.SHUNT_COMPENSATOR_INDUCTOR;
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

        ThreeWtNode tn = new ThreeWtNode(idProvider.createSvgId(thwt), thwt.getId(), thwt.getNameOrId());
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
            BoundaryNode boundaryNode = new BoundaryNode(idProvider.createSvgId(dl), dl.getId(), dl.getNameOrId());
            BusNode boundaryBusNode = new BoundaryBusNode(idProvider.createSvgId(dl), dl.getId());
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

        String edgeType = switch (converterStation.getHvdcType()) {
            case LCC -> BranchEdge.HVDC_LINE_LCC_EDGE;
            case VSC -> BranchEdge.HVDC_LINE_VSC_EDGE;
        };

        addEdge(graph, terminal, otherSideTerminal, hvdcLine, edgeType, otherSide == TwoSides.ONE);
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

        String branchId = identifiable.getId();
        String svgId = idProvider.createSvgId(identifiable);
        SvgEdgeInfo svgEdgeInfo1 = labelProvider.getBranchEdgeInfo(branchId, BranchEdge.Side.ONE, edgeType)
                .map(ei -> new SvgEdgeInfo(idProvider.createSvgId(identifiable), ei))
                .orElse(null);
        SvgEdgeInfo svgEdgeInfo2 = labelProvider.getBranchEdgeInfo(branchId, BranchEdge.Side.TWO, edgeType)
                .map(ei -> new SvgEdgeInfo(idProvider.createSvgId(identifiable), ei))
                .orElse(null);
        String branchLabel = labelProvider.getBranchLabel(branchId);

        BranchEdge edge = new BranchEdge(svgId, branchId, identifiable.getNameOrId(), edgeType,
                svgEdgeInfo1, svgEdgeInfo2, branchLabel);
        if (!terminalsInReversedOrder) {
            graph.addEdge(vlNodeA, busNodeA, vlNodeB, busNodeB, edge);
        } else {
            graph.addEdge(vlNodeB, busNodeB, vlNodeA, busNodeA, edge);
        }
    }

    private void addThreeWtEdge(Graph graph, ThreeWindingsTransformer twt, ThreeWtNode tn, ThreeSides side) {
        Terminal terminal = twt.getTerminal(side);
        VoltageLevelNode vlNode = getVoltageLevelNode(graph, terminal);
        String type = twt.getLeg(side).hasPhaseTapChanger() ? ThreeWtEdge.PST_EDGE : ThreeWtEdge.THREE_WT_EDGE;
        ThreeWtEdge.Side twtEdgeSide = IidmUtils.getThreeWtEdgeSideFromIidmSide(side);

        ThreeWindingsTransformer.Leg leg = IidmUtils.get3wtLeg(twt, side);
        String svgId = idProvider.createSvgId(leg);

        SvgEdgeInfo svgEdgeInfo = labelProvider.getThreeWindingTransformerEdgeInfo(twt.getId(), twtEdgeSide)
                .map(ei -> new SvgEdgeInfo(idProvider.createSvgId(leg), ei))
                .orElse(null);

        ThreeWtEdge edge = new ThreeWtEdge(svgId,
                twt.getId(), twt.getNameOrId(), twtEdgeSide,
                type, vlNode.isVisible(), svgEdgeInfo);
        graph.addEdge(vlNode, getBusNode(graph, terminal), tn, edge);
    }

    private void addEdge(Graph graph, DanglingLine dl, BoundaryNode boundaryVlNode, BusNode boundaryBusNode) {
        Terminal terminal = dl.getTerminal();
        VoltageLevelNode vlNode = getVoltageLevelNode(graph, terminal);

        String branchType = BranchEdge.DANGLING_LINE_EDGE;
        String svgId = idProvider.createSvgId(dl);

        SvgEdgeInfo svgEdgeInfo = labelProvider.getBranchEdgeInfo(dl.getId(), BranchEdge.Side.ONE, branchType)
                .map(ei -> new SvgEdgeInfo(idProvider.createSvgId(dl), ei))
                .orElse(null);

        BranchEdge edge = new BranchEdge(svgId,
                dl.getId(), dl.getNameOrId(), branchType, svgEdgeInfo, null, null);
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
