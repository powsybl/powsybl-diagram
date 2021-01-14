/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestInternalBranchesBusBreaker extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters();
    }

    @Before
    public void setUp() {
        network = Network.create("TestInternalBranchesBusBreaker", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "S", "S", Country.FR);

        VoltageLevel vl1 = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        vl1.getBusBreakerView().newBus()
                .setId("B11")
                .add();
        vl1.newLoad()
                .setId("LD1")
                .setConnectableBus("B11")
                .setBus("B11")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        network.getVoltageLevel("VL1").getBusBreakerView().newBus()
                .setId("B12")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR1")
                .setBus1("B12")
                .setBus2("B11")
                .setOpen(false)
                .add();

        vl1.newGenerator()
                .setId("G")
                .setBus("B12")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        network.newLine()
                .setId("L11")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B11")
                .setBus1("B11")
                .setVoltageLevel2("VL1")
                .setConnectableBus2("B12")
                .setBus2("B12")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        substation.newTwoWindingsTransformer()
                .setId("T11")
                .setVoltageLevel1("VL1")
                .setBus1("B11")
                .setConnectableBus1("B11")
                .setVoltageLevel2("VL1")
                .setBus2("B12")
                .setConnectableBus2("B12")
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B21")
                .add();
        vl2.newLoad()
                .setId("LD2")
                .setConnectableBus("B21")
                .setBus("B21")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        network.newLine()
                .setId("L12")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B11")
                .setBus1("B11")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B21")
                .setBus2("B21")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        substation.newTwoWindingsTransformer()
                .setId("T12")
                .setVoltageLevel1("VL1")
                .setBus1("B12")
                .setConnectableBus1("B12")
                .setVoltageLevel2("VL2")
                .setBus2("B21")
                .setConnectableBus2("B21")
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();
    }

    @Test
    public void testVLGraph() {
        // build graph
        Graph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId(), true, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        // write Json and compare to reference
        assertEquals(toString("/InternalBranchesBusBreaker.json"), toJson(g, "/InternalBranchesBusBreaker.json"));
    }

    @Test
    public void testSubstationGraphH() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), true);

        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/InternalBranchesBusBreakerH.json"), toJson(g, "/InternalBranchesBusBreakerH.json"));
    }

    @Test
    public void testSubstationGraphV() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), true);

        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/InternalBranchesBusBreakerV.json"), toJson(g, "/InternalBranchesBusBreakerV.json"));
    }
}
