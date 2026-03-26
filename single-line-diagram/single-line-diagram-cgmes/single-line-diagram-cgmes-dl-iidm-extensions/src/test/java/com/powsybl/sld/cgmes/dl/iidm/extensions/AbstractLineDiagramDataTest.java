/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
abstract class AbstractLineDiagramDataTest {
    protected static final String DIAGRAM_NAME = "default";
    protected static final String DIAGRAM2_NAME = "diagram2";

    protected <T> void checkDiagramData(LineDiagramData<?> diagramData, String diagramName) {
        assertNotNull(diagramData);
        assertNotNull(diagramName);
        switch (diagramName) {
            case DIAGRAM_NAME:
                assertEquals(1, diagramData.getPoints(diagramName).get(0).seq(), 0);
                assertEquals(0, diagramData.getPoints(diagramName).get(0).x(), 0);
                assertEquals(10, diagramData.getPoints(diagramName).get(0).y(), 0);
                assertEquals(2, diagramData.getPoints(diagramName).get(1).seq(), 0);
                assertEquals(10, diagramData.getPoints(diagramName).get(1).x(), 0);
                assertEquals(0, diagramData.getPoints(diagramName).get(1).y(), 0);
                assertEquals(1, diagramData.getFirstPoint(diagramName).seq(), 0);
                assertEquals(0, diagramData.getFirstPoint(diagramName).x(), 0);
                assertEquals(10, diagramData.getFirstPoint(diagramName).y(), 0);
                assertEquals(2, diagramData.getLastPoint(diagramName).seq(), 0);
                assertEquals(10, diagramData.getLastPoint(diagramName).x(), 0);
                assertEquals(0, diagramData.getLastPoint(diagramName).y(), 0);
                assertEquals(1, diagramData.getFirstPoint(diagramName, 5).seq(), 0);
                assertEquals(3.535, diagramData.getFirstPoint(diagramName, 5).x(), .001);
                assertEquals(6.464, diagramData.getFirstPoint(diagramName, 5).y(), .001);
                assertEquals(2, diagramData.getLastPoint(diagramName, 5).seq(), 0);
                assertEquals(6.464, diagramData.getLastPoint(diagramName, 5).x(), .001);
                assertEquals(3.535, diagramData.getLastPoint(diagramName, 5).y(), .001);
                break;
            case DIAGRAM2_NAME:
                assertEquals(10, diagramData.getPoints(diagramName).get(0).x(), 0);
                assertEquals(20, diagramData.getPoints(diagramName).get(0).y(), 0);
                assertEquals(1, diagramData.getPoints(diagramName).get(0).seq(), 0);
                assertEquals(20, diagramData.getPoints(diagramName).get(1).x(), 0);
                assertEquals(10, diagramData.getPoints(diagramName).get(1).y(), 0);
                assertEquals(2, diagramData.getPoints(diagramName).get(1).seq(), 0);
                assertEquals(10, diagramData.getFirstPoint(diagramName).x(), 0);
                assertEquals(20, diagramData.getFirstPoint(diagramName).y(), 0);
                assertEquals(1, diagramData.getFirstPoint(diagramName).seq(), 0);
                assertEquals(20, diagramData.getLastPoint(diagramName).x(), 0);
                assertEquals(10, diagramData.getLastPoint(diagramName).y(), 0);
                assertEquals(2, diagramData.getLastPoint(diagramName).seq(), 0);
                assertEquals(1, diagramData.getFirstPoint(diagramName, 5).seq(), 0);
                assertEquals(13.535, diagramData.getFirstPoint(diagramName, 5).x(), .001);
                assertEquals(16.464, diagramData.getFirstPoint(diagramName, 5).y(), .001);
                assertEquals(2, diagramData.getLastPoint(diagramName, 5).seq(), 0);
                assertEquals(16.464, diagramData.getLastPoint(diagramName, 5).x(), .001);
                assertEquals(13.535, diagramData.getLastPoint(diagramName, 5).y(), .001);
                break;
        }

    }

}
