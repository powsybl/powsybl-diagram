/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.library.SldComponentTypeName;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class AddNodeGraphTest extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("S1VL2");
    }

    @Test
    void testNewNode() {

        // graph construction
        VoltageLevelGraph graph = graphBuilder.buildVoltageLevelGraph(vl.getId());

        int originNbNodes = graph.getNodes().size();
        int originNbEdges = graph.getEdges().size();

        String originNodeId = "S1VL2_BBS2_LD4_DISCONNECTOR";
        String replacingNodeId = "s1vl2_replacingBreaker";

        // Creates new node
        Node replacingNode = new SwitchNode(replacingNodeId, "replacingNode",
            SldComponentTypeName.BREAKER, false, SwitchNode.SwitchKind.BREAKER, false);

        // Replace the origin node with that new node
        Node originNode = graph.getNode(originNodeId);
        List<Node> originAdjacentNodes = originNode.getAdjacentNodes();
        PowsyblException e = assertThrows(PowsyblException.class, () -> graph.substituteNode(originNode, replacingNode));
        assertEquals("New node [s1vl2_replacingBreaker] is not in current voltage level graph", e.getMessage());

        // Checks the replacement failed
        assertNotNull(graph.getNode(originNodeId));
        assertNull(graph.getNode(replacingNodeId));
        assertEquals(originNbNodes, graph.getNodes().size());
        assertEquals(originNbEdges, graph.getEdges().size());
    }

    @Test
    void testExistingNode() {
        // graph construction
        VoltageLevelGraph graph = graphBuilder.buildVoltageLevelGraph(vl.getId());
        int originNbNodes = graph.getNodes().size();
        int originNbEdges = graph.getEdges().size();

        // Replacing the node
        String originNodeId = "S1VL2_BBS2_LD4_DISCONNECTOR";
        String replacingNodeId = "S1VL2_TWT_BREAKER";
        Node originNode = graph.getNode(originNodeId);
        Node replacingNode = graph.getNode(replacingNodeId);
        graph.substituteNode(originNode, replacingNode);

        assertNull(graph.getNode(originNodeId));
        assertEquals(originNbNodes - 1, graph.getNodes().size());
        assertEquals(originNbEdges, graph.getEdges().size());
    }

    @Test
    void testSubstitute() {

        // Creates new node non-connected to any equipments (connected to fictitious nodes)
        String replacingNodeId = "s1vl2_replacingBreaker";
        Networks.createSwitch(vl, replacingNodeId, "replacingNode", SwitchKind.BREAKER, false, false, false, 100, 101);

        // graph construction
        VoltageLevelGraph graph = graphBuilder.buildVoltageLevelGraph(vl.getId());
        Node replacingNode = graph.getNode(replacingNodeId);
        assertNotNull(replacingNode);

        // substitute with substitue deprecated method
        String originNodeId = "S1VL2_LD4_BREAKER";
        Node originNode = graph.getNode(originNodeId);
        graph.substituteNode(originNode, replacingNode);
        assertNull(graph.getNode(originNodeId));

        // substitute fictitious node
        String fictitiousNodeId = "INTERNAL_S1VL2_101";
        assertTrue(graph.getNode(fictitiousNodeId) instanceof ConnectivityNode);
        graph.substituteSingularFictitiousByFeederNode();
        assertTrue(graph.getNode(fictitiousNodeId) instanceof FeederNode);

    }
}
