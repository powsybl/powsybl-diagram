/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.Optional;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class Networks {

    private static final String NETWORK_ID = "Network";

    private static final String CASE_DATE = "2018-01-01T00:30:00.000+01:00";

    private static final String SUBSTATION_ID = "Substation";

    private static final String SUBSTATION_1_ID = "Substation1";

    private static final String VOLTAGELEVEL_ID = "VoltageLevel";

    private static final String VOLTAGELEVEL_1_ID = "VoltageLevel1";

    private static final String VOLTAGELEVEL_2_ID = "VoltageLevel2";

    private Networks() {
    }

    public static Network createNetworkWithBusbar() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel.getNodeBreakerView().newBusbarSection()
                .setId("Busbar")
                .setNode(0)
                .add();
        return network;
    }

    public static Network createNetworkWithBus() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        return network;
    }

    public static Network createNetworkWithGenerator() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newGenerator()
                .setId("Generator")
                .setBus("Bus")
                .setConnectableBus("Bus")
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        return network;
    }

    public static Network createNetworkWithLine() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation1 = network.newSubstation()
                .setId(SUBSTATION_1_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId(VOLTAGELEVEL_1_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId(VOLTAGELEVEL_2_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        network.newLine()
                .setId("Line")
                .setVoltageLevel1(voltageLevel1.getId())
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setVoltageLevel2(voltageLevel2.getId())
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        return network;
    }

    public static Network createNetworkWithLoad() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newLoad()
                .setId("Load")
                .setBus("Bus")
                .setConnectableBus("Bus")
                .setP0(100)
                .setQ0(50)
                .add();
        return network;
    }

    public static Network createNetworkWithShuntCompensator() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newShuntCompensator()
                .setId("Shunt")
                .setBus("Bus")
                .setConnectableBus("Bus")
                .setSectionCount(1)
                .newLinearModel()
                    .setBPerSection(1e-5)
                    .setMaximumSectionCount(1)
                .add()
                .add();
        return network;
    }

    public static Network createNetworkWithStaticVarCompensator() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newStaticVarCompensator()
                .setId("Svc")
                .setConnectableBus("Bus")
                .setBus("Bus")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(390.0)
                .setReactivePowerSetpoint(1.0)
                .add();
        return network;
    }

    public static Network createNetworkWithSwitch() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        voltageLevel.getBusBreakerView().newSwitch()
                .setId("Switch")
                .setBus1("Bus1")
                .setBus2("Bus2")
                .setOpen(false)
                .add();
        return network;
    }

    public static Network createNetworkWithThreeWindingsTransformer() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_1_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_2_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        VoltageLevel voltageLevel3 = substation.newVoltageLevel()
                .setId("VoltageLevel3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel3.getBusBreakerView().newBus()
                .setId("Bus3")
                .add();
        substation.newThreeWindingsTransformer()
                .setId("Transformer3w")
                .newLeg1()
                    .setR(17.424)
                    .setX(1.7424)
                    .setB(0.000573921028466483)
                    .setG(0.00573921028466483)
                    .setRatedU(132.0)
                    .setVoltageLevel(voltageLevel1.getId())
                    .setBus("Bus1")
                    .add()
                .newLeg2()
                    .setR(1.089)
                    .setX(0.1089)
                    .setRatedU(33.0)
                    .setVoltageLevel(voltageLevel2.getId())
                    .setBus("Bus2")
                    .add()
                .newLeg3()
                    .setR(0.121)
                    .setX(0.0121)
                    .setRatedU(11.0)
                    .setVoltageLevel(voltageLevel3.getId())
                    .setBus("Bus3")
                    .add()
                .add();
        return network;
    }

    public static Network createNetworkWithTwoWindingsTransformer() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_1_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_2_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        int zb380 = 380 * 380 / 100;
        substation.newTwoWindingsTransformer()
                .setId("Transformer")
                .setVoltageLevel1(voltageLevel1.getId())
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setRatedU1(24.0)
                .setVoltageLevel2(voltageLevel2.getId())
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
        return network;
    }

    public static Network createNetworkWithDanglingLine() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation = network.newSubstation()
                .setId(SUBSTATION_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newDanglingLine()
                .setId("DanglingLine")
                .setBus("Bus")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
        return network;
    }

    public static Network createNetworkWithHvdcLine() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation substation1 = network.newSubstation()
                .setId(SUBSTATION_1_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId(VOLTAGELEVEL_1_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        voltageLevel1.newVscConverterStation()
                .setId("Converter1")
                .setConnectableBus("Bus1")
                .setBus("Bus1")
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId(VOLTAGELEVEL_2_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        voltageLevel2.newVscConverterStation()
                .setId("Converter2")
                .setConnectableBus("Bus2")
                .setBus("Bus2")
                .setLossFactor(0.011f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        network.newHvdcLine()
                .setId("HvdcLine")
                .setConverterStationId1("Converter1")
                .setConverterStationId2("Converter2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(280)
                .add();
        return network;
    }

    public static Network createNetworkWithBusbarAndSwitch() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse(CASE_DATE));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(true)
                .add();
        return network;
    }

    public static Network createNetworkWithDoubleBusbarSections() {
        int countNodes = 0;

        int bbN1 = countNodes++;
        int bbN2 = countNodes++;
        int iN1 = countNodes++;
        int gN1 = countNodes;

        Network network = Network.create("network1", "test");

        Substation substation1 = network.newSubstation()
                .setId(SUBSTATION_1_ID)
                .setCountry(Country.FR)
                .add();

        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId(VOLTAGELEVEL_1_ID)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();

        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection1")
                .setNode(bbN1)
                .add();
        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection2")
                .setNode(bbN2)
                .add();

        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector1")
                .setNode1(bbN1)
                .setNode2(iN1)
                .setOpen(true)
                .add();

        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector2")
                .setNode1(bbN2)
                .setNode2(iN1)
                .setOpen(false)
                .add();

        voltageLevel1.newGenerator()
                .setId("Generator1")
                .setNode(gN1)
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();

        voltageLevel1.getNodeBreakerView().newBreaker()
                .setId("Breaker1")
                .setNode1(gN1)
                .setNode2(iN1)
                .add();
        return network;
    }

    public static Network createNetworkWithPhaseShiftTransformer() {
        Network network = Networks.createNetworkWithTwoWindingsTransformer();
        Optional<TwoWindingsTransformer> twt = network.getTwoWindingsTransformerStream().findFirst();
        twt.ifPresent(twoWindingsTransformer -> twoWindingsTransformer.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(twoWindingsTransformer.getTerminal2())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setRegulationValue(200)
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
                .add());
        return network;
    }

    public static Network createNetworkWithBridge() {
        Network network = NetworkFactory.findDefault().createNetwork(NETWORK_ID, "test");
        network.setCaseDate(DateTime.parse("2020-01-01T00:30:00.000+01:00"));

        Substation substation = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId("V1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("Busbar1_1")
                .setNode(1)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_0")
                .setOpen(false)
                .setNode1(1)
                .setNode2(2)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_1")
                .setOpen(false)
                .setNode1(1)
                .setNode2(3)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_2")
                .setOpen(false)
                .setNode1(2)
                .setNode2(4)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_3")
                .setOpen(false)
                .setNode1(3)
                .setNode2(5)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_6_BRIDGE")
                .setOpen(true)
                .setNode1(4)
                .setNode2(5)
                .add();

        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId("V2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel2.getNodeBreakerView().newBusbarSection()
                .setId("Busbar2_1")
                .setNode(0)
                .add();
        voltageLevel2.getNodeBreakerView().newBreaker()
                .setId("Breaker2_0")
                .setOpen(false)
                .setNode1(1)
                .setNode2(2)
                .add();
        voltageLevel2.getNodeBreakerView().newDisconnector()
                .setId("D2_0")
                .setOpen(false)
                .setNode1(0)
                .setNode2(1)
                .add();

        VoltageLevel voltageLevel3 = substation.newVoltageLevel()
                .setId("V3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel3.getNodeBreakerView().newBusbarSection()
                .setId("Busbar3_1")
                .setNode(0)
                .add();
        voltageLevel3.getNodeBreakerView().newBreaker()
                .setId("Breaker3_0")
                .setOpen(false)
                .setNode1(1)
                .setNode2(2)
                .add();
        voltageLevel3.getNodeBreakerView().newDisconnector()
                .setId("D3_0")
                .setOpen(false)
                .setNode1(0)
                .setNode2(1)
                .add();

        network.newLine()
                .setId("Line1")
                .setVoltageLevel1(voltageLevel1.getId())
                .setVoltageLevel2(voltageLevel2.getId())
                .setR(5.0)
                .setX(32.0)
                .setG1(2.0)
                .setB1(386E-6 / 2)
                .setG2(2.0)
                .setB2(386E-6 / 2)
                .setNode1(4)
                .setNode2(2)
                .add();
        network.newLine()
                .setId("Line2")
                .setVoltageLevel1(voltageLevel1.getId())
                .setVoltageLevel2(voltageLevel3.getId())
                .setR(4.0)
                .setX(34.0)
                .setG1(1.0)
                .setB1(386E-6 / 2)
                .setG2(1.0)
                .setB2(386E-6 / 2)
                .setNode1(5)
                .setNode2(2)
                .add();

        return network;
    }

    /**
     * <pre>
     *  g1     dl1
     *  |       |
     *  b1 ---- b2
     *      l1 </pre>
     */
    public static Network createTwoVoltageLevels() {
        Network network = Network.create("dl", "test");
        Substation s = network.newSubstation().setId("s1").setName("Substation 1").add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("vl1")
                .setName("Voltage level 1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setConnectableBus("b1")
                .setBus("b1")
                .setTargetP(101.3664)
                .setTargetV(390)
                .setMinP(0)
                .setMaxP(150)
                .setVoltageRegulatorOn(true)
                .add();
        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("vl2")
                .setName("Voltage level 2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newDanglingLine()
                .setId("dl1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setR(0.7)
                .setX(1)
                .setG(1e-6)
                .setB(3e-6)
                .setP0(101)
                .setQ0(150)
                .newGeneration()
                .setTargetP(0)
                .setTargetQ(0)
                .setTargetV(390)
                .setVoltageRegulationOn(false)
                .add()
                .add();
        network.newLine()
                .setId("l1")
                .setVoltageLevel1("vl1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setR(1)
                .setX(3)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

    public static Network createTwoVoltageLevelsThreeBuses() {
        Network network = createTwoVoltageLevels();
        network.getVoltageLevel("vl1").getBusBreakerView().newBus()
                .setId("b0")
                .add();
        network.newLine()
                .setId("l2")
                .setVoltageLevel1("vl1")
                .setBus1("b0")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("b2")
                .setR(1)
                .setX(3)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

    public static Network createThreeVoltageLevelsFiveBuses() {

        Network network = createTwoVoltageLevelsThreeBuses();

        Substation s = network.getSubstation("s1");
        VoltageLevel vl3 = s.newVoltageLevel()
                .setId("vl3")
                .setNominalV(200)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b4")
                .add();
        vl3.newLoad()
                .setId("load3")
                .setBus("b3")
                .setP0(10.0)
                .setQ0(5.0)
                .add();

        s.newTwoWindingsTransformer()
                .setId("tr1")
                .setVoltageLevel1("vl1")
                .setBus1("b0")
                .setVoltageLevel2("vl3")
                .setBus2("b3")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();
        s.newTwoWindingsTransformer()
                .setId("tr2")
                .setVoltageLevel1("vl2")
                .setBus1("b2")
                .setVoltageLevel2("vl3")
                .setBus2("b4")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();

        return network;
    }

    /**
     * <pre>
     *   g1         dl1
     *   |    tr1    |
     *   |  --oo--   |
     *  b1 /      \ b2
     *     \      /
     *      --oo--
     *       tr2</pre>
     */
    public static Network createTwoVoltageLevelsTwoTransformers() {
        Network network = Network.create("dl", "test");
        Substation s = network.newSubstation().setId("s1").add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        network.getVoltageLevel("vl1").getBusBreakerView().newBus()
                .setId("b0")
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setConnectableBus("b1")
                .setBus("b1")
                .setTargetP(101.3664)
                .setTargetV(390)
                .setMinP(0)
                .setMaxP(150)
                .setVoltageRegulatorOn(true)
                .add();
        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("vl2")
                .setNominalV(200)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newDanglingLine()
                .setId("dl1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setR(0.7)
                .setX(1)
                .setG(1e-6)
                .setB(3e-6)
                .setP0(101)
                .setQ0(150)
                .newGeneration()
                .setTargetP(0)
                .setTargetQ(0)
                .setTargetV(390)
                .setVoltageRegulationOn(false)
                .add()
                .add();
        s.newTwoWindingsTransformer()
                .setId("tr1")
                .setVoltageLevel1("vl1")
                .setBus1("b0")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();
        s.newTwoWindingsTransformer()
                .setId("tr2")
                .setVoltageLevel1("vl1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();
        return network;
    }
}
