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
import com.powsybl.sld.svg.CustomLabelProvider.SldFeederContext;
import com.powsybl.sld.svg.CustomLabelProvider.SldCustomFeederInfos;
import com.powsybl.sld.svg.CustomLabelProvider.SldCustomLabels;
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
              Map<String, SldCustomLabels> labels,
              Map<SldFeederContext, List<SldCustomFeederInfos>> feederInfosData,
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

        Map<String, List<SldCustomFeederInfos>> feederInfosData = new HashMap<>();

        //test data1
        Network network1 = Networks.createNetworkGroundDisconnector();
        VoltageLevel vl1 = network1.getVoltageLevel("vl");
        assertNotNull(vl1);

        Map<String, SldCustomLabels> labels1 = new HashMap<>();
        labels1.put("bbs", new SldCustomLabels("C_BBS1"));
        labels1.put("line", new SldCustomLabels("C_LINE"));
        labels1.put("load", new SldCustomLabels("C_LOAD"));
        labels1.put("ground", new SldCustomLabels("C_GROUND"));
        labels1.put("d1", new SldCustomLabels("C_D1"));
        labels1.put("b1", new SldCustomLabels("C_B1"));
        labels1.put("gd1", new SldCustomLabels("C_GD1"));
        labels1.put("d2", new SldCustomLabels("C_D2"));
        labels1.put("b2", new SldCustomLabels("C_B2"));
        labels1.put("gd2", new SldCustomLabels("C_GD2"));
        labels1.put("gd", new SldCustomLabels("C_GD"));
        SvgParameters svgParameters1 = createSvgParameters().setDisplayEquipmentNodesLabel(true);

        //test data2
        Network network2 = Networks.createNodeBreakerNetworkWithInternalBranches("TestInternalBranchesNodeBreaker", "test");
        VoltageLevel vl2 = network2.getVoltageLevel("VL1");

        Map<String, SldCustomLabels> labels2 = new HashMap<>();
        labels2.put("BBS11", new SldCustomLabels("C_BBS11"));
        labels2.put("BBS12", new SldCustomLabels("C_BBS12"));
        labels2.put("BR1", new SldCustomLabels("C_BR1"));
        labels2.put("BR12", new SldCustomLabels("C_BR12"));
        labels2.put("BR14", new SldCustomLabels("C_BR14"));
        labels2.put("BR16", new SldCustomLabels("C_BR16"));
        labels2.put("BR18", new SldCustomLabels("C_BR18"));
        labels2.put("BR20", new SldCustomLabels("C_BR20"));
        labels2.put("BR22", new SldCustomLabels("C_BR22"));
        labels2.put("BR24", new SldCustomLabels("C_BR24"));
        labels2.put("BR26", new SldCustomLabels("C_BR26"));
        labels2.put("BR28", new SldCustomLabels("C_BR28"));
        labels2.put("BR30", new SldCustomLabels("C_BR30"));
        labels2.put("D10", new SldCustomLabels("C_D10"));
        labels2.put("D11", new SldCustomLabels("C_D11"));
        labels2.put("D13", new SldCustomLabels("C_D13"));
        labels2.put("D15", new SldCustomLabels("C_D15"));
        labels2.put("D17", new SldCustomLabels("C_D17"));
        labels2.put("D19", new SldCustomLabels("C_D19"));
        labels2.put("D20", new SldCustomLabels("C_D20"));
        labels2.put("D21", new SldCustomLabels("C_D21"));
        labels2.put("D23", new SldCustomLabels("C_D23"));
        labels2.put("D25", new SldCustomLabels("C_D25"));
        labels2.put("D27", new SldCustomLabels("C_D27"));
        labels2.put("D29", new SldCustomLabels("C_D29"));
        labels2.put("G", new SldCustomLabels("C_G"));
        labels2.put("L1", new SldCustomLabels("C_L1"));
        labels2.put("L11", new SldCustomLabels("C_L11"));
        labels2.put("L12", new SldCustomLabels("C_L12"));
        labels2.put("T11", new SldCustomLabels("C_T11"));
        labels2.put("T12", new SldCustomLabels("C_T12"));
        labels2.put("T3_12", new SldCustomLabels("C_T3_12"));

        Map<SldFeederContext, List<SldCustomFeederInfos>> feederInfosData2 = new HashMap<>();
        feederInfosData2.put(new SldFeederContext("L11", NodeSide.ONE), List.of(
                new SldCustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1"),
                new SldCustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.IN, "reactive1")
        ));
        feederInfosData2.put(new SldFeederContext("L11", NodeSide.TWO), List.of(
                new SldCustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.OUT, "active2"),
                new SldCustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.OUT, "ractive2")
        ));

        feederInfosData2.put(new SldFeederContext("T12", NodeSide.ONE), List.of(
                new SldCustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1")
        ));
        feederInfosData2.put(new SldFeederContext("T3_12", NodeSide.ONE), List.of(
                new SldCustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1")
        ));

        //test data3
        Network network3 = Networks.createNetworkWithSvcVscScDl();
        VoltageLevel vl3 = network3.getVoltageLevel("vl");

        Map<String, SldCustomLabels> labels3 = new HashMap<>();
        labels3.put("bbs", new SldCustomLabels("C_bbs"));
        labels3.put("bbs2", new SldCustomLabels("C_bbs2"));
        labels3.put("svc", new SldCustomLabels("C_svc"));
        labels3.put("hvdc", new SldCustomLabels("C_hvdc"));
        labels3.put("C1", new SldCustomLabels("C_C1"));
        labels3.put("dl1", new SldCustomLabels("C_dl1"));

        Map<SldFeederContext, List<SldCustomFeederInfos>> feederInfosData3 = new HashMap<>();
        feederInfosData3.put(new SldFeederContext("hvdc", NodeSide.ONE), List.of(
                new SldCustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active1"),
                new SldCustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.IN, "reactive1")
        ));

        //test data 4
        Network network4 = createTestNetwork();
        VoltageLevel vl4 = network4.getVoltageLevel("vl");

        Map<String, SldCustomLabels> labels4 = new HashMap<>();
        labels4.put("bbs", new SldCustomLabels("C_bbs"));
        labels4.put("d", new SldCustomLabels("C_disconnector"));
        labels4.put("b", new SldCustomLabels("C_breaker"));
        labels4.put("l", new SldCustomLabels("C_load"));

        Map<SldFeederContext, List<SldCustomFeederInfos>> feederInfosData4 = new HashMap<>();
        feederInfosData4.put(new SldFeederContext("l"), List.of(
                new SldCustomFeederInfos(ARROW_ACTIVE, LabelProvider.LabelDirection.IN, "active"),
                new SldCustomFeederInfos(ARROW_REACTIVE, LabelProvider.LabelDirection.OUT, "reactive"),
                new SldCustomFeederInfos(ARROW_CURRENT, LabelProvider.LabelDirection.IN, "current")
        ));

        //test data 5
        SvgParameters svgParameters5 = createSvgParameters().setDisplayConnectivityNodesId(true).setFeederInfoSymmetry(false).setShowInternalNodes(true);

        //test data 6
        Map<String, SldCustomLabels> labels5 = new HashMap<>();
        labels5.put("bbs", new SldCustomLabels(null, "BBS"));
        labels5.put("d", new SldCustomLabels("C_disconnector", "DISCONNECTOR"));
        labels5.put("b", new SldCustomLabels("C_breaker", "BREAKER"));
        labels5.put("l", new SldCustomLabels("C_load", "LOAD"));

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
