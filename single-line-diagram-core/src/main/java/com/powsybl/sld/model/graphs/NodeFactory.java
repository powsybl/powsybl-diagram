/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.SwitchNode.SwitchKind;
import com.powsybl.sld.model.nodes.feeders.BaseFeeder;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import static com.powsybl.sld.library.ComponentTypeName.*;

import com.powsybl.commons.PowsyblException;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public final class NodeFactory {

    private NodeFactory() {
    }

    public static BusNode createBusNode(VoltageLevelGraph graph, String id, String name) {
        BusNode bn = new BusNode(id, name, false);
        graph.addNode(bn);
        return bn;
    }

    public static BusNode createFictitiousBusNode(VoltageLevelGraph graph, String id) {
        BusNode bn = new BusNode(id, null, true);
        bn.setBusBarIndexSectionIndex(1, 1);
        graph.addNode(bn);
        return bn;
    }

    public static FeederNode createFeederNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentTypeName, boolean fictitious, Feeder feeder, Orientation orientation) {
        FeederNode fn = new FeederNode(id, name, equipmentId, componentTypeName, fictitious, feeder, orientation);
        graph.addNode(fn);
        return fn;
    }

    public static FeederNode createFeederNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, Feeder feeder) {
        return createFeederNode(graph, id, name, equipmentId, componentType, false, feeder, null);
    }

    public static FeederNode createFictitiousFeederNode(VoltageLevelGraph graph, String id, Orientation orientation) {
        return createFeederNode(graph, id, id, id, NODE, true, new BaseFeeder(FeederType.FICTITIOUS), orientation);
    }

    public static FeederNode createFeederInjectionNode(VoltageLevelGraph graph, String id, String name, String componentType) {
        return createFeederNode(graph, id, name, id, componentType, false, new BaseFeeder(FeederType.INJECTION), null);
    }

    public static FeederNode createGenerator(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.GENERATOR);
    }

    public static FeederNode createLoad(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.LOAD);
    }

    public static FeederNode createVscConverterStation(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        if (side == null || otherSideVoltageLevelInfos == null) {
            return createFeederInjectionNode(graph, id, name, VSC_CONVERTER_STATION);
        }
        return createFeederWithSideNode(graph, id, name, equipmentId, ComponentTypeName.VSC_CONVERTER_STATION, side, otherSideVoltageLevelInfos, FeederType.HVDC);
    }

    public static FeederNode createStaticVarCompensator(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.STATIC_VAR_COMPENSATOR);
    }

    public static FeederNode createInductor(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.INDUCTOR);
    }

    public static FeederNode createCapacitor(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.CAPACITOR);
    }

    public static FeederNode createDanglingLine(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.DANGLING_LINE);
    }

    public static FeederNode createFeederWithSideNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos, FeederType feederType) {
        return createFeederNode(graph, id, name, equipmentId, componentType, new FeederWithSides(feederType, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos));
    }

    public static FeederNode createFeederBranchNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return createFeederNode(graph, id, name, equipmentId, componentType, new FeederWithSides(FeederType.BRANCH, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos));
    }

    public static FeederNode createFeeder2WTNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return createFeederBranchNode(graph, id, name, equipmentId, componentType, side, otherSideVoltageLevelInfos);
    }

    public static FeederNode createFeeder2WTNode(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return createFeeder2WTNode(graph, id, name, equipmentId, TWO_WINDINGS_TRANSFORMER, side, otherSideVoltageLevelInfos);
    }

    public static FeederNode createFeeder2WTNode(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side) {
        return createFeeder2WTNode(graph, id, name, equipmentId, side, graph.getVoltageLevelInfos());
    }

    public static FeederNode createFeeder2WTNodeWithPhaseShifter(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return createFeeder2WTNode(graph, id, name, equipmentId, PHASE_SHIFT_TRANSFORMER, side, otherSideVoltageLevelInfos);
    }

    public static FeederNode createFeederLineNode(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return createFeederBranchNode(graph, id, name, equipmentId, ComponentTypeName.LINE, side, otherSideVoltageLevelInfos);
    }

    public static FeederNode createFeederTwtLegNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos, FeederType feederType) {
        return createFeederNode(graph, id, name, equipmentId, componentType, new FeederTwLeg(feederType, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos));
    }

    public static FeederNode createFeeder2WTLegNode(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side) {
        return createFeederTwtLegNode(graph, id, name, equipmentId, TWO_WINDINGS_TRANSFORMER_LEG, side, graph.getVoltageLevelInfos(), FeederType.TWO_WINDINGS_TRANSFORMER_LEG);
    }

    public static FeederNode createFeeder2WTLegNodeWithPhaseShifter(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side) {
        return createFeederTwtLegNode(graph, id, name, equipmentId, PHASE_SHIFT_TRANSFORMER_LEG, side, graph.getVoltageLevelInfos(), FeederType.TWO_WINDINGS_TRANSFORMER_LEG);
    }

    public static FeederNode createFeeder3WTLegNodeForVoltageLevelDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return createFeederTwtLegNode(graph, id, name, equipmentId, THREE_WINDINGS_TRANSFORMER_LEG, side, otherSideVoltageLevelInfos, FeederType.THREE_WINDINGS_TRANSFORMER_LEG);
    }

    public static FeederNode createFeeder3WTLegNodeForSubstationDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side) {
        return createFeederTwtLegNode(graph, id, name, equipmentId, THREE_WINDINGS_TRANSFORMER_LEG, side, graph.getVoltageLevelInfos(), FeederType.THREE_WINDINGS_TRANSFORMER_LEG);
    }

    public static FictitiousNode createFictitiousNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType) {
        FictitiousNode fn = new FictitiousNode(id, name, equipmentId, componentType);
        graph.addNode(fn);
        return fn;
    }

    public static BusConnection createBusConnection(VoltageLevelGraph graph, String id) {
        BusConnection bc = new BusConnection(id);
        graph.addNode(bc);
        return bc;
    }

    public static InternalNode createInternalNode(VoltageLevelGraph graph, String id) {
        InternalNode in = new InternalNode(id, graph.getVoltageLevelInfos().getId());
        graph.addNode(in);
        return in;
    }

    public static InternalNode createInternalNode(VoltageLevelGraph graph, int id) {
        InternalNode in = new InternalNode(id, graph.getVoltageLevelInfos().getId());
        graph.addNode(in);
        return in;
    }

    public static SwitchNode createSwitchNode(VoltageLevelGraph graph, String id, String name, String componentType, boolean fictitious, SwitchKind kind, boolean open) {
        SwitchNode sn = new SwitchNode(id, name, componentType, fictitious, kind, open);
        graph.addNode(sn);
        return sn;
    }

    public static Middle2WTNode createMiddle2WTNode(BaseGraph baseGraph, String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, String componentType) {
        Middle2WTNode m2wn = new Middle2WTNode(id, name, voltageLevelInfosLeg1, voltageLevelInfosLeg2, componentType);
        baseGraph.addMultiTermNode(m2wn);
        return m2wn;
    }

    public static Middle2WTNode createMiddle2WTNode(BaseGraph baseGraph, String id, String name, FeederNode legNode1, FeederNode legNode2, VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, boolean hasPhaseTapChanger) {
        String componentType = hasPhaseTapChanger ? PHASE_SHIFT_TRANSFORMER : TWO_WINDINGS_TRANSFORMER;
        Middle2WTNode m2wn = new Middle2WTNode(id, name, vlInfos1, vlInfos2, componentType);
        baseGraph.addTwtEdge(legNode1, m2wn);
        baseGraph.addTwtEdge(legNode2, m2wn);
        baseGraph.addMultiTermNode(m2wn);
        return m2wn;
    }

    public static Middle3WTNode createMiddle3WTNode(VoltageLevelGraph baseGraph, String id, String name, NodeSide vlSide,
                                                    FeederNode firstOtherLegNode, FeederNode secondOtherLegNode,
                                                    VoltageLevelInfos vlLeg1, VoltageLevelInfos vlLeg2, VoltageLevelInfos vlLeg3) {
        if (!ComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG.equals(firstOtherLegNode.getComponentType())
                || !ComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG.equals(secondOtherLegNode.getComponentType())) {
            throw new PowsyblException("Middle3WTNode must be created with FeederNode with ComponentTypeName THREE_WINDINGS_TRANSFORMER_LEG");
        }
        Middle3WTNode m3wn = new Middle3WTNode(id, name, vlLeg1, vlLeg2, vlLeg3, true);
        m3wn.setWindingOrder(Middle3WTNode.Winding.DOWN, vlSide);
        m3wn.setWindingOrder(Middle3WTNode.Winding.UPPER_LEFT, ((FeederTwLeg) firstOtherLegNode.getFeeder()).getSide());
        m3wn.setWindingOrder(Middle3WTNode.Winding.UPPER_RIGHT, ((FeederTwLeg) secondOtherLegNode.getFeeder()).getSide());
        baseGraph.addNode(m3wn);
        baseGraph.addEdge(firstOtherLegNode, m3wn);
        baseGraph.addEdge(secondOtherLegNode, m3wn);
        return m3wn;
    }

    public static Middle3WTNode createMiddle3WTNode(BaseGraph baseGraph, String id, String name, FeederNode legNode1, FeederNode legNode2, FeederNode legNode3) {
        if (!ComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG.equals(legNode1.getComponentType())
                || !ComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG.equals(legNode2.getComponentType())
                || !ComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG.equals(legNode3.getComponentType())) {
            throw new PowsyblException("Middle3WTNode must be created with FeederNode with ComponentTypeName THREE_WINDINGS_TRANSFORMER_LEG");
        }
        Middle3WTNode m3wn =  new Middle3WTNode(id, name,
                ((FeederTwLeg) legNode1.getFeeder()).getVoltageLevelInfos(),
                ((FeederTwLeg) legNode2.getFeeder()).getVoltageLevelInfos(),
                ((FeederTwLeg) legNode3.getFeeder()).getVoltageLevelInfos(),
                false);
        baseGraph.addTwtEdge(legNode1, m3wn);
        baseGraph.addTwtEdge(legNode2, m3wn);
        baseGraph.addTwtEdge(legNode3, m3wn);
        baseGraph.addMultiTermNode(m3wn);
        return m3wn;
    }

}
