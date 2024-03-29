/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class TestCaseLoadBreakSwitch extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("testCaseLoadBreakSwitch", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs2", "bbs2", 1, 2, 2);
        Networks.createGenerator(vl, "G", "G", "G", 0, ConnectablePosition.Direction.TOP, 2, 50, 100, false, 100, 400);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.BOTTOM, 3, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.LOAD_BREAK_SWITCH, false, false, false, 0, 2);
        Networks.createSwitch(vl, "b", "b", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 1, 3);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 0, 1);

        Networks.createTwoWindingsTransformer(substation, "T11", "T11", 250, 100, 52, 12, 65, 90,
                4, 6, vl.getId(), vl.getId(),
                "T11", null, ConnectablePosition.Direction.TOP,
                "T11", null, ConnectablePosition.Direction.BOTTOM);
        Networks.createSwitch(vl, "b2", "b2", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 0, 4);
        Networks.createSwitch(vl, "b3", "b3", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 1, 5);
        Networks.createSwitch(vl, "b4", "b4", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 5, 6);
        Networks.createSwitch(vl, "b5", "b5", SwitchKind.LOAD_BREAK_SWITCH, false, true, false, 5, 3);

    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseLoadBreakSwitch.svg"), toSVG(g, "/TestCaseLoadBreakSwitch.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }
}
