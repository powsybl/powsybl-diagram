/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.CAPACITOR;
import static com.powsybl.sld.library.ComponentTypeName.DANGLING_LINE;
import static com.powsybl.sld.library.ComponentTypeName.LINE;
import static com.powsybl.sld.library.ComponentTypeName.LOAD;
import static com.powsybl.sld.library.ComponentTypeName.STATIC_VAR_COMPENSATOR;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NodeDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusTopologyTest extends AbstractCgmesVoltageLevelLayoutTest {

    private VoltageLevel voltageLevel;
    private Substation substation;
    private VoltageLevel voltageLevel2;
    Network network;

    @Before
    public void setUp() {
        createNetwork();
    }

    private void createNetwork() {
        network = Network.create("test", "test");
        substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        voltageLevel = createFirstVoltageLevel(substation);
        voltageLevel2 = createSecondVoltageLevel(substation);
        createTransformer(substation);
        addDiagramData(network);
    }

    private VoltageLevel createFirstVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        voltageLevel1.newLoad()
                .setId("Load")
                .setBus("Bus1")
                .setConnectableBus("Bus1")
                .setP0(100)
                .setQ0(50)
                .add();
        voltageLevel1.newShuntCompensator()
                .setId("Shunt")
                .setBus("Bus1")
                .setConnectableBus("Bus1")
                .setbPerSection(1e-5)
                .setCurrentSectionCount(1)
                .setMaximumSectionCount(1)
                .add();
        voltageLevel1.newDanglingLine()
                .setId("DanglingLine")
                .setBus("Bus1")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
        return voltageLevel1;
    }

    private VoltageLevel createSecondVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        voltageLevel2.newStaticVarCompensator()
                .setId("Svc")
                .setConnectableBus("Bus2")
                .setBus("Bus2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390.0)
                .setReactivePowerSetPoint(1.0)
                .add();
        return voltageLevel2;
    }

    private void createTransformer(Substation substation) {
        int zb380 = 380 * 380 / 100;
        substation.newTwoWindingsTransformer()
                .setId("Transformer")
                .setVoltageLevel1("VoltageLevel1")
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setRatedU1(24.0)
                .setVoltageLevel2("VoltageLevel2")
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
    }

    private void addDiagramData(Network network) {
        Bus bus = voltageLevel.getBusBreakerView().getBus("Bus1");
        NodeDiagramData<Bus> busDiagramData = new NodeDiagramData<>(bus);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails = busDiagramData.new NodeDiagramDataDetails();
        diagramDetails.setPoint1(new DiagramPoint(60, 10, 1));
        diagramDetails.setPoint2(new DiagramPoint(60, 70, 2));
        busDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        bus.addExtension(NodeDiagramData.class, busDiagramData);

        Load load = network.getLoad("Load");
        InjectionDiagramData<Load> loadDiagramData = new InjectionDiagramData<>(load);
        InjectionDiagramData.InjectionDiagramDetails loadsDiagramDetails = loadDiagramData.new InjectionDiagramDetails(new DiagramPoint(10, 20, 0), 90);
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(15, 20, 2));
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(60, 20, 1));
        loadDiagramData.addData(DIAGRAM_NAME, loadsDiagramDetails);
        load.addExtension(InjectionDiagramData.class, loadDiagramData);

        ShuntCompensator shunt = network.getShuntCompensator("Shunt");
        InjectionDiagramData<ShuntCompensator> shuntDiagramData = new InjectionDiagramData<>(shunt);
        InjectionDiagramData.InjectionDiagramDetails shuntDiagramDetails = shuntDiagramData.new InjectionDiagramDetails(new DiagramPoint(15, 55, 0), 90);
        shuntDiagramDetails.addTerminalPoint(new DiagramPoint(20, 55, 1));
        shuntDiagramDetails.addTerminalPoint(new DiagramPoint(60, 55, 2));
        shuntDiagramData.addData(DIAGRAM_NAME, shuntDiagramDetails);
        shunt.addExtension(InjectionDiagramData.class, shuntDiagramData);

        DanglingLine danglingLine = network.getDanglingLine("DanglingLine");
        LineDiagramData<DanglingLine> danglingLineDiagramData = new LineDiagramData<>(danglingLine);
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(60, 60, 1));
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(120, 60, 2));
        danglingLine.addExtension(LineDiagramData.class, danglingLineDiagramData);

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("Transformer");
        CouplingDeviceDiagramData<TwoWindingsTransformer> twtDiagramData = new CouplingDeviceDiagramData<>(twt);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails twtDiagramDetails = twtDiagramData.new CouplingDeviceDiagramDetails(new DiagramPoint(100, 15, 0), 90);
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(95, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(60, 15, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(105, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(120, 15, 2));
        twtDiagramData.addData(DIAGRAM_NAME, twtDiagramDetails);
        twt.addExtension(CouplingDeviceDiagramData.class, twtDiagramData);

        Bus bus2 = voltageLevel2.getBusBreakerView().getBus("Bus2");
        NodeDiagramData<Bus> bus2DiagramData = new NodeDiagramData<>(bus2);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails2 = bus2DiagramData.new NodeDiagramDataDetails();
        diagramDetails2.setPoint1(new DiagramPoint(120, 10, 1));
        diagramDetails2.setPoint2(new DiagramPoint(120, 25, 2));
        bus2DiagramData.addData(DIAGRAM_NAME, diagramDetails2);
        bus2.addExtension(NodeDiagramData.class, bus2DiagramData);

        StaticVarCompensator svc = network.getStaticVarCompensator("Svc");
        InjectionDiagramData<StaticVarCompensator> svcDiagramData = new InjectionDiagramData<>(svc);
        InjectionDiagramData.InjectionDiagramDetails svcDiagramDataDetails = svcDiagramData.new InjectionDiagramDetails(new DiagramPoint(140, 15, 0), 270);
        svcDiagramDataDetails.addTerminalPoint(new DiagramPoint(135, 15, 1));
        svcDiagramDataDetails.addTerminalPoint(new DiagramPoint(120, 15, 2));
        svcDiagramData.addData(DIAGRAM_NAME, svcDiagramDataDetails);
        svc.addExtension(InjectionDiagramData.class, svcDiagramData);

        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME);
    }

    @Test
    public void testVoltageLevelLayout() {
        test(voltageLevel);
    }

    @Test
    public void testSubstationLayout() {
        SubstationGraph graph = new NetworkGraphBuilder(network).buildSubstationGraph(substation.getId(), false);
        LayoutParameters layoutParameters = new LayoutParameters();
        layoutParameters.setScaleFactor(2);
        layoutParameters.setDiagramName(DIAGRAM_NAME);
        new CgmesSubstationLayout(graph, network).run(layoutParameters);
        checkGraph(graph.getNode(voltageLevel.getId()));
        checkCoordinates(graph.getNode(voltageLevel.getId()));
        checkGraphVl2(graph.getNode(voltageLevel2.getId()));
        checkCoordinatesVl2(graph.getNode(voltageLevel2.getId()));
    }

    @Override
    protected void checkGraph(Graph graph) {
        assertEquals(5, graph.getNodes().size());

        assertEquals(Node.NodeType.BUS, graph.getNodes().get(0).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(2).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(3).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(4).getType());

        assertEquals("Bus1", graph.getNodes().get(0).getId());
        assertEquals("Load", graph.getNodes().get(1).getId());
        assertEquals("Shunt", graph.getNodes().get(2).getId());
        assertEquals("DanglingLine", graph.getNodes().get(3).getId());
        assertEquals("Transformer_ONE", graph.getNodes().get(4).getId());

        assertEquals(BUSBAR_SECTION, graph.getNodes().get(0).getComponentType());
        assertEquals(LOAD, graph.getNodes().get(1).getComponentType());
        assertEquals(CAPACITOR, graph.getNodes().get(2).getComponentType());
        assertEquals(DANGLING_LINE, graph.getNodes().get(3).getComponentType());
        assertTrue(graph.getNodes().get(4).getComponentType().equals(TWO_WINDINGS_TRANSFORMER) ||
                graph.getNodes().get(4).getComponentType().equals(LINE));

        assertEquals(4, graph.getNodes().get(0).getAdjacentNodes().size());
        checkAdjacentNodes(graph.getNodes().get(0), Arrays.asList("Load", "Shunt", "DanglingLine", "Transformer_ONE"));
        checkBusConnection(graph.getNodes().get(1));
        checkBusConnection(graph.getNodes().get(2));
        checkBusConnection(graph.getNodes().get(3));
        checkBusConnection(graph.getNodes().get(4));

        assertEquals(4, graph.getEdges().size());
    }

    private void checkBusConnection(Node node) {
        assertEquals(1, node.getAdjacentNodes().size());
        assertEquals("Bus1", node.getAdjacentNodes().get(0).getId());
    }

    @Override
    protected void checkCoordinates(Graph graph) {
        assertEquals(120, graph.getNodes().get(0).getX(), 0);
        assertEquals(10, graph.getNodes().get(0).getY(), 0);
        assertEquals(120, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertTrue(graph.getNodes().get(0).isRotated());
        assertEquals(20, graph.getNodes().get(1).getX(), 0);
        assertEquals(30, graph.getNodes().get(1).getY(), 0);
        assertTrue(graph.getNodes().get(1).isRotated());
        assertEquals(30, graph.getNodes().get(2).getX(), 0);
        assertEquals(100, graph.getNodes().get(2).getY(), 0);
        assertTrue(graph.getNodes().get(2).isRotated());
        assertEquals(160, graph.getNodes().get(3).getX(), 0);
        assertEquals(110, graph.getNodes().get(3).getY(), 0);
        assertTrue(graph.getNodes().get(3).isRotated());
        assertEquals(200, graph.getNodes().get(4).getX(), 0);
        assertEquals(20, graph.getNodes().get(4).getY(), 0);
        assertFalse(graph.getNodes().get(4).isRotated());
    }

    private void checkGraphVl2(Graph graph) {
        assertEquals(3, graph.getNodes().size());

        assertEquals(Node.NodeType.BUS, graph.getNodes().get(0).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(2).getType());

        assertEquals("Bus2", graph.getNodes().get(0).getId());
        assertEquals("Svc", graph.getNodes().get(1).getId());
        assertEquals("Transformer_TWO", graph.getNodes().get(2).getId());

        assertEquals(BUSBAR_SECTION, graph.getNodes().get(0).getComponentType());
        assertEquals(STATIC_VAR_COMPENSATOR, graph.getNodes().get(1).getComponentType());
        assertEquals(LINE, graph.getNodes().get(2).getComponentType());

        assertEquals(2, graph.getNodes().get(0).getAdjacentNodes().size());
        assertEquals("Svc", graph.getNodes().get(0).getAdjacentNodes().get(0).getId());
        assertEquals("Transformer_TWO", graph.getNodes().get(0).getAdjacentNodes().get(1).getId());
        assertEquals(1, graph.getNodes().get(1).getAdjacentNodes().size());
        assertEquals("Bus2", graph.getNodes().get(1).getAdjacentNodes().get(0).getId());
        assertEquals(1, graph.getNodes().get(2).getAdjacentNodes().size());
        assertEquals("Bus2", graph.getNodes().get(2).getAdjacentNodes().get(0).getId());
    }

    protected void checkCoordinatesVl2(Graph graph) {
        assertEquals(240, graph.getNodes().get(0).getX(), 0);
        assertEquals(10, graph.getNodes().get(0).getY(), 0);
        assertEquals(30, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertTrue(graph.getNodes().get(0).isRotated());
        assertEquals(280, graph.getNodes().get(1).getX(), 0);
        assertEquals(20, graph.getNodes().get(1).getY(), 0);
        assertTrue(graph.getNodes().get(1).isRotated());
        assertEquals(200, graph.getNodes().get(2).getX(), 0);
        assertEquals(20, graph.getNodes().get(2).getY(), 0);
        assertFalse(graph.getNodes().get(2).isRotated());
    }

}
