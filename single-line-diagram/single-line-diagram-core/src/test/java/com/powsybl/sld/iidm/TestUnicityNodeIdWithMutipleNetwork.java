/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.GraphBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.SvgParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestUnicityNodeIdWithMutipleNetwork extends AbstractTestCaseIidm {

    private Network network2;
    private GraphBuilder graphBuilder2;
    private Substation substation2;
    private VoltageLevel vl2;

    @BeforeEach
    public void setUp() {
        layoutParameters.setAdaptCellHeightToContent(false);
        svgParameters.setCssLocation(SvgParameters.CssLocation.INSERTED_IN_SVG);

        // Create first network with a substation and a voltageLevel
        network = Network.create("n1", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        // Create second network with a substation and a voltageLevel
        network2 = Network.create("n2", "test");
        graphBuilder2 = new NetworkGraphBuilder(network2);
        substation2 = Networks.createSubstation(network2, "s", "s", Country.FR);
        vl2 = Networks.createVoltageLevel(substation2, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl2, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl2, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl2, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl2, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
    }

    @Test
    void test() {
        // Generating json for voltage level in first network
        VoltageLevelGraph graph1 = graphBuilder.buildVoltageLevelGraph(vl.getId());

        voltageLevelGraphLayout(graph1);

        String refJson1 = toString("/TestUnicityNodeIdNetWork1.json");
        assertEquals(refJson1, toJson(graph1, "/TestUnicityNodeIdNetWork1.json"));

        // Generating json for voltage level in second network
        VoltageLevelGraph graph2 = graphBuilder2.buildVoltageLevelGraph(vl2.getId());

        voltageLevelGraphLayout(graph2);

        network = network2; // overwrite network with network2 for debug purposes (svg generated for debug in toJson if writeFile=true takes network as reference)
        String refJson2 = toString("/TestUnicityNodeIdNetWork2.json");
        assertEquals(refJson2, toJson(graph2, "/TestUnicityNodeIdNetWork2.json"));

        assertEquals(refJson1, refJson2);
    }
}
