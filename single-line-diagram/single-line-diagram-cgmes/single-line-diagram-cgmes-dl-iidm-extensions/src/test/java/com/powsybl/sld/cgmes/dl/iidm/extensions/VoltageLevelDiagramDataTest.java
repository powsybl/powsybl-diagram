/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.diagram.test.Networks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
class VoltageLevelDiagramDataTest {

    protected static String DIAGRAM_NAME = "diagram";
    protected static String DIAGRAM_NAME2 = "diagram2";

    @Test
    void test() {
        Network network = Networks.createNetworkWithGenerator();
        VoltageLevel vl = network.getVoltageLevels().iterator().next();
        assertFalse(VoltageLevelDiagramData.checkDiagramData(vl));
        assertNull(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME, 1));
        assertEquals(0, VoltageLevelDiagramData.getInternalNodeDiagramPoints(vl, DIAGRAM_NAME).length);

        int[] nodes = {1, 2};
        DiagramPoint point1 = new DiagramPoint(1, 1, 1);
        DiagramPoint point2 = new DiagramPoint(2, 2, 2);
        VoltageLevelDiagramData.addInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[0], point1);
        VoltageLevelDiagramData.addInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[1], point2);

        assertEquals(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[0]), point1);
        assertEquals(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[1]), point2);

        assertEquals(2, nodes.length);
        assertArrayEquals(nodes, VoltageLevelDiagramData.getInternalNodeDiagramPoints(vl, DIAGRAM_NAME));

        assertNull(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME2, 1));
    }

}
