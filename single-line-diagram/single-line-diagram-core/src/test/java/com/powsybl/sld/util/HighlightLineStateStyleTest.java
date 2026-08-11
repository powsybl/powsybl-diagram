/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.util;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.styles.StyleClassConstants;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.iidm.HighlightLineStateStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class HighlightLineStateStyleTest extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() throws IOException {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.newSubstation().setId("substation").setCountry(Country.FR).add();
        // bbs - disconnector - breaker - load
        VoltageLevel vl1 = Networks.createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        Networks.createLoad(vl1, "load", "load", "load", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl1, "disconnector", "disconnector", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl1, "breaker", "breaker", SwitchKind.BREAKER, false, false, false, 1, 2);
    }

    @Test
    void testHighlightLineStateAtFictitiousNode() {
        network.getLoad("load").getTerminal().disconnect();
        StyleProvider styleProvider = new HighlightLineStateStyleProvider(network);
        VoltageLevelGraph graph = new NetworkGraphBuilder(network).buildVoltageLevelGraph("vl1");
        new SmartVoltageLevelLayoutFactory(network).create(graph).run(new LayoutParameters());
        assertThat(graph.getEdges()).hasSize(5);
        assertThat(graph.getEdges())
                .filteredOn(edge -> styleProvider.getEdgeStyles(graph, edge)
                        .contains(StyleClassConstants.FEEDER_DISCONNECTED_CONNECTED))
                .hasSize(2)
                .extracting(edge -> edge.getNodes().stream().map(Node::getId).toList())
                .containsExactlyInAnyOrder(List.of("INTERNAL_vl1_load", "load"), List.of("breaker", "INTERNAL_vl1_load"));
    }
}
