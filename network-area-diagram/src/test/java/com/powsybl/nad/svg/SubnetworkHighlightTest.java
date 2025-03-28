/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

class SubnetworkHighlightTest extends AbstractTest {

    protected java.nio.file.FileSystem fileSystem;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800)
                .setHighlightGraph(true));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters()) {
        };
    }

    @Test
    void testSubnetworkHighlight() {
        Network network = createWithTieLines();
        assertSvgEquals("/subnetwork_highlight.svg", network);
    }

    public static Network createWithTieLines() {
        Network network = createWithLFResults(NetworkFactory.findDefault());
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).remove();
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_2).remove();

        DanglingLine nhv1xnode1 = network.getVoltageLevel(EurostagTutorialExample1Factory.VLHV1)
                .newDanglingLine()
                .setId(EurostagTutorialExample1Factory.DANGLING_LINE_XNODE1_1)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(20.0)
                .setG(1E-6)
                .setB(386E-6 / 2)
                .setPairingKey(EurostagTutorialExample1Factory.XNODE_1)
                .setBus(EurostagTutorialExample1Factory.NHV1)
                .add();
        DanglingLine xnode1nhv2 = network.getVoltageLevel(EurostagTutorialExample1Factory.VLHV2)
                .newDanglingLine()
                .setId(EurostagTutorialExample1Factory.DANGLING_LINE_XNODE1_2)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(13.0)
                .setG(2E-6)
                .setB(386E-6 / 2)
                .setBus("NHV2")
                .setPairingKey(EurostagTutorialExample1Factory.XNODE_1)
                .add();
        network.newTieLine()
                .setId(EurostagTutorialExample1Factory.NHV1_NHV2_1)
                .setDanglingLine1(nhv1xnode1.getId())
                .setDanglingLine2(xnode1nhv2.getId())
                .add();
        network.getVoltageLevel(EurostagTutorialExample1Factory.VLHV1)
                .newDanglingLine()
                .setId(EurostagTutorialExample1Factory.DANGLING_LINE_XNODE2_1)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(20.0)
                .setG(1E-6)
                .setB(386E-6 / 2)
                .setBus(EurostagTutorialExample1Factory.NHV1)
                .setPairingKey("XNODE2")
                .add();
        network.getVoltageLevel(EurostagTutorialExample1Factory.VLHV2)
                .newDanglingLine()
                .setId(EurostagTutorialExample1Factory.DANGLING_LINE_XNODE2_2)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(13.0)
                .setG(2E-6)
                .setB(386E-6 / 2)
                .setBus("NHV2")
                .setPairingKey("XNODE2")
                .add();

        network.getTieLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getDanglingLine1().getTerminal()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);

        network.getTieLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getDanglingLine2().getTerminal()
                .setP(-300.43389892578125)
                .setQ(-137.18849182128906);

        return network;
    }

    public static Network createWithLFResults(NetworkFactory factory) {
        Network network = createwith3wt(factory);
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        network.getBusBreakerView().getBus(EurostagTutorialExample1Factory.NGEN)
                .setV(24.500000610351563)
                .setAngle(2.3259763717651367);
        network.getBusBreakerView().getBus(EurostagTutorialExample1Factory.NHV1)
                .setV(402.1428451538086)
                .setAngle(0.0);
        network.getBusBreakerView().getBus("NHV2")
                .setV(389.9526763916016)
                .setAngle(-3.5063576698303223);
        network.getBusBreakerView().getBus("NLOAD")
                .setV(147.57861328125)
                .setAngle(-9.614486694335938);

        network.getBusBreakerView().getBus("BUS_3")
                .setV(197.57861328125)
                .setAngle(3.0);

        network.getBusBreakerView().getBus("BUS_4")
                .setV(197.57861328125)
                .setAngle(3.0);

        network.getBusBreakerView().getBus("BUS_5")
                .setV(197.57861328125)
                .setAngle(3.0);

        network.getGenerator("GEN").getTerminal()
                .setP(-605.558349609375)
                .setQ(-225.2825164794922);
        network.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NGEN_NHV1).getTerminal1()
                .setP(605.558349609375)
                .setQ(225.2825164794922);
        network.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NGEN_NHV1).getTerminal2()
                .setP(-604.8909301757812)
                .setQ(-197.48046875);
        network.getLoad("LOAD").getTerminal()
                .setP(600.0)
                .setQ(200.0);
        network.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NHV2_NLOAD).getTerminal1()
                .setP(600.8677978515625)
                .setQ(274.3769836425781);
        network.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NHV2_NLOAD).getTerminal2()
                .setP(-600.0)
                .setQ(-200.0);
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal2()
                .setP(-300.43389892578125)
                .setQ(-137.18849182128906);
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_2).getTerminal1()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_2).getTerminal2()
                .setP(-300.43389892578125)
                .setQ(-137.188491821289060);

        return network;
    }

    public static Network createwith3wt(NetworkFactory networkFactory) {
        Network network0 = networkFactory.createNetwork("sim1", "test");
        Network network1 = network0.createSubnetwork("sub1", "subnetwork1", "test");
        Network network2 = network0.createSubnetwork("sub2", "subnetwork2", "test");
        Network network3 = network0.createSubnetwork("sub3", "subnetwork3", "test");

        Substation p1 = network1.newSubstation()
                .setId("P1")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
        Substation p2 = network2.newSubstation()
                .setId("P2")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("B")
                .add();
        Substation p3 = network3.newSubstation()
                .setId("P3")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("B")
                .add();
        VoltageLevel vlgen = p1.newVoltageLevel()
                .setId(EurostagTutorialExample1Factory.VLGEN)
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlhv1 = p1.newVoltageLevel()
                .setId(EurostagTutorialExample1Factory.VLHV1)
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlhv2 = p2.newVoltageLevel()
                .setId(EurostagTutorialExample1Factory.VLHV2)
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlload = p2.newVoltageLevel()
                .setId(EurostagTutorialExample1Factory.VLLOAD)
                .setNominalV(150.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus ngen = vlgen.getBusBreakerView().newBus()
                .setId(EurostagTutorialExample1Factory.NGEN)
                .add();
        Bus nhv1 = vlhv1.getBusBreakerView().newBus()
                .setId(EurostagTutorialExample1Factory.NHV1)
                .add();
        Bus nhv2 = vlhv2.getBusBreakerView().newBus()
                .setId("NHV2")
                .add();
        Bus nload = vlload.getBusBreakerView().newBus()
                .setId("NLOAD")
                .add();

        VoltageLevel vlhv3 = p3.newVoltageLevel()
                .setId("VLHV3")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlhv4 = p3.newVoltageLevel()
                .setId("VLHV4")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlhv5 = p3.newVoltageLevel()
                .setId("VLHV5")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bus3 = vlhv3.getBusBreakerView().newBus()
                .setId("BUS_3")
                .add();
        bus3.setV(197.57861328125).setAngle(3.0);

        Bus bus4 = vlhv4.getBusBreakerView().newBus()
                .setId("BUS_4")
                .add();
        bus4.setV(197.57861328125).setAngle(3.0);

        Bus bus5 = vlhv5.getBusBreakerView().newBus()
                .setId("BUS_5")
                .add();
        bus5.setV(197.57861328125).setAngle(3.0);

        network0.newLine()
                .setId(EurostagTutorialExample1Factory.NHV1_NHV2_1)
                .setVoltageLevel1(vlhv1.getId())
                .setBus1(nhv1.getId())
                .setConnectableBus1(nhv1.getId())
                .setVoltageLevel2(vlhv2.getId())
                .setBus2(nhv2.getId())
                .setConnectableBus2(nhv2.getId())
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        network0.newLine()
                .setId(EurostagTutorialExample1Factory.NHV1_NHV2_2)
                .setVoltageLevel1(vlhv1.getId())
                .setBus1(nhv1.getId())
                .setConnectableBus1(nhv1.getId())
                .setVoltageLevel2(vlhv2.getId())
                .setBus2(nhv2.getId())
                .setConnectableBus2(nhv2.getId())
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();

        network0.newLine()
                .setId("NHV2_NHV2_3")
                .setVoltageLevel1(vlhv2.getId())
                .setBus1(nhv2.getId())
                .setConnectableBus1(nhv2.getId())
                .setVoltageLevel2(vlhv3.getId())
                .setBus2(bus3.getId())
                .setConnectableBus2(bus3.getId())
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        int zb380 = 380 * 380 / 100;
        p1.newTwoWindingsTransformer()
                .setId(EurostagTutorialExample1Factory.NGEN_NHV1)
                .setVoltageLevel1(vlgen.getId())
                .setBus1(ngen.getId())
                .setConnectableBus1(ngen.getId())
                .setRatedU1(24.0)
                .setVoltageLevel2(vlhv1.getId())
                .setBus2(nhv1.getId())
                .setConnectableBus2(nhv1.getId())
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
        int zb150 = 150 * 150 / 100;
        TwoWindingsTransformer nhv2Nload = p2.newTwoWindingsTransformer()
                .setId(EurostagTutorialExample1Factory.NHV2_NLOAD)
                .setVoltageLevel1(vlhv2.getId())
                .setBus1(nhv2.getId())
                .setConnectableBus1(nhv2.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vlload.getId())
                .setBus2(nload.getId())
                .setConnectableBus2(nload.getId())
                .setRatedU2(158.0)
                .setR(0.21 / 1000 * zb150)
                .setX(Math.sqrt(18 * 18 - 0.21 * 0.21) / 1000 * zb150)
                .setG(0.0)
                .setB(0.0)
                .add();
        double a = (158.0 / 150.0) / (400.0 / 380.0);
        nhv2Nload.newRatioTapChanger()
                .beginStep()
                .setRho(0.85f * a)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(a)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1.15f * a)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(true)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(158.0)
                .setTargetDeadband(0)
                .setRegulationTerminal(nhv2Nload.getTerminal2())
                .add();
        vlload.newLoad()
                .setId("LOAD")
                .setBus(nload.getId())
                .setConnectableBus(nload.getId())
                .setP0(600.0)
                .setQ0(200.0)
                .add();
        Generator generator = vlgen.newGenerator()
                .setId("GEN")
                .setBus(ngen.getId())
                .setConnectableBus(ngen.getId())
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();
        generator.newMinMaxReactiveLimits()
                .setMinQ(-9999.99)
                .setMaxQ(9999.99)
                .add();

        p3.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(132.0)
                .newLeg1()
                .setR(17.424)
                .setX(1.7424)
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setRatedU(132.0)
                .setVoltageLevel(vlhv5.getId())
                .setBus(bus5.getId())
                .add()
                .newLeg2()
                .setR(1.089)
                .setX(0.1089)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(33.0)
                .setVoltageLevel(vlhv3.getId())
                .setBus(bus3.getId())
                .add()
                .newLeg3()
                .setR(0.121)
                .setX(0.0121)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(11.0)
                .setVoltageLevel(vlhv4.getId())
                .setBus(bus4.getId())
                .add()
                .add();

        VscConverterStation cs1 = vlhv3.newVscConverterStation()
                .setId("C1")
                .setName("Converter1")
                .setConnectableBus("BUS_3")
                .setBus("BUS_3")
                .setLossFactor(1.1f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        cs1.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        cs1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(5.0)
                .setMinQ(0.0)
                .setMaxQ(10.0)
                .endPoint()
                .beginPoint()
                .setP(10.0)
                .setMinQ(0.0)
                .setMaxQ(10.0)
                .endPoint()
                .add();
        VscConverterStation cs2 = vlload.newVscConverterStation()
                .setId("C2")
                .setName("Converter2")
                .setBus(nload.getId())
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .setRegulatingTerminal(cs1.getTerminal())
                .add();
        cs2.newMinMaxReactiveLimits()
                .setMinQ(0.0)
                .setMaxQ(10.0)
                .add();

        network0.newHvdcLine()
            .setId("L")
            .setName("HVDC")
            .setConverterStationId1("C1")
            .setConverterStationId2("C2")
            .setR(1)
            .setNominalV(400)
            .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
            .setMaxP(300.0)
            .setActivePowerSetpoint(280)
            .add();
        return network0;
    }
}
