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
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.NodeSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class CustomLabelProviderTest extends AbstractTestCaseIidm {
    @BeforeEach
    public void setUp() {
    }

    @ParameterizedTest(name = "{4}")
    @MethodSource("provideTestData")
    void test(Network network, VoltageLevel vl,
              Map<String, String> labels,
              Map<String, List<CustomLabelProvider.SldCustomFeederInfos>> feederInfosData,
              SvgParameters svgParameters,
              String resourceName) {
        assertNotNull(network);
        assertNotNull(vl);
        assertNotNull(labels);
        assertNotNull(feederInfosData);
        assertNotNull(resourceName);

        this.network = network;
        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();

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

        Map<String, List<CustomLabelProvider.SldCustomFeederInfos>> feederInfosData = new HashMap<>();

        //test data1
        Network network1 = Networks.createNetworkGroundDisconnector();
        VoltageLevel vl1 = network1.getVoltageLevel("vl");
        assertNotNull(vl1);

        Map<String, String> labels1 = new HashMap<>();
        labels1.put("bbs", "C_BBS1");
        labels1.put("line", "C_LINE");
        labels1.put("load", "C_LOAD");
        labels1.put("ground", "C_GROUND");
        labels1.put("d1", "C_D1");
        labels1.put("b1", "C_B1");
        labels1.put("gd1", "C_GD1");
        labels1.put("d2", "C_D2");
        labels1.put("b2", "C_B2");
        labels1.put("gd2", "C_GD2");
        labels1.put("gd", "C_GD");
        SvgParameters svgParameters1 = createSvgParameters().setDisplayEquipmentNodesLabel(true);

        //test data2
        Network network2 = Networks.createNodeBreakerNetworkWithInternalBranches("TestInternalBranchesNodeBreaker", "test");
        VoltageLevel vl2 = network2.getVoltageLevel("VL1");

        Map<String, String> labels2 = new HashMap<>();
        labels2.put("BBS11", "C_BBS11");
        labels2.put("BBS12", "C_BBS12");
        labels2.put("BR1", "C_BR1");
        labels2.put("BR12", "C_BR12");
        labels2.put("BR14", "C_BR14");
        labels2.put("BR16", "C_BR16");
        labels2.put("BR18", "C_BR18");
        labels2.put("BR20", "C_BR20");
        labels2.put("BR22", "C_BR22");
        labels2.put("BR24", "C_BR24");
        labels2.put("BR26", "C_BR26");
        labels2.put("BR28", "C_BR28");
        labels2.put("BR30", "C_BR30");
        labels2.put("D10", "C_D10");
        labels2.put("D11", "C_D11");
        labels2.put("D13", "C_D13");
        labels2.put("D15", "C_D15");
        labels2.put("D17", "C_D17");
        labels2.put("D19", "C_D19");
        labels2.put("D20", "C_D20");
        labels2.put("D21", "C_D21");
        labels2.put("D23", "C_D23");
        labels2.put("D25", "C_D25");
        labels2.put("D27", "C_D27");
        labels2.put("D29", "C_D29");
        labels2.put("G", "C_G");
        labels2.put("L1", "C_L1");
        labels2.put("L11", "C_L11");
        labels2.put("L12", "C_L12");
        labels2.put("T11", "C_T11");
        labels2.put("T12", "C_T12");
        labels2.put("T3_12", "C_T3_12");

        Map<String, List<CustomLabelProvider.SldCustomFeederInfos>> feederInfosData2 = new HashMap<>();
        feederInfosData2.put("L11", List.of(
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_ACTIVE, NodeSide.ONE, LabelProvider.LabelDirection.IN, "active1"),
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_REACTIVE, NodeSide.ONE, LabelProvider.LabelDirection.IN, "reactive1"),
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_ACTIVE, NodeSide.TWO, LabelProvider.LabelDirection.OUT, "active2"),
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_REACTIVE, NodeSide.TWO, LabelProvider.LabelDirection.OUT, "ractive2")
        ));
        feederInfosData2.put("T12", List.of(
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_ACTIVE, NodeSide.ONE, LabelProvider.LabelDirection.IN, "active1")
        ));
        feederInfosData2.put("T3_12", List.of(
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_ACTIVE, NodeSide.ONE, LabelProvider.LabelDirection.IN, "active1")
        ));

        //test data3
        Network network3 = Networks.createNetworkWithSvcVscScDl();
        VoltageLevel vl3 = network3.getVoltageLevel("vl");

        Map<String, String> labels3 = new HashMap<>();
        labels3.put("bbs", "C_bbs");
        labels3.put("bbs2", "C_bbs2");
        labels3.put("svc", "C_svc");
        labels3.put("hvdc", "C_hvdc");
        labels3.put("C1", "C_C1");
        labels3.put("dl1", "C_dl1");

        Map<String, List<CustomLabelProvider.SldCustomFeederInfos>> feederInfosData3 = new HashMap<>();
        feederInfosData3.put("hvdc", List.of(
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_ACTIVE, NodeSide.ONE, LabelProvider.LabelDirection.IN, "active1"),
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_REACTIVE, NodeSide.ONE, LabelProvider.LabelDirection.IN, "reactive1")
        ));

        //test data 4
        Network network4 = createTestNetwork();
        VoltageLevel vl4 = network4.getVoltageLevel("vl");

        Map<String, String> labels4 = new HashMap<>();
        labels4.put("bbs", "C_bbs");
        labels4.put("d", "C_disconnector");
        labels4.put("b", "C_breaker");
        labels4.put("l", "C_load");

        Map<String, List<CustomLabelProvider.SldCustomFeederInfos>> feederInfosData4 = new HashMap<>();
        feederInfosData4.put("l", List.of(
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_ACTIVE, null, LabelProvider.LabelDirection.IN, "active"),
                new CustomLabelProvider.SldCustomFeederInfos(ARROW_REACTIVE, null, LabelProvider.LabelDirection.OUT, "reactive"),
                new CustomLabelProvider.SldCustomFeederInfos(ComponentTypeName.ARROW_CURRENT, null, LabelProvider.LabelDirection.IN, "current")
        ));

        //test data 5
        SvgParameters svgParameters5 = createSvgParameters().setDisplayConnectivityNodesId(true).setFeederInfoSymmetry(false).setShowInternalNodes(true);

        return List.of(
                Arguments.of(network1, vl1, labels1, feederInfosData, svgParameters1, "/CustomLabelProviderTest1.svg"),
                Arguments.of(network2, vl2, labels2, feederInfosData2, svgParameters1, "/CustomLabelProviderTest2.svg"),
                Arguments.of(network3, vl3, labels3, feederInfosData3, createSvgParameters(), "/CustomLabelProviderTest3.svg"),
                Arguments.of(network4, vl4, labels4, feederInfosData4, svgParameters1, "/CustomLabelProviderTest4.svg"),
                Arguments.of(network3, vl3, labels3, feederInfosData3, svgParameters5, "/CustomLabelProviderTest5.svg")
        );

    }

}
