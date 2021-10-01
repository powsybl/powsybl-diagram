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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        FlowTransfers init = initProvider.getInitialValue(g.getNode("svc"));
        assertFalse(init.getActive().getValueLabel().isPresent());
        assertFalse(init.getReactive().getValueLabel().isPresent());
        assertFalse(init.getActive().getCustomLabel().isPresent());
        assertFalse(init.getReactive().getCustomLabel().isPresent());
        assertFalse(init.getActive().getArrowDirection().isPresent());
        assertFalse(init.getReactive().getArrowDirection().isPresent());
        DefaultDiagramLabelProvider initProvider1 = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        FlowTransfers init1 = initProvider1.getInitialValue(g.getNode("svc"));
        assertTrue(init1.getActive().getValueLabel().isPresent());
        assertTrue(init1.getReactive().getValueLabel().isPresent());
        assertFalse(init1.getActive().getCustomLabel().isPresent());
        assertFalse(init1.getReactive().getCustomLabel().isPresent());
        assertTrue(init1.getActive().getArrowDirection().isPresent());
        assertTrue(init1.getReactive().getArrowDirection().isPresent());
        FlowTransfers init2 = initProvider1.getInitialValue(g.getNode("vsc"));
        assertTrue(init2.getActive().getValueLabel().isPresent());
        assertTrue(init2.getReactive().getValueLabel().isPresent());
        assertFalse(init2.getActive().getCustomLabel().isPresent());
        assertFalse(init2.getReactive().getCustomLabel().isPresent());
        assertTrue(init2.getActive().getArrowDirection().isPresent());
        assertTrue(init2.getReactive().getArrowDirection().isPresent());
        FlowTransfers init3 = initProvider1.getInitialValue(g.getNode("C1"));
        assertFalse(init3.getActive().getValueLabel().isPresent());
        assertFalse(init3.getReactive().getValueLabel().isPresent());
        assertFalse(init3.getActive().getCustomLabel().isPresent());
        assertFalse(init3.getReactive().getCustomLabel().isPresent());
        assertFalse(init3.getActive().getArrowDirection().isPresent());
        assertFalse(init3.getReactive().getArrowDirection().isPresent());
        FlowTransfers init4 = initProvider1.getInitialValue(g.getNode("b"));
        assertFalse(init4.getActive().getValueLabel().isPresent());
        assertFalse(init4.getReactive().getValueLabel().isPresent());
        assertFalse(init4.getActive().getCustomLabel().isPresent());
        assertFalse(init4.getReactive().getCustomLabel().isPresent());
        assertFalse(init4.getActive().getArrowDirection().isPresent());
        assertFalse(init4.getReactive().getArrowDirection().isPresent());
        FlowTransfers init5 = initProvider1.getInitialValue(g.getNode("dl1"));
        assertTrue(init5.getActive().getValueLabel().isPresent());
        assertTrue(init5.getReactive().getValueLabel().isPresent());
        assertFalse(init5.getActive().getCustomLabel().isPresent());
        assertFalse(init5.getReactive().getCustomLabel().isPresent());
        assertTrue(init5.getActive().getArrowDirection().isPresent());
        assertTrue(init5.getReactive().getArrowDirection().isPresent());
    }
}
