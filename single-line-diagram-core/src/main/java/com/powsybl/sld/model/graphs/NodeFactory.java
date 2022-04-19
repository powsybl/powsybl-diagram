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
import com.powsybl.sld.model.nodes.FeederWithSideNode.Side;
import com.powsybl.sld.model.nodes.SwitchNode.SwitchKind;

import static com.powsybl.sld.library.ComponentTypeName.*;

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

    public static FeederNode createFeederNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, boolean fictitious, FeederType feederType, Orientation orientation) {
        FeederNode fn = new FeederNode(id, name, equipmentId, componentType, fictitious, feederType, orientation);
        graph.addNode(fn);
        return fn;
    }

    public static FeederNode createFeederNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, FeederType feederType) {
        return createFeederNode(graph, id, name, equipmentId, componentType, false, feederType, null);
    }

    public static FeederNode createFictitiousFeederNode(VoltageLevelGraph graph, String id, Orientation orientation) {
        return createFeederNode(graph, id, id, id, NODE, true, FeederType.FICTITIOUS, orientation);
    }

    public static FeederNode createFeederInjectionNode(String id, String name, String componentType, VoltageLevelGraph graph) {
        FeederInjectionNode fin = new FeederInjectionNode(id, name, componentType);
        graph.addNode(fin);
        return fin;
    }

    public static FeederNode createGenerator(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.GENERATOR, graph);
    }

    public static FeederNode createLoad(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.LOAD, graph);
    }

    public static FeederNode createVscConverterStation(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.VSC_CONVERTER_STATION, graph);
    }

    public static FeederNode createStaticVarCompensator(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.STATIC_VAR_COMPENSATOR, graph);
    }

    public static FeederNode createInductor(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.INDUCTOR, graph);
    }

    public static FeederNode createCapacitor(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.CAPACITOR, graph);
    }

    public static FeederNode createDanglingLine(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.DANGLING_LINE, graph);
    }

    public static FeederWithSideNode createFeederWithSideNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, FeederWithSideNode.Side side, VoltageLevelInfos otherSideVoltageLevelInfos, FeederType feederType) {
        FeederWithSideNode fwsn = new FeederWithSideNode(id, name, equipmentId, componentType, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos, feederType);
        graph.addNode(fwsn);
        return fwsn;
    }

    public static FeederBranchNode createFeederBranchNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        FeederBranchNode fbn = new FeederBranchNode(id, name, equipmentId, componentType, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        graph.addNode(fbn);
        return fbn;
    }

    public static Feeder2WTNode createFeeder2WTNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        Feeder2WTNode f2wtN = new Feeder2WTNode(id, name, equipmentId, componentType, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        graph.addNode(f2wtN);
        return f2wtN;
    }

    public static Feeder2WTNode createFeeder2WTNode(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        Feeder2WTNode f2wtN = new Feeder2WTNode(id, name, equipmentId, TWO_WINDINGS_TRANSFORMER, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        graph.addNode(f2wtN);
        return f2wtN;
    }

    public static Feeder2WTNode createFeeder2WTNode(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side) {
        Feeder2WTNode f2wtN = new Feeder2WTNode(id, name, equipmentId, TWO_WINDINGS_TRANSFORMER, side, graph.getVoltageLevelInfos(), graph.getVoltageLevelInfos());
        graph.addNode(f2wtN);
        return f2wtN;
    }

    public static Feeder2WTNode createFeeder2WTNodeWithPhaseShifter(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        Feeder2WTNode f2wtN = new Feeder2WTNode(id, name, equipmentId, PHASE_SHIFT_TRANSFORMER, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        graph.addNode(f2wtN);
        return f2wtN;
    }

    public static FeederLineNode createFeederLineNode(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        FeederLineNode fln = new FeederLineNode(id, name, equipmentId, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        graph.addNode(fln);
        return fln;
    }

    public static FeederTwtLegNode createFeederTwtLegNode(VoltageLevelGraph graph, String id, String name, String equipmentId, String componentType, Side side, VoltageLevelInfos otherSideVoltageLevelInfos, FeederType feederType) {
        FeederTwtLegNode fTwln = new FeederTwtLegNode(id, name, equipmentId, componentType, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos, feederType);
        graph.addNode(fTwln);
        return fTwln;
    }

    public static Feeder2WTLegNode createFeeder2WTLegNode(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side) {
        Feeder2WTLegNode f2WtLN = new Feeder2WTLegNode(id, name, equipmentId, TWO_WINDINGS_TRANSFORMER_LEG, side, graph.getVoltageLevelInfos(), graph.getVoltageLevelInfos());
        graph.addNode(f2WtLN);
        return f2WtLN;
    }

    public static Feeder2WTLegNode createFeeder2WTLegNodeWithPhaseShifter(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side) {
        Feeder2WTLegNode f2WtLN = new Feeder2WTLegNode(id, name, equipmentId, PHASE_SHIFT_TRANSFORMER_LEG, side, graph.getVoltageLevelInfos(), graph.getVoltageLevelInfos());
        graph.addNode(f2WtLN);
        return f2WtLN;
    }

    public static Feeder3WTLegNode createFeeder3WTLegNodeForVoltageLevelDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        Feeder3WTLegNode f3WtLN = new Feeder3WTLegNode(id, name, equipmentId, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        graph.addNode(f3WtLN);
        return f3WtLN;
    }

    public static Feeder3WTLegNode createFeeder3WTLegNodeForSubstationDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side) {
        Feeder3WTLegNode f3WtLN = new Feeder3WTLegNode(id, name, equipmentId, side, graph.getVoltageLevelInfos(), graph.getVoltageLevelInfos());
        graph.addNode(f3WtLN);
        return f3WtLN;
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

    public static Middle2WTNode createMiddle2WTNode(BaseGraph baseGraph, String id, String name, Feeder2WTLegNode legNode1, Feeder2WTLegNode legNode2, VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, boolean hasPhaseTapChanger) {
        String componentType = hasPhaseTapChanger ? PHASE_SHIFT_TRANSFORMER : TWO_WINDINGS_TRANSFORMER;
        Middle2WTNode m2wn = new Middle2WTNode(id, name, vlInfos1, vlInfos2, componentType);
        baseGraph.addTwtEdge(legNode1, m2wn);
        baseGraph.addTwtEdge(legNode2, m2wn);
        baseGraph.addMultiTermNode(m2wn);
        return m2wn;
    }

    public static Middle3WTNode createMiddle3WTNode(VoltageLevelGraph baseGraph, String id, String name, FeederWithSideNode.Side vlSide,
                                                    Feeder3WTLegNode firstOtherLegNode, Feeder3WTLegNode secondOtherLegNode,
                                                    VoltageLevelInfos vlLeg1, VoltageLevelInfos vlLeg2, VoltageLevelInfos vlLeg3) {
        Middle3WTNode m3wn = new Middle3WTNode(id, name, vlLeg1, vlLeg2, vlLeg3, true);
        m3wn.setWindingOrder(Middle3WTNode.Winding.DOWN, vlSide);
        m3wn.setWindingOrder(Middle3WTNode.Winding.UPPER_LEFT, firstOtherLegNode.getSide());
        m3wn.setWindingOrder(Middle3WTNode.Winding.UPPER_RIGHT, secondOtherLegNode.getSide());
        baseGraph.addNode(m3wn);
        baseGraph.addEdge(firstOtherLegNode, m3wn);
        baseGraph.addEdge(secondOtherLegNode, m3wn);
        return m3wn;
    }

    public static Middle3WTNode createMiddle3WTNode(BaseGraph baseGraph, String id, String name, Feeder3WTLegNode legNode1, Feeder3WTLegNode legNode2, Feeder3WTLegNode legNode3) {
        Middle3WTNode m3wn =  new Middle3WTNode(id, name,
                legNode1.getVoltageLevelInfos(), legNode2.getVoltageLevelInfos(), legNode3.getVoltageLevelInfos(),
                false);
        baseGraph.addTwtEdge(legNode1, m3wn);
        baseGraph.addTwtEdge(legNode2, m3wn);
        baseGraph.addTwtEdge(legNode3, m3wn);
        baseGraph.addMultiTermNode(m3wn);
        return m3wn;
    }

}
