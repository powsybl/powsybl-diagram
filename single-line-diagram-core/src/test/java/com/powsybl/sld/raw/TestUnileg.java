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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestUnileg extends AbstractTestCaseRaw {
    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 2, 1);
        SwitchNode d12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d12", false, false);
        vlBuilder.connectNode(bbs1, d12);
        vlBuilder.connectNode(d12, bbs2);

        FictitiousNode fNode = vlBuilder.createFictitiousNode("Fictitious_unileg");
        SwitchNode unilegDc1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF", false, false);
        SwitchNode unilegDc2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF", false, false);
        vlBuilder.connectNode(bbs1, unilegDc1);
        vlBuilder.connectNode(bbs2, unilegDc2);
        vlBuilder.connectNode(unilegDc1, fNode);
        vlBuilder.connectNode(unilegDc2, fNode);
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

        // write Json and compare to reference
        assertEquals(toString("/testUnileg.json"), toJson(g, "/testUnileg.json"));
    }
}
