/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.dl.conversion.exporters;

import com.powsybl.cgmes.iidm.extensions.dl.NetworkDiagramData;
import org.junit.Before;
import org.mockito.Mockito;

import com.powsybl.cgmes.iidm.Networks;
import com.powsybl.cgmes.iidm.extensions.dl.LineDiagramData;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class DanglingLineDiagramDataExporterTest extends AbstractNodeLineDiagramDataExporterTest {

    private DanglingLine danglingLine;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithDanglingLine();
        danglingLine = network.getDanglingLine("DanglingLine");
        LineDiagramData<DanglingLine> danglingLineDiagramData = new LineDiagramData<>(danglingLine);
        danglingLineDiagramData.addPoint(basename, point1);
        danglingLineDiagramData.addPoint(basename, point2);
        danglingLine.addExtension(LineDiagramData.class, danglingLineDiagramData);
        NetworkDiagramData.addDiagramName(network, basename);

        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
    }

    @Override
    protected void checkStatements() {
        checkStatements(danglingLine.getId(), danglingLine.getName(), "bus-branch");
    }

}
