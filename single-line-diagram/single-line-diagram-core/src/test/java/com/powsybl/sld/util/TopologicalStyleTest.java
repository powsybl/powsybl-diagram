/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.styles.BusHighlightStyleProviderFactory;
import com.powsybl.sld.svg.styles.StyleClassConstants;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TopologicalStyleTest extends AbstractTestCaseIidm {

    VoltageLevel vl1;
    VoltageLevel vl2;
    VoltageLevel vl3;

    @BeforeEach
    public void setUp() throws IOException {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.newSubstation().setId("s").setCountry(Country.FR).add();

        // first voltage level
        vl1 = Networks.createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        Networks.createLoad(vl1, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl1, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl1, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        // second voltage level
        vl2 = Networks.createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225);
        Networks.createBusBarSection(vl2, "bbs2", "bbs2", 0, 1, 1);

        // third voltage level
        vl3 = Networks.createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 63);
        Networks.createBusBarSection(vl3, "bbs3", "bbs3", 0, 1, 1);

        // 2WT between first and second voltage level
        Networks.createTwoWindingsTransformer(substation, "2WT", "2WT", 1, 1, 1, 1, 1, 1,
                3, 1, vl1.getId(), vl2.getId(),
                "2WT_1", 1, ConnectablePosition.Direction.TOP,
                "2WT_2", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl1, "d2WT_1", "d2WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        Networks.createSwitch(vl1, "b2WT_1", "b2WT_1", SwitchKind.BREAKER, true, false, true, 3, 4);
        Networks.createSwitch(vl2, "d2WT_2", "d2WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        Networks.createSwitch(vl2, "b2WT_2", "b2WT_2", SwitchKind.BREAKER, true, true, true, 1, 2);

        // 3WT between the 3 voltage levels
        Networks.createThreeWindingsTransformer(substation, "3WT", "3WT", vl1.getId(), vl2.getId(), vl3.getId(),
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                5, 3, 1,
                "3WT_1", 2, ConnectablePosition.Direction.TOP,
                "3WT_2", 1, ConnectablePosition.Direction.TOP,
                "3WT_3", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl1, "d3WT_1", "d3WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        Networks.createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, false, true, 5, 6);
        Networks.createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        Networks.createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, true, true, 3, 4);
        Networks.createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        Networks.createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, false, true, 1, 2);
    }

    @Test
    void test() throws IOException {
        // building graphs
        VoltageLevelGraph graph1 = graphBuilder.buildVoltageLevelGraph(vl1.getId());
        VoltageLevelGraph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId());
        VoltageLevelGraph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId());

        TopologicalStyleProvider styleProvider = new TopologicalStyleProvider(network);

        Node node1 = graph1.getNode("bbs1");
        List<String> nodeStyle1 = styleProvider.getNodeStyles(graph1, node1, componentLibrary, true);
        assertEquals(3, nodeStyle1.size());
        assertTrue(nodeStyle1.contains("sld-busbar-section"));
        assertTrue(nodeStyle1.contains("sld-vl300to500"));
        assertTrue(nodeStyle1.contains("sld-bus-0"));

        Node node2 = graph2.getNode("bbs2");
        List<String> nodeStyle2 = styleProvider.getNodeStyles(graph2, node2, componentLibrary, true);
        assertEquals(3, nodeStyle2.size());
        assertTrue(nodeStyle2.contains("sld-busbar-section"));
        assertTrue(nodeStyle2.contains(StyleClassConstants.DISCONNECTED_STYLE_CLASS));

        Node node3 = graph3.getNode("bbs3");
        List<String> nodeStyle3 = styleProvider.getNodeStyles(graph3, node3, componentLibrary, true);
        assertEquals(3, nodeStyle3.size());
        assertTrue(nodeStyle3.contains("sld-busbar-section"));
        assertTrue(nodeStyle3.contains("sld-vl50to70"));
        assertTrue(nodeStyle3.contains("sld-bus-0"));

        Edge edge = graph1.getEdges().get(12);

        List<String> wireStyles = styleProvider.getEdgeStyles(graph1, edge);
        assertEquals(3, wireStyles.size());
        assertTrue(wireStyles.contains(StyleClassConstants.WIRE_STYLE_CLASS));
        assertTrue(wireStyles.contains("sld-vl300to500"));
        assertTrue(wireStyles.contains("sld-bus-0"));

        Node fict3WTNode = graph1.getNode("3WT");
        List<String> node3WTStyle = styleProvider.getNodeStyles(graph1, fict3WTNode, componentLibrary, true);
        assertEquals(2, node3WTStyle.size());
        assertTrue(node3WTStyle.contains("sld-three-wt"));
        assertTrue(node3WTStyle.contains("sld-fictitious"));

        Node f2WTNode = graph1.getNode("2WT_ONE");
        List<String> node2WTStyle = styleProvider.getNodeStyles(graph1, f2WTNode, componentLibrary, true);
        assertEquals(1, node2WTStyle.size());
        assertTrue(node2WTStyle.contains("sld-two-wt"));

        network.getSwitch("b3WT_3").setOpen(true);
        styleProvider.reset();

        nodeStyle3 = styleProvider.getNodeStyles(graph3, node3, componentLibrary, true);
        assertEquals(3, nodeStyle3.size());
        assertTrue(nodeStyle3.contains("sld-busbar-section"));
        assertTrue(nodeStyle3.contains("sld-vl50to70"));
        assertTrue(nodeStyle3.contains(StyleClassConstants.DISCONNECTED_STYLE_CLASS));
    }

    @Test
    void testSubstation() {
        SubstationGraph graph = graphBuilder.buildSubstationGraph(substation.getId());
        substationGraphLayout(graph);

        assertEquals(toString("/topological_style_substation.svg"), toSVG(graph, "/topological_style_substation.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testBusHighlight() {
        SubstationGraph graph = graphBuilder.buildSubstationGraph(substation.getId());
        substationGraphLayout(graph);
        BusHighlightStyleProviderFactory styleFactory = new BusHighlightStyleProviderFactory();
        StyleProvider styleProvider = styleFactory.create(network, svgParameters);
        assertTrue(styleProvider.getCssFilenames().contains("busHighlight.css"));
        assertEquals(toString("/bus_highlight_style_substation.svg"), toSVG(graph, "/bus_highlight_style_substation.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), styleProvider));

        styleProvider = new TopologicalStyleProvider(network, false);
        assertFalse(styleProvider.getCssFilenames().contains("busHighlight.css"));
    }

    @Test
    void testBusHighlightSubstationUnifiedVoltageLevelColors() {
        svgParameters.setUnifyVoltageLevelColors(true);

        // build graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run layout
        new VerticalSubstationLayoutFactory().create(g, new SmartVoltageLevelLayoutFactory(network)).run(layoutParameters);

        // write SVG and compare to reference
        assertEquals(toString("/TestBusHighlightSubstationUnifiedColors.svg"), toSVG(g, "/TestBusHighlightSubstationUnifiedColors.svg", componentLibrary, layoutParameters, svgParameters, new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters), new TopologicalStyleProvider(network, svgParameters, true)));
    }

}
