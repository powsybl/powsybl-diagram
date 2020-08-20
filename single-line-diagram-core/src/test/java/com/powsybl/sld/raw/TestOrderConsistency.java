/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestOrderConsistency extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs11 = vlBuilder.createBusBarSection("bbs11", 1, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs12", 1, 2);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);

        FeederNode load1 = vlBuilder.createLoad("l1", 0, BusCell.Direction.TOP);
        SwitchNode d11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d11", false, false);
        SwitchNode d12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d12", false, false);
        FictitiousNode f1 = vlBuilder.createFictitiousNode("f1");
        SwitchNode b1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b1", false, false);
        vlBuilder.connectNode(bbs11, d11);
        vlBuilder.connectNode(d11, f1);
        vlBuilder.connectNode(bbs21, d12);
        vlBuilder.connectNode(d12, f1);
        vlBuilder.connectNode(f1, b1);
        vlBuilder.connectNode(b1, load1);

        FeederNode loadMiddle = vlBuilder.createLoad("l", 1, BusCell.Direction.TOP);
        SwitchNode dMiddle1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode dMiddle2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        FictitiousNode fMiddle = vlBuilder.createFictitiousNode("f");
        SwitchNode bMiddle = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        vlBuilder.connectNode(bbs12, dMiddle1);
        vlBuilder.connectNode(dMiddle1, fMiddle);
        vlBuilder.connectNode(bbs21, dMiddle2);
        vlBuilder.connectNode(dMiddle2, fMiddle);
        vlBuilder.connectNode(fMiddle, bMiddle);
        vlBuilder.connectNode(bMiddle, loadMiddle);

        FeederNode load2 = vlBuilder.createLoad("l2", 2, BusCell.Direction.TOP);
        SwitchNode d21 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d21", false, false);
        SwitchNode d22 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d22", false, false);
        FictitiousNode f2 = vlBuilder.createFictitiousNode("f2");
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        vlBuilder.connectNode(bbs12, d21);
        vlBuilder.connectNode(d21, f2);
        vlBuilder.connectNode(bbs22, d22);
        vlBuilder.connectNode(d22, f2);
        vlBuilder.connectNode(f2, b2);
        vlBuilder.connectNode(b2, load2);


    }

    @Test
    public void test() {
        // build graph
        Graph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(50)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(true)
                .setShowInternalNodes(true)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50)
                .setArrowDistance(20);

        new PositionVoltageLevelLayout(g).run(layoutParameters);
        writeFile = true;
        toSVG(g, "/test.svg", layoutParameters, getDiagramLabelProvider(g), new DefaultDiagramStyleProvider());

        // write Json and compare to reference
//        assertEquals(toString("/TestCase1.json"), toJson(g, "/TestCase1.json"));
    }
}
