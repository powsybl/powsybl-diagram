/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.library.ComponentTypeName;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class AddNodeGraphTest extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("S1VL2");
    }

    @Test
    public void testNewNode() {

        // graph construction
        Graph graph = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);

        int originNbNodes = graph.getNodes().size();
        int originNbEdges = graph.getEdges().size();

        String originNodeId = "S1VL2_BBS2_LD4_DISCONNECTOR";
        String replacingNodeId = "s1vl2_replacingBreaker";

        // Creates new node
        Node replacingNode = new SwitchNode(replacingNodeId, "replacingNode",
            ComponentTypeName.BREAKER, false, graph, SwitchNode.SwitchKind.BREAKER, false);

        // Replace the origin node with that new node
        Node originNode = graph.getNode(originNodeId);
        List<Node> originAdjacentNodes = originNode.getAdjacentNodes();
        graph.replaceNode(originNode, replacingNode);

        // Checks the replacement correctness
        assertNull(graph.getNode(originNodeId));
        assertNotNull(graph.getNode(replacingNodeId));
        assertEquals(originNbNodes, graph.getNodes().size());
        assertEquals(originNbEdges, graph.getEdges().size());
        assertThat(originAdjacentNodes, is(replacingNode.getAdjacentNodes()));
    }

    @Test
    public void testExistingNode() {
        // graph construction
        Graph graph = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);
        int originNbNodes = graph.getNodes().size();
        int originNbEdges = graph.getEdges().size();

        // Replacing the node
        String originNodeId = "S1VL2_BBS2_LD4_DISCONNECTOR";
        String replacingNodeId = "S1VL2_TWT_BREAKER";
        graph.replaceNode(graph.getNode(originNodeId), graph.getNode(replacingNodeId));

        assertNull(graph.getNode(originNodeId));
        assertNotNull(graph.getNode(replacingNodeId));
        assertEquals(originNbNodes - 1, graph.getNodes().size());
        assertEquals(originNbEdges, graph.getEdges().size());

    }
}
