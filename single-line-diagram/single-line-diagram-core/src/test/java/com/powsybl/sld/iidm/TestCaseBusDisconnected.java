/**
 * Copyright (c) 2024-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class TestCaseBusDisconnected extends AbstractTestCaseIidm {

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        network = Network.create("testCaseBusNodeDisconnected", "testCaseBusNodeDisconnected");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);

        // bus breaker topology
        VoltageLevel vlBb = Networks.createVoltageLevel(substation, "vlBb", "vlBb", TopologyKind.BUS_BREAKER, 225);
        Bus b1 = vlBb.getBusBreakerView().newBus().setId("b1").add();
        Bus b2 = vlBb.getBusBreakerView().newBus().setId("b2").add();
        Bus b3 = vlBb.getBusBreakerView().newBus().setId("b3").add();
        vlBb.getBusBreakerView().newSwitch().setId("s12").setBus1(b1.getId()).setBus2(b2.getId()).add();
        vlBb.getBusBreakerView().newSwitch().setId("s23").setOpen(true).setBus1(b2.getId()).setBus2(b3.getId()).add();

        // node breaker topology
        VoltageLevel vlNb = Networks.createVoltageLevel(substation, "vlNb", "vlNb", TopologyKind.NODE_BREAKER, 225);
        vlNb.getNodeBreakerView().newBusbarSection().setId("bbs1").setNode(0).add();
        vlNb.getNodeBreakerView().newBusbarSection().setId("bbs2").setNode(1).add();
        vlNb.getNodeBreakerView().newSwitch().setId("sLine").setKind(SwitchKind.DISCONNECTOR).setNode1(0).setNode2(2).add();
        vlNb.getNodeBreakerView().newSwitch().setId("sLoad").setOpen(true).setKind(SwitchKind.DISCONNECTOR).setNode1(1).setNode2(3).add();
        vlNb.newLoad().setNode(3).setId("load").setP0(10).setQ0(10).add();

        network.newLine().setId("line")
                .setG1(0).setB1(0).setVoltageLevel1(vlBb.getId()).setBus1(b2.getId())
                .setG2(0).setB2(0).setVoltageLevel2(vlNb.getId()).setNode2(2)
                .setR(1).setX(1)
                .add();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestData")
    void test(String testName, String vlId, String svgResourceName) {
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vlId);
        voltageLevelGraphLayout(g);
        assertEquals(toString(svgResourceName), toSVG(g, svgResourceName));
    }

    private static List<Arguments> provideTestData() {
        return List.of(
                Arguments.of("Test bus connected/disconnected", "vlBb", "/TestCaseBusBreakerBusConnected.svg"),
                Arguments.of("Test busbarSection connected/disconnected", "vlNb", "/TestCaseNodeBreakerBbsConnected.svg")
        );
    }
}
