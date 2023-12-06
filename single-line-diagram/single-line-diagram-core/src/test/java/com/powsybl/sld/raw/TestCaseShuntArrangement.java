/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactoryParameters;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
class TestCaseShuntArrangement extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        int i = 0;
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);
        FeederNode loadA = vlBuilder.createLoad("loadA", i++, TOP);
        FeederNode load = vlBuilder.createLoad("l", i++, TOP);
        FeederNode loadB = vlBuilder.createLoad("loadB", i++, TOP);
        SwitchNode dA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dA", false, false);
        SwitchNode dB1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dB1", false, false);
        SwitchNode dB2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dB2", false, false);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode bA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bA", false, false);
        SwitchNode bB = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bB", false, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        ConnectivityNode f = vlBuilder.createConnectivityNode("f");
        ConnectivityNode fB = vlBuilder.createConnectivityNode("fB");
        ConnectivityNode fShuntA = vlBuilder.createConnectivityNode("fShuntA");
        ConnectivityNode fShuntB = vlBuilder.createConnectivityNode("fShuntB");
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

        FeederNode gen1 = vlBuilder.createGenerator("gen1", i++, TOP);
        SwitchNode bg1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bg1", false, false);
        vlBuilder.connectNode(gen1, bg1);
        ConnectivityNode fg1 = vlBuilder.createConnectivityNode("fg1");
        vlBuilder.connectNode(fg1, bg1);
        SwitchNode dg11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg11", false, false);
        vlBuilder.connectNode(dg11, fg1);
        vlBuilder.connectNode(dg11, bbs1);
        SwitchNode dg12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg12", false, false);
        vlBuilder.connectNode(dg12, fg1);
        vlBuilder.connectNode(dg12, bbs22);

        FeederNode loadC = vlBuilder.createLoad("loadC", i++, TOP);
        SwitchNode bC = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bC", false, false);
        vlBuilder.connectNode(loadC, bC);
        ConnectivityNode fC = vlBuilder.createConnectivityNode("fC");
        vlBuilder.connectNode(fC, bC);
        SwitchNode dC1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dC1", false, false);
        vlBuilder.connectNode(dC1, fC);
        vlBuilder.connectNode(dC1, bbs1);
        SwitchNode dC2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dC2", false, false);
        vlBuilder.connectNode(dC2, fC);
        vlBuilder.connectNode(dC2, bbs22);

        FeederNode loadD = vlBuilder.createLoad("loadD", i++, TOP);
        SwitchNode bD = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bD", false, false);
        vlBuilder.connectNode(loadD, bD);
        ConnectivityNode fD = vlBuilder.createConnectivityNode("fD");
        vlBuilder.connectNode(fD, bD);
        SwitchNode dD1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dD1", false, false);
        vlBuilder.connectNode(dD1, fD);
        vlBuilder.connectNode(dD1, bbs1);
        SwitchNode dD2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dD2", false, false);
        vlBuilder.connectNode(dD2, fD);
        vlBuilder.connectNode(dD2, bbs22);

        FeederNode gen2 = vlBuilder.createGenerator("gen2", i++, TOP);
        SwitchNode bg2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bg2", false, false);
        vlBuilder.connectNode(gen2, bg2);
        ConnectivityNode fg2 = vlBuilder.createConnectivityNode("fg2");
        vlBuilder.connectNode(fg2, bg2);
        SwitchNode dg21 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg21", false, false);
        vlBuilder.connectNode(dg21, fg2);
        vlBuilder.connectNode(dg21, bbs1);
        SwitchNode dg22 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg22", false, false);
        vlBuilder.connectNode(dg22, fg2);
        vlBuilder.connectNode(dg22, bbs22);

        SwitchNode shuntGen = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "shuntGen", false, false);
        vlBuilder.connectNode(fg1, shuntGen);
        vlBuilder.connectNode(fg2, shuntGen);

        BusNode bbs13 = vlBuilder.createBusBarSection("bbs13", 1, 3);
        BusNode bbs23 = vlBuilder.createBusBarSection("bbs23", 2, 3);

        FeederNode loadE = vlBuilder.createLoad("loadE", i++, TOP);
        SwitchNode bE = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bE", false, false);
        vlBuilder.connectNode(loadE, bE);
        ConnectivityNode fE = vlBuilder.createConnectivityNode("fE");
        vlBuilder.connectNode(fE, bE);
        SwitchNode dE1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dE1", false, false);
        vlBuilder.connectNode(dE1, fE);
        vlBuilder.connectNode(dE1, bbs13);
        SwitchNode dE2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dE2", false, false);
        vlBuilder.connectNode(dE2, fE);
        vlBuilder.connectNode(dE2, bbs23);

        ConnectivityNode commonFG = vlBuilder.createConnectivityNode("commonFG");

        FeederNode loadF = vlBuilder.createLoad("loadF", i++, TOP);
        vlBuilder.connectNode(loadF, commonFG);
        SwitchNode bF = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bF", false, false);
        vlBuilder.connectNode(commonFG, bF);
        ConnectivityNode fF = vlBuilder.createConnectivityNode("fF");
        vlBuilder.connectNode(fF, bF);
        SwitchNode dF1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF1", false, false);
        vlBuilder.connectNode(dF1, fF);
        vlBuilder.connectNode(dF1, bbs13);
        SwitchNode dF2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF2", false, false);
        vlBuilder.connectNode(dF2, fF);
        vlBuilder.connectNode(dF2, bbs23);

        FeederNode loadG = vlBuilder.createLoad("loadG", i++, TOP);
        ConnectivityNode fLoadG = vlBuilder.createConnectivityNode("fLoadG");
        vlBuilder.connectNode(loadG, fLoadG);
        SwitchNode bFeederG = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bFeederG", false, false);
        vlBuilder.connectNode(fLoadG, bFeederG);
        vlBuilder.connectNode(bFeederG, commonFG);

        SwitchNode bSwitchCG = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bSwitchCG", false, false);
        vlBuilder.connectNode(fC, bSwitchCG);
        vlBuilder.connectNode(fLoadG, bSwitchCG);

    }

    @Test
    void test1() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        new PositionVoltageLevelLayoutFactory(new PositionVoltageLevelLayoutFactoryParameters().setExceptionIfPatternNotHandled(true))
                .create(g)
                .run(layoutParameters);
        assertEquals(toString("/TestCaseShuntArrangementNo.json"), toJson(g, "/TestCaseShuntArrangementNo.json"));
    }

    @Test
    void test2() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        PositionVoltageLevelLayoutFactoryParameters positionVoltageLevelLayoutFactoryParameters = new PositionVoltageLevelLayoutFactoryParameters()
                .setExceptionIfPatternNotHandled(true)
                .setHandleShunts(true);
        new PositionVoltageLevelLayoutFactory(positionVoltageLevelLayoutFactoryParameters)
                .create(g)
                .run(layoutParameters);
        assertEquals(toString("/TestCaseShuntArrangementYes.json"), toJson(g, "/TestCaseShuntArrangementYes.json"));
    }

}
