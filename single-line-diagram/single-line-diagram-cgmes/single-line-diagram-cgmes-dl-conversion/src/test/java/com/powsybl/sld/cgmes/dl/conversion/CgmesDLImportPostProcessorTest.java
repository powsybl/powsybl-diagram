/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NodeDiagramData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesDLImportPostProcessorTest extends CgmesDLModelTest {

    @Test
    void process() {
        Network network = Networks.createNetworkWithBusbar();
        new CgmesDLImportPostProcessor(queryCatalog).process(network, tripleStore);

        BusbarSection busbar = network.getBusbarSection("Busbar");
        NodeDiagramData<BusbarSection> busDiagramData = busbar.getExtension(NodeDiagramData.class);
        assertNotNull(busDiagramData);
        assertNotNull(busDiagramData.getData(DEFAULT_DIAGRAM_NAME));
        NodeDiagramData<BusbarSection>.NodeDiagramDataDetails nodeDiagramDataDetails = busDiagramData.getData(DEFAULT_DIAGRAM_NAME);
        assertEquals(1, nodeDiagramDataDetails.getPoint1().getSeq(), 0);
        assertEquals(20, nodeDiagramDataDetails.getPoint1().getX(), 0);
        assertEquals(5, nodeDiagramDataDetails.getPoint1().getY(), 0);
        assertEquals(2, nodeDiagramDataDetails.getPoint2().getSeq(), 0);
        assertEquals(20, nodeDiagramDataDetails.getPoint2().getX(), 0);
        assertEquals(40, nodeDiagramDataDetails.getPoint2().getY(), 0);
    }
}
