/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Position;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.InitialValue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestSVGWriter extends AbstractTestCase {

    private Graph g1;
    private Graph g2;
    private Graph g3;
    private SubstationGraph substG;

    @Before
    public void setUp() {
        // Creation "by hand" (without any network) of 3 voltage level graph and one substation graph
        // and then generation of a SVG with DefaultDiagramStyleProvider (no network necessary)
        //

        // First voltage level graph :
        //
        g1 = Graph.create("vl1", "vl1", 400, false, true, false);
        g1.setX(0);
        g1.setY(20);

        BusNode vl1Bbs1 = BusNode.create(g1, "vl1_bbs1", "vl1_bbs1");
        vl1Bbs1.setX(0);
        vl1Bbs1.setY(300);
        vl1Bbs1.setPxWidth(200);
        vl1Bbs1.setPosition(new Position(0, 1, 2, 0, false, null));
        g1.addNode(vl1Bbs1);
        BusNode vl1Bbs2 = BusNode.create(g1, "vl1_bbs2", "vl1_bbs2");
        vl1Bbs2.setX(280);
        vl1Bbs2.setY(300);
        vl1Bbs2.setPxWidth(200);
        vl1Bbs2.setPosition(new Position(3, 1, 3, 0, false, null));
        g1.addNode(vl1Bbs2);
        SwitchNode vl1D1 = new SwitchNode("vl1_d1", "vl1_d1", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D1.setX(220);
        vl1D1.setY(300);
        g1.addNode(vl1D1);
        SwitchNode vl1B1 = new SwitchNode("vl1_b1", "vl1_b1", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1B1.setRotationAngle(90.);
        vl1B1.setX(245);
        vl1B1.setY(300);
        g1.addNode(vl1B1);
        SwitchNode vl1D2 = new SwitchNode("vl1_d2", "vl1_d2", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D2.setX(270);
        vl1D2.setY(300);
        g1.addNode(vl1D2);
        g1.addEdge(vl1Bbs1, vl1D1);
        g1.addEdge(vl1D1, vl1B1);
        g1.addEdge(vl1B1, vl1D2);
        g1.addEdge(vl1D2, vl1Bbs2);

        FeederNode vl1Load1 = new FeederNode("vl1_load1", "vl1_load1", ComponentTypeName.LOAD, false, g1);
        vl1Load1.setOrder(0);
        vl1Load1.setDirection(BusCell.Direction.TOP);
        vl1Load1.setX(40);
        vl1Load1.setY(80);
        g1.addNode(vl1Load1);
        SwitchNode vl1Bload1 = new SwitchNode("vl1_bload1", "vl1_bload1", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1Bload1.setX(40);
        vl1Bload1.setY(180);
        g1.addNode(vl1Bload1);
        SwitchNode vl1Dload1 = new SwitchNode("vl1_dload1", "vl1_dload1", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dload1.setX(40);
        vl1Dload1.setY(300);
        g1.addNode(vl1Dload1);
        g1.addEdge(vl1Load1, vl1Bload1);
        g1.addEdge(vl1Bload1, vl1Dload1);
        g1.addEdge(vl1Dload1, vl1Bbs1);

        Feeder2WTNode vl1Trf1 = new Feeder2WTNode("vl1_trf1", "vl1_trf1", ComponentTypeName.TWO_WINDINGS_TRANSFORMER, false, g1, "vl2", 225);
        vl1Trf1.setOrder(1);
        vl1Trf1.setDirection(BusCell.Direction.BOTTOM);
        vl1Trf1.setX(80);
        vl1Trf1.setY(500);
        g1.addNode(vl1Trf1);
        SwitchNode vl1Btrf1 = new SwitchNode("vl1_btrf1", "vl1_btrf1", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf1.setX(80);
        vl1Btrf1.setY(400);
        g1.addNode(vl1Btrf1);
        SwitchNode vl1Dtrf1 = new SwitchNode("vl1_dtrf1", "vl1_dtrf1", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf1.setX(80);
        vl1Dtrf1.setY(300);
        g1.addNode(vl1Dtrf1);
        g1.addEdge(vl1Trf1, vl1Btrf1);
        g1.addEdge(vl1Btrf1, vl1Dtrf1);
        g1.addEdge(vl1Dtrf1, vl1Bbs1);

        Feeder2WTNode vl1Trf2One = new Feeder2WTNode("vl1_trf2_one", "vl1_trf2_one", ComponentTypeName.LINE, false, g1, "vl2", 225);
        vl1Trf2One.setOrder(2);
        vl1Trf2One.setDirection(BusCell.Direction.TOP);
        vl1Trf2One.setX(360);
        vl1Trf2One.setY(80);
        g1.addNode(vl1Trf2One);
        Feeder2WTNode vl1Trf2Two = new Feeder2WTNode("vl1_trf2_two", "vl1_trf2_two", ComponentTypeName.LINE, false, g1, "vl3", 63);
        vl1Trf2Two.setOrder(3);
        vl1Trf2Two.setDirection(BusCell.Direction.TOP);
        vl1Trf2Two.setX(440);
        vl1Trf2Two.setY(80);
        g1.addNode(vl1Trf2Two);
        Fictitious3WTNode vl1Trf2Fict = new Fictitious3WTNode(g1, "vl1_trf2", "vl1_trf2");
        vl1Trf2Fict.setX(400);
        vl1Trf2Fict.setY(140);
        g1.addNode(vl1Trf2Fict);
        g1.addEdge(vl1Trf2One, vl1Trf2Fict);
        g1.addEdge(vl1Trf2Two, vl1Trf2Fict);
        SwitchNode vl1Btrf2 = new SwitchNode("vl1_btrf2", "vl1_btrf2", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf2.setX(400);
        vl1Btrf2.setY(180);
        g1.addNode(vl1Btrf2);
        SwitchNode vl1Dtrf2 = new SwitchNode("vl1_dtrf2", "vl1_dtrf2", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf2.setX(400);
        vl1Dtrf2.setY(300);
        g1.addNode(vl1Dtrf2);
        g1.addEdge(vl1Trf2Fict, vl1Btrf2);
        g1.addEdge(vl1Btrf2, vl1Dtrf2);
        g1.addEdge(vl1Dtrf2, vl1Bbs2);

        // Second voltage level graph :
        //
        g2 = Graph.create("vl2", "vl2", 225, false, true, false);
        g2.setX(550);
        g2.setY(20);

        BusNode vl2Bbs1 = BusNode.create(g2, "vl2_bbs1", "vl2_bbs1");
        vl2Bbs1.setX(0);
        vl2Bbs1.setY(300);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPosition(new Position(0, 1, 3, 0, false, null));
        g2.addNode(vl2Bbs1);
        FeederNode vl2Gen1 = new FeederNode("vl2_gen1", "vl2_gen1", ComponentTypeName.GENERATOR, false, g2);
        vl2Gen1.setOrder(0);
        vl2Gen1.setDirection(BusCell.Direction.TOP);
        vl2Gen1.setX(50);
        vl2Gen1.setY(80);
        g2.addNode(vl2Gen1);
        SwitchNode vl2Bgen1 = new SwitchNode("vl2_bgen1", "vl2_bgen1", ComponentTypeName.BREAKER, false, g2, SwitchNode.SwitchKind.BREAKER, false);
        vl2Bgen1.setX(50);
        vl2Bgen1.setY(180);
        g2.addNode(vl2Bgen1);
        SwitchNode vl2Dgen1 = new SwitchNode("vl2_dgen1", "vl2_dgen1", ComponentTypeName.DISCONNECTOR, false, g2, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dgen1.setX(50);
        vl2Dgen1.setY(300);
        g2.addNode(vl2Dgen1);
        g2.addEdge(vl2Gen1, vl2Bgen1);
        g2.addEdge(vl2Bgen1, vl2Dgen1);
        g2.addEdge(vl2Dgen1, vl2Bbs1);

        Feeder2WTNode vl2Trf1 = new Feeder2WTNode("vl2_trf1", "vl2_trf1", ComponentTypeName.TWO_WINDINGS_TRANSFORMER, false, g2, "vl1", 400);
        vl2Trf1.setOrder(1);
        vl2Trf1.setDirection(BusCell.Direction.BOTTOM);
        vl2Trf1.setX(100);
        vl2Trf1.setY(500);
        g2.addNode(vl2Trf1);
        SwitchNode vl2Btrf1 = new SwitchNode("vl2_btrf1", "vl2_btrf1", ComponentTypeName.BREAKER, false, g2, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf1.setX(100);
        vl2Btrf1.setY(400);
        g2.addNode(vl2Btrf1);
        SwitchNode vl2Dtrf1 = new SwitchNode("vl2_dtrf1", "vl2_dtrf1", ComponentTypeName.DISCONNECTOR, false, g2, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf1.setX(100);
        vl2Dtrf1.setY(300);
        g2.addNode(vl2Dtrf1);
        g2.addEdge(vl2Trf1, vl2Btrf1);
        g2.addEdge(vl2Btrf1, vl2Dtrf1);
        g2.addEdge(vl2Dtrf1, vl2Bbs1);

        Feeder2WTNode vl2Trf2One = new Feeder2WTNode("vl2_trf2_one", "vl2_trf2_one", ComponentTypeName.LINE, false, g2, "vl1", 400);
        vl2Trf2One.setOrder(2);
        vl2Trf2One.setDirection(BusCell.Direction.TOP);
        vl2Trf2One.setX(130);
        vl2Trf2One.setY(80);
        g2.addNode(vl2Trf2One);
        Feeder2WTNode vl2Trf2Two = new Feeder2WTNode("vl2_trf2_two", "vl2_trf2_two", ComponentTypeName.LINE, false, g2, "vl3", 63);
        vl2Trf2Two.setOrder(3);
        vl2Trf2Two.setDirection(BusCell.Direction.TOP);
        vl2Trf2Two.setX(190);
        vl2Trf2Two.setY(80);
        g2.addNode(vl2Trf2Two);
        Fictitious3WTNode vl2Trf2Fict = new Fictitious3WTNode(g2, "vl2_trf2", "vl2_trf2");
        vl2Trf2Fict.setX(160);
        vl2Trf2Fict.setY(140);
        g2.addNode(vl2Trf2Fict);
        g2.addEdge(vl2Trf2One, vl2Trf2Fict);
        g2.addEdge(vl2Trf2Two, vl2Trf2Fict);
        SwitchNode vl2Btrf2 = new SwitchNode("vl2_btrf2", "vl2_btrf2", ComponentTypeName.BREAKER, false, g2, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf2.setX(160);
        vl2Btrf2.setY(180);
        g2.addNode(vl2Btrf2);
        SwitchNode vl2Dtrf2 = new SwitchNode("vl2_dtrf2", "vl2_dtrf2", ComponentTypeName.DISCONNECTOR, false, g2, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf2.setX(160);
        vl2Dtrf2.setY(300);
        g2.addNode(vl2Dtrf2);
        g2.addEdge(vl2Trf2Fict, vl2Btrf2);
        g2.addEdge(vl2Btrf2, vl2Dtrf2);
        g2.addEdge(vl2Dtrf2, vl2Bbs1);

        // Third voltage level graph :
        //
        g3 = Graph.create("vl3", "vl3", 63, false, true, false);
        g3.setX(850);
        g3.setY(20);

        BusNode vl3Bbs1 = BusNode.create(g3, "vl3_bbs1", "vl3_bbs1");
        vl3Bbs1.setX(0);
        vl3Bbs1.setY(300);
        vl3Bbs1.setPxWidth(200);
        vl3Bbs1.setPosition(new Position(0, 1, 3, 0, false, null));
        g3.addNode(vl3Bbs1);
        FeederNode vl3Capa1 = new FeederNode("vl3_capa1", "vl3_capa1", ComponentTypeName.CAPACITOR, false, g3);
        vl3Capa1.setOrder(0);
        vl3Capa1.setDirection(BusCell.Direction.TOP);
        vl3Capa1.setX(40);
        vl3Capa1.setY(80);
        g3.addNode(vl3Capa1);
        SwitchNode vl3Bcapa1 = new SwitchNode("vl3_bcapa1", "vl3_bcapa1", ComponentTypeName.BREAKER, false, g3, SwitchNode.SwitchKind.BREAKER, false);
        vl3Bcapa1.setX(40);
        vl3Bcapa1.setY(180);
        g3.addNode(vl3Bcapa1);
        SwitchNode vl3Dcapa1 = new SwitchNode("vl3_dcapa1", "vl3_dcapa1", ComponentTypeName.DISCONNECTOR, false, g3, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dcapa1.setX(40);
        vl3Dcapa1.setY(300);
        g3.addNode(vl3Dcapa1);
        g3.addEdge(vl3Capa1, vl3Bcapa1);
        g3.addEdge(vl3Bcapa1, vl3Dcapa1);
        g3.addEdge(vl3Dcapa1, vl3Bbs1);

        Feeder2WTNode vl3Trf2One = new Feeder2WTNode("vl3_trf2_one", "vl3_trf2_one", ComponentTypeName.LINE, false, g3, "vl1", 400);
        vl3Trf2One.setOrder(1);
        vl3Trf2One.setDirection(BusCell.Direction.TOP);
        vl3Trf2One.setX(110);
        vl3Trf2One.setY(80);
        g3.addNode(vl3Trf2One);
        Feeder2WTNode vl3Trf2Two = new Feeder2WTNode("vl3_trf2_two", "vl3_trf2_two", ComponentTypeName.LINE, false, g3, "vl2", 225);
        vl3Trf2Two.setOrder(2);
        vl3Trf2Two.setDirection(BusCell.Direction.TOP);
        vl3Trf2Two.setX(190);
        vl3Trf2Two.setY(80);
        g3.addNode(vl3Trf2Two);
        Fictitious3WTNode vl3Trf2Fict = new Fictitious3WTNode(g3, "vl3_trf2", "vl3_trf2");
        vl3Trf2Fict.setX(150);
        vl3Trf2Fict.setY(140);
        g3.addNode(vl3Trf2Fict);
        g3.addEdge(vl3Trf2One, vl3Trf2Fict);
        g3.addEdge(vl3Trf2Two, vl3Trf2Fict);
        SwitchNode vl3Btrf2 = new SwitchNode("vl3_btrf2", "vl3_btrf2", ComponentTypeName.BREAKER, false, g3, SwitchNode.SwitchKind.BREAKER, false);
        vl3Btrf2.setX(150);
        vl3Btrf2.setY(180);
        g3.addNode(vl3Btrf2);
        SwitchNode vl3Dtrf2 = new SwitchNode("vl3_dtrf2", "vl3_dtrf2", ComponentTypeName.DISCONNECTOR, false, g3, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dtrf2.setX(150);
        vl3Dtrf2.setY(300);
        g3.addNode(vl3Dtrf2);
        g3.addEdge(vl3Trf2Fict, vl3Btrf2);
        g3.addEdge(vl3Btrf2, vl3Dtrf2);
        g3.addEdge(vl3Dtrf2, vl3Bbs1);

        // Substation graph :
        //
        substG = SubstationGraph.create("subst");
        substG.addNode(g1);
        substG.addNode(g2);
        substG.addNode(g3);
        substG.addEdge(vl1Trf1, vl2Trf1);
        substG.getEdges().get(substG.getEdges().size() - 1).setSnakeLine(Arrays.asList(80., 500., 80., 550., 650., 550., 650., 500.));
        substG.addEdge(vl1Trf2One, vl2Trf2One);
        substG.getEdges().get(substG.getEdges().size() - 1).setSnakeLine(Arrays.asList(360., 80., 360., 50., 680., 50., 680., 80.));
        substG.addEdge(vl1Trf2Two, vl3Trf2One);
        substG.getEdges().get(substG.getEdges().size() - 1).setSnakeLine(Arrays.asList(440., 80., 440., 30., 960., 30., 960., 80.));
        substG.addEdge(vl2Trf2Two, vl3Trf2Two);
        substG.getEdges().get(substG.getEdges().size() - 1).setSnakeLine(Arrays.asList(740., 80., 740., 10., 1040., 10., 1040., 80.));
    }

    @Test
    public void test() {
        // initValueProvider example for the test :
        //
        DiagramInitialValueProvider initValueProvider = new DiagramInitialValueProvider() {
            @Override
            public InitialValue getInitialValue(Node node) {
                InitialValue initialValue;
                if (node.getType() == Node.NodeType.BUS) {
                    initialValue = new InitialValue(null, null, node.getLabel(), null, null, null);
                } else {
                    initialValue = new InitialValue(Direction.UP, Direction.DOWN, "10", "20", null, null);
                }
                return initialValue;
            }

            @Override
            public List<String> getNodeLabelValue(Node node) {
                List<String> res = new ArrayList<>();
                if (node instanceof FeederNode || node instanceof BusNode) {
                    res.add(node.getLabel());
                }
                return res;
            }
        };

        // Layout parameters :
        //
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(80)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(true)
                .setShowInternalNodes(false)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50)
                .setDrawStraightWires(false)
                .setHorizontalSnakeLinePadding(30)
                .setVerticalSnakeLinePadding(30)
                .setShowInductorFor3WT(true);

        Map<String, Graph> mapGr = new HashMap<>();
        mapGr.put("vl1.svg", g1);
        mapGr.put("vl2.svg", g2);
        mapGr.put("vl3.svg", g3);

        for (String filename : mapGr.keySet()) {
            // SVG file generation first voltage level and comparison to reference :
            assertEquals(toSVG(mapGr.get(filename), layoutParameters, initValueProvider), toString("/" + filename));
        }

        // SVG file generation for substation and comparison to reference
        assertEquals(toSVG(substG, layoutParameters, initValueProvider), toString("/substation.svg"));
    }
}
