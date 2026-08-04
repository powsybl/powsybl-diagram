/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class LayoutIdempotencyTest extends AbstractTestCaseIidm {

    LayoutParameters layoutParameter = new LayoutParameters();

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
    }

    @Test
    void layoutRunTwiceShouldProduceSameGraph() {
        VoltageLevelGraph graph = graphBuilder.buildVoltageLevelGraph(vl.getId());
        var layout = new PositionVoltageLevelLayoutFactory().create(graph);
//        var layout = new SmartVoltageLevelLayoutFactory(network).create(graph);
//        var layout = new RandomVoltageLevelLayoutFactory(40, 40).create(graph);
        layout.run(layoutParameter);
        String afterFirstRun = toJson(graph, "/afterFirstRun.json");
        debugSvgFiles = true; // check .powsybl/debug-sld
        String svgAfterFirstLayout = toSVG(graph, "/svg-after-first-layout.svg");
        layout.run(layoutParameter);
        String afterSecondRun = toJson(graph, "/afterSecondRun.json");
        debugSvgFiles = true;
        String svgAfterSecondLayout = toSVG(graph, "/svg-after-second-layout.svg");
        assertEquals(afterFirstRun, afterSecondRun);
        assertEquals(svgAfterFirstLayout, svgAfterSecondLayout);
    }
}
