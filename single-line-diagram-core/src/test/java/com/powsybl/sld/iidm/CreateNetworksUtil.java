/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.BranchStatusAdder;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import static com.powsybl.sld.iidm.AbstractTestCaseIidm.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class CreateNetworksUtil {

    private CreateNetworksUtil() {
    }

    static Network createNodeBreakerNetworkWithBranchStatus(String id, String sourceFormat) {
        Network network = createNodeBreakerNetworkWithInternalBranches(id, sourceFormat);

        createStatusExtensions(network);

        return network;
    }

    static Network createBusBreakerNetworkWithBranchStatus(String id, String sourceFormat) {
        Network network = createBusBreakerNetworkWithInternalBranches(id, sourceFormat);

        createStatusExtensions(network);

        return network;
    }

    private static void createStatusExtensions(Network network) {
        network.getLine("L11").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();
        network.getLine("L12").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.FORCED_OUTAGE).add();

        network.getTwoWindingsTransformer("T11").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();
        network.getTwoWindingsTransformer("T12").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.FORCED_OUTAGE).add();

        network.getThreeWindingsTransformer("T3_12").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.FORCED_OUTAGE).add();
    }

    static Network createNodeBreakerNetworkWithInternalBranches(String id, String sourceFormat) {
        Network network = Network.create(id, sourceFormat);
        Substation substation = createSubstation(network, "S1", "S1", Country.FR);

        VoltageLevel vl1 = createVoltageLevel(substation, "VL1", "VL1", TopologyKind.NODE_BREAKER, 400, 24);

        createBusBarSection(vl1, "BBS11", "BBS11", 10, 1, 1);
        createBusBarSection(vl1, "BBS12", "BBS12", 20, 2, 1);

        createSwitch(vl1, "D10", "D10", SwitchKind.DISCONNECTOR, false, false, false, 10, 120);
        createSwitch(vl1, "BR1", "BR1", SwitchKind.BREAKER, false, false, false, 120, 121);
        createSwitch(vl1, "D20", "D20", SwitchKind.DISCONNECTOR, false, false, false, 121, 20);

        createLoad(vl1, "L1", "L1", "L1", null, ConnectablePosition.Direction.TOP, 12, 1, 1);

        createSwitch(vl1, "D11", "D11", SwitchKind.DISCONNECTOR, false, false, false, 10, 11);
        createSwitch(vl1, "BR12", "BR12", SwitchKind.BREAKER, false, false, false, 11, 12);

        createGenerator(vl1, "G", "G", "G", null, ConnectablePosition.Direction.TOP, 22, 50, 100, false, 100, 400);

        createSwitch(vl1, "D21", "D21", SwitchKind.DISCONNECTOR, false, false, false, 20, 21);
        createSwitch(vl1, "BR22", "BR22", SwitchKind.BREAKER, false, false, false, 21, 22);

        createSwitch(vl1, "D13", "D13", SwitchKind.DISCONNECTOR, false, false, false, 10, 13);
        createSwitch(vl1, "BR14", "BR14", SwitchKind.BREAKER, false, false, false, 13, 14);

        createSwitch(vl1, "D23", "D23", SwitchKind.DISCONNECTOR, false, false, false, 20, 23);
        createSwitch(vl1, "BR24", "BR24", SwitchKind.BREAKER, false, false, false, 23, 24);

        createLine(network, "L11", "L11", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                14, 24, vl1.getId(), vl1.getId(),
                "L11", null, ConnectablePosition.Direction.TOP,
                "L11", null, ConnectablePosition.Direction.TOP);

        VoltageLevel vl2 = createVoltageLevel(substation, "VL2", "VL2", TopologyKind.NODE_BREAKER, 400, 9);

        createBusBarSection(vl2, "BBS2", "BBS2", 30, 1, 1);

        createLoad(vl2, "L2", "L2", "L2", null, ConnectablePosition.Direction.TOP, 32, 1, 1);

        createSwitch(vl2, "D31", "D31", SwitchKind.DISCONNECTOR, false, false, false, 30, 31);
        createSwitch(vl2, "BR32", "BR32", SwitchKind.BREAKER, false, false, false, 31, 32);

        createSwitch(vl1, "D15", "D15", SwitchKind.DISCONNECTOR, false, false, false, 10, 15);
        createSwitch(vl1, "BR16", "BR16", SwitchKind.BREAKER, false, false, false, 15, 16);

        createSwitch(vl2, "D33", "D33", SwitchKind.DISCONNECTOR, false, false, false, 30, 33);
        createSwitch(vl2, "BR34", "BR34", SwitchKind.BREAKER, false, false, false, 33, 34);

        createLine(network, "L12", "L12", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                16, 34, vl1.getId(), vl2.getId(),
                "L12", null, ConnectablePosition.Direction.TOP,
                "L12", null, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "D17", "D17", SwitchKind.DISCONNECTOR, false, false, false, 10, 17);
        createSwitch(vl1, "BR18", "BR18", SwitchKind.BREAKER, false, false, false, 17, 18);

        createSwitch(vl1, "D25", "D25", SwitchKind.DISCONNECTOR, false, false, false, 20, 25);
        createSwitch(vl1, "BR26", "BR26", SwitchKind.BREAKER, false, false, false, 25, 26);

        createTwoWindingsTransformer(substation, "T11", "T11", 250, 100, 52, 12, 65, 90,
                18, 26, vl1.getId(), vl1.getId(),
                "T11", null, ConnectablePosition.Direction.TOP,
                "T11", null, ConnectablePosition.Direction.TOP);

        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer("T11");
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setQ(48);

        createSwitch(vl1, "D27", "D27", SwitchKind.DISCONNECTOR, false, false, false, 20, 27);
        createSwitch(vl1, "BR28", "BR28", SwitchKind.BREAKER, false, false, false, 27, 28);

        createSwitch(vl2, "D35", "D35", SwitchKind.DISCONNECTOR, false, false, false, 30, 35);
        createSwitch(vl2, "BR36", "BR36", SwitchKind.BREAKER, false, false, false, 35, 36);

        createTwoWindingsTransformer(substation, "T12", "T12", 250, 100, 52, 12, 65, 90,
                28, 36, vl1.getId(), vl2.getId(),
                "T12", null, ConnectablePosition.Direction.TOP,
                "T12", null, ConnectablePosition.Direction.TOP);

        twoWindingsTransformer = network.getTwoWindingsTransformer("T12");
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setQ(48);

        createSwitch(vl1, "D19", "D19", SwitchKind.DISCONNECTOR, false, false, false, 10, 19);
        createSwitch(vl1, "BR20", "BR20", SwitchKind.BREAKER, false, false, false, 19, 190);

        createSwitch(vl1, "D29", "D29", SwitchKind.DISCONNECTOR, false, false, false, 20, 29);
        createSwitch(vl1, "BR30", "BR30", SwitchKind.BREAKER, false, false, false, 29, 290);

        createSwitch(vl2, "D37", "D37", SwitchKind.DISCONNECTOR, false, false, false, 30, 37);
        createSwitch(vl2, "BR38", "BR38", SwitchKind.BREAKER, false, false, false, 37, 38);

        createThreeWindingsTransformer(substation, "T3_12", "T3_12", vl1.getId(), vl1.getId(), vl2.getId(),
                45, 47, 49, 35, 32, 39., 25, 15,
                5, 7, 9,
                190, 290, 38,
                "T3_12", null, ConnectablePosition.Direction.TOP,
                "T3_12", null, ConnectablePosition.Direction.TOP,
                "T3_12", null, ConnectablePosition.Direction.TOP);

        return network;
    }

    static Network createBusBreakerNetworkWithInternalBranches(String id, String sourceFormat) {
        Network network = Network.create(id, sourceFormat);
        Substation substation = createSubstation(network, "S1", "S1", Country.FR);

        VoltageLevel vl1 = createVoltageLevel(substation, "VL1", "VL1", TopologyKind.BUS_BREAKER, 400, -1);

        vl1.getBusBreakerView().newBus()
                .setId("B11")
                .add();
        vl1.newLoad()
                .setId("LD1")
                .setConnectableBus("B11")
                .setBus("B11")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        network.getVoltageLevel("VL1").getBusBreakerView().newBus()
                .setId("B12")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR1")
                .setBus1("B12")
                .setBus2("B11")
                .setOpen(true)
                .add();

        vl1.newGenerator()
                .setId("G")
                .setBus("B12")
                .setConnectableBus("B12")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        network.newLine()
                .setId("L11")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B11")
                .setBus1("B11")
                .setVoltageLevel2("VL1")
                .setConnectableBus2("B12")
                .setBus2("B12")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        TwoWindingsTransformer twt = substation.newTwoWindingsTransformer()
                .setId("T11")
                .setVoltageLevel1("VL1")
                .setBus1("B11")
                .setConnectableBus1("B11")
                .setVoltageLevel2("VL1")
                .setBus2("B12")
                .setConnectableBus2("B12")
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();
        twt.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(twt.getTerminal2())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setRegulationValue(400)
                .beginStep()
                .setAlpha(-20.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(0.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(20.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B21")
                .add();
        vl2.newLoad()
                .setId("LD2")
                .setConnectableBus("B21")
                .setBus("B21")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        network.newLine()
                .setId("L12")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B11")
                .setBus1("B11")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B21")
                .setBus2("B21")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        substation.newTwoWindingsTransformer()
                .setId("T12")
                .setVoltageLevel1("VL1")
                .setBus1("B12")
                .setConnectableBus1("B12")
                .setVoltageLevel2("VL2")
                .setBus2("B21")
                .setConnectableBus2("B21")
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();

        ThreeWindingsTransformer threeWindingsTransformer = substation.newThreeWindingsTransformer()
                .setId("T3_12")
                .setRatedU0(234)
                .newLeg1()
                .setVoltageLevel("VL1")
                .setBus("B11")
                .setConnectableBus("B11")
                .setR(45)
                .setX(35)
                .setG(25)
                .setB(15)
                .setRatedU(5)
                .add()
                .newLeg2()
                .setVoltageLevel("VL1")
                .setBus("B12")
                .setConnectableBus("B12")
                .setR(47)
                .setX(37)
                .setG(27)
                .setB(17)
                .setRatedU(7)
                .add()
                .newLeg3()
                .setVoltageLevel("VL2")
                .setBus("B21")
                .setConnectableBus("B21")
                .setR(49)
                .setX(39)
                .setG(29)
                .setB(19)
                .setRatedU(9)
                .add()
                .add();

        return network;
    }

    public static Network createNetworkWithSvcVscScDl() {
        Network network = Network.create("testCase1", "test");
        Substation substation = network.newSubstation().setId("s").setCountry(Country.FR).add();
        VoltageLevel vl = substation.newVoltageLevel().setId("vl").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(380).add();
        VoltageLevel.NodeBreakerView view = vl.getNodeBreakerView();
        BusbarSection bbs = view.newBusbarSection().setId("bbs").setNode(0).add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1);
        BusbarSection bbs2 = view.newBusbarSection().setId("bbs2").setNode(3).add();
        bbs2.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(2);
        StaticVarCompensator svc = vl.newStaticVarCompensator()
                .setId("svc")
                .setName("svc")
                .setNode(2)
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390)
                .add();
        svc.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        VscConverterStation vsc = vl.newVscConverterStation()
                .setId("vsc")
                .setName("Converter1")
                .setNode(1)
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        vsc.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        ShuntCompensator c1 = vl.newShuntCompensator()
                .setId("C1")
                .setName("Filter 1")
                .setNode(4)
                .setSectionCount(1)
                .newLinearModel()
                .setBPerSection(1e-5)
                .setMaximumSectionCount(1)
                .add()
                .add();
        DanglingLine dl1 = vl.newDanglingLine()
                .setId("dl1")
                .setName("Dangling line 1")
                .setNode(5)
                .setP0(1)
                .setQ0(1)
                .setR(0)
                .setX(0)
                .setB(0)
                .setG(0)
                .add();
        dl1.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        view.newDisconnector().setId("d").setNode1(0).setNode2(1).add();
        view.newBreaker().setId("b").setNode1(1).setNode2(2).add();
        view.newBreaker().setId("b2").setNode1(3).setNode2(4).add();
        view.newBreaker().setId("b3").setNode1(3).setNode2(5).add();
        view.newBreaker().setId("bt").setNode1(0).setNode2(3).add();

        VoltageLevel vl2 = substation.newVoltageLevel().setId("vl2").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(380).add();
        VoltageLevel.NodeBreakerView view2 = vl2.getNodeBreakerView();
        BusbarSection bbs1Vl2 = view2.newBusbarSection().setId("bbs1Vl2").setNode(0).add();
        bbs1Vl2.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1);
        BusbarSection bbs2Vl2 = view2.newBusbarSection().setId("bbs2Vl2").setNode(3).add();
        bbs2Vl2.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(2);

        VscConverterStation vsc2 = vl2.newVscConverterStation()
                .setId("vsc2")
                .setName("Converter2")
                .setNode(1)
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        vsc2.getTerminal()
                .setP(300.0)
                .setQ(75.0);
        view2.newDisconnector().setId("d2").setNode1(0).setNode2(1).add();

        network.newHvdcLine()
            .setId("hvdc")
            .setName("hvdc")
            .setR(1)
            .setNominalV(380)
            .setMaxP(200)
            .setActivePowerSetpoint(150)
            .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
            .setConverterStationId1("vsc")
            .setConverterStationId2("vsc2")
            .add();

        return network;
    }

    public static Network createNetworkWithFiveBusesFourLoads() {
        Network network = createNetworkWithTwoParallelLoads();
        VoltageLevel vl = network.getVoltageLevel("vl1");

        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        createBusBarSection(vl, "bbs21", "bbs21", 1, 2, 1);
        createBusBarSection(vl, "bbs22", "bbs22", 2, 2, 2);
        createSwitch(vl, "bA", "bA", SwitchKind.BREAKER, false, false, false, 3, 4);
        createLoad(vl, "loadA", "loadA", "loadA", null, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createSwitch(vl, "dA1", "dA1", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        createSwitch(vl, "dA2", "dA2", SwitchKind.DISCONNECTOR, false, false, false, 1, 3);

        createSwitch(vl, "bB", "bB", SwitchKind.BREAKER, false, false, false, 5, 6);
        createLoad(vl, "loadB", "loadB", "loadB", null, ConnectablePosition.Direction.TOP, 6, 10, 10);
        createSwitch(vl, "dB1", "dB1", SwitchKind.DISCONNECTOR, false, false, false, 2, 5);
        createSwitch(vl, "dB2", "dB2", SwitchKind.DISCONNECTOR, false, false, false, 0, 5);

        createSwitch(vl, "link", "link", SwitchKind.BREAKER, false, false, false, 5, 9);

        return network;
    }

    public static Network createNetworkWithTwoParallelLoads() {
        Network network = Network.create("TestSingleLineDiagramClass", "test");
        Substation substation = createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs13", "bbs13", 7, 1, 3);
        createBusBarSection(vl, "bbs23", "bbs23", 8, 2, 3);
        createLoad(vl, "loadC", "loadC", "loadC", null, ConnectablePosition.Direction.TOP, 9, 10, 10);
        createSwitch(vl, "bCD1", "bCD1", SwitchKind.BREAKER, false, false, false, 8, 9);
        createSwitch(vl, "bCD2", "bCD2", SwitchKind.BREAKER, false, false, false, 7, 9);
        createSwitch(vl, "bCD3", "bCD3", SwitchKind.BREAKER, false, false, false, 7, 9);
        createSwitch(vl, "bD1", "bD1", SwitchKind.BREAKER, false, false, false, 20, 9);
        createLoad(vl, "loadD", "loadD", "loadD", null, ConnectablePosition.Direction.TOP, 20, 10, 10);
        return network;
    }

    public static Network createNetworkWithFlatSections() {
        Network network = Network.create("TestSingleLineDiagramClass", "test");
        Substation substation = createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs11", "bbs11", 0, 1, 1);
        createBusBarSection(vl, "bbs21", "bbs21", 1, 2, 1);
        createBusBarSection(vl, "bbs12", "bbs12", 2, 1, 2);
        createBusBarSection(vl, "bbs22", "bbs22", 3, 2, 2);
        createBusBarSection(vl, "bbs13", "bbs13", 4, 1, 3);
        createBusBarSection(vl, "bbs23", "bbs23", 5, 2, 3);
        createSwitch(vl, "d112", "d112", SwitchKind.DISCONNECTOR, false, false, false, 0, 2);
        createSwitch(vl, "d212", "d212", SwitchKind.DISCONNECTOR, false, false, false, 1, 3);
        createSwitch(vl, "d123a", "d123a", SwitchKind.DISCONNECTOR, false, false, false, 2, 6);
        createSwitch(vl, "b123", "b123", SwitchKind.BREAKER, false, false, false, 6, 7);
        createSwitch(vl, "d123b", "d123b", SwitchKind.DISCONNECTOR, false, false, false, 7, 4);
        createSwitch(vl, "d223a", "d223a", SwitchKind.DISCONNECTOR, false, false, false, 3, 8);
        createSwitch(vl, "b223", "b223", SwitchKind.BREAKER, false, false, false, 8, 9);
        createSwitch(vl, "d223b", "d223b", SwitchKind.DISCONNECTOR, false, false, false, 9, 5);
        return network;
    }
}
