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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class LayoutWithGeographicalPositionsTest {

    @Test
    void layoutWithGeographicalPositionsTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();

        LayoutParameters layoutParameters = new LayoutParameters();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER, layoutParameters).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network).create();
        forceLayout.run(graph, layoutParameters);
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(7854.0, actual.get("VL1").x(), 0.1);
        assertEquals(-5237.1, actual.get("VL1").y(), 0.1);
        assertEquals(13090.0, actual.get("VL2").x(), 0.1);
        assertEquals(-10480.5, actual.get("VL2").y(), 0.1);
    }

    @Test
    void layoutWithGeographicalPositionsCustomisedParametersTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();

        LayoutParameters layoutParameters = new LayoutParameters();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER, layoutParameters).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network, 200, 50d, BasicForceLayout::new).create();
        forceLayout.run(graph, layoutParameters);
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(10.5, actual.get("VL1").x(), 0.1);
        assertEquals(-7.0, actual.get("VL1").y(), 0.1);
        assertEquals(17.5, actual.get("VL2").x(), 0.1);
        assertEquals(-14.0, actual.get("VL2").y(), 0.1);
    }

    @Test
    void layoutWithGeographicalPositionsTwoVoltageLevelsInSameSubstationTest() {
        Network network = Networks.createIeee9NetworkWithOneMissingSubstationPosition();
        network.getSubstation("S1").newVoltageLevel().setNominalV(400d).setTopologyKind(TopologyKind.BUS_BREAKER).setId("VL1_1").add();
        network.getSubstation("S1").newVoltageLevel().setNominalV(400d).setTopologyKind(TopologyKind.BUS_BREAKER).setId("VL1_2").add();
        LayoutParameters layoutParameters = new LayoutParameters();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER, layoutParameters).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network, 100, 50d, BasicForceLayout::new).create();
        forceLayout.run(graph, layoutParameters);
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(55.2, actual.get("VL1").x(), 0.1);
        assertEquals(-3.5, actual.get("VL1").y(), 0.1);
        assertEquals(-19.8, actual.get("VL1_1").x(), 0.1);
        assertEquals(39.8, actual.get("VL1_1").y(), 0.1);
        assertEquals(-19.8, actual.get("VL1_2").x(), 0.1);
        assertEquals(-46.8, actual.get("VL1_2").y(), 0.1);
    }

    @Test
    void layoutWithGeographicalPositionRelativePositionsTest() {
        Network network = Networks.createThreeSubstationsWithSubstationPosition();

        LayoutParameters layoutParameters = new LayoutParameters();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER, layoutParameters).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network).create();
        forceLayout.run(graph, layoutParameters);
        Map<String, Point> actual = graph.getNodePositions();

        assertTrue(actual.get("vl11").x() < actual.get("vl21").x());
        assertTrue(actual.get("vl11").y() < actual.get("vl21").y());
        assertTrue(actual.get("vl12").x() < actual.get("vl21").x());
        assertTrue(actual.get("vl12").y() < actual.get("vl21").y());
        assertTrue(actual.get("vl13").x() < actual.get("vl21").x());
        assertTrue(actual.get("vl13").y() < actual.get("vl21").y());

        assertTrue(actual.get("vl11").x() < actual.get("vl31").x());
        assertTrue(actual.get("vl11").y() > actual.get("vl31").y());
        assertTrue(actual.get("vl12").x() < actual.get("vl31").x());
        assertTrue(actual.get("vl12").y() > actual.get("vl31").y());
        assertTrue(actual.get("vl13").x() < actual.get("vl31").x());
        assertTrue(actual.get("vl13").y() > actual.get("vl31").y());

        assertTrue(actual.get("vl11").distance(actual.get("vl31")) > actual.get("vl11").distance(actual.get("vl21")));
    }
}

