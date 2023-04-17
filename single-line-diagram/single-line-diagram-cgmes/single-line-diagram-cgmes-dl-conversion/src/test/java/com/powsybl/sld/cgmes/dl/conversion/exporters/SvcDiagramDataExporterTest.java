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

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
class SvcDiagramDataExporterTest extends AbstractInjectionDiagramDataExporterTest {

    private StaticVarCompensator svc;

    @BeforeEach
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithStaticVarCompensator();
        svc = network.getStaticVarCompensator("Svc");
        InjectionDiagramData<StaticVarCompensator> svcDiagramData = new InjectionDiagramData<>(svc);
        InjectionDiagramData.InjectionDiagramDetails details = svcDiagramData.new InjectionDiagramDetails(point, rotation);
        details.addTerminalPoint(terminalPoint1);
        details.addTerminalPoint(terminalPoint2);
        svcDiagramData.addData(basename, details);
        svc.addExtension(InjectionDiagramData.class, svcDiagramData);
        NetworkDiagramData.addDiagramName(network, basename, "Substation");

        Mockito.when(cgmesDLModel.getTerminals()).thenReturn(getTerminals(svc.getId()));
    }

    @Override
    protected void checkStatements() {
        checkStatements(svc.getId(), svc.getName(), "bus-branch");
    }

}
