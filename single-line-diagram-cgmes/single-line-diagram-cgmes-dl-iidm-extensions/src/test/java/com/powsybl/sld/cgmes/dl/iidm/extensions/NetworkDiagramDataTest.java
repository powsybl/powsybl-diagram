/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class NetworkDiagramDataTest {

    protected static String DIAGRAM_NAME = "diagram";
    protected static String DIAGRAM_NAME2 = "diagram2";

    @Test
    public void test() {
        Network network = Networks.createNetworkWithGenerator();
        assertFalse(NetworkDiagramData.checkNetworkDiagramData(network));
        assertEquals(0, NetworkDiagramData.getDiagramsNames(network).size());
        assertFalse(NetworkDiagramData.containsDiagramName(network, DIAGRAM_NAME));

        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME);
        assertEquals(1, NetworkDiagramData.getDiagramsNames(network).size());
        assertEquals(DIAGRAM_NAME, NetworkDiagramData.getDiagramsNames(network).get(0));
        assertNotEquals(DIAGRAM_NAME2, NetworkDiagramData.getDiagramsNames(network).get(0));

        Network network2 = Networks.createNetworkWithGenerator();
        assertEquals(0, NetworkDiagramData.getDiagramsNames(network2).size());
        assertEquals(1, NetworkDiagramData.getDiagramsNames(network).size());

        assertTrue(NetworkDiagramData.checkNetworkDiagramData(network));
        assertTrue(NetworkDiagramData.containsDiagramName(network, DIAGRAM_NAME));
        assertFalse(NetworkDiagramData.containsDiagramName(network, DIAGRAM_NAME2));

    }

}
