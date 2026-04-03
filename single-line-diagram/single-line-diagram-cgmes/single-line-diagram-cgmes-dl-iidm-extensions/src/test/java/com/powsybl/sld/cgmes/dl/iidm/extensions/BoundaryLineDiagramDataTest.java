/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.diagram.test.Networks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class BoundaryLineDiagramDataTest extends AbstractLineDiagramDataTest {

    @Test
    void test() {
        Network network = Networks.createNetworkWithBoundaryLine();
        BoundaryLine boundaryLine = network.getBoundaryLine("BoundaryLine");

        LineDiagramData<BoundaryLine> boundaryLineDiagramData = LineDiagramData.getOrCreateDiagramData(boundaryLine);
        assertNotNull(boundaryLineDiagramData);

        boundaryLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        boundaryLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        boundaryLine.addExtension(LineDiagramData.class, boundaryLineDiagramData);

        BoundaryLine boundaryLine2 = network.getBoundaryLine("BoundaryLine");
        LineDiagramData<BoundaryLine> boundaryLineDiagramData2 = boundaryLine2.getExtension(LineDiagramData.class);

        assertEquals(1, boundaryLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(boundaryLineDiagramData2, DIAGRAM_NAME);
    }

    @Test
    void testMultipleDiagrams() {
        Network network = Networks.createNetworkWithBoundaryLine();
        BoundaryLine boundaryLine = network.getBoundaryLine("BoundaryLine");

        LineDiagramData<BoundaryLine> boundaryLineDiagramData = LineDiagramData.getOrCreateDiagramData(boundaryLine);
        assertNotNull(boundaryLineDiagramData);

        boundaryLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        boundaryLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        boundaryLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(10, 20, 1));
        boundaryLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(20, 10, 2));
        boundaryLine.addExtension(LineDiagramData.class, boundaryLineDiagramData);

        BoundaryLine boundaryLine2 = network.getBoundaryLine("BoundaryLine");
        LineDiagramData<BoundaryLine> boundaryLineDiagramData2 = boundaryLine2.getExtension(LineDiagramData.class);

        assertEquals(2, boundaryLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(boundaryLineDiagramData2, DIAGRAM_NAME);
        checkDiagramData(boundaryLineDiagramData2, DIAGRAM2_NAME);
    }
}
