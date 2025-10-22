/**
 * Copyright (c) 2019-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCaseMissingBusbarPosition extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);

        vl.getNodeBreakerView().newBusbarSection().setId("bbs1").setName("bbs1").setNode(0).add(); // no position extension
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 1, 2);

        Networks.createBusBarSection(vl, "bbs2", "bbs2", 3, 1, 1);
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 3, 4);
        Networks.createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 4, 5);
        Networks.createGenerator(vl, "g", "g", "generator", 2, ConnectablePosition.Direction.BOTTOM, 5, 0, 20, false, 10, 10);
    }

    @Test
    void busApartTest() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseMissingBusbarPositionBusApart.json"), toJson(g, "/TestCaseMissingBusbarPositionBusApart.json"));
    }

    @Test
    void busParallelTest() {
        Networks.createSwitch(vl, "d2l", "d2l", SwitchKind.DISCONNECTOR, false, false, false, 3, 1);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseMissingBusbarPositionBusParallel.json"), toJson(g, "/TestCaseMissingBusbarPositionBusParallel.json"));
    }
}
