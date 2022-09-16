/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCaseMissingBusbarPosition extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);

        vl.getNodeBreakerView().newBusbarSection().setId("bbs1").setName("bbs1").setNode(0).add(); // no position extension
        createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 1, 2);

        createBusBarSection(vl, "bbs2", "bbs2", 3, 1, 1);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 3, 4);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 4, 5);
        createGenerator(vl, "g", "g", "generator", 2, ConnectablePosition.Direction.BOTTOM, 5, 0, 20, false, 10, 10);
    }

    @Test
    public void busApartTest() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseMissingBusbarPositionBusApart.json"), toJson(g, "/TestCaseMissingBusbarPositionBusApart.json"));
    }

    @Test
    public void busParallelTest() {
        createSwitch(vl, "d2l", "d2l", SwitchKind.DISCONNECTOR, false, false, false, 3, 1);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseMissingBusbarPositionBusParallel.json"), toJson(g, "/TestCaseMissingBusbarPositionBusParallel.json"));
    }
}
