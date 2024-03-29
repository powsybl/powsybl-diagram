/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.diagram.test.Networks;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class ShuntDiagramDataTest extends AbstractInjectionDiagramDataTest {

    @Test
    void test() {
        Network network = Networks.createNetworkWithShuntCompensator();
        ShuntCompensator shunt = network.getShuntCompensator("Shunt");

        InjectionDiagramData<ShuntCompensator> shuntDiagramData = new InjectionDiagramData<>(shunt);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = shuntDiagramData.new InjectionDiagramDetails(new DiagramPoint(20, 10, 0), 90);
        diagramDetails.addTerminalPoint(new DiagramPoint(15, 10, 2));
        diagramDetails.addTerminalPoint(new DiagramPoint(0, 10, 1));
        shuntDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        shunt.addExtension(InjectionDiagramData.class, shuntDiagramData);

        ShuntCompensator shunt2 = network.getShuntCompensator("Shunt");
        InjectionDiagramData<ShuntCompensator> shuntDiagramData2 = shunt2.getExtension(InjectionDiagramData.class);

        checkDiagramData(shuntDiagramData2, DIAGRAM_NAME);
    }

}
