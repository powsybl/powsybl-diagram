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
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class HvdcLineDiagramDataExporterTest extends AbstractNodeLineDiagramDataExporterTest {

    private HvdcLine hvdcLine;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithHvdcLine();
        hvdcLine = network.getHvdcLine("HvdcLine");
        LineDiagramData<HvdcLine> hvdcLineDiagramData = new LineDiagramData<>(hvdcLine);
        hvdcLineDiagramData.addPoint(basename, point1);
        hvdcLineDiagramData.addPoint(basename, point2);
        hvdcLine.addExtension(LineDiagramData.class, hvdcLineDiagramData);
        NetworkDiagramData.addDiagramName(network, basename);

        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
    }

    @Override
    protected void checkStatements() {
        checkStatements(hvdcLine.getId(), hvdcLine.getName(), "bus-branch");
    }

}
