/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class DoubleBusbarSectionTest {

    private static final String DIAGRAM_NAME = "default";

    private int countNodes = 0;
    private int bbN1 = countNodes++;
    private int bbN2 = countNodes++;
    private int iN1 = countNodes++;
    private int gN1 = countNodes++;

    private VoltageLevel voltageLevel;

    @Before
    public void setUp() {
        Network network = createNetwork();
        voltageLevel = network.getVoltageLevel("VoltageLevel1");
    }

    private Network createNetwork() {

        Network network = Network.create("network1", "test");

        Substation substation1 = network.newSubstation()
                .setId("Substation1")
                .setCountry(Country.FR)
                .add();

        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();

        voltageLevel1.getNodeBreakerView().setNodeCount(countNodes);

        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection1")
                .setNode(bbN1)
                .add();
        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection2")
                .setNode(bbN2)
                .add();

        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector1")
                .setNode1(bbN1)
                .setNode2(iN1)
                .setOpen(true)
                .add();

        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector2")
                .setNode1(bbN2)
                .setNode2(iN1)
                .setOpen(false)
                .add();

        voltageLevel1.newGenerator()
                .setId("Generator1")
                .setNode(gN1)
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();

        voltageLevel1.getNodeBreakerView().newBreaker()
                .setId("Breaker1")
                .setNode1(gN1)
                .setNode2(iN1)
                .add();
        return network;
    }

    private void addDiagramData(boolean isVoltageLevelDataEnabled) {
        Network network = voltageLevel.getNetwork();
        addBusbarSectionDiagramData(network.getBusbarSection("BusbarSection1"), new DiagramPoint(20, 10, 1),
                new DiagramPoint(180, 10, 2));
        addBusbarSectionDiagramData(network.getBusbarSection("BusbarSection2"), new DiagramPoint(20, 40, 1),
                new DiagramPoint(180, 40, 2));
        addGeneratorDiagramData(network.getGenerator("Generator1"), new DiagramPoint(80, 100, 0));
        addSwitchDiagramData(network.getSwitch("Disconnector1"), new DiagramPoint(75, 10, 0), 0);
        addSwitchDiagramData(network.getSwitch("Disconnector2"), new DiagramPoint(75, 40, 0), 0);
        addSwitchDiagramData(network.getSwitch("Breaker1"), new DiagramPoint(80, 50, 0), 0);
        if (isVoltageLevelDataEnabled) {
            VoltageLevelDiagramData.addInternalNodeDiagramPoint(voltageLevel, DIAGRAM_NAME, iN1, new DiagramPoint(80, 45, 0));
        }
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME);
    }

    private void addBusbarSectionDiagramData(BusbarSection busbarSection, DiagramPoint point1, DiagramPoint point2) {
        NodeDiagramData<BusbarSection> busbarDiagramData = new NodeDiagramData<>(busbarSection);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails = busbarDiagramData.new NodeDiagramDataDetails();
        diagramDetails.setPoint1(point1);
        diagramDetails.setPoint2(point2);
        busbarDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        busbarSection.addExtension(NodeDiagramData.class, busbarDiagramData);
    }

    private void addGeneratorDiagramData(Generator generator, DiagramPoint generatorPoint) {
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = generatorDiagramData.new InjectionDiagramDetails(generatorPoint, 0);
        generatorDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
    }

    private void addSwitchDiagramData(Switch sw, DiagramPoint switchPoint, int rotation) {
        CouplingDeviceDiagramData<Switch> switchDiagramData = new CouplingDeviceDiagramData<>(sw);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = switchDiagramData.new CouplingDeviceDiagramDetails(switchPoint, rotation);
        switchDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        sw.addExtension(CouplingDeviceDiagramData.class, switchDiagramData);
    }

    private void checkGraph(Graph graph) {
        assertEquals(7, graph.getNodes().size());

        assertEquals(Node.NodeType.BUS, graph.getNodes().get(0).getType());
        assertEquals(Node.NodeType.BUS, graph.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(2).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(3).getType());
        assertEquals(Node.NodeType.SWITCH, graph.getNodes().get(4).getType());
        assertEquals(Node.NodeType.SWITCH, graph.getNodes().get(5).getType());
        assertEquals(Node.NodeType.SWITCH, graph.getNodes().get(6).getType());

        assertEquals(1, graph.getNodes().get(0).getAdjacentNodes().size());
        assertEquals(1, graph.getNodes().get(1).getAdjacentNodes().size());
        assertEquals(1, graph.getNodes().get(2).getAdjacentNodes().size());
        assertEquals(3, graph.getNodes().get(3).getAdjacentNodes().size());
        assertEquals(2, graph.getNodes().get(4).getAdjacentNodes().size());
        assertEquals(2, graph.getNodes().get(5).getAdjacentNodes().size());
        assertEquals(2, graph.getNodes().get(6).getAdjacentNodes().size());

        assertEquals(6, graph.getEdges().size());
    }

    private void checkNodeCoordinates(Graph graph, boolean isVoltageLevelDataEnabled) {
        assertEquals(20, graph.getNodes().get(0).getX(), 0);
        assertEquals(10, graph.getNodes().get(0).getY(), 0);
        assertEquals(160, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertFalse(graph.getNodes().get(0).isRotated());

        assertEquals(20, graph.getNodes().get(1).getX(), 0);
        assertEquals(40, graph.getNodes().get(1).getY(), 0);
        assertEquals(160, ((BusNode) graph.getNodes().get(1)).getPxWidth(), 0);
        assertFalse(graph.getNodes().get(1).isRotated());

        assertEquals(80, graph.getNodes().get(2).getX(), 0);
        assertEquals(100, graph.getNodes().get(2).getY(), 0);

        assertEquals(isVoltageLevelDataEnabled ? 80 : -1, graph.getNodes().get(3).getX(), 0);
        assertEquals(isVoltageLevelDataEnabled ? 45 : -1, graph.getNodes().get(3).getY(), 0);
        assertFalse(graph.getNodes().get(3).isRotated());

        assertEquals(75, graph.getNodes().get(4).getX(), 0);
        assertEquals(10, graph.getNodes().get(4).getY(), 0);
        assertFalse(graph.getNodes().get(4).isRotated());

        assertEquals(75, graph.getNodes().get(5).getX(), 0);
        assertEquals(40, graph.getNodes().get(5).getY(), 0);
        assertFalse(graph.getNodes().get(5).isRotated());

        assertEquals(80, graph.getNodes().get(6).getX(), 0);
        assertEquals(50, graph.getNodes().get(6).getY(), 0);
        assertFalse(graph.getNodes().get(6).isRotated());
    }

    private Graph processCgmesLayout() {
        NetworkGraphBuilder graphBuilder = new NetworkGraphBuilder(voltageLevel.getNetwork());
        Graph graph = graphBuilder.buildVoltageLevelGraph(voltageLevel.getId(), false, true, false);
        LayoutParameters layoutParameters = new LayoutParameters();
        layoutParameters.setScaleFactor(1);
        layoutParameters.setDiagramName(DIAGRAM_NAME);
        new CgmesVoltageLevelLayout(graph, voltageLevel.getNetwork()).run(layoutParameters);
        return graph;
    }

    @Test
    public void testVoltageLevelData() {
        addDiagramData(true);
        Graph graph = processCgmesLayout();
        checkGraph(graph);
        checkNodeCoordinates(graph, true);
    }

    @Test
    public void testNoVoltageLevelData() {
        addDiagramData(false);
        Graph graph = processCgmesLayout();
        checkGraph(graph);
        checkNodeCoordinates(graph, false);
    }
}
