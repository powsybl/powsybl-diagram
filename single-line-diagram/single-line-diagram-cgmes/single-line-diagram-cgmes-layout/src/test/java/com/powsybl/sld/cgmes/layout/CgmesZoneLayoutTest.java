/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.graphs.ZoneGraph;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesZoneLayoutTest {

    private static final String SUBSTATION_1_ID = "Substation1";
    private static final String SUBSTATION_2_ID = "Substation2";
    private static final String VOLTAGE_LEVEL_11_ID = "VoltageLevel11";
    private static final String VOLTAGE_LEVEL_12_ID = "VoltageLevel12";
    private static final String VOLTAGE_LEVEL_21_ID = "VoltageLevel21";
    private static final String BUS_11_ID = "Bus11";
    private static final String BUS_12_ID = "Bus12";
    private static final String BUS_21_ID = "Bus21";
    private static final String LOAD_ID = "Load";
    private static final String LINE_ID = "Line";
    private static final String GENERATOR_ID = "Generator";
    private static final String TRANSFORMER_ID = "Transformer";
    private static final String DIAGRAM_ID = "Diagram";

    private Network createNetwork() {
        Network network = Network.create("Network", "test");
        network.setCaseDate(ZonedDateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation1 = network.newSubstation()
                .setId(SUBSTATION_1_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel11 = substation1.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_11_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel11.getBusBreakerView().newBus()
                .setId(BUS_11_ID)
                .add();
        voltageLevel11.newLoad()
                .setId(LOAD_ID)
                .setBus(BUS_11_ID)
                .setConnectableBus(BUS_11_ID)
                .setP0(100)
                .setQ0(50)
                .add();
        VoltageLevel voltageLevel12 = substation1.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_12_ID)
                .setNominalV(280)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel12.getBusBreakerView().newBus()
                .setId(BUS_12_ID)
                .add();
        int zb380 = 380 * 380 / 100;
        substation1.newTwoWindingsTransformer()
                .setId(TRANSFORMER_ID)
                .setVoltageLevel1(voltageLevel11.getId())
                .setBus1(BUS_11_ID)
                .setConnectableBus1(BUS_11_ID)
                .setRatedU1(24.0)
                .setVoltageLevel2(voltageLevel12.getId())
                .setBus2(BUS_12_ID)
                .setConnectableBus2(BUS_12_ID)
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
        Substation substation2 = network.newSubstation()
                .setId(SUBSTATION_2_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel21 = substation2.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_21_ID)
                .setNominalV(280)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel21.getBusBreakerView().newBus()
                .setId(BUS_21_ID)
                .add();
        voltageLevel21.newGenerator()
                .setId(GENERATOR_ID)
                .setBus(BUS_21_ID)
                .setConnectableBus(BUS_21_ID)
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        network.newLine()
                .setId(LINE_ID)
                .setVoltageLevel1(voltageLevel12.getId())
                .setBus1(BUS_12_ID)
                .setConnectableBus1(BUS_12_ID)
                .setVoltageLevel2(voltageLevel21.getId())
                .setBus2(BUS_21_ID)
                .setConnectableBus2(BUS_21_ID)
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        return network;
    }

    private void addDiagramData(Network network) {
        Load load = network.getLoad(LOAD_ID);
        InjectionDiagramData<Load> loadDiagramData = new InjectionDiagramData<>(load);
        InjectionDiagramData.InjectionDiagramDetails loadsDiagramDetails = new InjectionDiagramData.InjectionDiagramDetails(new DiagramPoint(10, 20, 0), 90);
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(15, 20, 1));
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(30, 20, 2));
        loadDiagramData.addData(DIAGRAM_ID, loadsDiagramDetails);
        load.addExtension(InjectionDiagramData.class, loadDiagramData);

        Bus bus11 = network.getVoltageLevel(VOLTAGE_LEVEL_11_ID).getBusBreakerView().getBus(BUS_11_ID);
        NodeDiagramData<Bus> busDiagramData11 = new NodeDiagramData<>(bus11);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails11 = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails11.setPoint1(new DiagramPoint(30, 10, 1));
        diagramDetails11.setPoint2(new DiagramPoint(30, 30, 2));
        busDiagramData11.addData(DIAGRAM_ID, diagramDetails11);
        bus11.addExtension(NodeDiagramData.class, busDiagramData11);

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(TRANSFORMER_ID);
        CouplingDeviceDiagramData<TwoWindingsTransformer> twtDiagramData = new CouplingDeviceDiagramData<>(twt);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails twtDiagramDetails = new CouplingDeviceDiagramData.CouplingDeviceDiagramDetails(new DiagramPoint(50, 20, 0), 90);
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(45, 20, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(30, 20, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(55, 20, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(70, 20, 2));
        twtDiagramData.addData(DIAGRAM_ID, twtDiagramDetails);
        twt.addExtension(CouplingDeviceDiagramData.class, twtDiagramData);

        Bus bus12 = network.getVoltageLevel(VOLTAGE_LEVEL_12_ID).getBusBreakerView().getBus(BUS_12_ID);
        NodeDiagramData<Bus> busDiagramData12 = new NodeDiagramData<>(bus12);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails12 = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails12.setPoint1(new DiagramPoint(70, 10, 1));
        diagramDetails12.setPoint2(new DiagramPoint(70, 30, 2));
        busDiagramData12.addData(DIAGRAM_ID, diagramDetails12);
        bus12.addExtension(NodeDiagramData.class, busDiagramData12);

        Line line = network.getLine(LINE_ID);
        LineDiagramData<Line> lineDiagramData = new LineDiagramData<>(line);
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(70, 20, 1));
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(100, 20, 2));
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(100, 60, 3));
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(130, 60, 4));
        line.addExtension(LineDiagramData.class, lineDiagramData);

        Bus bus21 = network.getVoltageLevel(VOLTAGE_LEVEL_21_ID).getBusBreakerView().getBus(BUS_21_ID);
        NodeDiagramData<Bus> busDiagramData21 = new NodeDiagramData<>(bus21);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails21 = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails21.setPoint1(new DiagramPoint(130, 50, 1));
        diagramDetails21.setPoint2(new DiagramPoint(130, 70, 2));
        busDiagramData21.addData(DIAGRAM_ID, diagramDetails21);
        bus21.addExtension(NodeDiagramData.class, busDiagramData21);

        Generator generator = network.getGenerator(GENERATOR_ID);
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = new InjectionDiagramData.InjectionDiagramDetails(new DiagramPoint(150, 60, 0), 0);
        diagramDetails.addTerminalPoint(new DiagramPoint(145, 60, 1));
        diagramDetails.addTerminalPoint(new DiagramPoint(130, 60, 2));
        generatorDiagramData.addData(DIAGRAM_ID, diagramDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);

        NetworkDiagramData.addDiagramName(network, DIAGRAM_ID, SUBSTATION_1_ID);
        NetworkDiagramData.addDiagramName(network, DIAGRAM_ID, SUBSTATION_2_ID);
    }

    @Test
    void test() {
        Network network = createNetwork();
        addDiagramData(network);
        List<String> zone = Arrays.asList(SUBSTATION_1_ID, SUBSTATION_2_ID);
        ZoneGraph graph = new NetworkGraphBuilder(network).buildZoneGraph(zone);
        LayoutParameters layoutParameters = new LayoutParameters().setCgmesScaleFactor(2);
        new CgmesZoneLayout(graph, network).run(layoutParameters);

    }
}
