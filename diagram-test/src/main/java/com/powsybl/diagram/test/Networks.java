/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.test;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class Networks {

    private static final String NETWORK_ID = "Network";

    private static final String CASE_DATE = "2018-01-01T00:30:00.000+01:00";

    private static final String SUBSTATION_ID = "Substation";

    private static final String SUBSTATION_1_ID = "Substation1";

    private static final String SUBSTATION_2_ID = "Substation2";

    private static final String VOLTAGELEVEL_ID = "VoltageLevel";

    private static final String VOLTAGELEVEL_1_ID = "VoltageLevel1";

    private static final String VOLTAGELEVEL_2_ID = "VoltageLevel2";
    private static final String CONVERTER_1_ID = "Converter1";

    private static final String CONVERTER_2_ID = "Converter2";
    private static final String CONVERTER_3_ID = "Converter3";
    private static final String CONVERTER_4_ID = "Converter4";
    private static final String LOAD_3_ID = "load3";
    private static final String LOAD_1_ID = "load1";
    private static final String LOAD_2_ID = "load2";
    private static final String LOAD_4_ID = "load4";
    private static final String LINE_1_ID = "line1";
    private static final String BBS_21_ID = "bbs21";
    private static final String BBS_22_ID = "bbs22";
    private static final String LOAD_A_ID = "loadA";
    private static final String LOAD_B_ID = "loadB";
    private static final String BATT_2_ID = "batt2";
    private static final String BATT_1_ID = "batt1";
    private static final String BBS_11_ID = "bbs11";
    private static final String BBS_13_ID = "bbs13";
    private static final String BBS_23_ID = "bbs23";
    private static final String LOAD_C_ID = "loadC";
    private static final String LOAD_D_ID = "loadD";

    private static final String XNODE_1_ID = "XNODE1";

    private static final String THREE_WINDING_TRANSFORMER_12_ID = "T3_12";

    private Networks() {
    }

    public static Network createNetworkWithBusbar() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
                .setId(SUBSTATION_2_ID)
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        return createNetworkWithHvdcLine(CONVERTER_1_ID, CONVERTER_2_ID);
    }

    public static Network createNetworkWithHvdcLine(String vsc1Id, String vsc2Id) {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        // VSC 1
        voltageLevel1.newVscConverterStation()
                .setId(vsc1Id)
                .setConnectableBus("Bus1")
                .setBus("Bus1")
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        Substation substation2 = network.newSubstation()
                .setId(SUBSTATION_2_ID)
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
        // VSC 2
        voltageLevel2.newVscConverterStation()
                .setId(vsc2Id)
                .setConnectableBus("Bus2")
                .setBus("Bus2")
                .setLossFactor(0.011f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        network.newHvdcLine()
                .setId("HvdcLine")
                .setConverterStationId1(vsc1Id)
                .setConverterStationId2(vsc2Id)
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(280)
                .add();
        return network;
    }

    public static Network createNetworkWithHvdcLines(String vsc1Id, String vsc2Id,
                                                     String lcc1Id, String lcc2Id) {
        Network network = Networks.createNetworkWithHvdcLine(vsc1Id, vsc2Id);
        VoltageLevel voltageLevel1 = network.getVoltageLevel(VOLTAGELEVEL_1_ID);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(VOLTAGELEVEL_2_ID);
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus3")
                .add();
        // LCC 1
        voltageLevel1.newLccConverterStation()
                .setId(lcc1Id)
                .setConnectableBus("Bus3")
                .setBus("Bus3")
                .setLossFactor(0.011f)
                .setPowerFactor(0.5f)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus4")
                .add();
        // LCC 2
        voltageLevel2.newLccConverterStation()
                .setId(lcc2Id)
                .setConnectableBus("Bus4")
                .setBus("Bus4")
                .setLossFactor(0.011f)
                .setPowerFactor(-0.5f)
                .add();
        network.newHvdcLine()
                .setId("HvdcLine2")
                .setConverterStationId1(lcc1Id)
                .setConverterStationId2(lcc2Id)
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(280)
                .add();
        return network;
    }

    public static Network createNetworkWithHvdcLines() {
        return createNetworkWithHvdcLines(CONVERTER_1_ID, CONVERTER_2_ID, CONVERTER_3_ID, CONVERTER_4_ID);
    }

    public static Network createNetworkWithBusbarAndSwitch() {
        Network network = Network.create(NETWORK_ID, "test");
        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));
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
        network.setCaseDate(ZonedDateTime.parse("2020-01-01T00:30:00.000+01:00"));

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
                .setId(LOAD_3_ID)
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

    public static Network createTestCase11Network() {
        Network network = Network.create("testCase11", "test");

        Substation substation = createSubstation(network, "subst", "subst", Country.FR);

        // first voltage level
        //
        VoltageLevel vl1 = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);

        createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        createBusBarSection(vl1, "bbs2", "bbs2", 1, 1, 2);
        createBusBarSection(vl1, "bbs3", "bbs3", 2, 2, 1);
        createBusBarSection(vl1, "bbs4", "bbs4", 3, 2, 2);

        createSwitch(vl1, "dsect11", "dsect11", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        createSwitch(vl1, "dtrct11", "dtrct11", SwitchKind.BREAKER, true, false, true, 14, 15);
        createSwitch(vl1, "dsect12", "dsect12", SwitchKind.DISCONNECTOR, false, false, true, 15, 1);

        createSwitch(vl1, "dsect21", "dsect21", SwitchKind.DISCONNECTOR, false, false, true, 2, 16);
        createSwitch(vl1, "dtrct21", "dtrct21", SwitchKind.BREAKER, true, false, true, 16, 17);
        createSwitch(vl1, "dsect22", "dsect22", SwitchKind.DISCONNECTOR, false, false, true, 17, 3);

        createLoad(vl1, LOAD_1_ID, LOAD_1_ID, LOAD_1_ID, 0, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createSwitch(vl1, "dload1", "dload1", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        createSwitch(vl1, "bload1", "bload1", SwitchKind.BREAKER, true, false, true, 4, 5);

        createGenerator(vl1, "gen1", "gen1", "gen1", 2, ConnectablePosition.Direction.BOTTOM, 6, 0, 20, false, 10, 10);
        createSwitch(vl1, "dgen1", "dgen1", SwitchKind.DISCONNECTOR, false, false, true, 2, 7);
        createSwitch(vl1, "bgen1", "bgen1", SwitchKind.BREAKER, true, false, true, 6, 7);

        createLoad(vl1, LOAD_2_ID, LOAD_2_ID, LOAD_2_ID, 8, ConnectablePosition.Direction.TOP, 8, 10, 10);
        createSwitch(vl1, "dload2", "dload2", SwitchKind.DISCONNECTOR, false, false, true, 1, 9);
        createSwitch(vl1, "bload2", "bload2", SwitchKind.BREAKER, true, false, true, 8, 9);

        createGenerator(vl1, "gen2", "gen2", "gen2", 12, ConnectablePosition.Direction.BOTTOM, 10, 0, 20, false, 10, 10);
        createSwitch(vl1, "dgen2", "dgen2", SwitchKind.DISCONNECTOR, false, false, true, 3, 11);
        createSwitch(vl1, "bgen2", "bgen2", SwitchKind.BREAKER, true, false, true, 10, 11);

        // second voltage level
        //
        VoltageLevel vl2 = createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225);

        createBusBarSection(vl2, "bbs5", "bbs5", 0, 1, 1);
        createBusBarSection(vl2, "bbs6", "bbs6", 1, 2, 1);

        createSwitch(vl2, "dscpl1", "dscpl1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        createSwitch(vl2, "ddcpl1", "ddcpl1", SwitchKind.BREAKER, true, false, true, 6, 7);
        createSwitch(vl2, "dscpl2", "dscpl2", SwitchKind.DISCONNECTOR, false, false, true, 7, 1);

        createLoad(vl2, LOAD_3_ID, LOAD_3_ID, LOAD_3_ID, 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl2, "dload3", "dload3", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(vl2, "bload3", "bload3", SwitchKind.BREAKER, true, false, true, 2, 3);

        createGenerator(vl2, "gen4", "gen4", "gen4", 2, ConnectablePosition.Direction.BOTTOM, 4, 0, 20, false, 10, 10);
        createSwitch(vl2, "dgen4", "dgen4", SwitchKind.DISCONNECTOR, false, false, true, 1, 5);
        createSwitch(vl2, "bgen4", "bgen4", SwitchKind.BREAKER, true, false, true, 4, 5);

        // third voltage level
        //
        VoltageLevel vl3 = createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 50);

        createBusBarSection(vl3, "bbs7", "bbs7", 0, 1, 1);

        createLoad(vl3, LOAD_4_ID, LOAD_4_ID, LOAD_4_ID, 0, ConnectablePosition.Direction.TOP, 1, 10, 10);
        createSwitch(vl3, "dload4", "dload4", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl3, "bload4", "bload4", SwitchKind.BREAKER, true, false, true, 2, 1);

        // two windings transformers between voltage levels
        //
        createSwitch(vl1, "dtrf11", "dtrf11", SwitchKind.DISCONNECTOR, false, false, true, 0, 18);
        createSwitch(vl1, "btrf11", "btrf11", SwitchKind.BREAKER, true, false, true, 18, 19);
        createSwitch(vl2, "dtrf21", "dtrf21", SwitchKind.DISCONNECTOR, false, false, true, 0, 8);
        createSwitch(vl2, "btrf21", "btrf21", SwitchKind.BREAKER, true, false, true, 8, 9);
        createTwoWindingsTransformer(substation, "trf1", "trf1", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                19, 9, vl1.getId(), vl2.getId(),
                "trf1", 1, ConnectablePosition.Direction.TOP,
                "trf1", 1, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "dtrf12", "dtrf12", SwitchKind.DISCONNECTOR, false, false, true, 1, 20);
        createSwitch(vl1, "btrf12", "btrf12", SwitchKind.BREAKER, true, false, true, 20, 21);
        createSwitch(vl2, "dtrf22", "dtrf22", SwitchKind.DISCONNECTOR, false, false, true, 1, 10);
        createSwitch(vl2, "btrf22", "btrf22", SwitchKind.BREAKER, true, false, true, 10, 11);
        createTwoWindingsTransformer(substation, "trf2", "trf2", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                21, 11, vl1.getId(), vl2.getId(),
                "trf2", 11, ConnectablePosition.Direction.TOP,
                "trf2", 7, ConnectablePosition.Direction.BOTTOM);

        createSwitch(vl1, "dtrf13", "dtrf13", SwitchKind.DISCONNECTOR, false, false, true, 2, 22);
        createSwitch(vl1, "btrf13", "btrf13", SwitchKind.BREAKER, true, false, true, 22, 23);
        createSwitch(vl2, "dtrf23", "dtrf23", SwitchKind.DISCONNECTOR, false, false, true, 1, 12);
        createSwitch(vl2, "btrf23", "btrf23", SwitchKind.BREAKER, true, false, true, 12, 13);
        createTwoWindingsTransformer(substation, "trf3", "trf3", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                23, 13, vl1.getId(), vl2.getId(),
                "trf3", 3, ConnectablePosition.Direction.BOTTOM,
                "trf3", 8, ConnectablePosition.Direction.BOTTOM);

        createSwitch(vl1, "dtrf14", "dtrf14", SwitchKind.DISCONNECTOR, false, false, true, 3, 24);
        createSwitch(vl1, "btrf14", "btrf14", SwitchKind.BREAKER, true, false, true, 24, 25);
        createSwitch(vl2, "dtrf24", "dtrf24", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        createSwitch(vl2, "btrf24", "btrf24", SwitchKind.BREAKER, true, false, true, 14, 15);
        createTwoWindingsTransformer(substation, "trf4", "trf4", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                25, 15, vl1.getId(), vl2.getId(),
                "trf4", 10, ConnectablePosition.Direction.BOTTOM,
                "trf4", 3, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "dtrf15", "dtrf15", SwitchKind.DISCONNECTOR, false, false, true, 0, 26);
        createSwitch(vl1, "btrf15", "btrf15", SwitchKind.BREAKER, true, false, true, 26, 27);
        createSwitch(vl3, "dtrf25", "dtrf25", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(vl3, "btrf25", "btrf25", SwitchKind.BREAKER, true, false, true, 3, 4);
        createTwoWindingsTransformer(substation, "trf5", "trf5", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                27, 4, vl1.getId(), vl3.getId(),
                "trf5", 4, ConnectablePosition.Direction.TOP,
                "trf5", 1, ConnectablePosition.Direction.BOTTOM);

        // three windings transformers between voltage levels
        //
        createSwitch(vl1, "dtrf16", "dtrf16", SwitchKind.DISCONNECTOR, false, false, true, 0, 28);
        createSwitch(vl1, "btrf16", "btrf16", SwitchKind.BREAKER, true, false, true, 28, 29);
        createSwitch(vl2, "dtrf26", "dtrf26", SwitchKind.DISCONNECTOR, false, false, true, 1, 16);
        createSwitch(vl2, "btrf26", "btrf26", SwitchKind.BREAKER, true, false, true, 16, 17);
        createSwitch(vl3, "dtrf36", "dtrf36", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        createSwitch(vl3, "btrf36", "btrf36", SwitchKind.BREAKER, true, false, true, 5, 6);

        createThreeWindingsTransformer(substation, "trf6", "trf6", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 50.,
                29, 17, 6,
                "trf61", 5, ConnectablePosition.Direction.TOP,
                "trf62", 5, ConnectablePosition.Direction.TOP,
                "trf63", 2, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "dtrf17", "dtrf17", SwitchKind.DISCONNECTOR, false, false, true, 2, 30);
        createSwitch(vl1, "btrf17", "btrf17", SwitchKind.BREAKER, true, false, true, 30, 31);
        createSwitch(vl2, "dtrf27", "dtrf27", SwitchKind.DISCONNECTOR, false, false, true, 0, 18);
        createSwitch(vl2, "btrf27", "btrf27", SwitchKind.BREAKER, true, false, true, 18, 19);
        createSwitch(vl3, "dtrf37", "dtrf37", SwitchKind.DISCONNECTOR, false, false, true, 0, 7);
        createSwitch(vl3, "btrf37", "btrf37", SwitchKind.BREAKER, true, false, true, 7, 8);

        createThreeWindingsTransformer(substation, "trf7", "trf7", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 50.,
                31, 19, 8,
                "trf71", 6, ConnectablePosition.Direction.BOTTOM,
                "trf72", 4, ConnectablePosition.Direction.TOP,
                "trf73", 3, ConnectablePosition.Direction.BOTTOM);

        createSwitch(vl1, "dtrf18", "dtrf18", SwitchKind.DISCONNECTOR, false, false, true, 1, 32);
        createSwitch(vl1, "btrf18", "btrf18", SwitchKind.BREAKER, true, false, true, 32, 33);
        createSwitch(vl2, "dtrf28", "dtrf28", SwitchKind.DISCONNECTOR, false, false, true, 1, 20);
        createSwitch(vl2, "btrf28", "btrf28", SwitchKind.BREAKER, true, false, true, 20, 21);
        createSwitch(vl3, "dtrf38", "dtrf38", SwitchKind.DISCONNECTOR, false, false, true, 0, 9);
        createSwitch(vl3, "btrf38", "btrf38", SwitchKind.BREAKER, true, false, true, 9, 10);

        createThreeWindingsTransformer(substation, "trf8", "trf8", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 50.,
                33, 21, 10,
                "trf81", 9, ConnectablePosition.Direction.TOP,
                "trf82", 6, ConnectablePosition.Direction.BOTTOM,
                "trf83", 4, ConnectablePosition.Direction.TOP);

        // Creation of another substation, another voltageLevel and a line between the two substations
        //
        Substation substation2 = createSubstation(network, "subst2", "subst2", Country.FR);
        VoltageLevel vlSubst2 = createVoltageLevel(substation2, "vlSubst2", "vlSubst2", TopologyKind.NODE_BREAKER, 380);

        createBusBarSection(vlSubst2, "bbs1_2", "bbs1_2", 0, 1, 1);

        createSwitch(vl1, "dline11_2", "dline11_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 34);
        createSwitch(vl1, "bline11_2", "bline11_2", SwitchKind.BREAKER, true, false, true, 34, 35);
        createSwitch(vlSubst2, "dline21_2", "dline21_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(vlSubst2, "bline21_2", "bline21_2", SwitchKind.BREAKER, true, false, true, 1, 2);
        createLine(network, LINE_1_ID, LINE_1_ID, 2.0, 14.745, 1.0, 1.0, 1.0, 1.0,
                35, 2, vl1.getId(), vlSubst2.getId(),
                LINE_1_ID, 7, ConnectablePosition.Direction.TOP,
                LINE_1_ID, 1, ConnectablePosition.Direction.TOP);

        return network;
    }

    public static Network createNodeBreakerNetworkWithBranchStatus(String id, String sourceFormat) {
        Network network = createNodeBreakerNetworkWithInternalBranches(id, sourceFormat);

        createStatusExtensions(network);

        return network;
    }

    public static Network createBusBreakerNetworkWithBranchStatus(String id, String sourceFormat) {
        Network network = createBusBreakerNetworkWithInternalBranches(id, sourceFormat);

        createStatusExtensions(network);

        return network;
    }

    private static void createStatusExtensions(Network network) {
        network.getLine("L11").newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.PLANNED_OUTAGE).add();
        network.getLine("L12").newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();

        network.getTwoWindingsTransformer("T11").newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.PLANNED_OUTAGE).add();
        network.getTwoWindingsTransformer("T12").newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();

        network.getThreeWindingsTransformer(THREE_WINDING_TRANSFORMER_12_ID).newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();
    }

    public static Network createNodeBreakerNetworkWithInternalBranches(String id, String sourceFormat) {
        Network network = Network.create(id, sourceFormat);
        Substation substation = createSubstation(network, "S1", "S1", Country.FR);

        VoltageLevel vl1 = createVoltageLevel(substation, "VL1", "VL1", TopologyKind.NODE_BREAKER, 400);

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

        VoltageLevel vl2 = createVoltageLevel(substation, "VL2", "VL2", TopologyKind.NODE_BREAKER, 400);

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
        twoWindingsTransformer.getTerminal(TwoSides.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoSides.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoSides.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoSides.TWO).setQ(48);

        createSwitch(vl1, "D27", "D27", SwitchKind.DISCONNECTOR, false, false, false, 20, 27);
        createSwitch(vl1, "BR28", "BR28", SwitchKind.BREAKER, false, false, false, 27, 28);

        createSwitch(vl2, "D35", "D35", SwitchKind.DISCONNECTOR, false, false, false, 30, 35);
        createSwitch(vl2, "BR36", "BR36", SwitchKind.BREAKER, false, false, false, 35, 36);

        createTwoWindingsTransformer(substation, "T12", "T12", 250, 100, 52, 12, 65, 90,
                28, 36, vl1.getId(), vl2.getId(),
                "T12", null, ConnectablePosition.Direction.TOP,
                "T12", null, ConnectablePosition.Direction.TOP);

        twoWindingsTransformer = network.getTwoWindingsTransformer("T12");
        twoWindingsTransformer.getTerminal(TwoSides.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoSides.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoSides.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoSides.TWO).setQ(48);

        createSwitch(vl1, "D19", "D19", SwitchKind.DISCONNECTOR, false, false, false, 10, 19);
        createSwitch(vl1, "BR20", "BR20", SwitchKind.BREAKER, false, false, false, 19, 190);

        createSwitch(vl1, "D29", "D29", SwitchKind.DISCONNECTOR, false, false, false, 20, 29);
        createSwitch(vl1, "BR30", "BR30", SwitchKind.BREAKER, false, false, false, 29, 290);

        createSwitch(vl2, "D37", "D37", SwitchKind.DISCONNECTOR, false, false, false, 30, 37);
        createSwitch(vl2, "BR38", "BR38", SwitchKind.BREAKER, false, false, false, 37, 38);

        createThreeWindingsTransformer(substation, THREE_WINDING_TRANSFORMER_12_ID, THREE_WINDING_TRANSFORMER_12_ID, vl1.getId(), vl1.getId(), vl2.getId(),
                45, 47, 49, 35, 32, 39., 25, 15,
                5, 7, 9,
                190, 290, 38,
                THREE_WINDING_TRANSFORMER_12_ID, null, ConnectablePosition.Direction.TOP,
                THREE_WINDING_TRANSFORMER_12_ID, null, ConnectablePosition.Direction.TOP,
                THREE_WINDING_TRANSFORMER_12_ID, null, ConnectablePosition.Direction.TOP);

        return network;
    }

    public static Network createBusBreakerNetworkWithInternalBranches(String id, String sourceFormat) {
        Network network = Network.create(id, sourceFormat);
        Substation substation = createSubstation(network, "S1", "S1", Country.FR);

        VoltageLevel vl1 = createVoltageLevel(substation, "VL1", "VL1", TopologyKind.BUS_BREAKER, 400);

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

        substation.newThreeWindingsTransformer()
                .setId(THREE_WINDING_TRANSFORMER_12_ID)
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
                .setVoltageSetpoint(390)
                .add();
        svc.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        VscConverterStation vsc = vl.newVscConverterStation()
                .setId("vsc")
                .setName(CONVERTER_1_ID)
                .setNode(1)
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        vsc.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        vl.newShuntCompensator()
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
                .setName(CONVERTER_2_ID)
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
        createBusBarSection(vl, BBS_21_ID, BBS_21_ID, 1, 2, 1);
        createBusBarSection(vl, BBS_22_ID, BBS_22_ID, 2, 2, 2);
        createSwitch(vl, "bA", "bA", SwitchKind.BREAKER, false, false, false, 3, 4);
        createLoad(vl, LOAD_A_ID, LOAD_A_ID, LOAD_A_ID, null, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createSwitch(vl, "dA1", "dA1", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        createSwitch(vl, "dA2", "dA2", SwitchKind.DISCONNECTOR, false, false, false, 1, 3);

        createSwitch(vl, "bB", "bB", SwitchKind.BREAKER, false, false, false, 5, 6);
        createLoad(vl, LOAD_B_ID, LOAD_B_ID, LOAD_B_ID, null, ConnectablePosition.Direction.TOP, 6, 10, 10);
        createSwitch(vl, "dB1", "dB1", SwitchKind.DISCONNECTOR, false, false, false, 2, 5);
        createSwitch(vl, "dB2", "dB2", SwitchKind.DISCONNECTOR, false, false, false, 0, 5);

        createSwitch(vl, "link", "link", SwitchKind.BREAKER, false, false, false, 5, 9);

        return network;
    }

    public static Network createNetworkWithTwoParallelLoads() {
        Network network = Network.create("TestSingleLineDiagramClass", "test");
        Substation substation = createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        createBusBarSection(vl, BBS_13_ID, BBS_13_ID, 7, 1, 3);
        createBusBarSection(vl, BBS_23_ID, BBS_23_ID, 8, 2, 3);
        createLoad(vl, LOAD_C_ID, LOAD_C_ID, LOAD_C_ID, null, ConnectablePosition.Direction.TOP, 9, 10, 10);
        createSwitch(vl, "bCD1", "bCD1", SwitchKind.BREAKER, false, false, false, 8, 9);
        createSwitch(vl, "bCD2", "bCD2", SwitchKind.BREAKER, false, false, false, 7, 9);
        createSwitch(vl, "bCD3", "bCD3", SwitchKind.BREAKER, false, false, false, 7, 9);
        createSwitch(vl, "bD1", "bD1", SwitchKind.BREAKER, false, false, false, 20, 9);
        createLoad(vl, LOAD_D_ID, LOAD_D_ID, LOAD_D_ID, null, ConnectablePosition.Direction.TOP, 20, 10, 10);
        return network;
    }

    public static Network createNetworkWithFourParallelLegs() {
        Network network = Network.create("networkWithFourParallelLegs", "test");
        Substation substation = createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 3);
        createBusBarSection(vl, "bbs2", "bbs2", 1, 2, 3);
        createSwitch(vl, "d1a", "d1an", SwitchKind.DISCONNECTOR, false, false, false, 0, 2);
        createSwitch(vl, "d2a", "d2an", SwitchKind.DISCONNECTOR, false, false, false, 1, 2);
        createSwitch(vl, "b1", "b1n", SwitchKind.BREAKER, false, false, false, 2, 3);
        createSwitch(vl, "d1b", "d1bn", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        createSwitch(vl, "d2b", "d2bn", SwitchKind.DISCONNECTOR, false, false, false, 1, 3);
        createSwitch(vl, "bl", "bln", SwitchKind.BREAKER, false, false, false, 3, 4);
        createLoad(vl, "load", "load", "load displayed name", null, ConnectablePosition.Direction.BOTTOM, 4, 10, 10);
        return network;
    }

    public static Network createNetworkWithInternalPst() {
        Network network = Network.create("networkWithInternalPst", "test");
        Substation substation = createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 3);
        createBusBarSection(vl, "bbs2", "bbs2", 1, 2, 3);
        createSwitch(vl, "d1a", "d1an", SwitchKind.DISCONNECTOR, false, false, false, 0, 2);
        createSwitch(vl, "d2a", "d2an", SwitchKind.DISCONNECTOR, false, false, false, 1, 2);
        createSwitch(vl, "bpst", "bpstn", SwitchKind.BREAKER, false, false, false, 2, 3);
        createPhaseShiftTransformer(substation, "trf3", "trf3", 1.0, 14.745, 0.0, 3.2E-5, 380.0, 380.0,
                3, 4, vl.getId(), vl.getId(),
                "pst1a", 1, ConnectablePosition.Direction.BOTTOM,
                "pst1b", 1, ConnectablePosition.Direction.BOTTOM);
        createSwitch(vl, "dpst", "dpstn", SwitchKind.BREAKER, false, true, false, 4, 5);
        createSwitch(vl, "d1b", "d1bn", SwitchKind.DISCONNECTOR, false, true, false, 0, 5);
        createSwitch(vl, "d2b", "d2bn", SwitchKind.DISCONNECTOR, false, true, false, 1, 5);
        createSwitch(vl, "bl", "bln", SwitchKind.BREAKER, false, false, false, 5, 6);
        createLoad(vl, "load", "load", "l", null, ConnectablePosition.Direction.BOTTOM, 6, 10, 10);
        return network;
    }

    public static Network createNetworkWithFlatSections() {
        Network network = Network.create("TestSingleLineDiagramClass", "test");
        Substation substation = createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        createBusBarSection(vl, BBS_11_ID, BBS_11_ID, 0, 1, 1);
        createBusBarSection(vl, BBS_21_ID, BBS_21_ID, 1, 2, 1);
        createBusBarSection(vl, "bbs12", "bbs12", 2, 1, 2);
        createBusBarSection(vl, BBS_22_ID, BBS_22_ID, 3, 2, 2);
        createBusBarSection(vl, BBS_13_ID, BBS_13_ID, 4, 1, 3);
        createBusBarSection(vl, BBS_23_ID, BBS_23_ID, 5, 2, 3);
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

    public static Network createNetworkWithBatteries() {
        Network network = Network.create("TestBatteries", "test");
        Substation substation = createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        createBusBarSection(vl, BBS_11_ID, BBS_11_ID, 0, 1, 1);
        createSwitch(vl, "d1b", "d1b", SwitchKind.DISCONNECTOR, true, false, false, 1, 2);
        createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, true, false, false, 0, 1);
        createBattery(vl, BATT_1_ID, BATT_1_ID, BATT_1_ID, null, ConnectablePosition.Direction.TOP, 2, 1, 10, 5, 5);
        createSwitch(vl, "d2b", "d2b", SwitchKind.DISCONNECTOR, true, true, false, 3, 4);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, true, true, false, 0, 3);
        createBattery(vl, BATT_2_ID, BATT_2_ID, BATT_2_ID, null, ConnectablePosition.Direction.BOTTOM, 4, 3, 10, 6, 6);
        return network;
    }

    public static Substation createSubstation(Network n, String id, String name, Country country) {
        return n.newSubstation()
                .setId(id)
                .setName(name)
                .setCountry(country)
                .add();
    }

    public static VoltageLevel createVoltageLevel(Substation s, String id, String name,
                                                  TopologyKind topology, double vNom) {
        return s.newVoltageLevel()
                .setId(id)
                .setName(name)
                .setTopologyKind(topology)
                .setNominalV(vNom)
                .add();
    }

    public static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        createSwitch(vl, id, id, kind, retained, open, fictitious, node1, node2);
    }

    public static void createSwitch(VoltageLevel vl, String id, String name, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(name)
                .setKind(kind)
                .setRetained(retained)
                .setOpen(open)
                .setFictitious(fictitious)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    public static void createInternalConnection(VoltageLevel vl, int node1, int node2) {
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    public static void createBusBarSection(VoltageLevel vl, String id, int node, int busbarIndex, int sectionIndex) {
        createBusBarSection(vl, id, id, node, busbarIndex, sectionIndex);
    }

    public static void createBusBarSection(VoltageLevel vl, String id, String name, int node, int busbarIndex, int sectionIndex) {
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
                .setId(id)
                .setName(name)
                .setNode(node)
                .add();
        bbs.newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(busbarIndex)
                .withSectionIndex(sectionIndex)
                .add();
    }

    public static void createLoad(VoltageLevel vl, String id, Integer feederOrder,
                                  ConnectablePosition.Direction direction, int node, double p0, double q0) {
        createLoad(vl, id, id, id, feederOrder, direction, node, p0, q0);
    }

    public static void createLoad(VoltageLevel vl, String id, String name, String feederName, Integer feederOrder,
                                  ConnectablePosition.Direction direction, int node, double p0, double q0) {
        Load load = vl.newLoad()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setP0(p0)
                .setQ0(q0)
                .add();
        addFeederPosition(load, feederName, feederOrder, direction);
    }

    public static void createGenerator(VoltageLevel vl, String id, Integer feederOrder,
                                       ConnectablePosition.Direction direction, int node,
                                       double minP, double maxP, boolean voltageRegulator,
                                       double targetP, double targetQ) {
        createGenerator(vl, id, id, id, feederOrder, direction, node, minP, maxP, voltageRegulator, targetP, targetQ);
    }

    public static void createGenerator(VoltageLevel vl, String id, String name, String feederName, Integer feederOrder,
                                       ConnectablePosition.Direction direction, int node,
                                       double minP, double maxP, boolean voltageRegulator,
                                       double targetP, double targetQ) {
        Generator gen = vl.newGenerator()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(voltageRegulator)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .add();
        addFeederPosition(gen, feederName, feederOrder, direction);
    }

    private static void createBattery(VoltageLevel vl, String id, String name, String feederName, Integer feederOrder,
                                      ConnectablePosition.Direction direction, int node,
                                      double minP, double maxP,
                                      double targetP, double targetQ) {
        Battery battery = vl.newBattery()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setMinP(minP)
                .setMaxP(maxP)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .add();
        addFeederPosition(battery, feederName, feederOrder, direction);
    }

    public static void createShunt(VoltageLevel vl, String id, String name, String feederName, Integer feederOrder,
                                   ConnectablePosition.Direction direction, int node,
                                   double bPerSection, int maximumSectionCount, int currentSectionCount) {
        ShuntCompensator shunt = vl.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setSectionCount(currentSectionCount)
                .newLinearModel()
                .setBPerSection(bPerSection)
                .setMaximumSectionCount(maximumSectionCount)
                .add()
                .add();
        addFeederPosition(shunt, feederName, feederOrder, direction);
    }

    public static TwoWindingsTransformer createTwoWindingsTransformer(Substation s, String id, String name,
                                                                      double r, double x, double g, double b,
                                                                      double ratedU1, double ratedU2,
                                                                      int node1, int node2,
                                                                      String idVoltageLevel1, String idVoltageLevel2,
                                                                      String feederName1, Integer feederOrder1, ConnectablePosition.Direction direction1,
                                                                      String feederName2, Integer feederOrder2, ConnectablePosition.Direction direction2) {
        TwoWindingsTransformer t = s.newTwoWindingsTransformer()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2)
                .setNode1(node1)
                .setVoltageLevel1(idVoltageLevel1)
                .setNode2(node2)
                .setVoltageLevel2(idVoltageLevel2)
                .add();
        addTwoFeedersPosition(t, feederName1, feederOrder1, direction1, feederName2, feederOrder2, direction2);
        return t;
    }

    private static void createPhaseShiftTransformer(Substation s, String id, String name,
                                                    double r, double x, double g, double b,
                                                    double ratedU1, double ratedU2,
                                                    int node1, int node2,
                                                    String idVoltageLevel1, String idVoltageLevel2,
                                                    String feederName1, Integer feederOrder1, ConnectablePosition.Direction direction1,
                                                    String feederName2, Integer feederOrder2, ConnectablePosition.Direction direction2) {
        TwoWindingsTransformer twt = createTwoWindingsTransformer(s, id, name, r, x, g, b, ratedU1, ratedU2, node1, node2,
                idVoltageLevel1, idVoltageLevel2, feederName1, feederOrder1, direction1, feederName2, feederOrder2, direction2);
        twt.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(twt.getTerminal2())
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
                .add();
    }

    public static void createThreeWindingsTransformer(Substation s, String id, String name,
                                                      String vl1, String vl2, String vl3,
                                                      double r1, double r2, double r3,
                                                      double x1, double x2, double x3,
                                                      double g1, double b1,
                                                      double ratedU1, double ratedU2, double ratedU3,
                                                      int node1, int node2, int node3,
                                                      String feederName1, Integer feederOrder1, ConnectablePosition.Direction direction1,
                                                      String feederName2, Integer feederOrder2, ConnectablePosition.Direction direction2,
                                                      String feederName3, Integer feederOrder3, ConnectablePosition.Direction direction3) {
        ThreeWindingsTransformer t = s.newThreeWindingsTransformer()
                .setId(id)
                .setName(name)
                .newLeg1()
                .setR(r1)
                .setX(x1)
                .setG(g1)
                .setB(b1)
                .setRatedU(ratedU1)
                .setVoltageLevel(vl1)
                .setNode(node1)
                .add()
                .newLeg2()
                .setR(r2)
                .setX(x2)
                .setRatedU(ratedU2)
                .setVoltageLevel(vl2)
                .setNode(node2)
                .add()
                .newLeg3()
                .setR(r3)
                .setX(x3)
                .setRatedU(ratedU3)
                .setVoltageLevel(vl3)
                .setNode(node3)
                .add()
                .add();

        addThreeFeedersPosition(t, feederName1, feederOrder1, direction1, feederName2, feederOrder2, direction2, feederName3, feederOrder3, direction3);
    }

    public static void createLine(Network network,
                                  String id, String name,
                                  double r, double x,
                                  double g1, double b1,
                                  double g2, double b2,
                                  int node1, int node2,
                                  String idVoltageLevel1, String idVoltageLevel2,
                                  String feederName1, Integer feederOrder1, ConnectablePosition.Direction direction1,
                                  String feederName2, Integer feederOrder2, ConnectablePosition.Direction direction2) {
        Line line = network.newLine()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2)
                .setNode1(node1)
                .setVoltageLevel1(idVoltageLevel1)
                .setNode2(node2)
                .setVoltageLevel2(idVoltageLevel2)
                .add();
        addTwoFeedersPosition(line, feederName1, feederOrder1, direction1, feederName2, feederOrder2, direction2);
    }

    private static void addFeederPosition(Extendable<?> extendable, String feederName, Integer feederOrder, ConnectablePosition.Direction direction) {
        ConnectablePositionAdder.FeederAdder<?> feederAdder = extendable.newExtension(ConnectablePositionAdder.class).newFeeder();
        if (feederOrder != null) {
            feederAdder.withOrder(feederOrder);
        }
        feederAdder.withDirection(direction).withName(feederName).add()
                .add();
    }

    private static void addTwoFeedersPosition(Extendable<?> extendable,
                                              String feederName1, Integer feederOrder1, ConnectablePosition.Direction direction1,
                                              String feederName2, Integer feederOrder2, ConnectablePosition.Direction direction2) {
        ConnectablePositionAdder<?> extensionAdder = extendable.newExtension(ConnectablePositionAdder.class);
        ConnectablePositionAdder.FeederAdder<?> feederAdder1 = extensionAdder.newFeeder1();
        if (feederOrder1 != null) {
            feederAdder1.withOrder(feederOrder1);
        }
        feederAdder1.withName(feederName1).withDirection(direction1).add();
        ConnectablePositionAdder.FeederAdder<?> feederAdder2 = extensionAdder.newFeeder2();
        if (feederOrder2 != null) {
            feederAdder2.withOrder(feederOrder2);
        }
        feederAdder2.withName(feederName2).withDirection(direction2).add();
        extensionAdder.add();
    }

    private static void addThreeFeedersPosition(Extendable<?> extendable,
                                                String feederName1, Integer feederOrder1, ConnectablePosition.Direction direction1,
                                                String feederName2, Integer feederOrder2, ConnectablePosition.Direction direction2,
                                                String feederName3, Integer feederOrder3, ConnectablePosition.Direction direction3) {
        ConnectablePositionAdder<?> extensionAdder = extendable.newExtension(ConnectablePositionAdder.class);
        ConnectablePositionAdder.FeederAdder<?> feederAdder1 = extensionAdder.newFeeder1();
        if (feederOrder1 != null) {
            feederAdder1.withOrder(feederOrder1);
        }
        feederAdder1.withName(feederName1).withDirection(direction1).add();
        ConnectablePositionAdder.FeederAdder<?> feederAdder2 = extensionAdder.newFeeder2();
        if (feederOrder2 != null) {
            feederAdder2.withOrder(feederOrder2);
        }
        feederAdder2.withName(feederName2).withDirection(direction2).add();
        ConnectablePositionAdder.FeederAdder<?> feederAdder3 = extensionAdder.newFeeder3();
        if (feederOrder3 != null) {
            feederAdder3.withOrder(feederOrder3);
        }
        feederAdder3.withName(feederName3).withDirection(direction3).add();
        extensionAdder.add();
    }

    public static Network createNetworkWithTieLineInVoltageLevel() {
        Network network = createBusBreakerNetworkWithInternalBranches("tieLineWithinVoltageLevel", "test");
        network.getLine("L11").remove();
        String tieLineId = "B11_B12_1";
        DanglingLine b11xnode1 = network.getVoltageLevel("VL1").newDanglingLine().setId("B11_XNODE1").setR(1.5).setX(20.0).setG(0.0).setB(1.93E-4).setP0(0).setQ0(0).setBus("B11").setPairingKey(XNODE_1_ID).add();
        DanglingLine xnode1b12 = network.getVoltageLevel("VL1").newDanglingLine().setId("XNODE1_B12").setR(1.5).setX(13.0).setG(0.0).setB(1.93E-4).setP0(0).setQ0(0).setBus("B12").setPairingKey(XNODE_1_ID).add();
        network.newTieLine().setId(tieLineId).setDanglingLine1(b11xnode1.getId()).setDanglingLine2(xnode1b12.getId()).add();
        network.getTieLine(tieLineId).getDanglingLine1().getTerminal().setP(302.4440612792969).setQ(98.74027252197266);
        network.getTieLine(tieLineId).getDanglingLine2().getTerminal().setP(-300.43389892578125).setQ(-137.18849182128906);

        return network;

    }

    public static Network createNetworkWithTieLineInSubstation() {
        Network network = createBusBreakerNetworkWithInternalBranches("tieLineWithinSubstation", "test");
        network.getLine("L12").remove();
        String tieLineId = "B11_B21_1";
        DanglingLine b11xnode1 = network.getVoltageLevel("VL1").newDanglingLine().setId("B11_XNODE1").setR(1.5).setX(20.0).setG(0.0).setB(1.93E-4).setP0(0).setQ0(0).setBus("B11").setPairingKey(XNODE_1_ID).add();
        DanglingLine xnode1b21 = network.getVoltageLevel("VL2").newDanglingLine().setId("XNODE1_B21").setR(1.5).setX(13.0).setG(0.0).setB(1.93E-4).setP0(0).setQ0(0).setBus("B21").setPairingKey(XNODE_1_ID).add();
        network.newTieLine().setId(tieLineId).setDanglingLine1(b11xnode1.getId()).setDanglingLine2(xnode1b21.getId()).add();
        network.getTieLine(tieLineId).getDanglingLine1().getTerminal().setP(302.4440612792969).setQ(98.74027252197266);
        network.getTieLine(tieLineId).getDanglingLine2().getTerminal().setP(-300.43389892578125).setQ(-137.18849182128906);

        return network;

    }

    public static Network createNetworkWithManySubstations() {
        Network network = com.powsybl.iidm.network.NetworkFactory.findDefault().createNetwork("diamond", "manual");
        network.setName("diamond");

        Substation subA = network.newSubstation().setId("A").add();
        Bus subA400 = createBus(subA, 400);
        Bus subA230 = createBus(subA, 230);
        createTransformer(subA400, subA230);

        Substation subB = network.newSubstation().setId("B").add();
        Bus subB230 = createBus(subB, 230);
        createLine(subA230, subB230);

        Substation subC = network.newSubstation().setId("C").add();
        Bus subC230 = createBus(subC, 230);
        Bus subC66 = createBus(subC, 66);
        Bus subC20 = createBus(subC, 20);
        createTransformer(subC230, subC66);
        createTransformer(subC66, subC20);
        createLine(subB230, subC230);

        Substation subD = network.newSubstation().setId("D").add();
        Bus subD66 = createBus(subD, 66);
        Bus subD10 = createBus(subD, 10);
        createTransformer(subD66, subD10);
        createLine(subC66, subD66);

        Substation subE = network.newSubstation().setId("E").add();
        Bus subE10 = createBus(subE, 10);
        createLine(subD10, subE10);

        Bus subF10 = createBus(network, "F", 10);
        Bus subG10 = createBus(network, "G", 10);
        Bus subH10 = createBus(network, "H", 10);
        Bus subI10 = createBus(network, "I", 10);
        Bus subJ10 = createBus(network, "J", 10);
        Bus subK10 = createBus(network, "K", 10);

        createLine(subE10, subF10);
        createLine(subF10, subG10);
        createLine(subG10, subH10);
        createLine(subH10, subD10);

        createLine(subF10, subI10);
        createLine(subI10, subJ10);
        createLine(subJ10, subK10);
        createLine(subK10, subD10);

        // HDVCLine between A 230 & B 230
        String vlFormat = "%s %.0f";
        String busIdFormat = "%s Bus";
        String vlId = String.format(vlFormat, subA.getId(), 230.0);
        String busId = String.format(busIdFormat, vlId);
        network.getVoltageLevel(vlId).newVscConverterStation()
                .setId(CONVERTER_1_ID)
                .setConnectableBus(busId)
                .setBus(busId)
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        vlId = String.format(vlFormat, subB.getId(), 230.0);
        busId = String.format(busIdFormat, vlId);
        network.getVoltageLevel(vlId).newVscConverterStation()
                .setId(CONVERTER_2_ID)
                .setConnectableBus(busId)
                .setBus(busId)
                .setLossFactor(0.011f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        network.newHvdcLine()
                .setId("HvdcLine (Vsc)")
                .setConverterStationId1(CONVERTER_1_ID)
                .setConverterStationId2(CONVERTER_2_ID)
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(280)
                .add();

        // Dangling Line
        vlId = String.format(vlFormat, subC.getId(), 66.0);
        busId = String.format(busIdFormat, vlId);
        subC66.getVoltageLevel().newDanglingLine()
                .setId("C66 - D")
                .setBus(busId)
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .setPairingKey("C66 -D- D66")
                .add();

        vlId = String.format(vlFormat, subD.getId(), 66.0);
        busId = String.format(busIdFormat, vlId);
        subD66.getVoltageLevel().newDanglingLine()
                .setId("D66 - D")
                .setBus(busId)
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .setPairingKey("C66 -D- D66")
                .add();

        // TieLine between A 230 & B 230
        String xnodeId = XNODE_1_ID;
        vlId = String.format(vlFormat, subA.getId(), 230.0);
        busId = String.format(busIdFormat, vlId);
        DanglingLine a230xnode1 = network.getVoltageLevel(vlId).newDanglingLine().setId("A230_XNODE1").setR(1.5).setX(20.0).setG(0.0).setB(1.93E-4).setP0(0).setQ0(0).setBus(busId).setPairingKey(xnodeId).add();
        vlId = String.format(vlFormat, subB.getId(), 230.0);
        busId = String.format(busIdFormat, vlId);
        String tieLineId = "A230_B230";
        DanglingLine xnode1b230 = network.getVoltageLevel(vlId).newDanglingLine().setId("XNODE1_B230").setR(1.5).setX(13.0).setG(0.0).setB(1.93E-4).setP0(0).setQ0(0).setBus(busId).setPairingKey(xnodeId).add();
        network.newTieLine().setId(tieLineId).setDanglingLine1(a230xnode1.getId()).setDanglingLine2(xnode1b230.getId()).add();

        return network;
    }

    public static Network createNetworkGroundDisconnectorOnLineNodeBreaker() {
        Network network = Network.create("testCaseGroundDisconnectorOnLineNB", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Substation substation2 = Networks.createSubstation(network, "s2", "s2", Country.FR);
        VoltageLevel vl2 = Networks.createVoltageLevel(substation2, "vl2", "vl2", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLine(network, "line", "line", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 2, 4, vl.getId(), vl2.getId(), "fn1", 1, ConnectablePosition.Direction.TOP, "fn2", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 1, 2);
        Networks.createSwitch(vl, "gd", "gd", SwitchKind.DISCONNECTOR, false, true, false, 2, 3);
        Networks.createGround(vl, "ground", 3);
        return network;
    }

    public static Network createNetworkGroundDisconnectorOnBusBarNodeBreaker() {
        Network network = Network.create("testCaseGroundDisconnectorOnBusBarNB", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createSwitch(vl, "gd", "gd", SwitchKind.DISCONNECTOR, false, true, false, 0, 1);
        Networks.createGround(vl, "ground", 1);
        return network;
    }

    public static Network createNetworkGroundDisconnectorOnLineBusBreaker() {
        Network network = Network.create("testCaseGroundDisconnectorOnLineBB", "test");
        Substation substation = Networks.createSubstation(network, "s1", "s1", Country.FR);
        VoltageLevel vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.BUS_BREAKER, 380);
        Substation substation2 = Networks.createSubstation(network, "s2", "s2", Country.FR);
        Networks.createVoltageLevel(substation2, "vl2", "vl2", TopologyKind.BUS_BREAKER, 380);
        Bus b1 = vl.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        Bus b2 = network.getVoltageLevel("vl2").getBusBreakerView().newBus()
                .setId("b2")
                .add();
        Bus b1g = vl.getBusBreakerView().newBus()
                .setId("b1g")
                .add();
        Networks.createLine(b1, b2);
        Networks.createGround(b1g);
        return network;
    }

    public static Network createNetworkGroundDisconnectorOnBusBarBusBreaker() {
        Network network = Network.create("testCaseGroundDisconnectorOnBusBarBB", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.BUS_BREAKER, 380);
        vl.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        Bus b1g = vl.getBusBreakerView().newBus()
                .setId("b1g")
                .add();
        Networks.createGround(b1g);
        return network;
    }

    public static Network createNetworkWithInternCellDifferentSubsections() {
        Network network = Network.create("testCaseOneLegInternCellOnDifferentSubsections", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel voltageLevel = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(voltageLevel, "1.1", "1.1", 0, 1, 1);
        Networks.createBusBarSection(voltageLevel, "1.2", "1.2", 1, 1, 2);
        Networks.createBusBarSection(voltageLevel, "1.3", "1.3", 2, 1, 3);

        Networks.createSwitch(voltageLevel, "d1", "d1", SwitchKind.DISCONNECTOR, false, true, false, 0, 11);
        Networks.createSwitch(voltageLevel, "d2", "d2", SwitchKind.DISCONNECTOR, false, true, false, 11, 2);

        return network;
    }

    public static Network createNetworkWithComplexInternCellDifferentSubsections1() {
        Network network = Network.create("testCaseComplexInternCellOnDifferentSubsections", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel voltageLevel = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);

        Networks.createBusBarSection(voltageLevel, "1.1", "1.1", 0, 1, 1);
        Networks.createBusBarSection(voltageLevel, "1.2", "1.2", 1, 2, 1);
        Networks.createBusBarSection(voltageLevel, "2.1", "2.1", 2, 1, 2);
        Networks.createLoad(voltageLevel, "load", "load", "load", null, ConnectablePosition.Direction.TOP, 3, 10d, 10d);
        Networks.createSwitch(voltageLevel, "dl11", "dl111", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        Networks.createSwitch(voltageLevel, "dl121", "dl12", SwitchKind.DISCONNECTOR, false, true, false, 1, 3);
        Networks.createSwitch(voltageLevel, "d11", "d11", SwitchKind.DISCONNECTOR, false, false, false, 0, 11);
        Networks.createSwitch(voltageLevel, "d12", "d12", SwitchKind.DISCONNECTOR, false, true, false, 1, 11);
        Networks.createSwitch(voltageLevel, "d21", "d21", SwitchKind.DISCONNECTOR, false, false, false, 2, 11);

        return network;
    }

    public static Network createNetworkWithComplexInternCellDifferentSubsections() {
        Network network = Network.create("testCaseComplexInternCellOnDifferentSubsections", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel voltageLevel = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);

        Networks.createBusBarSection(voltageLevel, "1.1", "1.1", 0, 1, 1);
        Networks.createBusBarSection(voltageLevel, "1.2", "1.2", 1, 2, 1);
        Networks.createBusBarSection(voltageLevel, "2.1", "2.1", 2, 1, 2);

        Networks.createLoad(voltageLevel, LOAD_1_ID, LOAD_1_ID, LOAD_1_ID, null, ConnectablePosition.Direction.TOP, 3, 10d, 10d);
        Networks.createSwitch(voltageLevel, "dl1", "dl1", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        Networks.createLoad(voltageLevel, LOAD_2_ID, LOAD_2_ID, LOAD_2_ID, null, ConnectablePosition.Direction.TOP, 4, 10d, 10d);
        Networks.createSwitch(voltageLevel, "dl2", "dl2", SwitchKind.DISCONNECTOR, false, false, false, 2, 4);

        Networks.createSwitch(voltageLevel, "d11", "d11", SwitchKind.DISCONNECTOR, false, true, false, 0, 11);
        Networks.createSwitch(voltageLevel, "d12", "d12", SwitchKind.DISCONNECTOR, false, false, false, 1, 11);
        Networks.createSwitch(voltageLevel, "d21", "d21", SwitchKind.DISCONNECTOR, false, false, false, 2, 11);

        return network;
    }

    /**
     * <pre>
     *     vl1  vl2
     *     |    |
     * L1  |    |  L2
     *     |    |
     *     --*---  Fictitious busbar section
     *       |
     *   L3  |
     *       |
     *       vl3
     *
     * </pre>
     */
    public static Network createTeePointNetwork() {
        Network network = Network.create("testCase1", "test");
        VoltageLevel vl = network.newVoltageLevel()
                .setId("vl")
                .setNominalV(50)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        VoltageLevel vl1 = network.newVoltageLevel()
                .setId("vl1")
                .setNominalV(10)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        VoltageLevel vl2 = network.newVoltageLevel()
                .setId("vl2")
                .setNominalV(30)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        VoltageLevel vl3 = network.newVoltageLevel()
                .setId("vl3")
                .setNominalV(10)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        createInternalConnection(vl, 1, 0);
        createInternalConnection(vl, 2, 0);
        createInternalConnection(vl, 3, 0);

        createLine(network, "L1", "L1", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                1, 10, vl.getId(), vl1.getId(),
                "L1", 0, ConnectablePosition.Direction.TOP,
                "L1", 1, ConnectablePosition.Direction.TOP);

        createLine(network, "L2", "L2", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                2, 20, vl.getId(), vl2.getId(),
                "L2", 1, ConnectablePosition.Direction.BOTTOM,
                "L2", 0, ConnectablePosition.Direction.TOP);

        createLine(network, "L3", "L3", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                3, 30, vl.getId(), vl3.getId(),
                "L3", 2, ConnectablePosition.Direction.TOP,
                "L3", 0, ConnectablePosition.Direction.TOP);

        return network;
    }

    public static Network createDanglingConnectablesNetwork() {
        Network network = Network.create("testDLoad", "testDLoad");
        VoltageLevel vl = network.newVoltageLevel()
                .setId("vl")
                .setNominalV(50)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        createBusBarSection(vl, "bbs1", 0, 1, 1);
        createBusBarSection(vl, "bbs2", 1, 1, 2);
        createSwitch(vl, "d", SwitchKind.DISCONNECTOR, true, false, false, 0, 2);
        createSwitch(vl, "d12", SwitchKind.DISCONNECTOR, true, false, false, 0, 1);
        createSwitch(vl, "ddl2", SwitchKind.DISCONNECTOR, true, false, false, 4, 5);
        createLoad(vl, "load", 1, ConnectablePosition.Direction.TOP, 2, 0, 0);
        createLoad(vl, "dLoad1", 2, ConnectablePosition.Direction.BOTTOM, 3, 0, 0);
        createLoad(vl, "dLoad2", 0, ConnectablePosition.Direction.TOP, 4, 10, 0);
        createGenerator(vl, "dGen", null, ConnectablePosition.Direction.TOP, 5, 50, 100, false, 100, 400);
        return network;
    }

    public static void createLine(Bus bus1, Bus bus2) {
        String id = String.format("%s - %s",
                bus1.getVoltageLevel().getSubstation().orElseThrow().getId(),
                bus2.getVoltageLevel().getSubstation().orElseThrow().getId());
        bus1.getNetwork().newLine().setId(id)
                .setR(0.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setVoltageLevel1(bus1.getVoltageLevel().getId())
                .setVoltageLevel2(bus2.getVoltageLevel().getId())
                .setConnectableBus1(bus1.getId())
                .setConnectableBus2(bus2.getId())
                .setBus1(bus1.getId())
                .setBus2(bus2.getId())
                .add();
    }

    public static Bus createBus(Network network, String substationId, double nominalVoltage) {
        Substation substation = network.newSubstation().setId(substationId).add();
        return createBus(substation, nominalVoltage);
    }

    public static Bus createBus(Substation substation, double nominalVoltage) {
        String vlId = String.format("%s %.0f", substation.getId(), nominalVoltage);
        String busId = String.format("%s %s", vlId, "Bus");
        return substation.newVoltageLevel()
                .setId(vlId)
                .setNominalV(nominalVoltage)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add()
                .getBusBreakerView()
                .newBus()
                .setId(busId)
                .add();
    }

    public static void createTransformer(Bus bus1, Bus bus2) {
        Substation substation = bus1.getVoltageLevel().getSubstation().orElseThrow();
        String id = String.format("%s %.0f %.0f",
                substation.getId(),
                bus1.getVoltageLevel().getNominalV(),
                bus2.getVoltageLevel().getNominalV());
        substation.newTwoWindingsTransformer().setId(id)
                .setR(0.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setVoltageLevel1(bus1.getVoltageLevel().getId())
                .setVoltageLevel2(bus2.getVoltageLevel().getId())
                .setConnectableBus1(bus1.getId())
                .setConnectableBus2(bus2.getId())
                .setRatedU1(bus1.getVoltageLevel().getNominalV())
                .setRatedU2(bus2.getVoltageLevel().getNominalV())
                .setBus1(bus1.getId())
                .setBus2(bus2.getId())
                .add();
    }

    public static void createGround(VoltageLevel vl, String id, int node) {
        vl.newGround()
                .setId(id)
                .setNode(node)
                .setEnsureIdUnicity(true)
                .add();
    }

    public static void createGround(Bus bus) {
        VoltageLevel voltageLevel = bus.getVoltageLevel();
        String id = String.format("%s %s", voltageLevel.getId(), bus.getId());
        voltageLevel.newGround()
                .setId(id)
                .setBus(bus.getId())
                .setEnsureIdUnicity(true)
                .add();
    }
}
