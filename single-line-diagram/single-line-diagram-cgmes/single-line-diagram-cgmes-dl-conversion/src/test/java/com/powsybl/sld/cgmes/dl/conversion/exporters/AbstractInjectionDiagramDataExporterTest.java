/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.exporters;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.powsybl.sld.cgmes.dl.conversion.AbstractCgmesDLExporterTest;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
abstract class AbstractInjectionDiagramDataExporterTest extends AbstractCgmesDLExporterTest {
    protected final DiagramPoint point = new DiagramPoint(20, 10, 0);
    protected final double rotation = 90;
    protected final DiagramPoint terminalPoint1 = new DiagramPoint(5, 10, 1);
    protected final DiagramPoint terminalPoint2 = new DiagramPoint(15, 10, 2);
    protected final String terminalId = "terminalId";

    @BeforeEach
    public void setUp() {
        super.setUp();

        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
    }

    protected PropertyBags getTerminals(String injectionId) {
        return new PropertyBags(Collections.singleton(getTerminal(injectionId, terminalId, 1)));
    }

    protected void checkStatements(String injectionId, String injectionName, String styleName) {
        Mockito.verify(tripleStore, Mockito.times(8)).add(contextCaptor.capture(), nsCaptor.capture(),
                                                          typeCaptor.capture(), propertiesCaptor.capture());
        checkDiagram(contextCaptor.getAllValues().get(1), nsCaptor.getAllValues().get(1), typeCaptor.getAllValues().get(1),
                     propertiesCaptor.getAllValues().get(1));
        checkDiagramObjectStyle(contextCaptor.getAllValues().get(2), nsCaptor.getAllValues().get(2), typeCaptor.getAllValues().get(2),
                                propertiesCaptor.getAllValues().get(2), styleName);
        checkDiagramObject(contextCaptor.getAllValues().get(3), nsCaptor.getAllValues().get(3), typeCaptor.getAllValues().get(3),
                           propertiesCaptor.getAllValues().get(3), injectionName, injectionId, rotation);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(4), nsCaptor.getAllValues().get(4), typeCaptor.getAllValues().get(4),
                                propertiesCaptor.getAllValues().get(4), point);
        checkDiagramObject(contextCaptor.getAllValues().get(5), nsCaptor.getAllValues().get(5), typeCaptor.getAllValues().get(5),
                           propertiesCaptor.getAllValues().get(5), injectionName + "_0", terminalId, 0);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(6), nsCaptor.getAllValues().get(6), typeCaptor.getAllValues().get(6),
                                propertiesCaptor.getAllValues().get(6), terminalPoint1);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(7), nsCaptor.getAllValues().get(7), typeCaptor.getAllValues().get(7),
                                propertiesCaptor.getAllValues().get(7), terminalPoint2);
    }

}
