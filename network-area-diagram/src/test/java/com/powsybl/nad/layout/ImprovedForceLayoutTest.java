/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class ImprovedForceLayoutTest {

    @Test
    void testMissingVL8InitialPosition() {
        Network network = IeeeCdfNetworkFactory.create9();
        Map<String, Point> initialPositions = new HashMap<>();
        initialPositions.put("VL1", new Point(900, -900));
        initialPositions.put("VL2", new Point(-300, 250));
        initialPositions.put("VL3", new Point(400, 200));
        initialPositions.put("VL5", new Point(500, 500));
        initialPositions.put("VL6", new Point(700, 700));

        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new ImprovedForceLayoutFactory(initialPositions).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(initialPositions.get("VL1").getX(), actual.get("VL1").getX());
        assertEquals(initialPositions.get("VL1").getY(), actual.get("VL1").getY());
        assertEquals(initialPositions.get("VL2").getX(), actual.get("VL2").getX());
        assertEquals(initialPositions.get("VL2").getY(), actual.get("VL2").getY());
        assertEquals(initialPositions.get("VL3").getX(), actual.get("VL3").getX());
        assertEquals(initialPositions.get("VL3").getY(), actual.get("VL3").getY());
        assertEquals(initialPositions.get("VL5").getX(), actual.get("VL5").getX());
        assertEquals(initialPositions.get("VL5").getY(), actual.get("VL5").getY());
        assertEquals(initialPositions.get("VL6").getX(), actual.get("VL6").getX());
        assertEquals(initialPositions.get("VL6").getY(), actual.get("VL6").getY());
    }
}
