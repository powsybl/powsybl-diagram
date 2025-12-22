/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
abstract class AbstractCouplingDeviceDiagramDataTest {
    protected static String DIAGRAM_NAME = "default";

    protected <T> void checkDiagramData(CouplingDeviceDiagramData<?> diagramData, String diagramName) {
        assertNotNull(diagramData);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails dataDetails = diagramData.getData(diagramName);
        assertEquals(0, dataDetails.getPoint().seq(), 0);
        assertEquals(20, dataDetails.getPoint().x(), 0);
        assertEquals(10, dataDetails.getPoint().y(), 0);
        assertEquals(90, dataDetails.getRotation(), 0);
        List<DiagramPoint> t1Points = dataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1);
        List<DiagramPoint> t2Points = dataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2);
        assertEquals(1, t1Points.get(0).seq(), 0);
        assertEquals(0, t1Points.get(0).x(), 0);
        assertEquals(10, t1Points.get(0).y(), 0);
        assertEquals(2, t1Points.get(1).seq(), 0);
        assertEquals(15, t1Points.get(1).x(), 0);
        assertEquals(10, t1Points.get(1).y(), 0);
        assertEquals(1, t2Points.get(0).seq(), 0);
        assertEquals(25, t2Points.get(0).x(), 0);
        assertEquals(10, t2Points.get(0).y(), 0);
        assertEquals(2, t2Points.get(1).seq(), 0);
        assertEquals(40, t2Points.get(1).x(), 0);
        assertEquals(10, t2Points.get(1).y(), 0);
    }

}
