/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.NominalVoltageStyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCase12GraphWith3WT extends AbstractTestCaseIidm {

    private VoltageLevel vl1;
    private VoltageLevel vl2;
    private VoltageLevel vl3;

    @BeforeEach
    public void setUp() {
        layoutParameters.setCellWidth(80);

        network = Network.create("testCase11", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = Networks.createSubstation(network, "subst", "subst", Country.FR);

        // first voltage level
        //
        vl1 = Networks.createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);

        Networks.createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        Networks.createBusBarSection(vl1, "bbs2", "bbs2", 1, 1, 2);
        Networks.createBusBarSection(vl1, "bbs3", "bbs3", 2, 2, 1);
        Networks.createBusBarSection(vl1, "bbs4", "bbs4", 3, 2, 2);

        Networks.createSwitch(vl1, "dsect11", "dsect11", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        Networks.createSwitch(vl1, "dtrct11", "dtrct11", SwitchKind.BREAKER, true, false, true, 14, 15);
        Networks.createSwitch(vl1, "dsect12", "dsect12", SwitchKind.DISCONNECTOR, false, false, true, 15, 1);

        Networks.createSwitch(vl1, "dsect21", "dsect21", SwitchKind.DISCONNECTOR, false, false, true, 2, 16);
        Networks.createSwitch(vl1, "dtrct21", "dtrct21", SwitchKind.BREAKER, true, false, true, 16, 17);
        Networks.createSwitch(vl1, "dsect22", "dsect22", SwitchKind.DISCONNECTOR, false, false, true, 17, 3);

        Networks.createLoad(vl1, "load1", "load1", "load1", 0, ConnectablePosition.Direction.TOP, 4, 10, 10);
        Networks.createSwitch(vl1, "dload1", "dload1", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        Networks.createSwitch(vl1, "bload1", "bload1", SwitchKind.BREAKER, true, false, true, 4, 5);

        Networks.createGenerator(vl1, "gen1", "gen1", "gen1", 2, ConnectablePosition.Direction.BOTTOM, 6, 0, 20, false, 10, 10);
        Networks.createSwitch(vl1, "dgen1", "dgen1", SwitchKind.DISCONNECTOR, false, false, true, 2, 7);
        Networks.createSwitch(vl1, "bgen1", "bgen1", SwitchKind.BREAKER, true, false, true, 6, 7);

        Networks.createLoad(vl1, "load2", "load2", "load2", 7, ConnectablePosition.Direction.TOP, 8, 10, 10);
        Networks.createSwitch(vl1, "dload2", "dload2", SwitchKind.DISCONNECTOR, false, false, true, 1, 9);
        Networks.createSwitch(vl1, "bload2", "bload2", SwitchKind.BREAKER, true, false, true, 8, 9);

        Networks.createGenerator(vl1, "gen2", "gen2", "gen2", 11, ConnectablePosition.Direction.BOTTOM, 10, 0, 20, false, 10, 10);
        Networks.createSwitch(vl1, "dgen2", "dgen2", SwitchKind.DISCONNECTOR, false, false, true, 3, 11);
        Networks.createSwitch(vl1, "bgen2", "bgen2", SwitchKind.BREAKER, true, false, true, 10, 11);

        // second voltage level
        //
        vl2 = Networks.createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225);

        Networks.createBusBarSection(vl2, "bbs5", "bbs5", 0, 1, 1);
        Networks.createBusBarSection(vl2, "bbs6", "bbs6", 1, 2, 1);

        Networks.createSwitch(vl2, "dscpl1", "dscpl1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        Networks.createSwitch(vl2, "ddcpl1", "ddcpl1", SwitchKind.BREAKER, true, false, true, 6, 7);
        Networks.createSwitch(vl2, "dscpl2", "dscpl2", SwitchKind.DISCONNECTOR, false, false, true, 7, 1);

        Networks.createLoad(vl2, "load3", "load3", "load3", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl2, "dload3", "dload3", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        Networks.createSwitch(vl2, "bload3", "bload3", SwitchKind.BREAKER, true, false, true, 2, 3);

        Networks.createGenerator(vl2, "gen4", "gen4", "gen4", 2, ConnectablePosition.Direction.BOTTOM, 4, 0, 20, false, 10, 10);
        Networks.createSwitch(vl2, "dgen4", "dgen4", SwitchKind.DISCONNECTOR, false, false, true, 1, 5);
        Networks.createSwitch(vl2, "bgen4", "bgen4", SwitchKind.BREAKER, true, false, true, 4, 5);

        // third voltage level
        //
        vl3 = Networks.createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 63);

        Networks.createBusBarSection(vl3, "bbs7", "bbs7", 0, 1, 1);

        Networks.createLoad(vl3, "load4", "load4", "load4", 0, ConnectablePosition.Direction.TOP, 1, 10, 10);
        Networks.createSwitch(vl3, "dload4", "dload4", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        Networks.createSwitch(vl3, "bload4", "bload4", SwitchKind.BREAKER, true, false, true, 2, 1);
        Networks.createShunt(vl3, "self4", "self4", "self4", 1, ConnectablePosition.Direction.BOTTOM, 3, -1, 1, 1);
        Networks.createSwitch(vl3, "dself4", "dself4", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        Networks.createSwitch(vl3, "bself4", "bself4", SwitchKind.BREAKER, true, false, true, 4, 3);

        // two windings transformers between voltage levels
        //
        Networks.createSwitch(vl1, "dtrf11", "dtrf11", SwitchKind.DISCONNECTOR, false, false, true, 0, 18);
        Networks.createSwitch(vl1, "btrf11", "btrf11", SwitchKind.BREAKER, true, false, true, 18, 19);
        Networks.createSwitch(vl2, "dtrf21", "dtrf21", SwitchKind.DISCONNECTOR, false, false, true, 0, 8);
        Networks.createSwitch(vl2, "btrf21", "btrf21", SwitchKind.BREAKER, true, false, true, 8, 9);
        Networks.createTwoWindingsTransformer(substation, "trf1", "trf1", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                19, 9, vl1.getId(), vl2.getId(),
                "trf1", 1, ConnectablePosition.Direction.TOP,
                "trf1", 1, ConnectablePosition.Direction.TOP);

        Networks.createSwitch(vl1, "dtrf12", "dtrf12", SwitchKind.DISCONNECTOR, false, false, true, 1, 20);
        Networks.createSwitch(vl1, "btrf12", "btrf12", SwitchKind.BREAKER, true, false, true, 20, 21);
        Networks.createSwitch(vl2, "dtrf22", "dtrf22", SwitchKind.DISCONNECTOR, false, false, true, 1, 10);
        Networks.createSwitch(vl2, "btrf22", "btrf22", SwitchKind.BREAKER, true, false, true, 10, 11);
        Networks.createTwoWindingsTransformer(substation, "trf2", "trf2", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                21, 11, vl1.getId(), vl2.getId(),
                "trf2", 10, ConnectablePosition.Direction.TOP,
                "trf2", 7, ConnectablePosition.Direction.BOTTOM);

        Networks.createSwitch(vl1, "dtrf13", "dtrf13", SwitchKind.DISCONNECTOR, false, false, true, 2, 22);
        Networks.createSwitch(vl1, "btrf13", "btrf13", SwitchKind.BREAKER, true, false, true, 22, 23);
        Networks.createSwitch(vl2, "dtrf23", "dtrf23", SwitchKind.DISCONNECTOR, false, false, true, 1, 12);
        Networks.createSwitch(vl2, "btrf23", "btrf23", SwitchKind.BREAKER, true, false, true, 12, 13);
        Networks.createTwoWindingsTransformer(substation, "trf3", "trf3", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                23, 13, vl1.getId(), vl2.getId(),
                "trf3", 3, ConnectablePosition.Direction.BOTTOM,
                "trf3", 8, ConnectablePosition.Direction.BOTTOM);

        Networks.createSwitch(vl1, "dtrf14", "dtrf14", SwitchKind.DISCONNECTOR, false, false, true, 3, 24);
        Networks.createSwitch(vl1, "btrf14", "btrf14", SwitchKind.BREAKER, true, false, true, 24, 25);
        Networks.createSwitch(vl2, "dtrf24", "dtrf24", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        Networks.createSwitch(vl2, "btrf24", "btrf24", SwitchKind.BREAKER, true, false, true, 14, 15);
        Networks.createTwoWindingsTransformer(substation, "trf4", "trf4", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                25, 15, vl1.getId(), vl2.getId(),
                "trf4", 9, ConnectablePosition.Direction.BOTTOM,
                "trf4", 3, ConnectablePosition.Direction.TOP);

        Networks.createSwitch(vl1, "dtrf15", "dtrf15", SwitchKind.DISCONNECTOR, false, false, true, 0, 26);
        Networks.createSwitch(vl1, "btrf15", "btrf15", SwitchKind.BREAKER, true, false, true, 26, 27);
        Networks.createSwitch(vl3, "dtrf25", "dtrf25", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        Networks.createSwitch(vl3, "btrf25", "btrf25", SwitchKind.BREAKER, true, false, true, 5, 6);
        Networks.createTwoWindingsTransformer(substation, "trf5", "trf5", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                27, 6, vl1.getId(), vl3.getId(),
                "trf5", 4, ConnectablePosition.Direction.TOP,
                "trf5", 2, ConnectablePosition.Direction.BOTTOM);

        // three windings transformers between voltage levels
        //
        Networks.createSwitch(vl1, "dtrf16", "dtrf16", SwitchKind.DISCONNECTOR, false, false, true, 0, 28);
        Networks.createSwitch(vl1, "btrf16", "btrf16", SwitchKind.BREAKER, true, false, true, 28, 29);
        Networks.createSwitch(vl2, "dtrf26", "dtrf26", SwitchKind.DISCONNECTOR, false, false, true, 1, 16);
        Networks.createSwitch(vl2, "btrf26", "btrf26", SwitchKind.BREAKER, true, false, true, 16, 17);
        Networks.createSwitch(vl3, "dtrf36", "dtrf36", SwitchKind.DISCONNECTOR, false, false, true, 0, 7);
        Networks.createSwitch(vl3, "btrf36", "btrf36", SwitchKind.BREAKER, true, false, true, 7, 8);

        Networks.createThreeWindingsTransformer(substation, "trf6", "trf6", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 225.,
                29, 17, 8,
                "trf61", 5, ConnectablePosition.Direction.TOP,
                "trf62", 5, ConnectablePosition.Direction.TOP,
                "trf63", 3, ConnectablePosition.Direction.TOP);

        Networks.createSwitch(vl1, "dtrf17", "dtrf17", SwitchKind.DISCONNECTOR, false, false, true, 2, 30);
        Networks.createSwitch(vl1, "btrf17", "btrf17", SwitchKind.BREAKER, true, false, true, 30, 31);
        Networks.createSwitch(vl2, "dtrf27", "dtrf27", SwitchKind.DISCONNECTOR, false, false, true, 0, 18);
        Networks.createSwitch(vl2, "btrf27", "btrf27", SwitchKind.BREAKER, true, false, true, 18, 19);
        Networks.createSwitch(vl3, "dtrf37", "dtrf37", SwitchKind.DISCONNECTOR, false, false, true, 0, 9);
        Networks.createSwitch(vl3, "btrf37", "btrf37", SwitchKind.BREAKER, true, false, true, 9, 10);

        Networks.createThreeWindingsTransformer(substation, "trf7", "trf7", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 225.,
                31, 19, 10,
                "trf71", 6, ConnectablePosition.Direction.BOTTOM,
                "trf72", 4, ConnectablePosition.Direction.TOP,
                "trf73", 4, ConnectablePosition.Direction.BOTTOM);

        Networks.createSwitch(vl1, "dtrf18", "dtrf18", SwitchKind.DISCONNECTOR, false, false, true, 1, 32);
        Networks.createSwitch(vl1, "btrf18", "btrf18", SwitchKind.BREAKER, true, false, true, 32, 33);
        Networks.createSwitch(vl2, "dtrf28", "dtrf28", SwitchKind.DISCONNECTOR, false, false, true, 1, 20);
        Networks.createSwitch(vl2, "btrf28", "btrf28", SwitchKind.BREAKER, true, false, true, 20, 21);
        Networks.createSwitch(vl3, "dtrf38", "dtrf38", SwitchKind.DISCONNECTOR, false, false, true, 0, 11);
        Networks.createSwitch(vl3, "btrf38", "btrf38", SwitchKind.BREAKER, true, false, true, 11, 12);

        Networks.createThreeWindingsTransformer(substation, "trf8", "trf8", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 225.,
                33, 21, 12,
                "trf81", 8, ConnectablePosition.Direction.TOP,
                "trf82", 6, ConnectablePosition.Direction.BOTTOM,
                "trf83", 5, ConnectablePosition.Direction.TOP);

        Networks.createShunt(vl3, "self5", "self5", "self5", 6, ConnectablePosition.Direction.BOTTOM, 13, -1, 1, 1);
        Networks.createSwitch(vl3, "dself5", "dself5", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        Networks.createSwitch(vl3, "bself5", "bself5", SwitchKind.BREAKER, true, false, true, 14, 13);

        Networks.createBusBarSection(vl3, "bbs8", "bbs8", 15, 1, 2);

        Networks.createShunt(vl3, "self6", "self6", "self6", 7, ConnectablePosition.Direction.BOTTOM, 16, 1, 1, 1);
        Networks.createSwitch(vl3, "dself6", "dself6", SwitchKind.DISCONNECTOR, false, false, true, 15, 17);
        Networks.createSwitch(vl3, "bself6", "bself6", SwitchKind.BREAKER, true, false, true, 17, 16);
    }

    private void separateBusVoltages() {
        Load load = network.getLoad("load1");
        load.getTerminal().getBusView().getBus().setV(392);
        load.getTerminal().getBusView().getBus().setAngle(-2.3);

        Generator generator = network.getGenerator("gen1");
        generator.getTerminal().getBusView().getBus().setV(403);
        generator.getTerminal().getBusView().getBus().setAngle(-1.7);
    }

    @Test
    void testVl1() {
        // build voltage level 1 graph
        VoltageLevelGraph g1 = graphBuilder.buildVoltageLevelGraph(vl1.getId());

        voltageLevelGraphLayout(g1);

        // write JSON and compare to reference (horizontal layout)
        assertEquals(toString("/TestCase12GraphVL1.json"), toJson(g1, "/TestCase12GraphVL1.json"));
    }

    @Test
    void testVl2() {
        // build voltage level 2 graph
        VoltageLevelGraph g2 = graphBuilder.buildVoltageLevelGraph(vl2.getId());

        voltageLevelGraphLayout(g2);

        // write JSON and compare to reference (horizontal layout)
        assertEquals(toString("/TestCase12GraphVL2.json"), toJson(g2, "/TestCase12GraphVL2.json"));
    }

    @Test
    void testVl3() {
        // build voltage level 3 graph
        VoltageLevelGraph g3 = graphBuilder.buildVoltageLevelGraph(vl3.getId());

        voltageLevelGraphLayout(g3);

        // write JSON and compare to reference (horizontal layout)
        assertEquals(toString("/TestCase12GraphVL3.json"), toJson(g3, "/TestCase12GraphVL3.json"));
    }

    @Test
    void testMetadata() {

        // Optimize SVG by avoiding duplication
        svgParameters.setAvoidSVGComponentsDuplication(true)
                .setAddNodesInfos(true);

        // compare metadata of voltage level diagram with reference
        VoltageLevelGraph graph = graphBuilder.buildVoltageLevelGraph(vl1.getId());
        assertTrue(compareMetadata(graph, "/vlDiag_metadata.json", new PositionVoltageLevelLayoutFactory(), componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new NominalVoltageStyleProvider()));
    }

    @Test
    void testNodesInfosNominalVoltageStyle() {
        separateBusVoltages();

        svgParameters.setAddNodesInfos(true);

        // build voltage level 1 graph
        VoltageLevelGraph g1 = graphBuilder.buildVoltageLevelGraph(vl1.getId());

        voltageLevelGraphLayout(g1);

        // write SVGs and compare to reference
        assertEquals(toString("/TestCase12GraphWithNodesInfosNominalVoltage.svg"),
                toSVG(g1, "/TestCase12GraphWithNodesInfosNominalVoltage.svg", new ConvergenceComponentLibrary(), layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new NominalVoltageStyleProvider()));
    }

    @Test
    void testNodesInfosTopologicalStyle() {
        separateBusVoltages();

        //parametrize diagram
        svgParameters.setAddNodesInfos(true);

        VoltageLevelGraph g1 = graphBuilder.buildVoltageLevelGraph(vl1.getId());
        voltageLevelGraphLayout(g1);

        assertEquals(toString("/TestCase12GraphWithNodesInfosTopological.svg"),
                toSVG(g1, "/TestCase12GraphWithNodesInfosTopological.svg", new ConvergenceComponentLibrary(), layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }

}
