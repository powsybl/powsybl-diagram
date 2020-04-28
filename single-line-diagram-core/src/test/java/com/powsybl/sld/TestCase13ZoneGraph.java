/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.model.ZoneGraphTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class TestCase13ZoneGraph extends AbstractTestCase {
    @Before
    public void setUp() {
        network = ZoneGraphTest.createNetwork();
    }

    @Test
    public void test() {
        List<String> zone = Arrays.asList(ZoneGraphTest.SUBSTATION_ID_1, ZoneGraphTest.SUBSTATION_ID_2);
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone, false);
        // write Json and compare to reference
        assertEquals(toString("/TestCase13ZoneGraph.json"), toJson(g, "/TestCase13ZoneGraph.json"));
    }
}
