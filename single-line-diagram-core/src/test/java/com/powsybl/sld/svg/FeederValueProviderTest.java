/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.BusbarSectionPositionAdder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FeederValueProviderTest {

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
        LayoutParameters layoutParameters = new LayoutParameters().setFeederArrowSymmetry(true);
        DefaultDiagramLabelProvider initProvider = new DefaultDiagramLabelProvider(network2, componentLibrary, layoutParameters);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, false);
        List<FeederValue> feederValues = initProvider.getFeederValues((FeederNode) g.getNode("svc"));
        assertTrue(feederValues.isEmpty());
        DefaultDiagramLabelProvider initProvider1 = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        List<FeederValue> feederValues1 = initProvider1.getFeederValues((FeederNode) g.getNode("svc"));
        assertEquals(2, feederValues1.size());
        assertEquals(ARROW_ACTIVE, feederValues1.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederValues1.get(1).getComponentType());
        assertTrue(feederValues1.get(0).getRightLabel().isPresent());
        assertTrue(feederValues1.get(1).getRightLabel().isPresent());
        assertFalse(feederValues1.get(0).getLeftLabel().isPresent());
        assertFalse(feederValues1.get(1).getLeftLabel().isPresent());
        assertTrue(feederValues1.get(0).getDirection().isPresent());
        assertTrue(feederValues1.get(1).getDirection().isPresent());
        List<FeederValue> feederValues2 = initProvider1.getFeederValues((FeederNode) g.getNode("vsc"));
        assertEquals(2, feederValues2.size());
        assertEquals(ARROW_ACTIVE, feederValues2.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederValues2.get(1).getComponentType());
        assertTrue(feederValues2.get(0).getRightLabel().isPresent());
        assertTrue(feederValues2.get(1).getRightLabel().isPresent());
        assertFalse(feederValues2.get(0).getLeftLabel().isPresent());
        assertFalse(feederValues2.get(1).getLeftLabel().isPresent());
        assertTrue(feederValues2.get(0).getDirection().isPresent());
        assertTrue(feederValues2.get(1).getDirection().isPresent());
        List<FeederValue> feederValues3 = initProvider1.getFeederValues((FeederNode) g.getNode("C1"));
        assertEquals(2, feederValues3.size());
        assertEquals(ARROW_ACTIVE, feederValues3.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederValues3.get(1).getComponentType());
        assertTrue(feederValues3.get(0).getRightLabel().isPresent());
        assertTrue(feederValues3.get(1).getRightLabel().isPresent());
        assertFalse(feederValues3.get(0).getLeftLabel().isPresent());
        assertFalse(feederValues3.get(1).getLeftLabel().isPresent());
        assertTrue(feederValues3.get(0).getDirection().isPresent());
        assertTrue(feederValues3.get(1).getDirection().isPresent());
        List<FeederValue> feederValues4 = initProvider1.getFeederValues((FeederNode) g.getNode("dl1"));
        assertEquals(2, feederValues4.size());
        assertEquals(ARROW_ACTIVE, feederValues4.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederValues4.get(1).getComponentType());
        assertTrue(feederValues4.get(0).getRightLabel().isPresent());
        assertTrue(feederValues4.get(1).getRightLabel().isPresent());
        assertFalse(feederValues4.get(0).getLeftLabel().isPresent());
        assertFalse(feederValues4.get(1).getLeftLabel().isPresent());
        assertTrue(feederValues4.get(0).getDirection().isPresent());
        assertTrue(feederValues4.get(1).getDirection().isPresent());
        // Reverse order
        layoutParameters.setFeederArrowSymmetry(false);
        List<FeederValue> feederValues5 = initProvider1.getFeederValues((FeederNode) g.getNode("dl1"));
        assertEquals(ARROW_REACTIVE, feederValues5.get(0).getComponentType());
        assertEquals(ARROW_ACTIVE, feederValues5.get(1).getComponentType());
    }
}
