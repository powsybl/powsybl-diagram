/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.ParamBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.NominalVoltageStyleProvider;
import com.powsybl.sld.svg.styles.StyleClassConstants;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.StyleProvidersList;
import com.powsybl.sld.svg.styles.iidm.HighlightLineStateStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class NominalVoltageStyleTest extends AbstractTestCaseIidm {

    VoltageLevel vl1;
    VoltageLevel vl2;
    VoltageLevel vl3;
    private StyleProvider styleProvider;

    @BeforeEach
    public void setUp() {
        layoutParameters.setCellWidth(80);

        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);

        styleProvider = new StyleProvidersList(
                new NominalVoltageStyleProvider(),
                new HighlightLineStateStyleProvider(network));

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
        Networks.createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, true, true, 5, 6);
        Networks.createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        Networks.createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, false, true, 3, 4);
        Networks.createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        Networks.createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, true, true, 1, 2);
    }

    private final ParamBuilder.LabelProviderFactory labelNoFeederInfoProviderFactory = new DefaultLabelProviderFactory() {
        @Override
        public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
            return new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

                @Override
                public List<FeederInfo> getFeederInfos(FeederNode node) {
                    return Collections.emptyList();
                }
            };
        }
    };

    @Test
    void testAttributes() {
        // building graphs
        VoltageLevelGraph graph1 = graphBuilder.buildVoltageLevelGraph(vl1.getId());
        VoltageLevelGraph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId());
        VoltageLevelGraph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId());

        Node node1 = graph1.getNode("bbs1");
        List<String> nodeStyle1 = styleProvider.getNodeStyles(graph1, node1, componentLibrary, false);
        assertEquals(2, nodeStyle1.size());
        assertTrue(nodeStyle1.contains("sld-busbar-section"));
        assertTrue(nodeStyle1.contains("sld-vl300to500"));

        Node node2 = graph2.getNode("bbs2");
        List<String> nodeStyle2 = styleProvider.getNodeStyles(graph2, node2, componentLibrary, false);
        assertEquals(2, nodeStyle2.size());
        assertTrue(nodeStyle2.contains("sld-busbar-section"));
        assertTrue(nodeStyle2.contains("sld-vl180to300"));

        Node node3 = graph3.getNode("bbs3");
        List<String> nodeStyle3 = styleProvider.getNodeStyles(graph3, node3, componentLibrary, false);
        assertEquals(2, nodeStyle3.size());
        assertTrue(nodeStyle3.contains("sld-busbar-section"));
        assertTrue(nodeStyle3.contains("sld-vl50to70"));

        Edge edge = graph1.getEdges().get(12);
        List<String> wireStyles = styleProvider.getEdgeStyles(graph1, edge);
        assertEquals(2, wireStyles.size());
        assertTrue(wireStyles.contains(StyleClassConstants.WIRE_STYLE_CLASS));
        assertTrue(wireStyles.contains("sld-vl300to500"));

        Node fict3WTNode = graph1.getNode("3WT");
        List<String> node3WTStyle = styleProvider.getNodeStyles(graph1, fict3WTNode, componentLibrary, false);
        assertEquals(2, node3WTStyle.size());
        assertTrue(node3WTStyle.contains("sld-three-wt"));
        assertTrue(node3WTStyle.contains("sld-fictitious"));

        Node f2WTNode = graph1.getNode("2WT_ONE");
        List<String> node2WTStyle = styleProvider.getNodeStyles(graph1, f2WTNode, componentLibrary, false);
        assertEquals(1, node2WTStyle.size());
        assertTrue(node2WTStyle.contains("sld-two-wt"));

    }

    @Test
    void testSubstation() {
        SubstationGraph graph = graphBuilder.buildSubstationGraph(substation.getId());
        substationGraphLayout(graph);

        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        LabelProvider labelProvider = labelNoFeederInfoProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters);
        assertEquals(toString("/nominal_voltage_style_substation.svg"), toSVG(graph, "/nominal_voltage_style_substation.svg", defaultSVGWriter, labelProvider, styleProvider, svgParameters.getPrefixId()));
    }

    @Test
    void testVl2() {
        VoltageLevelGraph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId());
        voltageLevelGraphLayout(graph2);

        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        LabelProvider labelProvider = labelNoFeederInfoProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters);
        assertEquals(toString("/nominal_voltage_style_vl2.svg"), toSVG(graph2, "/nominal_voltage_style_vl2.svg", defaultSVGWriter, labelProvider, styleProvider, svgParameters.getPrefixId()));
    }

    @Test
    void testVl3() {
        VoltageLevelGraph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId());
        voltageLevelGraphLayout(graph3);

        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        LabelProvider labelProvider = labelNoFeederInfoProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters);
        assertEquals(toString("/nominal_voltage_style_vl3.svg"), toSVG(graph3, "/nominal_voltage_style_vl3.svg", defaultSVGWriter, labelProvider, styleProvider, svgParameters.getPrefixId()));
    }

}
