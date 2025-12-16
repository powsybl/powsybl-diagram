/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class NodeTopologyVerticalBusbarIcTest extends AbstractTest {

    @Override
    @BeforeEach
    public void setup() throws IOException {
        super.setup();
        network = Networks.createNodeTopologyNetworkWithInternalConnections();
        Networks.addNodeTopologyVerticalBusbarDiagramData(network);
    }

    @Test
    void testVoltageLevelLayout() throws IOException {
        assertSvgDrawnEqualsReference("VoltageLevel1", "/nodeTopologyIcTestV.svg", 2);
    }
}
