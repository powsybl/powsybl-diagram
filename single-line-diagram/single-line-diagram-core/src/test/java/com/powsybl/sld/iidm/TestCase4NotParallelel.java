/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <pre>
 *            la                        gc
 *             |                        |
 *            ba                        bc
 *           /  \                       |
 *          |    |                      |
 * bbs1.1 -da1---|--- ss1 --db1--------dc- bbs1.2
 * bbs2.1 ------da2----------|---db2------
 *                           |    |
 *                            \  /
 *                             bb
 *                              |
 *                             lb
 *
 * </pre>
 * <p>
 * the branch c is to cover the merging part of SubSections class (and use of generator)
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCase4NotParallelel extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs1.1", "bbs1.1", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs1.2", "bbs1.2", 1, 1, 2);
        Networks.createBusBarSection(vl, "bbs2.1", "bbs2.1", 2, 2, 1);
        Networks.createLoad(vl, "la", "la", "la", 10, ConnectablePosition.Direction.TOP, 3, 10, 10);
        Networks.createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 3, 4);
        Networks.createSwitch(vl, "da1", "da1", SwitchKind.DISCONNECTOR, false, false, false, 4, 0);
        Networks.createSwitch(vl, "da2", "da2", SwitchKind.DISCONNECTOR, false, false, false, 4, 2);
        Networks.createLoad(vl, "lb", "lb", "lb", 20, ConnectablePosition.Direction.BOTTOM, 5, 10, 10);
        Networks.createSwitch(vl, "bb", "bb", SwitchKind.BREAKER, false, false, false, 5, 6);
        Networks.createSwitch(vl, "db1", "db1", SwitchKind.DISCONNECTOR, false, false, false, 6, 1);
        Networks.createSwitch(vl, "db2", "db2", SwitchKind.DISCONNECTOR, false, false, false, 6, 2);
        Networks.createSwitch(vl, "ss1", "ss1", SwitchKind.DISCONNECTOR, false, false, false, 1, 0);
        Networks.createGenerator(vl, "gc", "gc", "gc", 30, ConnectablePosition.Direction.TOP, 7, 0, 20, false, 10, 10);
        Networks.createSwitch(vl, "bc", "bc", SwitchKind.BREAKER, false, false, false, 7, 8);
        Networks.createSwitch(vl, "dc1", "dc1", SwitchKind.DISCONNECTOR, false, false, false, 8, 1);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCase4NotParallelel.json"), toJson(g, "/TestCase4NotParallelel.json"));
    }
}
