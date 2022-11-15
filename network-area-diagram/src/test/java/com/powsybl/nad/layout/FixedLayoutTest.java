/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.NetworkTestFactory;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
public class FixedLayoutTest {

    @Test
    public void testCurrentLimits() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();

        Map<String, Point> expected = Map.of(
                "vl1", new Point(1, 0),
                "vl2", new Point(2, 1));
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout fixedLayout = new BasicFixedLayoutFactory(expected).create();
        fixedLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(expected.keySet(), actual.keySet());
        expected.keySet().forEach(k -> {
            Point pexpected = expected.get(k);
            Point pactual = actual.get(k);
            assertNotNull(pactual);
            assertEquals(pexpected.getX(), pactual.getX(), 0);
            assertEquals(pexpected.getY(), pactual.getY(), 0);
        });
    }
}
