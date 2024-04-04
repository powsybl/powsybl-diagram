/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.exporters;

import com.powsybl.sld.cgmes.dl.conversion.AbstractCgmesDLExporterTest;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

/**
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
abstract class AbstractVoltageLevelDiagramDataExporterTest extends AbstractCgmesDLExporterTest {

    protected final DiagramPoint point1 = new DiagramPoint(0, 0, 1);

    @BeforeEach
    public void setUp() {
        super.setUp();

        Mockito.when(cgmesDLModel.getTerminals()).thenReturn(new PropertyBags());
    }

    protected void checkStatements(String nodeId, String nodeName, String styleName) {
        Mockito.verify(tripleStore, Mockito.times(5)).add(contextCaptor.capture(), nsCaptor.capture(),
                                                          typeCaptor.capture(), propertiesCaptor.capture());
        checkDiagram(contextCaptor.getAllValues().get(1), nsCaptor.getAllValues().get(1), typeCaptor.getAllValues().get(1),
                     propertiesCaptor.getAllValues().get(1));
        checkDiagramObjectStyle(contextCaptor.getAllValues().get(2), nsCaptor.getAllValues().get(2), typeCaptor.getAllValues().get(2),
                                propertiesCaptor.getAllValues().get(2), styleName);
        checkDiagramObject(contextCaptor.getAllValues().get(3), nsCaptor.getAllValues().get(3), typeCaptor.getAllValues().get(3),
                           propertiesCaptor.getAllValues().get(3), nodeName, nodeId, 0);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(4), nsCaptor.getAllValues().get(4), typeCaptor.getAllValues().get(4),
                                propertiesCaptor.getAllValues().get(4), point1);
    }

}
