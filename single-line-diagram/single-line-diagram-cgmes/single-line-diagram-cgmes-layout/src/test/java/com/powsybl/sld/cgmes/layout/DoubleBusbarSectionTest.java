/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
class DoubleBusbarSectionTest extends AbstractTest {

    private static final String DIAGRAM_NAME = "default";

    @Override
    @BeforeEach
    void setup() throws IOException {
        super.setup();
        network = Networks.createNetworkWithDoubleBusbarSections();
    }

    private void addDiagramData(boolean isVoltageLevelDataEnabled) {
        addBusbarSectionDiagramData(network.getBusbarSection("BusbarSection1"), new DiagramPoint(20, 10, 1),
                new DiagramPoint(180, 10, 2));
        addBusbarSectionDiagramData(network.getBusbarSection("BusbarSection2"), new DiagramPoint(20, 40, 1),
                new DiagramPoint(180, 40, 2));
        addGeneratorDiagramData(network.getGenerator("Generator1"), new DiagramPoint(80, 100, 0));
        addSwitchDiagramData(network.getSwitch("Disconnector1"), new DiagramPoint(75, 10, 0), 0);
        addSwitchDiagramData(network.getSwitch("Disconnector2"), new DiagramPoint(75, 40, 0), 0);
        addSwitchDiagramData(network.getSwitch("Breaker1"), new DiagramPoint(80, 50, 0), 0);
        if (isVoltageLevelDataEnabled) {
            VoltageLevel voltageLevel1 = network.getVoltageLevel("VoltageLevel1");
            VoltageLevelDiagramData.addInternalNodeDiagramPoint(voltageLevel1, DIAGRAM_NAME, 2, new DiagramPoint(80, 45, 0));
        }
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation1");
    }

    private void addBusbarSectionDiagramData(BusbarSection busbarSection, DiagramPoint point1, DiagramPoint point2) {
        NodeDiagramData<BusbarSection> busbarDiagramData = new NodeDiagramData<>(busbarSection);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails = new NodeDiagramData.NodeDiagramDataDetails();
        diagramDetails.setPoint1(point1);
        diagramDetails.setPoint2(point2);
        busbarDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        busbarSection.addExtension(NodeDiagramData.class, busbarDiagramData);
    }

    private void addGeneratorDiagramData(Generator generator, DiagramPoint generatorPoint) {
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = new InjectionDiagramData.InjectionDiagramDetails(generatorPoint, 0);
        generatorDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
    }

    private void addSwitchDiagramData(Switch sw, DiagramPoint switchPoint, int rotation) {
        CouplingDeviceDiagramData<Switch> switchDiagramData = new CouplingDeviceDiagramData<>(sw);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = new CouplingDeviceDiagramData.CouplingDeviceDiagramDetails(switchPoint, rotation);
        switchDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        sw.addExtension(CouplingDeviceDiagramData.class, switchDiagramData);
    }

    @Test
    void testVoltageLevelData() throws IOException {
        addDiagramData(true);
        assertSvgDrawnEqualsReference("VoltageLevel1", "/doubleBbsWithVlData.svg", 2);
    }

    @Test
    void testNoVoltageLevelData() throws IOException {
        addDiagramData(false);
        assertSvgDrawnEqualsReference("VoltageLevel1", "/doubleBbsWithoutVlData.svg", 2);
    }
}
