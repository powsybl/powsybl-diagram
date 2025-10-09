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
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.NodeSide;
import com.powsybl.sld.svg.CustomLabelProvider.FeederContext;
import com.powsybl.sld.svg.CustomLabelProvider.CustomFeederInfos;
import com.powsybl.sld.svg.CustomLabelProvider.CustomLabels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.powsybl.sld.library.SldComponentTypeName.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class CustomLabelProviderTest extends AbstractTestCaseIidm {

    SldComponentLibrary componentLibrary;

    @BeforeEach
    public void setUp() {
        componentLibrary = new ConvergenceComponentLibrary();
    }

    @ParameterizedTest(name = "{5}")
    @MethodSource("provideTestData")
    void test(Network network, VoltageLevel vl,
              Map<String, CustomLabels> labels,
              Map<FeederContext, List<CustomFeederInfos>> feederInfosData,
              SvgParameters svgParameters,
              String resourceName) {
        assertNotNull(network);
        assertNotNull(vl);
        assertNotNull(labels);
        assertNotNull(feederInfosData);
        assertNotNull(resourceName);

        this.network = network;

        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(g);

        LabelProvider labelProvider = new CustomLabelProvider(labels, feederInfosData, componentLibrary, layoutParameters, svgParameters);

        assertEquals(toString(resourceName), toSVG(g, resourceName, componentLibrary, layoutParameters, svgParameters,
                labelProvider, getDefaultDiagramStyleProvider()));
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
        Network network = Network.create("testCase1", "test");
        Substation substation = Networks.createSubstation(network, "s", "s", Country.FR);
        VoltageLevel vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
        return network;
    }

    private static List<Arguments> provideTestData() {

        Map<String, List<CustomFeederInfos>> feederInfosData = new HashMap<>();

        //test data1
        Network network1 = Networks.createNetworkGroundDisconnector();
        VoltageLevel vl1 = network1.getVoltageLevel("vl");
        assertNotNull(vl1);

        Map<String, CustomLabels> labels1 = new HashMap<>();
        labels1.put("bbs", new CustomLabels("C_BBS1"));
        labels1.put("line", new CustomLabels("C_LINE"));
        labels1.put("load", new CustomLabels("C_LOAD"));
        labels1.put("ground", new CustomLabels("C_GROUND"));
        labels1.put("d1", new CustomLabels("C_D1"));
        labels1.put("b1", new CustomLabels("C_B1"));
        labels1.put("gd1", new CustomLabels("C_GD1"));
        labels1.put("d2", new CustomLabels("C_D2"));
        labels1.put("b2", new CustomLabels("C_B2"));
        labels1.put("gd2", new CustomLabels("C_GD2"));
        labels1.put("gd", new CustomLabels("C_GD"));
        SvgParameters svgParameters1 = createSvgParameters().setDisplayEquipmentNodesLabel(true);

        //test data2
        Network network2 = Networks.createNodeBreakerNetworkWithInternalBranches("TestInternalBranchesNodeBreaker", "test");
        VoltageLevel vl2 = network2.getVoltageLevel("VL1");

        Map<String, CustomLabels> labels2 = new HashMap<>();
        labels2.put("BBS11", new CustomLabels("C_BBS11"));
        labels2.put("BBS12", new CustomLabels("C_BBS12"));
        labels2.put("BR1", new CustomLabels("C_BR1"));
        labels2.put("BR12", new CustomLabels("C_BR12"));
        labels2.put("BR14", new CustomLabels("C_BR14"));
        labels2.put("BR16", new CustomLabels("C_BR16"));
        labels2.put("BR18", new CustomLabels("C_BR18"));
        labels2.put("BR20", new CustomLabels("C_BR20"));
        labels2.put("BR22", new CustomLabels("C_BR22"));
        labels2.put("BR24", new CustomLabels("C_BR24"));
        labels2.put("BR26", new CustomLabels("C_BR26"));
        labels2.put("BR28", new CustomLabels("C_BR28"));
        labels2.put("BR30", new CustomLabels("C_BR30"));
        labels2.put("D10", new CustomLabels("C_D10"));
        labels2.put("D11", new CustomLabels("C_D11"));
        labels2.put("D13", new CustomLabels("C_D13"));
        labels2.put("D15", new CustomLabels("C_D15"));
        labels2.put("D17", new CustomLabels("C_D17"));
        labels2.put("D19", new CustomLabels("C_D19"));
        labels2.put("D20", new CustomLabels("C_D20"));
        labels2.put("D21", new CustomLabels("C_D21"));
        labels2.put("D23", new CustomLabels("C_D23"));
        labels2.put("D25", new CustomLabels("C_D25"));
        labels2.put("D27", new CustomLabels("C_D27"));
        labels2.put("D29", new CustomLabels("C_D29"));
        labels2.put("G", new CustomLabels("C_G"));
        labels2.put("L1", new CustomLabels("C_L1"));
        labels2.put("L11", new CustomLabels("C_L11"));
        labels2.put("L12", new CustomLabels("C_L12"));
        labels2.put("T11", new CustomLabels("C_T11"));
        labels2.put("T12", new CustomLabels("C_T12"));
        labels2.put("T3_12", new CustomLabels("C_T3_12"));

        Map<FeederContext, List<CustomFeederInfos>> feederInfosData2 = new HashMap<>();
        feederInfosData2.put(new FeederContext("L11", NodeSide.ONE), List.of(
                new CustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1"),
                new CustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.IN, "reactive1")
        ));
        feederInfosData2.put(new FeederContext("L11", NodeSide.TWO), List.of(
                new CustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.OUT, "active2"),
                new CustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.OUT, "ractive2")
        ));

        feederInfosData2.put(new FeederContext("T12", NodeSide.ONE), List.of(
                new CustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1")
        ));
        feederInfosData2.put(new FeederContext("T3_12", NodeSide.ONE), List.of(
                new CustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1")
        ));

        //test data3
        Network network3 = Networks.createNetworkWithSvcVscScDl();
        VoltageLevel vl3 = network3.getVoltageLevel("vl");

        Map<String, CustomLabels> labels3 = new HashMap<>();
        labels3.put("bbs", new CustomLabels("C_bbs"));
        labels3.put("bbs2", new CustomLabels("C_bbs2"));
        labels3.put("svc", new CustomLabels("C_svc"));
        labels3.put("hvdc", new CustomLabels("C_hvdc"));
        labels3.put("C1", new CustomLabels("C_C1"));
        labels3.put("dl1", new CustomLabels("C_dl1"));

        Map<FeederContext, List<CustomFeederInfos>> feederInfosData3 = new HashMap<>();
        feederInfosData3.put(new FeederContext("hvdc", NodeSide.ONE), List.of(
                new CustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1"),
                new CustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.IN, "reactive1")
        ));

        //test data 4
        Network network4 = createTestNetwork();
        VoltageLevel vl4 = network4.getVoltageLevel("vl");

        Map<String, CustomLabels> labels4 = new HashMap<>();
        labels4.put("bbs", new CustomLabels("C_bbs"));
        labels4.put("d", new CustomLabels("C_disconnector"));
        labels4.put("b", new CustomLabels("C_breaker"));
        labels4.put("l", new CustomLabels("C_load"));

        Map<FeederContext, List<CustomFeederInfos>> feederInfosData4 = new HashMap<>();
        feederInfosData4.put(new FeederContext("l"), List.of(
                new CustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active"),
                new CustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.OUT, "reactive"),
                new CustomFeederInfos(ARROW_CURRENT, LabelProvider.LabelDirection.IN, "current")
        ));

        //test data 5
        SvgParameters svgParameters5 = createSvgParameters().setDisplayConnectivityNodesId(true).setFeederInfoSymmetry(false).setShowInternalNodes(true);

        //test data 6
        Map<String, CustomLabels> labels5 = new HashMap<>();
        labels5.put("bbs", new CustomLabels(null, "BBS"));
        labels5.put("d", new CustomLabels("C_disconnector", "DISCONNECTOR"));
        labels5.put("b", new CustomLabels("C_breaker", "BREAKER"));
        labels5.put("l", new CustomLabels("C_load", "LOAD"));

        SvgParameters svgParameters6 = createSvgParameters().setDisplayEquipmentNodesLabel(true).setLabelDiagonal(true).setAngleLabelShift(45);

        return List.of(
                Arguments.of(network1, vl1, labels1, feederInfosData, svgParameters1, "/CustomLabelProviderTest1.svg"),
                Arguments.of(network2, vl2, labels2, feederInfosData2, svgParameters1, "/CustomLabelProviderTest2.svg"),
                Arguments.of(network3, vl3, labels3, feederInfosData3, createSvgParameters(), "/CustomLabelProviderTest3.svg"),
                Arguments.of(network4, vl4, labels4, feederInfosData4, svgParameters1, "/CustomLabelProviderTest4.svg"),
                Arguments.of(network3, vl3, labels3, feederInfosData3, svgParameters5, "/CustomLabelProviderTest5.svg"),
                Arguments.of(network4, vl4, labels5, feederInfosData3, svgParameters1, "/CustomLabelProviderTest6.svg"),
                Arguments.of(network4, vl4, labels5, feederInfosData3, svgParameters6, "/CustomLabelProviderTest7.svg")
        );
    }
}
