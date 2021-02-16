/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.BusbarSectionPositionAdder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestInternalBranchesNodeBreaker extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters();
    }

    @Before
    public void setUp() {
        network = Network.create("TestInternalBranchesNodeBreaker", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "S", "S", Country.FR);

        VoltageLevel vl1 = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        BusbarSection bbs = vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS11")
                .setNode(10)
                .add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1).add();

        bbs = vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS12")
                .setNode(20)
                .add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(1).add();

        vl1.getNodeBreakerView().newDisconnector()
                .setId("D10")
                .setNode1(10)
                .setNode2(120)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR1")
                .setNode1(120)
                .setNode2(121)
                .setOpen(false)
                .setRetained(true)
                .add();
        vl1.getNodeBreakerView().newDisconnector()
                .setId("D20")
                .setNode1(121)
                .setNode2(20)
                .setOpen(false)
                .add();

        vl1.newLoad()
                .setId("L1")
                .setNode(12)
                .setP0(1)
                .setQ0(1)
                .add();
        vl1.getNodeBreakerView().newDisconnector()
                .setId("D11")
                .setNode1(10)
                .setNode2(11)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR12")
                .setNode1(11)
                .setNode2(12)
                .setOpen(false)
                .add();

        vl1.newGenerator()
                .setId("G")
                .setNode(22)
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl1.getNodeBreakerView().newDisconnector()
                .setId("D21")
                .setNode1(20)
                .setNode2(21)
                .setOpen(false)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("BR22")
                .setNode1(21)
                .setNode2(22)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newDisconnector()
                .setId("D13")
                .setNode1(10)
                .setNode2(13)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR14")
                .setNode1(13)
                .setNode2(14)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newDisconnector()
                .setId("D23")
                .setNode1(20)
                .setNode2(23)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR24")
                .setNode1(23)
                .setNode2(24)
                .setOpen(false)
                .add();

        network.newLine()
                .setId("L11")
                .setVoltageLevel1("VL1")
                .setNode1(14)
                .setVoltageLevel2("VL1")
                .setNode2(24)
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        bbs = vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(30)
                .add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1).add();

        vl2.newLoad()
                .setId("L2")
                .setNode(32)
                .setP0(1)
                .setQ0(1)
                .add();
        vl2.getNodeBreakerView().newDisconnector()
                .setId("D31")
                .setNode1(30)
                .setNode2(31)
                .setOpen(false)
                .add();

        vl2.getNodeBreakerView().newBreaker()
                .setId("BR32")
                .setNode1(31)
                .setNode2(32)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newDisconnector()
                .setId("D15")
                .setNode1(10)
                .setNode2(15)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR16")
                .setNode1(15)
                .setNode2(16)
                .setOpen(false)
                .add();

        vl2.getNodeBreakerView().newDisconnector()
                .setId("D33")
                .setNode1(30)
                .setNode2(33)
                .setOpen(false)
                .add();

        vl2.getNodeBreakerView().newBreaker()
                .setId("BR34")
                .setNode1(33)
                .setNode2(34)
                .setOpen(false)
                .add();

        network.newLine()
                .setId("L12")
                .setVoltageLevel1("VL1")
                .setNode1(16)
                .setVoltageLevel2("VL2")
                .setNode2(34)
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        vl1.getNodeBreakerView().newDisconnector()
                .setId("D17")
                .setNode1(10)
                .setNode2(17)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR18")
                .setNode1(17)
                .setNode2(18)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newDisconnector()
                .setId("D25")
                .setNode1(20)
                .setNode2(25)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR26")
                .setNode1(25)
                .setNode2(26)
                .setOpen(false)
                .add();

        TwoWindingsTransformer twoWindingsTransformer = substation.newTwoWindingsTransformer()
                .setId("T11")
                .setVoltageLevel1("VL1")
                .setNode1(18)
                .setVoltageLevel2("VL1")
                .setNode2(26)
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setQ(48);

        vl1.getNodeBreakerView().newDisconnector()
                .setId("D27")
                .setNode1(20)
                .setNode2(27)
                .setOpen(false)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("BR28")
                .setNode1(27)
                .setNode2(28)
                .setOpen(false)
                .add();

        vl2.getNodeBreakerView().newDisconnector()
                .setId("D35")
                .setNode1(30)
                .setNode2(35)
                .setOpen(false)
                .add();

        vl2.getNodeBreakerView().newBreaker()
                .setId("BR36")
                .setNode1(35)
                .setNode2(36)
                .setOpen(false)
                .add();

        twoWindingsTransformer = substation.newTwoWindingsTransformer()
                .setId("T12")
                .setVoltageLevel1("VL1")
                .setNode1(28)
                .setVoltageLevel2("VL2")
                .setNode2(36)
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setQ(48);
    }

    @Test
    public void testVLGraph() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId(), true, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        // write Json and compare to reference
        assertEquals(toString("/InternalBranchesNodeBreaker.json"), toJson(g, "/InternalBranchesNodeBreaker.json"));
    }

    @Test
    public void testSubstationGraphH() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), true);

        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/InternalBranchesNodeBreakerH.json"), toJson(g, "/InternalBranchesNodeBreakerH.json"));
    }

    @Test
    public void testSubstationGraphV() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), true);

        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/InternalBranchesNodeBreakerV.json"), toJson(g, "/InternalBranchesNodeBreakerV.json"));
    }
}
