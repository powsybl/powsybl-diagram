/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class TestShiftFeedersPosition extends AbstractTestCase {

    @Before
    public void setUp() {
        network = Network.create("testCase11", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = createSubstation(network, "subst", "subst", Country.FR);

        VoltageLevel vl1 = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 400, 50);

        createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        createBusBarSection(vl1, "bbs2", "bbs2", 1, 1, 2);
        createBusBarSection(vl1, "bbs3", "bbs3", 2, 2, 1);
        createBusBarSection(vl1, "bbs4", "bbs4", 3, 2, 2);

        createSwitch(vl1, "dsect11", "dsect11", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        createSwitch(vl1, "dtrct11", "dtrct11", SwitchKind.BREAKER, true, false, true, 14, 15);
        createSwitch(vl1, "dsect12", "dsect12", SwitchKind.DISCONNECTOR, false, false, true, 15, 1);

        createSwitch(vl1, "dsect21", "dsect21", SwitchKind.DISCONNECTOR, false, false, true, 2, 16);
        createSwitch(vl1, "dtrct21", "dtrct21", SwitchKind.BREAKER, true, false, true, 16, 17);
        createSwitch(vl1, "dsect22", "dsect22", SwitchKind.DISCONNECTOR, false, false, true, 17, 3);

        createLoad(vl1, "load1", "load1", "load1", 0, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createSwitch(vl1, "dload1", "dload1", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        createSwitch(vl1, "bload1", "bload1", SwitchKind.BREAKER, true, false, true, 4, 5);

        createGenerator(vl1, "gen1", "gen1", "gen1", 1, ConnectablePosition.Direction.BOTTOM, 6, 0, 20, false, 10, 10);
        createSwitch(vl1, "dgen1", "dgen1", SwitchKind.DISCONNECTOR, false, false, true, 2, 7);
        createSwitch(vl1, "bgen1", "bgen1", SwitchKind.BREAKER, true, false, true, 6, 7);

        createLoad(vl1, "load2", "load2", "load2", 2, ConnectablePosition.Direction.TOP, 8, 10, 10);
        createSwitch(vl1, "dload2", "dload2", SwitchKind.DISCONNECTOR, false, false, true, 1, 9);
        createSwitch(vl1, "bload2", "bload2", SwitchKind.BREAKER, true, false, true, 8, 9);

        createGenerator(vl1, "gen2", "gen2", "gen2", 3, ConnectablePosition.Direction.BOTTOM, 10, 0, 20, false, 10, 10);
        createSwitch(vl1, "dgen2", "dgen2", SwitchKind.DISCONNECTOR, false, false, true, 3, 11);
        createSwitch(vl1, "bgen2", "bgen2", SwitchKind.BREAKER, true, false, true, 10, 11);

    }

    private static Substation createSubstation(Network n, String id, String name, Country country) {
        Substation s = n.newSubstation()
                .setId(id)
                .setName(name)
                .setCountry(country)
                .add();
        return s;
    }

    private static VoltageLevel createVoltageLevel(Substation s, String id, String name,
                                                   TopologyKind topology, double vNom, int nodeCount) {
        VoltageLevel vl = s.newVoltageLevel()
                .setId(id)
                .setName(name)
                .setTopologyKind(topology)
                .setNominalV(vNom)
                .add();
        vl.getNodeBreakerView()
                .setNodeCount(nodeCount);
        return vl;
    }

    private static void createSwitch(VoltageLevel vl, String id, String name, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(name)
                .setKind(kind)
                .setRetained(retained)
                .setOpen(open)
                .setFictitious(fictitious)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    private static void createBusBarSection(VoltageLevel vl, String id, String name, int node, int busbarIndex, int sectionIndex) {
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
                .setId(id)
                .setName(name)
                .setNode(node)
                .add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs, busbarIndex, sectionIndex));
    }

    private static void createLoad(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                   ConnectablePosition.Direction direction, int node, double p0, double q0) {
        Load load = vl.newLoad()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setP0(p0)
                .setQ0(q0)
                .add();
        load.addExtension(ConnectablePosition.class, new ConnectablePosition<>(load, new ConnectablePosition
                .Feeder(feederName, feederOrder, direction), null, null, null));
    }

    private static void createGenerator(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                        ConnectablePosition.Direction direction, int node,
                                        double minP, double maxP, boolean voltageRegulator,
                                        double targetP, double targetQ) {
        Generator gen = vl.newGenerator()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(voltageRegulator)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .add();
        gen.addExtension(ConnectablePosition.class, new ConnectablePosition<>(gen, new ConnectablePosition
                .Feeder(feederName, feederOrder, direction), null, null, null));
    }

    @Test
    public void test() {
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
                .setShowInternalNodes(false)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50)
                .setDrawStraightWires(false)
                .setHorizontalSnakeLinePadding(30)
                .setVerticalSnakeLinePadding(30)
                .setShowInductorFor3WT(false);

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);

        // write SVG and compare to reference (horizontal layout and defaut style provider)
        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        compareSvg(g, layoutParameters, "/TestDefaultFeedersPosition.svg", new DefaultDiagramStyleProvider());

        // re-build substation graph, write SVG using the shifted feeders positioner and compare to reference (same layout providers)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);

        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        compareSvg(g, layoutParameters.setShiftFeedersPosition(true), "/TestShiftFeedersPosition.svg", new DefaultDiagramStyleProvider());
    }

    @Test
    public void test2() {
        LayoutParameters layoutParameters = new LayoutParameters();
        VoltageLevelLayoutFactory fakeVoltageLevelLayoutFactory = new VoltageLevelLayoutFactory() {
            @Override
            public VoltageLevelLayout create(Graph graph) {
                return new VoltageLevelLayout() {
                    @Override
                    public void run(LayoutParameters layoutParam) {
                    }
                };
            }
        };

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);

        new HorizontalSubstationLayoutFactory().create(g, fakeVoltageLevelLayoutFactory).run(layoutParameters);
        // write SVG and compare to reference (with a fake VL layout)
        compareSvg(g, layoutParameters, "/TestDefaultFeedersPosition2.svg", new DefaultDiagramStyleProvider());

        // re-build substation graph, write SVG using the shifted feeders positioner (same output as before expected)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);

        new HorizontalSubstationLayoutFactory().create(g, fakeVoltageLevelLayoutFactory).run(layoutParameters);
        compareSvg(g, layoutParameters.setShiftFeedersPosition(true), "/TestDefaultFeedersPosition2.svg", new DefaultDiagramStyleProvider());
    }
}
