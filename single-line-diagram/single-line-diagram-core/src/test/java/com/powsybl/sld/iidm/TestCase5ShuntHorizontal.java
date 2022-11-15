/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <pre>
 *
 *       la     lb
 *       |      |
 *      nsa-bs-nsb
 *       |      |
 *       ba     bb
 *       |      |
 * bbs---da-----db---
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCase5ShuntHorizontal extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createLoad(vl, "la", "la", "la", 10, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 2, 1);
        createSwitch(vl, "da", "da", SwitchKind.DISCONNECTOR, false, false, false, 1, 0);
        createLoad(vl, "lb", "lb", "lb", 20, ConnectablePosition.Direction.BOTTOM, 4, 10, 10);
        createSwitch(vl, "bb", "bb", SwitchKind.BREAKER, false, false, false, 4, 3);
        createSwitch(vl, "db", "db", SwitchKind.DISCONNECTOR, false, false, false, 3, 0);
        createSwitch(vl, "bs", "bs", SwitchKind.BREAKER, false, false, false, 2, 4);
    }

    @Test
    public void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCase5H.json"), toJson(g, "/TestCase5H.json"));
    }
}
