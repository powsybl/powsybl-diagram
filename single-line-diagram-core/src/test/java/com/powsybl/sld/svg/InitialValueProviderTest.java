/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.BusbarSectionPositionAdder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class InitialValueProviderTest {

    private Network network;
    private Substation substation;
    private VoltageLevel vl;
    private GraphBuilder graphBuilder;

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.newSubstation().setId("s").setCountry(Country.FR).add();
        vl = substation.newVoltageLevel().setId("vl").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(380).add();
        VoltageLevel.NodeBreakerView view = vl.getNodeBreakerView();
        BusbarSection bbs = view.newBusbarSection().setId("bbs").setNode(0).add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1);
        BusbarSection bbs2 = view.newBusbarSection().setId("bbs2").setNode(3).add();
        bbs2.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(2);
        StaticVarCompensator svc = vl.newStaticVarCompensator()
            .setId("svc")
            .setName("svc")
            .setNode(2)
            .setBmin(0.0002)
            .setBmax(0.0008)
            .setRegulationMode(RegulationMode.VOLTAGE)
            .setVoltageSetPoint(390)
            .add();
        svc.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        VscConverterStation vsc = vl.newVscConverterStation()
            .setId("vsc")
            .setName("Converter1")
            .setNode(1)
            .setLossFactor(0.011f)
            .setVoltageSetpoint(405.0)
            .setVoltageRegulatorOn(true)
            .add();
        vsc.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        ShuntCompensator c1 = vl.newShuntCompensator()
            .setId("C1")
            .setName("Filter 1")
            .setNode(4)
            .setSectionCount(1)
            .newLinearModel()
                .setBPerSection(1e-5)
                .setMaximumSectionCount(1)
            .add()
            .add();
        DanglingLine dl1 = vl.newDanglingLine()
                .setId("dl1")
                .setName("Dangling line 1")
                .setNode(5)
                .setP0(1)
                .setQ0(1)
                .setR(0)
                .setX(0)
                .setB(0)
                .setG(0)
                .add();
        dl1.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        view.newDisconnector().setId("d").setNode1(0).setNode2(1).add();
        view.newBreaker().setId("b").setNode1(1).setNode2(2).add();
        view.newBreaker().setId("b2").setNode1(3).setNode2(4).add();
        view.newBreaker().setId("b3").setNode1(3).setNode2(5).add();
    }

    @Test
    public void test() {
        Network network2 = Network.create("testCase2", "test2");
        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
        LayoutParameters layoutParameters = new LayoutParameters();
        DefaultDiagramLabelProvider initProvider = new DefaultDiagramLabelProvider(network2, componentLibrary, layoutParameters);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, false);
        List<FlowArrow> arrows = initProvider.getFlowArrows(g.getNode("svc"));
        assertEquals(2, arrows.size());
        assertFalse(arrows.get(0).getLeftLabel().isPresent());
        assertFalse(arrows.get(1).getLeftLabel().isPresent());
        assertFalse(arrows.get(0).getRightLabel().isPresent());
        assertFalse(arrows.get(1).getRightLabel().isPresent());
        assertFalse(arrows.get(0).getDirection().isPresent());
        assertFalse(arrows.get(1).getDirection().isPresent());
        DefaultDiagramLabelProvider initProvider1 = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        List<FlowArrow> arrows1 = initProvider1.getFlowArrows(g.getNode("svc"));
        assertEquals(2, arrows1.size());
        assertTrue(arrows1.get(0).getLeftLabel().isPresent());
        assertTrue(arrows1.get(1).getLeftLabel().isPresent());
        assertFalse(arrows1.get(0).getRightLabel().isPresent());
        assertFalse(arrows1.get(1).getRightLabel().isPresent());
        assertTrue(arrows1.get(0).getDirection().isPresent());
        assertTrue(arrows1.get(1).getDirection().isPresent());
        List<FlowArrow> arrows2 = initProvider1.getFlowArrows(g.getNode("vsc"));
        assertEquals(2, arrows2.size());
        assertTrue(arrows2.get(0).getLeftLabel().isPresent());
        assertTrue(arrows2.get(1).getLeftLabel().isPresent());
        assertFalse(arrows2.get(0).getRightLabel().isPresent());
        assertFalse(arrows2.get(1).getRightLabel().isPresent());
        assertTrue(arrows2.get(0).getDirection().isPresent());
        assertTrue(arrows2.get(1).getDirection().isPresent());
        List<FlowArrow> arrows3 = initProvider1.getFlowArrows(g.getNode("C1"));
        assertEquals(2, arrows3.size());
        assertTrue(arrows3.get(0).getLeftLabel().isPresent());
        assertTrue(arrows3.get(1).getLeftLabel().isPresent());
        assertFalse(arrows3.get(0).getRightLabel().isPresent());
        assertFalse(arrows3.get(1).getRightLabel().isPresent());
        assertTrue(arrows3.get(0).getDirection().isPresent());
        assertTrue(arrows3.get(1).getDirection().isPresent());
        List<FlowArrow> arrows4 = initProvider1.getFlowArrows(g.getNode("b"));
        assertEquals(2, arrows4.size());
        assertFalse(arrows4.get(0).getLeftLabel().isPresent());
        assertFalse(arrows4.get(1).getLeftLabel().isPresent());
        assertFalse(arrows4.get(0).getRightLabel().isPresent());
        assertFalse(arrows4.get(1).getRightLabel().isPresent());
        assertFalse(arrows4.get(0).getDirection().isPresent());
        assertFalse(arrows4.get(1).getDirection().isPresent());
        List<FlowArrow> arrows5 = initProvider1.getFlowArrows(g.getNode("dl1"));
        assertEquals(2, arrows5.size());
        assertTrue(arrows5.get(0).getLeftLabel().isPresent());
        assertTrue(arrows5.get(1).getLeftLabel().isPresent());
        assertFalse(arrows5.get(0).getRightLabel().isPresent());
        assertFalse(arrows5.get(1).getRightLabel().isPresent());
        assertTrue(arrows5.get(0).getDirection().isPresent());
        assertTrue(arrows5.get(1).getDirection().isPresent());
    }
}
