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

        assertEquals(7854.0, actual.get("VL1").getX(), 0.1);
        assertEquals(-5237.1, actual.get("VL1").getY(), 0.1);
        assertEquals(13090.0, actual.get("VL2").getX(), 0.1);
        assertEquals(-10480.5, actual.get("VL2").getY(), 0.1);
    }

    @Test
    void layoutWithGeographicalPositionsCustomisedParametersTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();

        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network, 200, 50d, BasicForceLayout::new).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(10.5, actual.get("VL1").getX(), 0.1);
        assertEquals(-7.0, actual.get("VL1").getY(), 0.1);
        assertEquals(17.5, actual.get("VL2").getX(), 0.1);
        assertEquals(-14.0, actual.get("VL2").getY(), 0.1);
    }

    @Test
    void layoutWithGeographicalPositionsTwoVoltageLevelsInSameSubstationTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();
        network.getSubstation("S1").newVoltageLevel().setNominalV(400d).setTopologyKind(TopologyKind.BUS_BREAKER).setId("VL1_1").add();
        network.getSubstation("S1").newVoltageLevel().setNominalV(400d).setTopologyKind(TopologyKind.BUS_BREAKER).setId("VL1_2").add();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network, 100, 50d, BasicForceLayout::new).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(55.2, actual.get("VL1").getX(), 0.1);
        assertEquals(-3.5, actual.get("VL1").getY(), 0.1);
        assertEquals(-19.8, actual.get("VL1_1").getX(), 0.1);
        assertEquals(39.8, actual.get("VL1_1").getY(), 0.1);
        assertEquals(-19.8, actual.get("VL1_2").getX(), 0.1);
        assertEquals(-46.8, actual.get("VL1_2").getY(), 0.1);
    }
}

