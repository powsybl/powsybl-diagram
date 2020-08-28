/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestCaseShuntArrangement extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);
        FeederNode loadA = vlBuilder.createLoad("lA", 0, BusCell.Direction.TOP);
        FeederNode loadB = vlBuilder.createLoad("lA", 2, BusCell.Direction.TOP);
        FeederNode load = vlBuilder.createLoad("l", 1, BusCell.Direction.TOP);
        SwitchNode dA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dA", false, false);
        SwitchNode dB1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dB1", false, false);
        SwitchNode dB2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dB2", false, false);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode bA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bA", false, false);
        SwitchNode bB = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bB", false, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        FictitiousNode f = vlBuilder.createFictitiousNode("f");
        FictitiousNode fB = vlBuilder.createFictitiousNode("fB");
        FictitiousNode fShuntA = vlBuilder.createFictitiousNode("fShuntA");
        FictitiousNode fShuntB = vlBuilder.createFictitiousNode("fShuntB");
        SwitchNode bShunt = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bShunt", false, false);

        vlBuilder.connectNode(bbs1, dA);
        vlBuilder.connectNode(bA, dA);
        vlBuilder.connectNode(bA, fShuntA);
        vlBuilder.connectNode(loadA, fShuntA);

        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(f, d1);
        vlBuilder.connectNode(bbs21, d2);
        vlBuilder.connectNode(f, d2);
        vlBuilder.connectNode(f, b);
        vlBuilder.connectNode(b, load);

        vlBuilder.connectNode(bbs1, dB1);
        vlBuilder.connectNode(bbs22, dB2);
        vlBuilder.connectNode(dB1, fB);
        vlBuilder.connectNode(dB2, fB);
        vlBuilder.connectNode(fB, bB);
        vlBuilder.connectNode(fShuntB, bB);
        vlBuilder.connectNode(fShuntB, loadB);

        vlBuilder.connectNode(fShuntA, bShunt);
        vlBuilder.connectNode(fShuntB, bShunt);
    }

    @Test
    public void test1() {
        Graph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), true, true, false).organize(g);
        new PositionVoltageLevelLayout(g).run(layoutParameters);
        assertEquals(toString("/TestCaseShuntArrangementNo.json"), toJson(g, "/TestCaseShuntArrangementNo.json"));
    }

    @Test
    public void test2() {
        Graph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), true, true, true).organize(g);
        new PositionVoltageLevelLayout(g).run(layoutParameters);
        assertEquals(toString("/TestCaseShuntArrangementYes.json"), toJson(g, "/TestCaseShuntArrangementYes.json"));
    }

}
