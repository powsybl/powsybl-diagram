/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.Config;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class ComponentsOnBusTest extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Networks.createNetworkWithSvcVscScDl();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("vl");
    }

    @Test
    void testNoComponentsOnBuses() {
        layoutParameters.setComponentsOnBusbars(Collections.emptyList());
        VoltageLevelGraph vlg = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(vlg);
        Config config = new ConfigBuilder(network)
                .withComponentLibrary(getResourcesComponentLibrary())
                .withLayoutParameters(layoutParameters)
                .withSvgParameters(svgParameters)
                .build();
        assertEquals(toString("/noComponentsOnBus.svg"), toSVG(vlg, "/noComponentsOnBus.svg", config));
    }

    @Test
    void testSwitchesOnBuses() {
        layoutParameters.setComponentsOnBusbars(List.of(ComponentTypeName.BREAKER, ComponentTypeName.DISCONNECTOR));
        VoltageLevelGraph vlg = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(vlg);
        Config config = new ConfigBuilder(network)
                .withComponentLibrary(getResourcesComponentLibrary())
                .withLayoutParameters(layoutParameters)
                .withSvgParameters(svgParameters)
                .build();
        assertEquals(toString("/switchesOnBus.svg"), toSVG(vlg, "/switchesOnBus.svg", config));
    }
}
