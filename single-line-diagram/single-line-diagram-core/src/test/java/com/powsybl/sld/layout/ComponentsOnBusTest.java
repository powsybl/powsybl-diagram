/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.SingleLineDiagramConfiguration;
import com.powsybl.sld.SingleLineDiagramConfigurationBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.iidm.CreateNetworksUtil;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ComponentsOnBusTest extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createNetworkWithSvcVscScDl();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("vl");
    }

    @Test
    public void testNoComponentsOnBuses() {
        layoutParameters.setComponentsOnBusbars(Collections.emptyList());
        VoltageLevelGraph vlg = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(vlg);
        SingleLineDiagramConfiguration singleLineDiagramConfiguration = new SingleLineDiagramConfigurationBuilder(network)
                .withComponentLibrary(getResourcesComponentLibrary())
                .withLayoutParameters(layoutParameters)
                .build();
        assertEquals(toString("/noComponentsOnBus.svg"), toSVG(vlg, "/noComponentsOnBus.svg", singleLineDiagramConfiguration));
    }

    @Test
    public void testSwitchesOnBuses() {
        layoutParameters.setComponentsOnBusbars(List.of(ComponentTypeName.BREAKER, ComponentTypeName.DISCONNECTOR));
        VoltageLevelGraph vlg = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(vlg);
        SingleLineDiagramConfiguration singleLineDiagramConfiguration = new SingleLineDiagramConfigurationBuilder(network)
                .withComponentLibrary(getResourcesComponentLibrary())
                .withLayoutParameters(layoutParameters)
                .build();
        assertEquals(toString("/switchesOnBus.svg"), toSVG(vlg, "/switchesOnBus.svg", singleLineDiagramConfiguration));
    }
}
