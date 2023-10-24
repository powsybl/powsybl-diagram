/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo  {@literal <sophie.frasnedo at rte-france.com>}
 */
class TestCaseGroundDisconnector extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("testCaseGroundDisconnector", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Substation substation2 = Networks.createSubstation(network, "s2", "s2", Country.FR);
        VoltageLevel vl2 = Networks.createVoltageLevel(substation2, "vl2", "vl2", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl, "load", "load", "l", 0, ConnectablePosition.Direction.BOTTOM, 2, 10, 10);
        Networks.createLine(network, "line", "line", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 4, 6, vl.getId(), vl2.getId(), "fn1", 1, ConnectablePosition.Direction.TOP, "fn2", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, true, false, 0, 3);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, true, false, 3, 4);
        Networks.createSwitch(vl, "gd", "gd", SwitchKind.GROUND_DISCONNECTOR, false, false, false, 4, 5);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseGroundDisconnector.svg"), toSVG(g, "/TestCaseGroundDisconnector.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }
}
