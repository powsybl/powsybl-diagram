/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.Config;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.BasicStyleProvider;
import com.powsybl.sld.svg.styles.NominalVoltageStyleProvider;
import com.powsybl.sld.svg.styles.StyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.*;
import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static com.powsybl.sld.model.nodes.NodeSide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class TestSVGWriter extends AbstractTestCaseIidm {

    private static final String SUBSTATION_1_ID = "Substation1";
    private static final String SUBSTATION_2_ID = "Substation2";
    private static final String VOLTAGE_LEVEL_11_ID = "VoltageLevel11";
    private static final double VOLTAGE_LEVEL_11_V = 400;
    private static final String VOLTAGE_LEVEL_12_ID = "VoltageLevel12";
    private static final double VOLTAGE_LEVEL_12_V = 280;
    private static final String VOLTAGE_LEVEL_21_ID = "VoltageLevel21";
    private static final double VOLTAGE_LEVEL_21_V = 280;
    private static final String BUS_11_ID = "Bus11";
    private static final String BUS_12_ID = "Bus12";
    private static final String BUS_21_ID = "Bus21";
    private static final String LOAD_ID = "Load";
    private static final String LINE_ID = "Line";
    private static final String GENERATOR_ID = "Generator";
    private static final String TRANSFORMER_ID = "Transformer";

    private VoltageLevelGraph g1;
    private VoltageLevelGraph g2;
    private VoltageLevelGraph g3;
    private SubstationGraph substG;
    private LabelProviderFactory labelProviderFactory;
    private LabelProviderFactory labelNoFeederInfoProviderFactory;
    private LabelProviderFactory diagramLabelMultiLineTooltipProviderFactory;
    private LabelProviderFactory diagramLabelSameNodeProviderFactory;
    private ZoneGraph zGraph;

    private void createVoltageLevelGraphs() {
        // Creation "by hand" (without any network) of 3 voltage level graphs
        // and then generation of a SVG with DefaultDiagramStyleProvider (no network necessary)
        //
        g1 = createVoltageLevelGraph1();
        g2 = createVoltageLevelGraph2();
        g3 = createVoltageLevelGraph3();
    }

    protected static VoltageLevelGraph createVoltageLevelGraph1() {
        VoltageLevelGraph g1 = new VoltageLevelGraph(new VoltageLevelInfos("vl1", "vl1", 400), null);
        g1.setCoord(40, 20);

        VoltageLevelInfos voltageLevelInfosLeg1 = new VoltageLevelInfos("vl1", "vl1", 400.);
        VoltageLevelInfos voltageLevelInfosLeg2 = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelInfos voltageLevelInfosLeg3 = new VoltageLevelInfos("vl3", "vl3", 63.);

        BusNode vl1Bbs1 = NodeFactory.createBusNode(g1, "vl1_bbs1", "vl1_bbs1");
        vl1Bbs1.setX(0);
        vl1Bbs1.setY(300);
        vl1Bbs1.setPxWidth(200);
        vl1Bbs1.setPosition(new Position(0, 1, 4, 0, null));
        BusNode vl1Bbs2 = NodeFactory.createBusNode(g1, "vl1_bbs2", "vl1_bbs2");
        vl1Bbs2.setX(280);
        vl1Bbs2.setY(300);
        vl1Bbs2.setPxWidth(200);
        vl1Bbs2.setPosition(new Position(6, 1, 6, 0, null));
        SwitchNode vl1D1 = NodeFactory.createSwitchNode(g1, "vl1_d1", "vl1_d1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D1.setX(220);
        vl1D1.setY(300);
        SwitchNode vl1B1 = NodeFactory.createSwitchNode(g1, "vl1_b1", "vl1_b1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1B1.setOrientation(Orientation.LEFT);
        vl1B1.setX(245);
        vl1B1.setY(300);
        SwitchNode vl1D2 = NodeFactory.createSwitchNode(g1, "vl1_d2", null, ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D2.setX(270);
        vl1D2.setY(300);
        g1.addEdge(vl1Bbs1, vl1D1);
        g1.addEdge(vl1D1, vl1B1);
        g1.addEdge(vl1B1, vl1D2);
        g1.addEdge(vl1D2, vl1Bbs2);

        FeederNode vl1Load1 = NodeFactory.createLoad(g1, "vl1_load1", "vl1_load1");
        vl1Load1.setOrder(0);
        vl1Load1.setDirection(TOP);
        vl1Load1.setX(40);
        vl1Load1.setY(80);
        SwitchNode vl1Bload1 = NodeFactory.createSwitchNode(g1, "vl1_bload1", "vl1_bload1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1Bload1.setX(40);
        vl1Bload1.setY(180);
        SwitchNode vl1Dload1 = NodeFactory.createSwitchNode(g1, "vl1_dload1", "vl1_dload1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dload1.setX(40);
        vl1Dload1.setY(300);
        g1.addEdge(vl1Load1, vl1Bload1);
        g1.addEdge(vl1Bload1, vl1Dload1);
        g1.addEdge(vl1Dload1, vl1Bbs1);

        FeederNode vl1Trf1 = NodeFactory.createFeeder2WTNode(g1, "vl1_trf1", "vl1_trf1", "vl1_trf1", ONE, new VoltageLevelInfos("vl2", "vl2", 225));
        vl1Trf1.setOrder(1);
        vl1Trf1.setDirection(BOTTOM);
        vl1Trf1.setX(80);
        vl1Trf1.setY(500);
        SwitchNode vl1Btrf1 = NodeFactory.createSwitchNode(g1, "vl1_btrf1", "vl1_btrf1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf1.setX(80);
        vl1Btrf1.setY(400);
        SwitchNode vl1Dtrf1 = NodeFactory.createSwitchNode(g1, "vl1_dtrf1", "vl1_dtrf1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf1.setX(80);
        vl1Dtrf1.setY(300);
        g1.addEdge(vl1Trf1, vl1Btrf1);
        g1.addEdge(vl1Btrf1, vl1Dtrf1);
        g1.addEdge(vl1Dtrf1, vl1Bbs1);

        FeederNode vl1Trf2One = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(g1, "vl1_trf2_one", "vl1_trf2", "vl1_trf2", ONE, new VoltageLevelInfos("vl2", "vl2", 225));
        vl1Trf2One.setOrder(2);
        vl1Trf2One.setDirection(TOP);
        vl1Trf2One.setX(360);
        vl1Trf2One.setY(80);
        FeederNode vl1Trf2Two = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(g1, "vl1_trf2_two", "vl1_trf2", "vl1_trf2", TWO, new VoltageLevelInfos("vl3", "vl3", 63));
        vl1Trf2Two.setOrder(3);
        vl1Trf2Two.setDirection(TOP);
        vl1Trf2Two.setX(440);
        vl1Trf2Two.setY(80);
        Middle3WTNode vl1Trf2Fict = new Middle3WTNode("vl1_trf2", "vl1_trf2", voltageLevelInfosLeg1, voltageLevelInfosLeg2, voltageLevelInfosLeg3, true);
        vl1Trf2Fict.setX(400);
        vl1Trf2Fict.setY(140);
        g1.addNode(vl1Trf2Fict);
        g1.addEdge(vl1Trf2One, vl1Trf2Fict);
        g1.addEdge(vl1Trf2Two, vl1Trf2Fict);
        SwitchNode vl1Btrf2 = NodeFactory.createSwitchNode(g1, "vl1_btrf2", "vl1_btrf2", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf2.setX(400);
        vl1Btrf2.setY(180);
        SwitchNode vl1Dtrf2 = NodeFactory.createSwitchNode(g1, "vl1_dtrf2", "vl1_dtrf2", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf2.setX(400);
        vl1Dtrf2.setY(300);
        g1.addEdge(vl1Trf2Fict, vl1Btrf2);
        g1.addEdge(vl1Btrf2, vl1Dtrf2);
        g1.addEdge(vl1Dtrf2, vl1Bbs2);

        g1.setSize(520, 580);

        return g1;
    }

    private static VoltageLevelGraph createVoltageLevelGraph2() {
        VoltageLevelGraph g2 = new VoltageLevelGraph(new VoltageLevelInfos("vl2", "vl2", 225), null);
        g2.setCoord(40, 20);

        VoltageLevelInfos voltageLevelInfosLeg1 = new VoltageLevelInfos("vl1", "vl1", 400.);
        VoltageLevelInfos voltageLevelInfosLeg2 = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelInfos voltageLevelInfosLeg3 = new VoltageLevelInfos("vl3", "vl3", 63.);

        BusNode vl2Bbs1 = NodeFactory.createBusNode(g2, "vl2_bbs1", "vl2_bbs1");
        vl2Bbs1.setX(0);
        vl2Bbs1.setY(300);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        FeederNode vl2Gen1 = NodeFactory.createGenerator(g2, "vl2_gen1", "vl2_gen1");
        vl2Gen1.setOrder(0);
        vl2Gen1.setDirection(TOP);
        vl2Gen1.setX(50);
        vl2Gen1.setY(80);
        SwitchNode vl2Bgen1 = NodeFactory.createSwitchNode(g2, "vl2_bgen1", "vl2_bgen1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl2Bgen1.setX(50);
        vl2Bgen1.setY(180);
        SwitchNode vl2Dgen1 = NodeFactory.createSwitchNode(g2, "vl2_dgen1", "vl2_dgen1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dgen1.setX(50);
        vl2Dgen1.setY(300);
        g2.addEdge(vl2Gen1, vl2Bgen1);
        g2.addEdge(vl2Bgen1, vl2Dgen1);
        g2.addEdge(vl2Dgen1, vl2Bbs1);

        FeederNode vl2Trf1 = NodeFactory.createFeeder2WTNode(g2, "vl2_trf1", "vl2_trf1", "vl2_trf1", TWO, new VoltageLevelInfos("vl1", "vl1", 400));
        vl2Trf1.setOrder(1);
        vl2Trf1.setDirection(BOTTOM);
        vl2Trf1.setX(100);
        vl2Trf1.setY(500);
        SwitchNode vl2Btrf1 = NodeFactory.createSwitchNode(g2, "vl2_btrf1", "vl2_btrf1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf1.setX(100);
        vl2Btrf1.setY(400);
        SwitchNode vl2Dtrf1 = NodeFactory.createSwitchNode(g2, "vl2_dtrf1", "vl2_dtrf1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf1.setX(100);
        vl2Dtrf1.setY(300);
        g2.addEdge(vl2Trf1, vl2Btrf1);
        g2.addEdge(vl2Btrf1, vl2Dtrf1);
        g2.addEdge(vl2Dtrf1, vl2Bbs1);

        FeederNode vl2Trf2One = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(g2, "vl2_trf2_one", "vl2_trf2", "vl2_trf2", ONE, new VoltageLevelInfos("vl1", "vl1", 400));
        vl2Trf2One.setOrder(2);
        vl2Trf2One.setDirection(TOP);
        vl2Trf2One.setX(130);
        vl2Trf2One.setY(80);
        FeederNode vl2Trf2Two = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(g2, "vl2_trf2_two", "vl2_trf2", "vl2_trf2", TWO, new VoltageLevelInfos("vl3", "vl3", 63));
        vl2Trf2Two.setOrder(3);
        vl2Trf2Two.setDirection(TOP);
        vl2Trf2Two.setX(190);
        vl2Trf2Two.setY(80);
        Middle3WTNode vl2Trf2Fict = new Middle3WTNode("vl2_trf2", "vl2_trf2", voltageLevelInfosLeg1, voltageLevelInfosLeg2, voltageLevelInfosLeg3, true);
        vl2Trf2Fict.setX(160);
        vl2Trf2Fict.setY(140);
        g2.addNode(vl2Trf2Fict);
        g2.addEdge(vl2Trf2One, vl2Trf2Fict);
        g2.addEdge(vl2Trf2Two, vl2Trf2Fict);
        SwitchNode vl2Btrf2 = NodeFactory.createSwitchNode(g2, "vl2_btrf2", "vl2_btrf2", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf2.setX(160);
        vl2Btrf2.setY(180);
        SwitchNode vl2Dtrf2 = NodeFactory.createSwitchNode(g2, "vl2_dtrf2", "vl2_dtrf2", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf2.setX(160);
        vl2Dtrf2.setY(300);
        g2.addEdge(vl2Trf2Fict, vl2Btrf2);
        g2.addEdge(vl2Btrf2, vl2Dtrf2);
        g2.addEdge(vl2Dtrf2, vl2Bbs1);

        g2.setSize(240, 580);

        return g2;
    }

    private static VoltageLevelGraph createVoltageLevelGraph3() {
        VoltageLevelGraph g3 = new VoltageLevelGraph(new VoltageLevelInfos("vl3", "vl3", 63), null);
        g3.setCoord(40, 20);

        VoltageLevelInfos voltageLevelInfosLeg1 = new VoltageLevelInfos("vl1", "vl1", 400.);
        VoltageLevelInfos voltageLevelInfosLeg2 = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelInfos voltageLevelInfosLeg3 = new VoltageLevelInfos("vl3", "vl3", 63.);

        BusNode vl3Bbs1 = NodeFactory.createBusNode(g3, "vl3_bbs1", "vl3_bbs1");
        vl3Bbs1.setX(0);
        vl3Bbs1.setY(300);
        vl3Bbs1.setPxWidth(200);
        vl3Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        FeederNode vl3Capa1 = NodeFactory.createCapacitor(g3, "vl3_capa1", "vl3_capa1");
        vl3Capa1.setOrder(0);
        vl3Capa1.setDirection(TOP);
        vl3Capa1.setX(40);
        vl3Capa1.setY(80);
        SwitchNode vl3Bcapa1 = NodeFactory.createSwitchNode(g3, "vl3_bcapa1", "vl3_bcapa1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl3Bcapa1.setX(40);
        vl3Bcapa1.setY(180);
        SwitchNode vl3Dcapa1 = NodeFactory.createSwitchNode(g3, "vl3_dcapa1", "vl3_dcapa1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dcapa1.setX(40);
        vl3Dcapa1.setY(300);
        g3.addEdge(vl3Capa1, vl3Bcapa1);
        g3.addEdge(vl3Bcapa1, vl3Dcapa1);
        g3.addEdge(vl3Dcapa1, vl3Bbs1);

        FeederNode vl3Trf2One = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(g3, "vl3_trf2_one", "vl3_trf2", "vl3_trf2", ONE, new VoltageLevelInfos("vl1", "vl1", 400));
        vl3Trf2One.setOrder(1);
        vl3Trf2One.setDirection(TOP);
        vl3Trf2One.setX(110);
        vl3Trf2One.setY(80);
        FeederNode vl3Trf2Two = NodeFactory.createFeeder3WTLegNodeForVoltageLevelDiagram(g3, "vl3_trf2_two", "vl3_trf2", "vl3_trf2", TWO, new VoltageLevelInfos("vl2", "vl2", 225));
        vl3Trf2Two.setOrder(2);
        vl3Trf2Two.setDirection(TOP);
        vl3Trf2Two.setX(190);
        vl3Trf2Two.setY(80);
        Middle3WTNode vl3Trf2Fict = new Middle3WTNode("vl3_trf2", "vl3_trf2", voltageLevelInfosLeg1, voltageLevelInfosLeg2, voltageLevelInfosLeg3, true);
        vl3Trf2Fict.setX(150);
        vl3Trf2Fict.setY(140);
        g3.addNode(vl3Trf2Fict);
        g3.addEdge(vl3Trf2One, vl3Trf2Fict);
        g3.addEdge(vl3Trf2Two, vl3Trf2Fict);
        SwitchNode vl3Btrf2 = NodeFactory.createSwitchNode(g3, "vl3_btrf2", "vl3_btrf2", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl3Btrf2.setX(150);
        vl3Btrf2.setY(180);
        SwitchNode vl3Dtrf2 = NodeFactory.createSwitchNode(g3, "vl3_dtrf2", "vl3_dtrf2", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dtrf2.setX(150);
        vl3Dtrf2.setY(300);
        g3.addEdge(vl3Trf2Fict, vl3Btrf2);
        g3.addEdge(vl3Btrf2, vl3Dtrf2);
        g3.addEdge(vl3Dtrf2, vl3Bbs1);

        g3.setSize(240, 580);

        return g3;
    }

    private void createSubstationGraph() {
        // Creation "by hand" (without any network) of 3 voltage level graphs and one substation graph
        // and then generation of a SVG with DefaultDiagramStyleProvider (no network necessary)
        //
        substG = SubstationGraph.create("subst");

        // First voltage level graph :
        //

        VoltageLevelInfos vl1Infos = new VoltageLevelInfos("vl1", "vl1", 400);
        VoltageLevelGraph g1ForSubstation = new VoltageLevelGraph(vl1Infos, substG);
        g1ForSubstation.setCoord(40, 40);

        BusNode vl1Bbs1 = NodeFactory.createBusNode(g1ForSubstation, "vl1_bbs1", "vl1_bbs1");
        vl1Bbs1.setX(0);
        vl1Bbs1.setY(300);
        vl1Bbs1.setPxWidth(200);
        vl1Bbs1.setPosition(new Position(0, 1, 4, 0, null));
        BusNode vl1Bbs2 = NodeFactory.createBusNode(g1ForSubstation, "vl1_bbs2", "vl1_bbs2");
        vl1Bbs2.setX(280);
        vl1Bbs2.setY(300);
        vl1Bbs2.setPxWidth(200);
        vl1Bbs2.setPosition(new Position(6, 1, 6, 0, null));
        SwitchNode vl1D1 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_d1", "vl1_d1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D1.setX(220);
        vl1D1.setY(300);
        SwitchNode vl1B1 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_b1", "vl1_b1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1B1.setOrientation(Orientation.LEFT);
        vl1B1.setX(245);
        vl1B1.setY(300);
        SwitchNode vl1D2 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_d2", "vl1_d2", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D2.setX(270);
        vl1D2.setY(300);
        g1ForSubstation.addEdge(vl1Bbs1, vl1D1);
        g1ForSubstation.addEdge(vl1D1, vl1B1);
        g1ForSubstation.addEdge(vl1B1, vl1D2);
        g1ForSubstation.addEdge(vl1D2, vl1Bbs2);

        FeederNode vl1Load1 = NodeFactory.createLoad(g1ForSubstation, "vl1_load1", "vl1_load1");
        vl1Load1.setOrder(0);
        vl1Load1.setDirection(TOP);
        vl1Load1.setX(40);
        vl1Load1.setY(80);
        SwitchNode vl1Bload1 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_bload1", "vl1_bload1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1Bload1.setX(40);
        vl1Bload1.setY(180);
        SwitchNode vl1Dload1 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_dload1", "vl1_dload1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dload1.setX(40);
        vl1Dload1.setY(300);
        g1ForSubstation.addEdge(vl1Load1, vl1Bload1);
        g1ForSubstation.addEdge(vl1Bload1, vl1Dload1);
        g1ForSubstation.addEdge(vl1Dload1, vl1Bbs1);

        FeederNode vl1Trf1 = NodeFactory.createFeeder2WTLegNode(g1ForSubstation, "vl1_trf1", "vl1_trf1", "vl1_trf1", ONE);
        vl1Trf1.setOrder(1);
        vl1Trf1.setDirection(BOTTOM);
        vl1Trf1.setX(80);
        vl1Trf1.setY(500);
        SwitchNode vl1Btrf1 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_btrf1", "vl1_btrf1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf1.setX(80);
        vl1Btrf1.setY(400);
        SwitchNode vl1Dtrf1 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_dtrf1", "vl1_dtrf1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf1.setX(80);
        vl1Dtrf1.setY(300);
        g1ForSubstation.addEdge(vl1Trf1, vl1Btrf1);
        g1ForSubstation.addEdge(vl1Btrf1, vl1Dtrf1);
        g1ForSubstation.addEdge(vl1Dtrf1, vl1Bbs1);

        FeederNode vl1Trf2 = NodeFactory.createFeeder3WTLegNodeForSubstationDiagram(g1ForSubstation, "vl1_trf2_one", "vl1_trf2", "vl1_trf2", ONE);
        vl1Trf2.setOrder(2);
        vl1Trf2.setDirection(TOP);
        vl1Trf2.setX(400);
        vl1Trf2.setY(80);
        SwitchNode vl1Btrf2 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_btrf2", "vl1_btrf2", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf2.setX(400);
        vl1Btrf2.setY(180);
        SwitchNode vl1Dtrf2 = NodeFactory.createSwitchNode(g1ForSubstation, "vl1_dtrf2", "vl1_dtrf2", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf2.setX(400);
        vl1Dtrf2.setY(300);
        g1ForSubstation.addEdge(vl1Trf2, vl1Btrf2);
        g1ForSubstation.addEdge(vl1Btrf2, vl1Dtrf2);
        g1ForSubstation.addEdge(vl1Dtrf2, vl1Bbs2);

        // Second voltage level graph :
        //
        VoltageLevelInfos vl2Infos = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelGraph g2ForSubstation = new VoltageLevelGraph(vl2Infos, substG);
        g2ForSubstation.setCoord(590, 40);

        BusNode vl2Bbs1 = NodeFactory.createBusNode(g2ForSubstation, "vl2_bbs1", "vl2_bbs1");
        vl2Bbs1.setX(0);
        vl2Bbs1.setY(300);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        FeederNode vl2Gen1 = NodeFactory.createGenerator(g2ForSubstation, "vl2_gen1", "vl2_gen1");
        vl2Gen1.setOrder(0);
        vl2Gen1.setDirection(TOP);
        vl2Gen1.setX(50);
        vl2Gen1.setY(80);
        SwitchNode vl2Bgen1 = NodeFactory.createSwitchNode(g2ForSubstation, "vl2_bgen1", "vl2_bgen1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl2Bgen1.setX(50);
        vl2Bgen1.setY(180);
        SwitchNode vl2Dgen1 = NodeFactory.createSwitchNode(g2ForSubstation, "vl2_dgen1", "vl2_dgen1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dgen1.setX(50);
        vl2Dgen1.setY(300);
        g2ForSubstation.addEdge(vl2Gen1, vl2Bgen1);
        g2ForSubstation.addEdge(vl2Bgen1, vl2Dgen1);
        g2ForSubstation.addEdge(vl2Dgen1, vl2Bbs1);

        FeederNode vl2Trf1 = NodeFactory.createFeeder2WTLegNode(g2ForSubstation, "vl2_trf1", "vl2_trf1", "vl2_trf1", ONE);
        vl2Trf1.setOrder(1);
        vl2Trf1.setDirection(BOTTOM);
        vl2Trf1.setX(100);
        vl2Trf1.setY(500);
        SwitchNode vl2Btrf1 = NodeFactory.createSwitchNode(g2ForSubstation, "vl2_btrf1", "vl2_btrf1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf1.setX(100);
        vl2Btrf1.setY(400);
        SwitchNode vl2Dtrf1 = NodeFactory.createSwitchNode(g2ForSubstation, "vl2_dtrf1", "vl2_dtrf1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf1.setX(100);
        vl2Dtrf1.setY(300);
        g2ForSubstation.addEdge(vl2Trf1, vl2Btrf1);
        g2ForSubstation.addEdge(vl2Btrf1, vl2Dtrf1);
        g2ForSubstation.addEdge(vl2Dtrf1, vl2Bbs1);

        FeederNode vl2Trf2 = NodeFactory.createFeeder3WTLegNodeForSubstationDiagram(g2ForSubstation, "vl2_trf2_one", "vl2_trf2", "vl2_trf2", TWO);
        vl2Trf2.setOrder(2);
        vl2Trf2.setDirection(TOP);
        vl2Trf2.setX(160);
        vl2Trf2.setY(80);
        SwitchNode vl2Btrf2 = NodeFactory.createSwitchNode(g2ForSubstation, "vl2_btrf2", "vl2_btrf2", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf2.setX(160);
        vl2Btrf2.setY(180);
        SwitchNode vl2Dtrf2 = NodeFactory.createSwitchNode(g2ForSubstation, "vl2_dtrf2", "vl2_dtrf2", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf2.setX(160);
        vl2Dtrf2.setY(300);
        g2ForSubstation.addEdge(vl2Trf2, vl2Btrf2);
        g2ForSubstation.addEdge(vl2Btrf2, vl2Dtrf2);
        g2ForSubstation.addEdge(vl2Dtrf2, vl2Bbs1);

        // Third voltage level graph :
        //
        VoltageLevelInfos vl3Infos = new VoltageLevelInfos("vl3", "vl3", 63);
        VoltageLevelGraph g3ForSubstation = new VoltageLevelGraph(vl3Infos, substG);
        g3ForSubstation.setCoord(890, 40);

        BusNode vl3Bbs1 = NodeFactory.createBusNode(g3ForSubstation, "vl3_bbs1", "vl3_bbs1");
        vl3Bbs1.setX(0);
        vl3Bbs1.setY(300);
        vl3Bbs1.setPxWidth(200);
        vl3Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        FeederNode vl3Capa1 = NodeFactory.createCapacitor(g3ForSubstation, "vl3_capa1", "vl3_capa1");
        vl3Capa1.setOrder(0);
        vl3Capa1.setDirection(TOP);
        vl3Capa1.setX(40);
        vl3Capa1.setY(80);
        SwitchNode vl3Bcapa1 = NodeFactory.createSwitchNode(g3ForSubstation, "vl3_bcapa1", "vl3_bcapa1", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl3Bcapa1.setX(40);
        vl3Bcapa1.setY(180);
        SwitchNode vl3Dcapa1 = NodeFactory.createSwitchNode(g3ForSubstation, "vl3_dcapa1", "vl3_dcapa1", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dcapa1.setX(40);
        vl3Dcapa1.setY(300);
        g3ForSubstation.addEdge(vl3Capa1, vl3Bcapa1);
        g3ForSubstation.addEdge(vl3Bcapa1, vl3Dcapa1);
        g3ForSubstation.addEdge(vl3Dcapa1, vl3Bbs1);

        FeederNode vl3Trf2 = NodeFactory.createFeeder3WTLegNodeForSubstationDiagram(g3ForSubstation, "vl3_trf2_one", "vl3_trf2", "vl3_trf2", THREE);
        vl3Trf2.setOrder(1);
        vl3Trf2.setDirection(TOP);
        vl3Trf2.setX(150);
        vl3Trf2.setY(80);
        SwitchNode vl3Btrf2 = NodeFactory.createSwitchNode(g3ForSubstation, "vl3_btrf2", "vl3_btrf2", ComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);
        vl3Btrf2.setX(150);
        vl3Btrf2.setY(180);
        SwitchNode vl3Dtrf2 = NodeFactory.createSwitchNode(g3ForSubstation, "vl3_dtrf2", "vl3_dtrf2", ComponentTypeName.DISCONNECTOR, false, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dtrf2.setX(150);
        vl3Dtrf2.setY(300);
        g3ForSubstation.addEdge(vl3Trf2, vl3Btrf2);
        g3ForSubstation.addEdge(vl3Btrf2, vl3Dtrf2);
        g3ForSubstation.addEdge(vl3Dtrf2, vl3Bbs1);

        // Substation graph :
        //
        substG.addVoltageLevel(g1ForSubstation);
        substG.addVoltageLevel(g2ForSubstation);
        substG.addVoltageLevel(g3ForSubstation);
        Middle2WTNode nMulti1 = new Middle2WTNode(vl1Trf1.getEquipmentId(), vl1Trf1.getEquipmentId(), vl1Infos, vl2Infos, TWO_WINDINGS_TRANSFORMER);
        nMulti1.setCoordinates(405., 590.);
        nMulti1.setOrientation(Orientation.LEFT);
        BranchEdge edge1 = substG.addTwtEdge(vl1Trf1, nMulti1);
        edge1.setSnakeLine(Point.createPointsList(120., 540., 120., 590., 405., 590.));
        BranchEdge edge2 = substG.addTwtEdge(vl2Trf1, nMulti1);
        edge2.setSnakeLine(Point.createPointsList(690., 540., 690., 590., 405., 590.));
        substG.addMultiTermNode(nMulti1);

        Middle3WTNode nMulti3 = new Middle3WTNode(vl1Trf2.getEquipmentId(), vl1Trf2.getEquipmentId(), vl1Infos, vl2Infos, vl3Infos, false);
        nMulti3.setCoordinates(750., 90.);
        nMulti3.setWindingOrder(Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_RIGHT);
        BranchEdge edge21 = substG.addTwtEdge(vl1Trf2, nMulti3);
        edge21.setSnakeLine(Point.createPointsList(440., 120., 440., 90., 750., 90.));
        BranchEdge edge22 = substG.addTwtEdge(vl2Trf2, nMulti3);
        edge22.setSnakeLine(Point.createPointsList(750., 120., 750., 90.));
        BranchEdge edge23 = substG.addTwtEdge(vl3Trf2, nMulti3);
        edge23.setSnakeLine(Point.createPointsList(1040., 120., 1040., 90., 750., 90.));

        substG.addMultiTermNode(nMulti3);

        substG.setSize(1090, 580);
    }

    private void createZoneGraph() {
        VoltageLevelInfos vl11Infos = new VoltageLevelInfos(VOLTAGE_LEVEL_11_ID, VOLTAGE_LEVEL_11_ID, VOLTAGE_LEVEL_11_V);
        VoltageLevelInfos vl12Infos = new VoltageLevelInfos(VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_V);
        VoltageLevelInfos vl21Infos = new VoltageLevelInfos(VOLTAGE_LEVEL_21_ID, VOLTAGE_LEVEL_21_ID, VOLTAGE_LEVEL_21_V);

        SubstationGraph s1Graph = SubstationGraph.create(SUBSTATION_1_ID);
        SubstationGraph s2Graph = SubstationGraph.create(SUBSTATION_2_ID);
        zGraph = ZoneGraph.create(Arrays.asList(SUBSTATION_1_ID, SUBSTATION_2_ID));

        // create first voltage level graph
        VoltageLevelGraph vl11Graph = new VoltageLevelGraph(vl11Infos, s1Graph);
        vl11Graph.setCoord(40, 40);
        BusNode bus11Node = NodeFactory.createBusNode(vl11Graph, BUS_11_ID, BUS_11_ID);
        bus11Node.setCoordinates(30, 160);
        bus11Node.setPxWidth(40);
        FeederNode loadNode = NodeFactory.createLoad(vl11Graph, LOAD_ID, LOAD_ID);
        loadNode.setCoordinates(50, 10);
        FeederNode twtSide1Node = NodeFactory.createFeeder2WTLegNode(vl11Graph, TRANSFORMER_ID + "_" + Side.ONE, TRANSFORMER_ID, TRANSFORMER_ID, ONE);
        twtSide1Node.setCoordinates(50, 260);
        vl11Graph.addEdge(bus11Node, loadNode);
        vl11Graph.addEdge(bus11Node, twtSide1Node);

        // create second voltage level graph
        VoltageLevelGraph vl12Graph = new VoltageLevelGraph(vl12Infos, s1Graph);
        vl12Graph.setCoord(40, 390);
        BusNode bus12Node = NodeFactory.createBusNode(vl12Graph, BUS_12_ID, BUS_12_ID);
        bus12Node.setCoordinates(30, 110);
        bus12Node.setPxWidth(40);
        FeederNode twtSide2Node = NodeFactory.createFeeder2WTLegNode(vl12Graph, TRANSFORMER_ID + "_" + Side.TWO, TRANSFORMER_ID, TRANSFORMER_ID, TWO);
        twtSide2Node.setCoordinates(50, 10);
        FeederNode lineSide1Node = NodeFactory.createFeederLineNode(vl12Graph, LINE_ID + "_" + Side.ONE, LINE_ID, LINE_ID, ONE, vl21Infos);
        lineSide1Node.setCoordinates(50, 260);
        vl12Graph.addEdge(bus12Node, twtSide2Node);
        vl12Graph.addEdge(bus12Node, lineSide1Node);

        // create third voltage level graph
        VoltageLevelGraph vl21Graph = new VoltageLevelGraph(vl21Infos, s1Graph);
        vl21Graph.setCoord(140, 940);
        BusNode bus21Node = NodeFactory.createBusNode(vl21Graph, BUS_21_ID, BUS_21_ID);
        bus21Node.setCoordinates(30, 160);
        bus21Node.setPxWidth(40);
        FeederNode genNode = NodeFactory.createGenerator(vl21Graph, GENERATOR_ID, GENERATOR_ID);
        genNode.setCoordinates(50, 310);
        FeederNode lineSide2Node = NodeFactory.createFeederLineNode(vl21Graph, LINE_ID + "_" + Side.TWO, LINE_ID, LINE_ID, TWO, vl12Infos);
        lineSide2Node.setCoordinates(50, 10);
        vl21Graph.addEdge(bus21Node, genNode);
        vl21Graph.addEdge(bus21Node, lineSide2Node);

        // build first substation graph
        s1Graph.addVoltageLevel(vl11Graph);
        s1Graph.addVoltageLevel(vl12Graph);
        twtSide1Node.setLabel(TRANSFORMER_ID);
        twtSide2Node.setLabel(TRANSFORMER_ID);
        Middle2WTNode nMulti1 = new Middle2WTNode(twtSide1Node.getEquipmentId(), twtSide1Node.getEquipmentId(), vl12Infos, vl11Infos, TWO_WINDINGS_TRANSFORMER);
        nMulti1.setCoordinates(90, 350);
        BranchEdge edge1 = s1Graph.addTwtEdge(twtSide1Node, nMulti1);
        edge1.setSnakeLine(Point.createPointsList(90., 300., 90., 320., 90., 350.));
        BranchEdge edge2 = s1Graph.addTwtEdge(twtSide2Node, nMulti1);
        edge2.setSnakeLine(Point.createPointsList(90., 400., 90., 380., 90., 350.));
        s1Graph.addMultiTermNode(nMulti1);

        // build second substation graph
        s2Graph.addVoltageLevel(vl21Graph);

        // build zone graph
        zGraph.addSubstation(s1Graph);
        zGraph.addSubstation(s2Graph);
        zGraph.addLineEdge(LINE_ID, lineSide1Node, lineSide2Node);
        zGraph.getLineEdge(LINE_ID).setSnakeLine(Point.createPointsList(90, 650, 90, 800, 190, 800, 190, 950));

        zGraph.setSize(240, 1300);
    }

    @BeforeEach
    public void setUp() {
        createVoltageLevelGraphs();
        createSubstationGraph();
        createZoneGraph();

        // Layout parameters :
        svgParameters.setShowGrid(false); // grid is only for SVG generated with a CellDetector

        // initValueProvider example for the test :
        //
        labelProviderFactory = new DefaultLabelProviderFactory() {

            @Override
            public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
                return new DefaultLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters) {

                    @Override
                    public List<FeederInfo> getFeederInfos(FeederNode node) {
                        List<FeederInfo> feederInfos = Arrays.asList(
                                new DirectionalFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, null, "10", null),
                                new DirectionalFeederInfo(ARROW_REACTIVE, LabelDirection.IN, null, "20", null));
                        boolean feederArrowSymmetry = node.getDirection() == TOP || svgParameters.isFeederInfoSymmetry();
                        if (!feederArrowSymmetry) {
                            Collections.reverse(feederInfos);
                        }
                        return feederInfos;
                    }

                    @Override
                    public List<LabelProvider.NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                        return new ArrayList<>();
                    }
                };
            }
        };

        // no feeder value provider example for the test :
        labelNoFeederInfoProviderFactory = new DefaultLabelProviderFactory() {

            @Override
            public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
                return new DefaultLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters) {

                    @Override
                    public List<FeederInfo> getFeederInfos(FeederNode node) {
                        return Collections.emptyList();
                    }

                    @Override
                    public List<LabelProvider.NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                        return new ArrayList<>();
                    }
                };
            }
        };

        // no feeder value provider example for the test :
        //
        diagramLabelMultiLineTooltipProviderFactory = new DefaultLabelProviderFactory() {
            @Override
            public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
                return new DefaultLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters) {

                    @Override
                    public String getTooltip(Node node) {
                        String tooltip = node.getId();
                        if (node.getType() == Node.NodeType.FEEDER) {
                            tooltip += "\n" + node.getComponentType();
                        }
                        return tooltip;
                    }

                    @Override
                    public List<NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                        return Collections.emptyList();
                    }
                };
            }
        };

        diagramLabelSameNodeProviderFactory = new DefaultLabelProviderFactory() {
            @Override
            public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
                return new DefaultLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters) {

                    @Override
                    public List<NodeLabel> getNodeLabels(Node node, Direction direction) {
                        LabelPosition labelPosition = new LabelPosition("default", 0, -5, true, 0);
                        return Collections.singletonList(new LabelProvider.NodeLabel("Tests", labelPosition, null));
                    }

                    @Override
                    public List<LabelProvider.NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                        return new ArrayList<>();
                    }
                };
            }
        };
    }

    @Test
    void testVl1() {
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl1.svg"),
                toSVG(g1, "/vl1.svg", config));
    }

    @Test
    void testVl1CssExternalImported() {
        svgParameters.setCssLocation(SvgParameters.CssLocation.EXTERNAL_IMPORTED);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl1_external_css.svg"),
                toSVG(g1, "/vl1_external_css.svg", config));
    }

    @Test
    void testVl1CssExternalNoImport() {
        svgParameters.setCssLocation(SvgParameters.CssLocation.EXTERNAL_NO_IMPORT);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl1_external_css_no_import.svg"),
                toSVG(g1, "/vl1_external_css_no_import.svg", config));
    }

    @Test
    void testVl2() {
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl2.svg"),
                toSVG(g2, "/vl2.svg", config));
    }

    @Test
    void testVl3() {
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl3.svg"),
                toSVG(g3, "/vl3.svg", config));
    }

    @Test
    void testSubstation() {
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new NominalVoltageStyleProvider())
                .build();
        // SVG file generation for substation and comparison to reference
        assertEquals(toString("/substation.svg"),
                toSVG(substG, "/substation.svg", config));
    }

    @Test
    void testSubstationArrowSymmetry() {
        // SVG file generation for substation with symmetric feeder arrow and comparison to reference
        svgParameters.setFeederInfoSymmetry(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/substation_feeder_arrow_symmetry.svg"),
                toSVG(substG, "/substation_feeder_arrow_symmetry.svg", config));
    }

    @Test
    void testSubstationNoFeederInfos() {
        // SVG file generation for substation and comparison to reference but with no feeder values
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelNoFeederInfoProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/substation_no_feeder_values.svg"),
                toSVG(substG, "/substation_no_feeder_values.svg", config));
    }

    @Test
    void testVl1Optimized() {
        // Same tests than before, with optimized svg
        svgParameters.setAvoidSVGComponentsDuplication(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl1_optimized.svg"),
                toSVG(g1, "/vl1_optimized.svg", config));
    }

    @Test
    void testVl2Optimized() {
        // Same tests than before, with optimized svg
        svgParameters.setAvoidSVGComponentsDuplication(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl2_optimized.svg"),
                toSVG(g2, "/vl2_optimized.svg", config));
    }

    @Test
    void testVl3Optimized() {
        // Same tests than before, with optimized svg
        svgParameters.setAvoidSVGComponentsDuplication(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl3_optimized.svg"),
                toSVG(g3, "/vl3_optimized.svg", config));
    }

    @Test
    void testSubstationOptimized() {
        // Same tests than before, with optimized svg
        svgParameters.setAvoidSVGComponentsDuplication(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/substation_optimized.svg"),
                toSVG(substG, "/substation_optimized.svg", config));
    }

    @Test
    void testWriteZone() {
        svgParameters.setShowGrid(false);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/zone.svg"),
                toSVG(zGraph, "/zone.svg", config));
    }

    @Test
    void testStraightWires() {
        svgParameters.setDrawStraightWires(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl1_straightWires.svg"),
                toSVG(g1, "/vl1_straightWires.svg", config));
    }

    @Test
    void testTooltip() {
        svgParameters.setTooltipEnabled(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl1_tooltip.svg"),
                toSVG(g1, "/vl1_tooltip.svg", config));
    }

    @Test
    void testMultiLineTooltip() {

        svgParameters.setAvoidSVGComponentsDuplication(true);
        svgParameters.setTooltipEnabled(true);
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(diagramLabelMultiLineTooltipProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/vl1_multiline_tooltip.svg"),
                toSVG(g1, "/vl1_multiline_tooltip.svg", config));
    }

    @Test
    void testLabelOnAllNodes() {
        // same node label provider example for the test :
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(diagramLabelSameNodeProviderFactory)
                .withStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/label_on_all_nodes.svg"),
                toSVG(g1, "/label_on_all_nodes.svg", config));
    }

    @Test
    void testWithGreyFrameBackground() {
        StyleProvider styleProvider = new BasicStyleProvider() {
            @Override
            public List<String> getCssFilenames() {
                return Arrays.asList("tautologies.css", "baseVoltages.css", "highlightLineStates.css", "TestWithGreyFrameBackground.css");
            }
        };
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withLabelProviderFactory(labelProviderFactory)
                .withStyleProvider(styleProvider)
                .build();
        assertEquals(toString("/with_frame_background.svg"),
                toSVG(g1, "/with_frame_background.svg", config));
    }
}
