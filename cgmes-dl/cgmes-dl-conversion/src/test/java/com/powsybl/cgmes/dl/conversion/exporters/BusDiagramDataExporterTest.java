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
import com.powsybl.cgmes.iidm.extensions.dl.NodeDiagramData;
import com.powsybl.iidm.network.Bus;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusDiagramDataExporterTest extends AbstractNodeLineDiagramDataExporterTest {

    private Bus bus;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithBus();
        bus = network.getVoltageLevel("VoltageLevel").getBusBreakerView().getBus("Bus");
        NodeDiagramData<Bus> busDiagramData = new NodeDiagramData<>(bus);
        NodeDiagramData.NodeDiagramDataDetails details = busDiagramData.new NodeDiagramDataDetails();
        details.setPoint1(point1);
        details.setPoint2(point2);
        busDiagramData.addData(basename, details);
        bus.addExtension(NodeDiagramData.class, busDiagramData);
        NetworkDiagramData.addDiagramName(network, basename);

        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
    }

    @Override
    protected void checkStatements() {
        checkStatements(bus.getId(), bus.getName(), "bus-branch");
    }

}
