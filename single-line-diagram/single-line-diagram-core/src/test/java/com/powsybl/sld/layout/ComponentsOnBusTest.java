/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.iidm.CreateNetworksUtil;
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
        network = CreateNetworksUtil.createNetworkWithSvcVscScDl();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("vl");
    }

    @Test
    void testNoComponentsOnBuses() {
        layoutParameters.setComponentsOnBusbars(Collections.emptyList());
        VoltageLevelGraph vlg = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(vlg);
        assertEquals(toString("/noComponentsOnBus.svg"), toSVG(vlg, "/noComponentsOnBus.svg"));
    }

    @Test
    void testSwitchesOnBuses() {
        layoutParameters.setComponentsOnBusbars(List.of(ComponentTypeName.BREAKER, ComponentTypeName.DISCONNECTOR));
        VoltageLevelGraph vlg = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(vlg);
        assertEquals(toString("/switchesOnBus.svg"), toSVG(vlg, "/switchesOnBus.svg"));
    }
}
