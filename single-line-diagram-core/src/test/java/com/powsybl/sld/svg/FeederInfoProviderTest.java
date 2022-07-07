/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.iidm.extensions.BusbarSectionPositionAdder;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FeederInfoProviderTest extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        layoutParameters.setFeederInfoSymmetry(true);
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
        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
        layoutParameters.setFeederInfoSymmetry(true);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());
        new SmartVoltageLevelLayoutFactory(network, layoutParameters).create(g).run(); // to have cell orientations (bottom / up)
        assertEquals(toString("/feederInfoTest.svg"), toSVG(g, "/feederInfoTest.svg"));

        Network network2 = Network.create("testCase2", "test2");
        DefaultDiagramLabelProvider wrongLabelProvider = new DefaultDiagramLabelProvider(network2, componentLibrary, layoutParameters);
        List<FeederInfo> feederInfos = wrongLabelProvider.getFeederInfos((FeederNode) g.getNode("svc"));
        assertTrue(feederInfos.isEmpty());

        DefaultDiagramLabelProvider labelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        List<FeederInfo> feederInfos1 = labelProvider.getFeederInfos((FeederNode) g.getNode("svc"));
        assertEquals(2, feederInfos1.size());
        assertEquals(ARROW_ACTIVE, feederInfos1.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos1.get(1).getComponentType());
        assertTrue(feederInfos1.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos1.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos1.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos1.get(1).getLeftLabel().isPresent());

        List<FeederInfo> feederInfos2 = labelProvider.getFeederInfos((FeederNode) g.getNode("vsc"));
        assertEquals(2, feederInfos2.size());
        assertEquals(ARROW_ACTIVE, feederInfos2.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos2.get(1).getComponentType());
        assertTrue(feederInfos2.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos2.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos2.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos2.get(1).getLeftLabel().isPresent());

        List<FeederInfo> feederInfos3 = labelProvider.getFeederInfos((FeederNode) g.getNode("C1"));
        assertEquals(2, feederInfos3.size());
        assertEquals(ARROW_ACTIVE, feederInfos3.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos3.get(1).getComponentType());
        assertTrue(feederInfos3.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos3.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos3.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos3.get(1).getLeftLabel().isPresent());

        List<FeederInfo> feederInfos4 = labelProvider.getFeederInfos((FeederNode) g.getNode("dl1"));
        assertEquals(2, feederInfos4.size());
        assertEquals(ARROW_ACTIVE, feederInfos4.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos4.get(1).getComponentType());
        assertTrue(feederInfos4.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos4.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos4.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos4.get(1).getLeftLabel().isPresent());

        // Reverse order
        layoutParameters.setFeederInfoSymmetry(false);
        List<FeederInfo> feederInfos5 = labelProvider.getFeederInfos((FeederNode) g.getNode("dl1"));
        assertEquals(ARROW_REACTIVE, feederInfos5.get(0).getComponentType());
        assertEquals(ARROW_ACTIVE, feederInfos5.get(1).getComponentType());
    }
}
