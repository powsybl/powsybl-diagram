/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.diagram.test.Networks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class BusDiagramDataTest extends AbstractNodeDiagramDataTest {

    @Test
    void test() {
        Network network = Networks.createNetworkWithBus();
        Bus bus = network.getVoltageLevel("VoltageLevel").getBusBreakerView().getBus("Bus");

        NodeDiagramData<Bus> busDiagramData = NodeDiagramData.getOrCreateDiagramData(bus);
        assertNotNull(busDiagramData);
        NodeDiagramData.NodeDiagramDataDetails busDiagramDetails = busDiagramData.new NodeDiagramDataDetails();

        busDiagramDetails.setPoint1(new DiagramPoint(0, 10, 1));
        busDiagramDetails.setPoint2(new DiagramPoint(10, 0, 2));
        busDiagramData.addData(DIAGRAM_NAME, busDiagramDetails);
        assertTrue(busDiagramData.getDiagramsNames().size() > 0);

        bus.addExtension(NodeDiagramData.class, busDiagramData);

        Bus bus2 = network.getVoltageLevel("VoltageLevel").getBusBreakerView().getBus("Bus");
        NodeDiagramData<Bus> busDiagramData2 = bus2.getExtension(NodeDiagramData.class);

        checkDiagramData(busDiagramData2, DIAGRAM_NAME);
    }

}
