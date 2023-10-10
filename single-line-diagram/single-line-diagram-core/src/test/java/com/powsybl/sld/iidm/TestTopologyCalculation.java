/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.util.TopologicallyConnectedNodesSet;
import com.powsybl.sld.util.TopologyCalculation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <PRE>
 * lA                                             lB
 * |   \                                           |
 * bA1  bA2                                        bB
 * |   /                                          /  \
 * dA          d11 --------- b1 -------- d12     dB1  dB2
 * |            |                         |      |     |
 * bbs 1.1 -----x----x-------d1-------x-- | -----x---- | -- bbs 1.2
 * bbs 2.1 ----------x-d21-x-b2-x-d22-x---x------------x--- bbs 2.2
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class TestTopologyCalculation extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs1.1", "bbs1.1", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs1.2", "bbs1.2", 1, 1, 2);
        Networks.createBusBarSection(vl, "bbs2.1", "bbs2.1", 2, 2, 1);
        Networks.createBusBarSection(vl, "bbs2.2", "bbs2.2", 3, 2, 2);

        Networks.createLoad(vl, "lA", "lA", "lA", 0, ConnectablePosition.Direction.TOP, 4, 10, 10);
        Networks.createSwitch(vl, "bA1", "bA1", SwitchKind.BREAKER, false, false, false, 4, 5);
        Networks.createSwitch(vl, "bA2", "bA2", SwitchKind.BREAKER, false, false, false, 4, 5);
        Networks.createSwitch(vl, "dA", "dA", SwitchKind.DISCONNECTOR, false, false, false, 0, 5);

        Networks.createSwitch(vl, "d11", "d11", SwitchKind.DISCONNECTOR, false, false, false, 0, 6);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 6, 7);
        Networks.createSwitch(vl, "d12", "d12", SwitchKind.DISCONNECTOR, false, false, false, 7, 3);

        Networks.createSwitch(vl, "d21", "d21", SwitchKind.DISCONNECTOR, false, false, false, 2, 8);
        Networks.createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 8, 9);
        Networks.createSwitch(vl, "d22", "d22", SwitchKind.DISCONNECTOR, false, false, false, 9, 3);

        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);

        Networks.createLoad(vl, "lB", "lB", "lB", 1, ConnectablePosition.Direction.TOP, 10, 10, 10);
        Networks.createSwitch(vl, "bB", "bB", SwitchKind.BREAKER, false, false, false, 10, 11);
        Networks.createSwitch(vl, "dB1", "dB1", SwitchKind.DISCONNECTOR, false, false, false, 11, 1);
        Networks.createSwitch(vl, "dB2", "dB2", SwitchKind.DISCONNECTOR, false, false, false, 11, 3);
    }

    public void assertTopo(List<TopologicallyConnectedNodesSet> tcnss,
                           int expectedNbTcns,
                           int exploredIndex,
                           int expectedNbNodes,
                           int expectedNbBorderSwitches) {
        assertEquals(expectedNbTcns, tcnss.size());
        TopologicallyConnectedNodesSet tcns = tcnss.get(exploredIndex);
        assertEquals(expectedNbNodes, tcns.getNodes().size());
        assertEquals(expectedNbBorderSwitches, tcns.getBorderNodes().size());
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());
        TopologyCalculation topologyCalculation = new TopologyCalculation();
        List<TopologicallyConnectedNodesSet> tcnss = topologyCalculation.findConnectedNodeSets(g);
        assertTopo(tcnss, 1, 0, 25, 0);

        ((SwitchNode) g.getNode("bA1")).setOpen(true);
        tcnss = topologyCalculation.findConnectedNodeSets(g);
        assertTopo(tcnss, 1, 0, 25, 0);

        ((SwitchNode) g.getNode("bA2")).setOpen(true);
        tcnss = topologyCalculation.findConnectedNodeSets(g);
        assertTopo(tcnss, 2, 0, 24, 2);
        assertTopo(tcnss, 2, 1, 3, 2);

        ((SwitchNode) g.getNode("d1")).setOpen(true);
        tcnss = topologyCalculation.findConnectedNodeSets(g);
        assertTopo(tcnss, 2, 0, 24, 2);
        assertTopo(tcnss, 2, 1, 3, 2);

        ((SwitchNode) g.getNode("b1")).setOpen(true);
        tcnss = topologyCalculation.findConnectedNodeSets(g);
        assertTopo(tcnss, 3, 0, 9, 4);
        assertTopo(tcnss, 3, 1, 17, 2);
        assertTopo(tcnss, 3, 2, 3, 2);

        ((SwitchNode) g.getNode("dB2")).setOpen(true);
        tcnss = topologyCalculation.findConnectedNodeSets(g);
        assertTopo(tcnss, 4, 0, 9, 4);
        assertTopo(tcnss, 4, 1, 7, 2);
        assertTopo(tcnss, 4, 2, 11, 2);
        assertTopo(tcnss, 4, 3, 3, 2);

        tcnss = topologyCalculation.findConnectedNodeSets(g, this::bordersBreakersOnly);
        assertTopo(tcnss, 1, 0, 3, 2);

        tcnss = topologyCalculation.findConnectedNodeSets(g, this::feedersSetsOnly);
        assertTopo(tcnss, 2, 0, 7, 2);
        assertTopo(tcnss, 2, 1, 3, 2);
    }

    private boolean feedersSetsOnly(TopologicallyConnectedNodesSet topologicallyConnectedNodesSet) {
        return topologicallyConnectedNodesSet.getNodes().stream().anyMatch(node -> node.getType() == Node.NodeType.FEEDER);
    }

    private boolean bordersBreakersOnly(TopologicallyConnectedNodesSet topologicallyConnectedNodesSet) {
        return topologicallyConnectedNodesSet.getBorderNodes().stream()
                .filter(SwitchNode.class::isInstance)
                .map(SwitchNode.class::cast)
                .allMatch(switchNode -> switchNode.getKind() == SwitchNode.SwitchKind.BREAKER);
    }
}
