/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class Networks {

    private static final String DIAGRAM_NAME = "default";

    private Networks() {
    }

    protected static Network createBusTopologyNetwork() {
        var network = Network.create("test", "test");
        var substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        var voltageLevel = createBusTopologyFirstVoltageLevel(substation);
        var voltageLevel2 = createBusTopologySecondVoltageLevel(substation);
        createTransformer(substation);
        addBusTopologyFirstVoltageLevelDiagramData(network, voltageLevel);
        addBusTopologySecondVoltageLevelDiagramData(network, voltageLevel2);
        addBusTopologyTransformerDiagramData(network);
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation");
        return network;
    }

    protected static Network createBusTopologyNetworkWith3WT() {
        var networkWith3WT = Network.create("test", "test");
        Substation substation2 = networkWith3WT.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel21 = createBusTopologyFirstVoltageLevel(substation2);
        VoltageLevel voltageLevel22 = createBusTopologySecondVoltageLevel(substation2);
        VoltageLevel voltageLevel23 = createBusTopologyThirdVoltageLevel(substation2);
        create3WTransformer(substation2);
        addBusTopologyFirstVoltageLevelDiagramData(networkWith3WT, voltageLevel21);
        addBusTopologySecondVoltageLevelDiagramData(networkWith3WT, voltageLevel22);
        addBusTopologyThirdVoltageLevelDiagramData(networkWith3WT, voltageLevel23);
        addBusTopology3WTransformerDiagramData(networkWith3WT);
        NetworkDiagramData.addDiagramName(networkWith3WT, DIAGRAM_NAME, "Substation");
        return networkWith3WT;
    }

    protected static Network createNodeTopologyNetwork() {
        Network network = Network.create("testCase1", "test");
        var voltageLevel = createNodeTopologyFirstVoltageLevel(network, 0, 4, 0, 1, 1, 2, 2, 3);
        voltageLevel.getNodeBreakerView().newInternalConnection()
                .setNode1(4)
                .setNode2(0)
                .add();
        createNodeTopologySecondVoltageLevel(network);
        createLine(network, 3);
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation");
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation2");
        return network;
    }

    protected static Network createNodeTopologyNetworkWithInternalConnections() {
        Network network = Network.create("testCase1", "test");
        var voltageLevel = createNodeTopologyFirstVoltageLevel(network, 4, 12, 7, 8, 5, 6, 9, 10);
        VoltageLevel.NodeBreakerView nbv = voltageLevel.getNodeBreakerView();
        nbv.newInternalConnection().setNode1(4).setNode2(0).add();
        nbv.newInternalConnection().setNode1(5).setNode2(2).add();
        nbv.newInternalConnection().setNode1(6).setNode2(1).add();
        nbv.newInternalConnection().setNode1(7).setNode2(1).add();
        nbv.newInternalConnection().setNode1(8).setNode2(0).add();
        nbv.newInternalConnection().setNode1(9).setNode2(3).add();
        nbv.newInternalConnection().setNode1(10).setNode2(2).add();
        nbv.newInternalConnection().setNode1(11).setNode2(3).add();
        nbv.newInternalConnection().setNode1(12).setNode2(0).add();
        createNodeTopologySecondVoltageLevel(network);
        createLine(network, 11);
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation");
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation2");
        return network;
    }

    private static VoltageLevel createBusTopologyFirstVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        voltageLevel1.newLoad()
                .setId("Load")
                .setBus("Bus1")
                .setConnectableBus("Bus1")
                .setP0(100)
                .setQ0(50)
                .add();
        voltageLevel1.newShuntCompensator()
                .setId("Shunt")
                .setBus("Bus1")
                .setConnectableBus("Bus1")
                .setSectionCount(1)
                .newLinearModel()
                .setBPerSection(1e-5)
                .setMaximumSectionCount(1)
                .add()
                .add();
        voltageLevel1.newDanglingLine()
                .setId("DanglingLine")
                .setBus("Bus1")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
        return voltageLevel1;
    }

    private static VoltageLevel createBusTopologySecondVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        voltageLevel2.newStaticVarCompensator()
                .setId("Svc")
                .setConnectableBus("Bus2")
                .setBus("Bus2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setRegulating(true)
                .setVoltageSetpoint(390.0)
                .setReactivePowerSetpoint(1.0)
                .add();
        return voltageLevel2;
    }

    private static VoltageLevel createBusTopologyThirdVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel3 = substation.newVoltageLevel()
                .setId("VoltageLevel3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel3.getBusBreakerView().newBus()
                .setId("Bus3")
                .add();
        voltageLevel3.newGenerator()
                .setId("Generator")
                .setBus("Bus3")
                .setConnectableBus("Bus3")
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        return voltageLevel3;
    }

    private static void createLine(Network network, int lineNode) {
        network.newLine()
                .setId("Line")
                .setVoltageLevel1("VoltageLevel1")
                .setNode1(lineNode)
                .setVoltageLevel2("VoltageLevel2")
                .setNode2(0)
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
    }

    private static void createTransformer(Substation substation) {
        int zb380 = 380 * 380 / 100;
        substation.newTwoWindingsTransformer()
                .setId("Transformer")
                .setVoltageLevel1("VoltageLevel1")
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setRatedU1(24.0)
                .setVoltageLevel2("VoltageLevel2")
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
    }

    private static void create3WTransformer(Substation substation) {
        substation.newThreeWindingsTransformer()
                .setId("Transformer")
                .newLeg1()
                .setR(17.424)
                .setX(1.7424)
                .setB(0.000573921028466483)
                .setG(0.00573921028466483)
                .setRatedU(132.0)
                .setVoltageLevel("VoltageLevel1")
                .setBus("Bus1")
                .add()
                .newLeg2()
                .setR(1.089)
                .setX(0.1089)
                .setRatedU(33.0)
                .setVoltageLevel("VoltageLevel2")
                .setBus("Bus2")
                .add()
                .newLeg3()
                .setR(0.121)
                .setX(0.0121)
                .setRatedU(11.0)
                .setVoltageLevel("VoltageLevel3")
                .setBus("Bus3")
                .add()
                .add();
    }

    private static void addBusTopologyFirstVoltageLevelDiagramData(Network network, VoltageLevel voltageLevel) {
        Bus bus = voltageLevel.getBusBreakerView().getBus("Bus1");
        NodeDiagramData<Bus> busDiagramData = new NodeDiagramData<>(bus);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails.setPoint1(new DiagramPoint(60, 10, 1));
        diagramDetails.setPoint2(new DiagramPoint(60, 70, 2));
        busDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        bus.addExtension(NodeDiagramData.class, busDiagramData);

        Load load = network.getLoad("Load");
        InjectionDiagramData<Load> loadDiagramData = new InjectionDiagramData<>(load);
        InjectionDiagramData.InjectionDiagramDetails loadsDiagramDetails = new InjectionDiagramData.InjectionDiagramDetails(new DiagramPoint(10, 20, 0), 90);
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(15, 20, 2));
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(60, 20, 1));
        loadDiagramData.addData(DIAGRAM_NAME, loadsDiagramDetails);
        load.addExtension(InjectionDiagramData.class, loadDiagramData);

        ShuntCompensator shunt = network.getShuntCompensator("Shunt");
        InjectionDiagramData<ShuntCompensator> shuntDiagramData = new InjectionDiagramData<>(shunt);
        InjectionDiagramData.InjectionDiagramDetails shuntDiagramDetails = new InjectionDiagramData.InjectionDiagramDetails(new DiagramPoint(15, 55, 0), 90);
        shuntDiagramDetails.addTerminalPoint(new DiagramPoint(20, 55, 1));
        shuntDiagramDetails.addTerminalPoint(new DiagramPoint(60, 55, 2));
        shuntDiagramData.addData(DIAGRAM_NAME, shuntDiagramDetails);
        shunt.addExtension(InjectionDiagramData.class, shuntDiagramData);

        DanglingLine danglingLine = network.getDanglingLine("DanglingLine");
        LineDiagramData<DanglingLine> danglingLineDiagramData = new LineDiagramData<>(danglingLine);
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(60, 60, 1));
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(120, 60, 2));
        danglingLine.addExtension(LineDiagramData.class, danglingLineDiagramData);
    }

    private static void addBusTopologySecondVoltageLevelDiagramData(Network network, VoltageLevel voltageLevel) {
        Bus bus2 = voltageLevel.getBusBreakerView().getBus("Bus2");
        NodeDiagramData<Bus> bus2DiagramData = new NodeDiagramData<>(bus2);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails2 = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails2.setPoint1(new DiagramPoint(120, 10, 1));
        diagramDetails2.setPoint2(new DiagramPoint(120, 25, 2));
        bus2DiagramData.addData(DIAGRAM_NAME, diagramDetails2);
        bus2.addExtension(NodeDiagramData.class, bus2DiagramData);

        StaticVarCompensator svc = network.getStaticVarCompensator("Svc");
        InjectionDiagramData<StaticVarCompensator> svcDiagramData = new InjectionDiagramData<>(svc);
        InjectionDiagramData.InjectionDiagramDetails svcDiagramDataDetails = new InjectionDiagramData.InjectionDiagramDetails(new DiagramPoint(140, 15, 0), 270);
        svcDiagramDataDetails.addTerminalPoint(new DiagramPoint(135, 15, 1));
        svcDiagramDataDetails.addTerminalPoint(new DiagramPoint(120, 15, 2));
        svcDiagramData.addData(DIAGRAM_NAME, svcDiagramDataDetails);
        svc.addExtension(InjectionDiagramData.class, svcDiagramData);
    }

    private static void addBusTopologyTransformerDiagramData(Network network) {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("Transformer");
        CouplingDeviceDiagramData<TwoWindingsTransformer> twtDiagramData = new CouplingDeviceDiagramData<>(twt);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails twtDiagramDetails = new CouplingDeviceDiagramData.CouplingDeviceDiagramDetails(new DiagramPoint(100, 15, 0), 90);
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(95, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(60, 15, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(105, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(120, 15, 2));
        twtDiagramData.addData(DIAGRAM_NAME, twtDiagramDetails);
        twt.addExtension(CouplingDeviceDiagramData.class, twtDiagramData);
    }

    private static void addBusTopologyThirdVoltageLevelDiagramData(Network network, VoltageLevel voltageLevel) {
        Bus bus3 = voltageLevel.getBusBreakerView().getBus("Bus3");
        NodeDiagramData<Bus> bus3DiagramData = new NodeDiagramData<>(bus3);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails2 = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails2.setPoint1(new DiagramPoint(80, 40, 1));
        diagramDetails2.setPoint2(new DiagramPoint(120, 40, 2));
        bus3DiagramData.addData(DIAGRAM_NAME, diagramDetails2);
        bus3.addExtension(NodeDiagramData.class, bus3DiagramData);

        Generator generator = network.getGenerator("Generator");
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails genDiagramDataDetails = new InjectionDiagramData.InjectionDiagramDetails(new DiagramPoint(100, 60, 0), 90);
        genDiagramDataDetails.addTerminalPoint(new DiagramPoint(100, 55, 1));
        genDiagramDataDetails.addTerminalPoint(new DiagramPoint(100, 40, 2));
        generatorDiagramData.addData(DIAGRAM_NAME, genDiagramDataDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
    }

    private static void addBusTopology3WTransformerDiagramData(Network network) {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("Transformer");
        ThreeWindingsTransformerDiagramData twtDiagramData = new ThreeWindingsTransformerDiagramData(twt);
        ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails twtDiagramDetails = twtDiagramData.new ThreeWindingsTransformerDiagramDataDetails(new DiagramPoint(100, 15, 0), 90);
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(95, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(60, 15, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(105, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(120, 15, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL3, new DiagramPoint(100, 20, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL3, new DiagramPoint(10, 40, 2));
        twtDiagramData.addData(DIAGRAM_NAME, twtDiagramDetails);
        twt.addExtension(ThreeWindingsTransformerDiagramData.class, twtDiagramData);
    }

    private static VoltageLevel createNodeTopologyFirstVoltageLevel(Network network, int busbarNode, int generatorNode, int disconnector1Node1, int disconnector1Node2,
                                                               int breaker1Node1, int breaker1Node2, int disconnector2Node1, int disconnector2Node2) {
        Substation substation1 = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();
        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection")
                .setNode(busbarNode)
                .add();
        voltageLevel1.newGenerator()
                .setId("Generator")
                .setNode(generatorNode)
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector1")
                .setNode1(disconnector1Node1)
                .setNode2(disconnector1Node2)
                .add();
        voltageLevel1.getNodeBreakerView().newBreaker()
                .setId("Breaker1")
                .setNode1(breaker1Node1)
                .setNode2(breaker1Node2)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector2")
                .setNode1(disconnector2Node1)
                .setNode2(disconnector2Node2)
                .add();
        return voltageLevel1;
    }

    private static void createNodeTopologySecondVoltageLevel(Network network) {
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        substation2.newVoltageLevel()
                .setId("VoltageLevel2")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();
    }

    protected static void addNodeTopologyHorizontalBusbarDiagramData(Network network) {
        addNodeTopologyBusbarSectionDiagramData(network.getBusbarSection("BusbarSection"), new DiagramPoint(20, 115, 1), new DiagramPoint(180, 115, 2));
        addNodeTopologyGeneratorDiagramData(network.getGenerator("Generator"), new DiagramPoint(105, 230, 0),
                new DiagramPoint(105, 225, 1), new DiagramPoint(105, 115, 2));
        addNodeTopologySwitchDiagramData(network.getSwitch("Disconnector1"), new DiagramPoint(105, 100, 0), 0, new DiagramPoint(105, 95, 1),
                new DiagramPoint(105, 90, 2), new DiagramPoint(105, 105, 1), new DiagramPoint(105, 115, 2));
        addNodeTopologySwitchDiagramData(network.getSwitch("Breaker1"), new DiagramPoint(105, 80, 0), 0, new DiagramPoint(105, 85, 1),
                new DiagramPoint(105, 90, 2), new DiagramPoint(105, 75, 1), new DiagramPoint(105, 70, 2));
        addNodeTopologySwitchDiagramData(network.getSwitch("Disconnector2"), new DiagramPoint(105, 60, 0), 0, new DiagramPoint(105, 65, 1),
                new DiagramPoint(105, 70, 2), new DiagramPoint(105, 55, 1), new DiagramPoint(105, 50, 2));
        addNodeTopologyLineDiagramData(network.getLine("Line"), new DiagramPoint(105, 50, 1), new DiagramPoint(105, 10, 2));
    }

    protected static void addNodeTopologyVerticalBusbarDiagramData(Network network) {
        addNodeTopologyBusbarSectionDiagramData(network.getBusbarSection("BusbarSection"), new DiagramPoint(140, 60, 1), new DiagramPoint(140, 170, 2));
        addNodeTopologyGeneratorDiagramData(network.getGenerator("Generator"), new DiagramPoint(45, 85, 0),
                new DiagramPoint(50, 85, 1), new DiagramPoint(140, 85, 2));
        addNodeTopologySwitchDiagramData(network.getSwitch("Disconnector1"), new DiagramPoint(155, 150, 0), 90, new DiagramPoint(150, 150, 1),
                new DiagramPoint(145, 150, 2), new DiagramPoint(130, 160, 1), new DiagramPoint(165, 150, 2));
        addNodeTopologySwitchDiagramData(network.getSwitch("Breaker1"), new DiagramPoint(175, 150, 0), 90, new DiagramPoint(170, 150, 1),
                new DiagramPoint(165, 150, 2), new DiagramPoint(180, 150, 1), new DiagramPoint(185, 150, 2));
        addNodeTopologySwitchDiagramData(network.getSwitch("Disconnector2"), new DiagramPoint(195, 150, 0), 90, new DiagramPoint(190, 150, 1),
                new DiagramPoint(185, 150, 2), new DiagramPoint(200, 150, 1), new DiagramPoint(205, 150, 1));
        addNodeTopologyLineDiagramData(network.getLine("Line"), new DiagramPoint(205, 150, 1), new DiagramPoint(260, 150, 2));
    }

    private static void addNodeTopologyBusbarSectionDiagramData(BusbarSection busbarSection, DiagramPoint point1, DiagramPoint point2) {
        NodeDiagramData<BusbarSection> busbarDiagramData = new NodeDiagramData<>(busbarSection);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails.setPoint1(point1);
        diagramDetails.setPoint2(point2);
        busbarDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        busbarSection.addExtension(NodeDiagramData.class, busbarDiagramData);
    }

    private static void addNodeTopologyGeneratorDiagramData(Generator generator, DiagramPoint generatorPoint, DiagramPoint terminalPoint1, DiagramPoint terminalPoint2) {
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = new InjectionDiagramData.InjectionDiagramDetails(generatorPoint, 0);
        diagramDetails.addTerminalPoint(terminalPoint1);
        diagramDetails.addTerminalPoint(terminalPoint2);
        generatorDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
    }

    private static void addNodeTopologySwitchDiagramData(Switch sw, DiagramPoint switchPoint, int rotation, DiagramPoint terminal1Point1, DiagramPoint terminal1Point2,
                                                    DiagramPoint terminal2Point1, DiagramPoint terminal2Point2) {
        CouplingDeviceDiagramData<Switch> switchDiagramData = new CouplingDeviceDiagramData<>(sw);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = new CouplingDeviceDiagramData.CouplingDeviceDiagramDetails(switchPoint, rotation);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point1);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point2);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point1);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point2);
        switchDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        sw.addExtension(CouplingDeviceDiagramData.class, switchDiagramData);
    }

    private static void addNodeTopologyLineDiagramData(Line line, DiagramPoint point1, DiagramPoint point2) {
        LineDiagramData<Line> lineDiagramData = new LineDiagramData<>(line);
        lineDiagramData.addPoint(DIAGRAM_NAME, point1);
        lineDiagramData.addPoint(DIAGRAM_NAME, point2);
        line.addExtension(LineDiagramData.class, lineDiagramData);
    }
}
