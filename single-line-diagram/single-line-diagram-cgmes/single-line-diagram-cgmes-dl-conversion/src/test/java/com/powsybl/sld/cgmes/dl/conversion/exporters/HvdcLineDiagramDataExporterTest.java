/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.exporters;

import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.powsybl.sld.cgmes.dl.iidm.extensions.Networks;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
class HvdcLineDiagramDataExporterTest extends AbstractNodeLineDiagramDataExporterTest {

    private HvdcLine hvdcLine;

    @BeforeEach
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithHvdcLine();
        hvdcLine = network.getHvdcLine("HvdcLine");
        LineDiagramData<HvdcLine> hvdcLineDiagramData = new LineDiagramData<>(hvdcLine);
        hvdcLineDiagramData.addPoint(basename, point1);
        hvdcLineDiagramData.addPoint(basename, point2);
        hvdcLine.addExtension(LineDiagramData.class, hvdcLineDiagramData);
        NetworkDiagramData.addDiagramName(network, basename, "Substation1");
        NetworkDiagramData.addDiagramName(network, basename, "Substation2");

        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
    }

    @Override
    protected void checkStatements() {
        checkStatements(hvdcLine.getId(), hvdcLine.getName(), "bus-branch");
    }

}
