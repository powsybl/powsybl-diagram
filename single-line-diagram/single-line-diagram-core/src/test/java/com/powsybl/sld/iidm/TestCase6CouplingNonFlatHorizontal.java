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
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <pre>
 *              b
 *           /     \
 *          |       |
 * bbs1.1 -d1- ds1 -|-- bbs1.2
 * bbs2.1 ---- ds2 -d2- bbs2.2
 *
 * </pre>
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCase6CouplingNonFlatHorizontal extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs1.1", "bbs1.1", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs1.2", "bbs1.2", 1, 1, 2);
        Networks.createBusBarSection(vl, "bbs2.1", "bbs2.1", 2, 2, 1);
        Networks.createBusBarSection(vl, "bbs2.2", "bbs2.2", 3, 2, 2);
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 4, 5);
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 5, 3);
        Networks.createSwitch(vl, "ds1", "ds1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "ds2", "ds2", SwitchKind.DISCONNECTOR, false, false, false, 2, 3);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCase6CouplingNonFlatHorizontal.json"), toJson(g, "/TestCase6CouplingNonFlatHorizontal.json"));
    }
}
