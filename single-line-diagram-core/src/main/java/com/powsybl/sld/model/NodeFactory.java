/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.model.FeederWithSideNode.Side;
import com.powsybl.sld.model.SwitchNode.SwitchKind;
import com.powsybl.sld.model.coordinate.Orientation;

import static com.powsybl.sld.library.ComponentTypeName.NODE;
import com.powsybl.sld.library.ComponentTypeName;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class NodeFactory {

    static BusNode createBusNode(VoltageLevelGraph graph, String id, String name) {
        return new BusNode(id, name, false, graph);
    }

    static BusNode createFictitiousBusNode(VoltageLevelGraph graph, String id) {
        return new BusNode(id, null, true, graph);
    }

    static FeederNode createFeederNode(String id, String name, String equipmentId, String componentType, boolean fictitious, VoltageLevelGraph graph, FeederType feederType, Orientation orientation) {
        return new FeederNode(id, name, equipmentId, componentType, fictitious, graph, feederType, orientation);
    }

    static FeederNode createFeederNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph, FeederType feederType) {
        return createFeederNode(id, name, equipmentId, componentType, false, graph, feederType, null);
    }

    static FeederNode createFictitiousFeederNode(VoltageLevelGraph graph, String id, Orientation orientation) {
        return createFeederNode(id, id, id, NODE, true, graph, FeederType.FICTITIOUS, orientation);
    }

    static FeederInjectionNode createFeederInjectionNode(String id, String name, String componentType, VoltageLevelGraph graph) {
        return new FeederInjectionNode(id, name, componentType, graph);
    }

    static FeederInjectionNode createGenerator(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.GENERATOR, graph);
    }

    static FeederInjectionNode createLoad(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.LOAD, graph);
    }

    static FeederInjectionNode createVscConverterStation(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.VSC_CONVERTER_STATION, graph);
    }

    static FeederInjectionNode createStaticVarCompensator(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.STATIC_VAR_COMPENSATOR, graph);
    }

    static FeederInjectionNode createInductor(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.INDUCTOR, graph);
    }

    static FeederInjectionNode createCapacitor(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.CAPACITOR, graph);
    }

    static FeederInjectionNode createDanglingLine(VoltageLevelGraph graph, String id, String name) {
        return createFeederInjectionNode(id, name, ComponentTypeName.DANGLING_LINE, graph);
    }

    static FeederWithSideNode createFeederWithSideNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos, FeederType feederType) {
        return new FeederWithSideNode(id, name, equipmentId, componentType, graph, side, otherSideVoltageLevelInfos, feederType);
    }

    static FeederBranchNode createFeederBranchNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new FeederBranchNode(id, name, equipmentId, componentType, graph, side, otherSideVoltageLevelInfos);
    }

    static Feeder2WTNode createFeeder2WTNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new Feeder2WTNode(id, name, equipmentId, componentType, graph, side, otherSideVoltageLevelInfos);
    }

    protected FeederLineNode createFeederLineNode(String id, String name, String equipmentId, VoltageLevelGraph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new FeederLineNode(id, name, equipmentId, graph, side, otherSideVoltageLevelInfos);
    }

    static FeederTwtLegNode createFeederTwtLegNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos, FeederType feederType) {
        return new FeederTwtLegNode(id, name, equipmentId, componentType, graph, side, otherSideVoltageLevelInfos, feederType);
    }

    static FictitiousNode createFictitiousNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph) {
        return new FictitiousNode(id, name, equipmentId, componentType, graph);
    }

    static BusConnection createBusConnection(String id, VoltageLevelGraph graph) {
        return new BusConnection(id, graph);
    }

    public InternalNode createInternalNode(String id, VoltageLevelGraph graph) {
        return new InternalNode(id, graph);
    }

    public InternalNode createInternalNode(int id, VoltageLevelGraph graph) {
        return createInternalNode(String.valueOf(id), graph);
    }

    static Middle2WTNode createMiddle2WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, String componentType) {
        return new Middle2WTNode(id, name, voltageLevelInfosLeg1, voltageLevelInfosLeg2, componentType);
    }

    static Middle2WTNode createMiddle2WTNode(String id, String name, BaseGraph graph, Feeder2WTLegNode legNode1, Feeder2WTLegNode legNode2, VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, boolean hasPhaseTapChanger) {
        return Middle2WTNode.create(id, name, graph, legNode1, legNode2, vlInfos1, vlInfos2, hasPhaseTapChanger);
    }

    static Middle3WTNode createMiddle3WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, VoltageLevelInfos voltageLevelInfosLeg3, boolean embeddedInVLGraph) {
        return new Middle3WTNode(id, name, voltageLevelInfosLeg1, voltageLevelInfosLeg2, voltageLevelInfosLeg3, embeddedInVLGraph);
    }

    static Middle3WTNode createMiddle3WTNode(String id, String name, BaseGraph ssGraph, Feeder3WTLegNode legNode1, Feeder3WTLegNode legNode2, Feeder3WTLegNode legNode3, VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, VoltageLevelInfos vlInfos3, boolean embeddedInVLGraph) {
        return Middle3WTNode.create(id, name, ssGraph, legNode1, legNode2, legNode3, vlInfos1, vlInfos2, vlInfos3, embeddedInVLGraph);
    }

    static SwitchNode createSwitchNode(String id, String name, String componentType, boolean fictitious, VoltageLevelGraph graph, SwitchKind kind, boolean open) {
        return new SwitchNode(id, name, componentType, fictitious, graph, kind, open);
    }
}
