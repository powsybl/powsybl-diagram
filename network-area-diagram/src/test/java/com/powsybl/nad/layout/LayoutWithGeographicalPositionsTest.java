/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class LayoutWithGeographicalPositionsTest {

    @Test
    void layoutWithGeographicalPositionsTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();

        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(300, actual.get("VL1").getX());
        assertEquals(200, actual.get("VL1").getY());
        assertEquals(500, actual.get("VL2").getX());
        assertEquals(400, actual.get("VL2").getY());
    }

    @Test
    void layoutWithGeographicalPositionsCustomisedParametersTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();

        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network, 200, 50d).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(600, actual.get("VL1").getX());
        assertEquals(400, actual.get("VL1").getY());
        assertEquals(1000, actual.get("VL2").getX());
        assertEquals(800, actual.get("VL2").getY());
    }

    @Test
    void layoutWithGeographicalPositionsTwoVoltageLevelsInSameSubstationTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();
        network.getSubstation("S1").newVoltageLevel().setNominalV(400d).setTopologyKind(TopologyKind.BUS_BREAKER).setId("VL1_1").add();
        network.getSubstation("S1").newVoltageLevel().setNominalV(400d).setTopologyKind(TopologyKind.BUS_BREAKER).setId("VL1_2").add();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network, 100, 50d).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(350, actual.get("VL1").getX());
        assertEquals(200, actual.get("VL1").getY());
        assertEquals(275, actual.get("VL1_1").getX());
        assertEquals(243.3d, actual.get("VL1_1").getY(), 0.1);
        assertEquals(275, actual.get("VL1_2").getX());
        assertEquals(156.7, actual.get("VL1_2").getY(), 0.1);
    }
}

