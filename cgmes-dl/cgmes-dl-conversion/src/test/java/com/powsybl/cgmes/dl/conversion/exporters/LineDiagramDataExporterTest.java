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
import com.powsybl.iidm.network.Line;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LineDiagramDataExporterTest extends AbstractNodeLineDiagramDataExporterTest {

    private Line line;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithLine();
        line = network.getLine("Line");
        LineDiagramData<Line> lineDiagramData = new LineDiagramData<>(line);
        lineDiagramData.addPoint(basename, point1);
        lineDiagramData.addPoint(basename, point2);
        line.addExtension(LineDiagramData.class, lineDiagramData);
        NetworkDiagramData.addDiagramName(network, basename);

        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
    }

    @Override
    protected void checkStatements() {
        checkStatements(line.getId(), line.getName(), "bus-branch");
    }

}
