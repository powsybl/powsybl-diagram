/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.components.ComponentTypeName;
import com.powsybl.sld.library.SldComponentTypeName;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.EquipmentNode;
import com.powsybl.sld.model.nodes.Feeder;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.GroundDisconnectionNode;
import com.powsybl.sld.model.nodes.Internal2WTNode;
import com.powsybl.sld.model.nodes.Middle2WTNode;
import com.powsybl.sld.model.nodes.Middle3WTNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.Node.NodeType;
import com.powsybl.sld.model.nodes.NodeSide;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.model.nodes.SwitchNode.SwitchKind;
import com.powsybl.sld.model.nodes.feeders.BaseFeeder;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import java.util.Objects;

import static com.powsybl.sld.library.SldComponentTypeName.*;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */

public final class NodeFactory {

    private static final String BUS_CONNECTION_ID_PREFIX = "BUSCO_";

    private static final String CONNECTIVITY_ID_PREFIX = "INTERNAL_";

    private NodeFactory() {
    }

    public static EquipmentNode createEquipmentNode(VoltageLevelGraph graph, NodeType type, String id, String name, String equipmentId, String componentType, boolean fictitious) {
        EquipmentNode node = new EquipmentNode(type, id, name, equipmentId, componentType, fictitious);
        graph.addNode(node);
        return node;
    }

    public static ConnectivityNode createConnectivityNode(VoltageLevelGraph graph, String id, String componentType) {
        ConnectivityNode node = new ConnectivityNode(id, componentType);
        graph.addNode(node);
        return node;
    }

    public static Node createBusConnection(VoltageLevelGraph graph, String idNodeConnectedToBusNode) {
        String idBusConnection = BUS_CONNECTION_ID_PREFIX + Objects.requireNonNull(idNodeConnectedToBusNode);
        return createConnectivityNode(graph, idBusConnection, BUS_CONNECTION);
    }

    public static BusNode createBusNode(VoltageLevelGraph graph, String id, String name) {
        BusNode bn = new BusNode(id, name, false);
        graph.addNode(bn);
        return bn;
    }

    public static BusNode createFictitiousBusNode(VoltageLevelGraph graph, String id, int busbarIndex, int sectionIndex) {
        BusNode bn = new BusNode(id, null, true);
        bn.setBusBarIndexSectionIndex(busbarIndex, sectionIndex);
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

    public static FeederNode createBattery(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.BATTERY);
    }

    public static FeederNode createLoad(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, ComponentTypeName.LOAD);
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
        return createFeederInjectionNode(graph, id, name, SldComponentTypeName.DANGLING_LINE);
    }

    public static FeederNode createGround(VoltageLevelGraph graph, String id, String name) {
        return createFeederNode(graph, id, name, id, SldComponentTypeName.GROUND, false, new BaseFeeder(FeederType.GROUND), null);
    }

    public static Node createGroundDisconnectionNode(VoltageLevelGraph graph, SwitchNode disconnector, FeederNode ground) {
        String name = "Ground disconnection (ground " + ground.getId() + ", disconnector " + disconnector.getId() + ")";
        GroundDisconnectionNode gdNode = new GroundDisconnectionNode(disconnector.getEquipmentId(), name, disconnector.isOpen(), SldComponentTypeName.GROUND_DISCONNECTION);
        graph.addNode(gdNode);
        return gdNode;
    }

    public static FeederNode createVscConverterStation(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        FeederWithSides feeder = new FeederWithSides(FeederType.HVDC, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        return createFeederNode(graph, id, name, equipmentId, SldComponentTypeName.VSC_CONVERTER_STATION, feeder);
    }

    public static FeederNode createVscConverterStationInjection(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, VSC_CONVERTER_STATION);
    }

    public static FeederNode createLccConverterStation(VoltageLevelGraph graph, String id, String name, String equipmentId, NodeSide side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        FeederWithSides feeder = new FeederWithSides(FeederType.HVDC, side, graph.getVoltageLevelInfos(), otherSideVoltageLevelInfos);
        return createFeederNode(graph, id, name, equipmentId, SldComponentTypeName.LCC_CONVERTER_STATION, feeder);
    }

    public static FeederNode createLccConverterStationInjection(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(graph, id, name, LCC_CONVERTER_STATION);
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
        return createFeederBranchNode(graph, id, name, equipmentId, SldComponentTypeName.LINE, side, otherSideVoltageLevelInfos);
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

    public static ConnectivityNode createConnectivityNode(VoltageLevelGraph graph, String id) {
        // for uniqueness purpose (in substation diagram), we prefix the id of the connectivity nodes with the voltageLevel id and "_"
        String connectivityNodeId = CONNECTIVITY_ID_PREFIX + graph.getVoltageLevelInfos().id() + "_" + Objects.requireNonNull(id);
        return createConnectivityNode(graph, connectivityNodeId, NODE);
    }

    public static SwitchNode createSwitchNode(VoltageLevelGraph graph, String id, String name, String componentType, boolean fictitious, SwitchKind kind, boolean open) {
        SwitchNode sn = new SwitchNode(id, name, componentType, fictitious, kind, open);
        graph.addNode(sn);
        return sn;
    }

    public static EquipmentNode createInternal2WTNode(VoltageLevelGraph graph, String id, String nameOrId, String equipmentId, Node n1, Node n2, String component) {
        EquipmentNode i2wt = new Internal2WTNode(id, nameOrId, equipmentId, component);
        graph.addNode(i2wt);
        graph.addEdge(n1, i2wt);
        graph.addEdge(n2, i2wt);
        return i2wt;
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
                                                    VoltageLevelInfos vlLeg1, VoltageLevelInfos vlLeg2, VoltageLevelInfos vlLeg3, boolean hasPhaseTapChanger1, boolean hasPhaseTapChanger2, boolean hasPhaseTapChanger3) {
        if (firstOtherLegNode.getFeeder().getFeederType() != FeederType.THREE_WINDINGS_TRANSFORMER_LEG
                || secondOtherLegNode.getFeeder().getFeederType() != FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
            throw new PowsyblException("Middle3WTNode must be created with FeederNode with ComponentTypeName THREE_WINDINGS_TRANSFORMER_LEG");
        }
        String componentType = getComponentType(hasPhaseTapChanger1, hasPhaseTapChanger2, hasPhaseTapChanger3);
        Middle3WTNode m3wn = new Middle3WTNode(id, name, vlLeg1, vlLeg2, vlLeg3, componentType, true);
        m3wn.setWindingOrder(Middle3WTNode.Winding.DOWN, vlSide);
        m3wn.setWindingOrder(Middle3WTNode.Winding.UPPER_LEFT, ((FeederTwLeg) firstOtherLegNode.getFeeder()).getSide());
        m3wn.setWindingOrder(Middle3WTNode.Winding.UPPER_RIGHT, ((FeederTwLeg) secondOtherLegNode.getFeeder()).getSide());
        baseGraph.addNode(m3wn);
        baseGraph.addEdge(firstOtherLegNode, m3wn);
        baseGraph.addEdge(secondOtherLegNode, m3wn);
        return m3wn;
    }

    private static String getComponentType(boolean hasPhaseTapChanger1, boolean hasPhaseTapChanger2, boolean hasPhaseTapChanger3) {
        if (hasPhaseTapChanger1 && hasPhaseTapChanger2 && hasPhaseTapChanger3) {
            return THREE_WINDINGS_TRANSFORMER_PST_1_2_3;
        } else if (hasPhaseTapChanger1 && hasPhaseTapChanger2) {
            return THREE_WINDINGS_TRANSFORMER_PST_1_2;
        } else if (hasPhaseTapChanger1 && hasPhaseTapChanger3) {
            return THREE_WINDINGS_TRANSFORMER_PST_1_3;
        } else if (hasPhaseTapChanger2 && hasPhaseTapChanger3) {
            return THREE_WINDINGS_TRANSFORMER_PST_2_3;
        } else if (hasPhaseTapChanger1) {
            return THREE_WINDINGS_TRANSFORMER_PST_1;
        } else if (hasPhaseTapChanger2) {
            return THREE_WINDINGS_TRANSFORMER_PST_2;
        } else if (hasPhaseTapChanger3) {
            return THREE_WINDINGS_TRANSFORMER_PST_3;
        }
        return THREE_WINDINGS_TRANSFORMER;
    }

    public static Middle3WTNode createMiddle3WTNode(BaseGraph baseGraph, String id, String name, FeederNode legNode1, FeederNode legNode2, FeederNode legNode3) {
        if (legNode1.getFeeder().getFeederType() != FeederType.THREE_WINDINGS_TRANSFORMER_LEG
                || legNode2.getFeeder().getFeederType() != FeederType.THREE_WINDINGS_TRANSFORMER_LEG
                || legNode3.getFeeder().getFeederType() != FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
            throw new PowsyblException("Middle3WTNode must be created with FeederNode with ComponentTypeName THREE_WINDINGS_TRANSFORMER_LEG");
        }
        Middle3WTNode m3wn = new Middle3WTNode(id, name,
                ((FeederTwLeg) legNode1.getFeeder()).getVoltageLevelInfos(),
                ((FeederTwLeg) legNode2.getFeeder()).getVoltageLevelInfos(),
                ((FeederTwLeg) legNode3.getFeeder()).getVoltageLevelInfos(),
                THREE_WINDINGS_TRANSFORMER,
                false);
        baseGraph.addTwtEdge(legNode1, m3wn);
        baseGraph.addTwtEdge(legNode2, m3wn);
        baseGraph.addTwtEdge(legNode3, m3wn);
        baseGraph.addMultiTermNode(m3wn);
        return m3wn;
    }
}
