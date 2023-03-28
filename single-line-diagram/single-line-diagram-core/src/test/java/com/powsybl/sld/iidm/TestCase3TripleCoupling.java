/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <pre>
 *     b        b2       b3
 *    / \      / \      / \
 *   |   |    |   |    |   |
 * -d1---|---d3---|---d6---|---- bbs1
 *       |    |   |        |
 * -----d2---d4--d5-------d7---- bbs2
 *
 * </pre>
 *
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestCase3TripleCoupling extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase2", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 2, 3);
        createBusBarSection(vl, "bbs2", "bbs2", 3, 2, 1);

        // second coupling
        createSwitch(vl, "d3", "d3", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        createSwitch(vl, "d4", "d4", SwitchKind.DISCONNECTOR, false, false, false, 3, 4);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 4, 5);
        createSwitch(vl, "d5", "d5", SwitchKind.DISCONNECTOR, false, false, false, 5, 3);

        // third coupling
        createSwitch(vl, "d6", "d6", SwitchKind.DISCONNECTOR, false, false, false, 0, 6);
        createSwitch(vl, "b3", "b3", SwitchKind.BREAKER, false, false, false, 6, 7);
        createSwitch(vl, "d7", "d7", SwitchKind.DISCONNECTOR, false, false, false, 7, 3);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCase3TripleCoupling.json"), toJson(g, "/TestCase3TripleCoupling.json"));
    }

    @Test
    void testDisconnectorOpen() {
        // the displayed result should remain the same in terms of intern cells order after opening a switch
        network.getSwitch("d3").setOpen(true);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        String reference = toString("/TestCase3TripleCoupling.json");
        int d3Index = reference.indexOf("d3");
        int d3StateIndex = d3Index + reference.substring(d3Index).indexOf("\"open\" : false");
        reference = reference.substring(0, d3StateIndex) + reference.substring(d3StateIndex).replaceFirst("\"open\" : false", "\"open\" : true");
        assertEquals(reference, toJson(g, "/TestCase3TripleCoupling_disconnectorOpen.json"));
    }
}
