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
abstract class AbstractInjectionDiagramDataTest {
    protected static String DIAGRAM_NAME = "default";

    protected <T> void checkDiagramData(InjectionDiagramData<?> diagramData, String diagramName) {
        assertNotNull(diagramData);
        InjectionDiagramData.InjectionDiagramDetails diagramDataDetails = diagramData.getData(diagramName);
        assertEquals(0, diagramDataDetails.getPoint().seq(), 0);
        assertEquals(20, diagramDataDetails.getPoint().x(), 0);
        assertEquals(10, diagramDataDetails.getPoint().y(), 0);
        assertEquals(90, diagramDataDetails.getRotation(), 0);
        List<DiagramPoint> points = diagramDataDetails.getTerminalPoints();
        assertEquals(1, points.get(0).seq(), 0);
        assertEquals(0, points.get(0).x(), 0);
        assertEquals(10, points.get(0).y(), 0);
        assertEquals(2, points.get(1).seq(), 0);
        assertEquals(15, points.get(1).x(), 0);
        assertEquals(10, points.get(1).y(), 0);
    }

}
