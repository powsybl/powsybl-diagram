/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.iidm.CustomTopologicalStyleProvider;
import com.powsybl.sld.svg.styles.iidm.CustomTopologicalStyleProvider.CustomStyle;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class CustomTopologicalStyleProviderTest extends AbstractTestCaseIidm {

    public static final CustomStyle DEMO_CUSTOM_STYLE = new CustomStyle("orange", "4px", "2px", null);
    public static final CustomStyle DEMO_CUSTOM_STYLE_DASH = new CustomStyle("orange", "4px", "2px", "2, 2");

    SldComponentLibrary componentLibrary;

    @BeforeEach
    public void setUp() {
        componentLibrary = new ConvergenceComponentLibrary();
    }

    private static SvgParameters createSvgParameters() {
        return new SvgParameters()
                .setFeederInfosIntraMargin(10)
                .setUseName(true)
                .setSvgWidthAndHeightAdded(true)
                .setCssLocation(SvgParameters.CssLocation.INSERTED_IN_SVG)
                .setFeederInfosOuterMargin(20)
                .setDrawStraightWires(false)
                .setShowGrid(false)
                .setShowInternalNodes(false)
                .setFeederInfoSymmetry(true);
    }

    private static Network createTestNetwork() {
        Network network = Network.create("testCaseBusNodeDisconnected", "testCaseBusNodeDisconnected");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);

        // bus breaker topology
        VoltageLevel vlBb = Networks.createVoltageLevel(substation, "vlBb", "vlBb", TopologyKind.BUS_BREAKER, 225);
        Bus b1 = vlBb.getBusBreakerView().newBus().setId("b1").add();
        Bus b2 = vlBb.getBusBreakerView().newBus().setId("b2").add();
        Bus b3 = vlBb.getBusBreakerView().newBus().setId("b3").add();
        vlBb.getBusBreakerView().newSwitch().setId("s12").setBus1(b1.getId()).setBus2(b2.getId()).add();
        vlBb.getBusBreakerView().newSwitch().setId("s23").setOpen(true).setBus1(b2.getId()).setBus2(b3.getId()).add();

        // node breaker topology
        VoltageLevel vlNb = Networks.createVoltageLevel(substation, "vlNb", "vlNb", TopologyKind.NODE_BREAKER, 225);
        vlNb.getNodeBreakerView().newBusbarSection().setId("bbs1").setNode(0).add();
        vlNb.getNodeBreakerView().newBusbarSection().setId("bbs2").setNode(1).add();
        vlNb.getNodeBreakerView().newSwitch().setId("sLine").setKind(SwitchKind.DISCONNECTOR).setNode1(0).setNode2(2).add();
        vlNb.getNodeBreakerView().newSwitch().setId("sLoad").setOpen(false).setKind(SwitchKind.DISCONNECTOR).setNode1(1).setNode2(3).add();
        vlNb.newLoad().setNode(3).setId("load").setP0(10).setQ0(10).add();

        network.newLine().setId("line")
                .setG1(0).setB1(0).setVoltageLevel1(vlBb.getId()).setBus1(b2.getId())
                .setG2(0).setB2(0).setVoltageLevel2(vlNb.getId()).setNode2(2)
                .setR(1).setX(1)
                .add();

        return network;
    }

    private static Network createTestNetwork2() {
        Network network = createTestNetwork();
        network.getSwitch("s23").setOpen(false);
        return network;
    }

    public static Network createTestNetwork3() {
        return Networks.createBusBreakerNetworkWithInternalBranches("TestInternalBranchesBusBreaker", "test");
    }

    public static Network createTestNetwork4() {
        Network network = createTestNetwork3();
        network.getSwitch("BR1").setOpen(false);
        return network;
    }

    public static Network createTestNetwork5() {
        Network network = Networks.createNetworkWithInternalPstAndBranchStatus();
        network.getSwitch("dpst").setOpen(false);
        return network;
    }

    public static Network createTestNetwork6() {
        Network network = Networks.createNetworkWithSvcVscScDl();
        network.getSwitch("bt").setOpen(true);
        return network;
    }

    public static Network createTestNetwork7() {
        Network network = Network.create("testCaseLoadBreakSwitch", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs2", "bbs2", 1, 2, 2);
        Networks.createGenerator(vl, "G", "G", "G", 0, ConnectablePosition.Direction.TOP, 2, 50, 100, false, 100, 400);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.BOTTOM, 3, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.LOAD_BREAK_SWITCH, false, false, false, 0, 2);
        Networks.createSwitch(vl, "b", "b", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 1, 3);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 0, 1);

        Networks.createTwoWindingsTransformer(substation, "T11", "T11", 250, 100, 52, 12, 65, 90,
                4, 6, vl.getId(), vl.getId(),
                "T11", null, ConnectablePosition.Direction.TOP,
                "T11", null, ConnectablePosition.Direction.BOTTOM);
        Networks.createSwitch(vl, "b2", "b2", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 0, 4);
        Networks.createSwitch(vl, "b3", "b3", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 1, 5);
        Networks.createSwitch(vl, "b4", "b4", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 5, 6);
        Networks.createSwitch(vl, "b5", "b5", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 5, 3);
        return network;
    }

    private static void addPhaseTapChanger(ThreeWindingsTransformer.Leg leg) {
        leg.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(leg.getTerminal())
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulating(false)
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

    public static Network createTestNetwork8() {
        Network network = Network.create("testCase1", "test");
        Substation substation = network.newSubstation().setId("s").setCountry(Country.FR).add();

        // first voltage level
        VoltageLevel vl1 = Networks.createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        Networks.createLoad(vl1, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl1, "d", "d", SwitchKind.DISCONNECTOR, true, false, false, 0, 1);
        Networks.createSwitch(vl1, "b", "b", SwitchKind.BREAKER, true, false, false, 1, 2);

        // second voltage level
        VoltageLevel vl2 = Networks.createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225);
        Networks.createBusBarSection(vl2, "bbs2", "bbs2", 0, 1, 1);

        // third voltage level
        VoltageLevel vl3 = Networks.createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 63);
        Networks.createBusBarSection(vl3, "bbs3", "bbs3", 0, 1, 1);

        // 2WT between first and second voltage level
        Networks.createTwoWindingsTransformer(substation, "2WT", "2WT", 1, 1, 1, 1, 1, 1,
                3, 1, vl1.getId(), vl2.getId(),
                "2WT_1", 1, ConnectablePosition.Direction.TOP,
                "2WT_2", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl1, "d2WT_1", "d2WT_1", SwitchKind.DISCONNECTOR, true, false, true, 0, 4);
        Networks.createSwitch(vl1, "b2WT_1", "b2WT_1", SwitchKind.BREAKER, true, false, true, 3, 4);
        Networks.createSwitch(vl2, "d2WT_2", "d2WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        Networks.createSwitch(vl2, "b2WT_2", "b2WT_2", SwitchKind.BREAKER, true, true, true, 1, 2);

        // 3WT between the 3 voltage levels
        Networks.createThreeWindingsTransformer(substation, "3WT", "3WT", vl1.getId(), vl2.getId(), vl3.getId(),
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                5, 3, 1,
                "3WT_1", 2, ConnectablePosition.Direction.TOP,
                "3WT_2", 1, ConnectablePosition.Direction.TOP,
                "3WT_3", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl1, "d3WT_1", "d3WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        Networks.createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, false, true, 5, 6);
        Networks.createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        Networks.createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, false, true, 3, 4);
        Networks.createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, false, 0, 2);
        Networks.createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, false, false, 1, 2);
        return network;
    }

    private static List<Arguments> provideTestData() {
        SvgParameters svgParameters1 = createSvgParameters();

        Network network1 = createTestNetwork();

        StyleProvider defaultStyleprovider = new TopologicalStyleProvider(network1, svgParameters1);

        StyleProvider sp0ForNetwork1 = new CustomTopologicalStyleProvider(network1, svgParameters1,
                Map.of("DOES_NOT_EXIST", new CustomStyle("yellow", null, null, null)));

        StyleProvider sp1ForNetwork1 = new CustomTopologicalStyleProvider(network1, svgParameters1,
                Map.of("b1", DEMO_CUSTOM_STYLE));

        StyleProvider sp2ForNetwork1 = new CustomTopologicalStyleProvider(network1, svgParameters1,
                Map.of("b1", DEMO_CUSTOM_STYLE_DASH));

        Network network2 = createTestNetwork2();

        StyleProvider soForNetwork2 = new CustomTopologicalStyleProvider(network2, svgParameters1,
                Map.of("bbs2", DEMO_CUSTOM_STYLE));

        Network network3 = createTestNetwork3();

        StyleProvider sp1ForNetwork3 = new CustomTopologicalStyleProvider(network3, svgParameters1,
                Map.of("B11", DEMO_CUSTOM_STYLE));

        StyleProvider sp2ForNetwork3 = new CustomTopologicalStyleProvider(network3, svgParameters1,
                Map.of("B12", DEMO_CUSTOM_STYLE));

        Network network4 = createTestNetwork4();

        Network network5 = createTestNetwork5();
        StyleProvider spForNetwork5 = new CustomTopologicalStyleProvider(network5, svgParameters1,
                Map.of("bbs1", DEMO_CUSTOM_STYLE));

        Network network6 = createTestNetwork6();
        StyleProvider spForNetwork6 = new CustomTopologicalStyleProvider(network6, svgParameters1,
                Map.of("bbs", DEMO_CUSTOM_STYLE));

        Network network7 = createTestNetwork7();
        StyleProvider spForNetwork7 = new CustomTopologicalStyleProvider(network7, svgParameters1,
                Map.of("bbs", DEMO_CUSTOM_STYLE));

        Network network8 = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer twt = network8.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg1());
        addPhaseTapChanger(twt.getLeg2());
        addPhaseTapChanger(twt.getLeg3());

        StyleProvider spForNetwork8 = new CustomTopologicalStyleProvider(network8, svgParameters1,
                Map.of("BUS_132", DEMO_CUSTOM_STYLE));

        return List.of(
                Arguments.of(network1, "vlBb", defaultStyleprovider, svgParameters1, "/CustomStyleProviderTest0.svg"),
                Arguments.of(network1, "vlBb", sp0ForNetwork1, svgParameters1, "/CustomStyleProviderTest0.svg"),
                Arguments.of(network1, "vlBb", sp1ForNetwork1, svgParameters1, "/CustomStyleProviderTest1.svg"),
                Arguments.of(network2, "vlBb", sp1ForNetwork1, svgParameters1, "/CustomStyleProviderTest2.svg"),
                Arguments.of(network2, "vlBb", sp2ForNetwork1, svgParameters1, "/CustomStyleProviderTest3.svg"),
                Arguments.of(network2, "vlNb", soForNetwork2, svgParameters1, "/CustomStyleProviderTest4.svg"),
                Arguments.of(network3, "VL1", sp1ForNetwork3, svgParameters1, "/CustomStyleProviderTest5.svg"),
                Arguments.of(network3, "VL1", sp2ForNetwork3, svgParameters1, "/CustomStyleProviderTest6.svg"),
                Arguments.of(network4, "VL1", sp2ForNetwork3, svgParameters1, "/CustomStyleProviderTest7.svg"),
                Arguments.of(network5, "vl1", spForNetwork5, svgParameters1, "/CustomStyleProviderTest8.svg"),
                Arguments.of(network6, "vl", spForNetwork6, svgParameters1, "/CustomStyleProviderTest9.svg"),
                Arguments.of(network7, "vl", spForNetwork7, svgParameters1, "/CustomStyleProviderTest10.svg"),
                Arguments.of(network8, "VL_132", spForNetwork8, svgParameters1, "/CustomStyleProviderTest11.svg")
        );
    }

    @ParameterizedTest(name = "{4}")
    @MethodSource("provideTestData")
    void test(Network network,
              String vlId,
              StyleProvider styleProvider,
              SvgParameters svgParameters,
              String resourceName) {
        assertNotNull(network);
        assertNotNull(vlId);
        assertNotNull(styleProvider);
        assertNotNull(resourceName);

        this.network = network;

        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vlId);
        voltageLevelGraphLayout(g);

        assertEquals(toString(resourceName), toSVG(g, resourceName, componentLibrary, layoutParameters, svgParameters,
                getDefaultDiagramLabelProvider(), styleProvider, getDefaultSVGLegendWriter()));
    }

    private static List<Arguments> provideSubstationTestData() {
        SvgParameters svgParameters1 = createSvgParameters();

        Network network1 = ThreeWindingsTransformerNetworkFactory.create();
        StyleProvider spForNetwork1 = new CustomTopologicalStyleProvider(network1, svgParameters1,
                Map.of("BUS_11", DEMO_CUSTOM_STYLE));

        Network network2 = createTestNetwork8();
        StyleProvider spForNetwork2 = new CustomTopologicalStyleProvider(network2, svgParameters1,
                Map.of("bbs2", DEMO_CUSTOM_STYLE));

        Network network3 = createTestNetwork3();
        StyleProvider sp1ForNetwork3 = new CustomTopologicalStyleProvider(network3, svgParameters1,
                Map.of("B11", DEMO_CUSTOM_STYLE));

        return List.of(
                Arguments.of(network1, "SUBSTATION", spForNetwork1, svgParameters1, "/CustomStyleProviderTest12.svg"),
                Arguments.of(network2, "s", spForNetwork2, svgParameters1, "/CustomStyleProviderTest13.svg"),
                Arguments.of(network3, "S1", sp1ForNetwork3, svgParameters1, "/CustomStyleProviderTest14.svg")
        );
    }

    @ParameterizedTest(name = "{4}")
    @MethodSource("provideSubstationTestData")
    void testSubstation(Network network,
              String subId,
              StyleProvider styleProvider,
              SvgParameters svgParameters,
              String resourceName) {
        assertNotNull(network);
        assertNotNull(subId);
        assertNotNull(styleProvider);
        assertNotNull(svgParameters);
        assertNotNull(resourceName);

        this.network = network;

        graphBuilder = new NetworkGraphBuilder(network);
        SubstationGraph g = graphBuilder.buildSubstationGraph(subId);
        substationGraphLayout(g);

        assertEquals(toString(resourceName), toSVG(g, resourceName, componentLibrary, layoutParameters, svgParameters,
                getDefaultDiagramLabelProvider(), styleProvider, getDefaultSVGLegendWriter()));
    }

}
