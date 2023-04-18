/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TestCase1BusBreaker extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("busBreakerTestCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.BUS_BREAKER, 380, 10);
        VoltageLevel.BusBreakerView view = vl.getBusBreakerView();
        view.newBus()
                .setId("b1")
                .add();
        view.newBus()
                .setId("b2")
                .add();
        view.newSwitch()
                .setId("sw")
                .setBus1("b1")
                .setBus2("b2")
                .setOpen(false)
                .add();
        Load l = vl.newLoad()
                .setId("l")
                .setConnectableBus("b1")
                .setBus("b1")
                .setP0(10)
                .setQ0(10)
                .add();
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCase1BusBreaker.json"), toJson(g, "/TestCase1BusBreaker.json"));
    }
}
