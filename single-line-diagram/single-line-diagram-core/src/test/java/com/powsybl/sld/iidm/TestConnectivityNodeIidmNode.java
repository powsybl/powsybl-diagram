/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The connectivity nodes inserted by the layout carry no node of the node/breaker view: they are given
 * back the one their neighbours have in common.
 *
 * @author Leclerc Clement {@literal <clement.leclerc at rte-france.com>}
 */
class TestConnectivityNodeIidmNode extends AbstractTestCaseIidm {

    private static final int LOAD_IIDM_NODE = 2;

    @BeforeEach
    public void setUp() {
        network = Network.create("testConnectivityNodeIidmNode", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = Networks.createSubstation(network, "subst", "subst", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);

        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl, "load", "load", "load", 0, ConnectablePosition.Direction.TOP, LOAD_IIDM_NODE, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, true, false, true, 1, LOAD_IIDM_NODE);
    }

    @Test
    void testConnectivityNodesCarryAnIidmNode() {
        VoltageLevelGraph graph = graphBuilder.buildVoltageLevelGraph(vl.getId());
        new PositionVoltageLevelLayoutFactory().create(graph).run(layoutParameters);

        assertTrue(graph.getConnectivityNodeStream().findAny().isPresent());
        assertTrue(graph.getConnectivityNodeStream().allMatch(node -> node.getIidmNode().isPresent()));

        assertEquals(List.of(LOAD_IIDM_NODE), graph.getConnectivityNodeStream()
                .filter(node -> isAdjacentTo(node, "load"))
                .map(node -> node.getIidmNode().orElseThrow())
                .toList());
    }

    private static boolean isAdjacentTo(ConnectivityNode node, String nodeId) {
        return node.getAdjacentNodes().stream().map(Node::getId).anyMatch(nodeId::equals);
    }
}
