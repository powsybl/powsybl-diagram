/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.google.common.collect.ImmutableMap;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.color.BaseVoltageColor;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.InitialValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageStyleTest extends AbstractTestCaseIidm {

    VoltageLevel vl1;
    VoltageLevel vl2;
    VoltageLevel vl3;

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);

        // first voltage level
        vl1 = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 400, 10);
        createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        createLoad(vl1, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl1, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl1, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        // second voltage level
        vl2 = createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225, 10);
        createBusBarSection(vl2, "bbs2", "bbs2", 0, 1, 1);

        // third voltage level
        vl3 = createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 63, 10);
        createBusBarSection(vl3, "bbs3", "bbs3", 0, 1, 1);

        // 2WT between first and second voltage level
        createTwoWindingsTransformer(substation, "2WT", "2WT", 1, 1, 1, 1, 1, 1,
                3, 1, vl1.getId(), vl2.getId(),
                "2WT_1", 1, ConnectablePosition.Direction.TOP,
                "2WT_2", 0, ConnectablePosition.Direction.TOP);
        createSwitch(vl1, "d2WT_1", "d2WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        createSwitch(vl1, "b2WT_1", "b2WT_1", SwitchKind.BREAKER, true, false, true, 3, 4);
        createSwitch(vl2, "d2WT_2", "d2WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl2, "b2WT_2", "b2WT_2", SwitchKind.BREAKER, true, true, true, 1, 2);

        // 3WT between the 3 voltage levels
        createThreeWindingsTransformer(substation, "3WT", "3WT", vl1.getId(), vl2.getId(), vl3.getId(),
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                5, 3, 1,
                "3WT_1", 2, ConnectablePosition.Direction.TOP,
                "3WT_2", 1, ConnectablePosition.Direction.TOP,
                "3WT_3", 0, ConnectablePosition.Direction.TOP);
        createSwitch(vl1, "d3WT_1", "d3WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, true, true, 5, 6);
        createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, false, true, 3, 4);
        createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, true, true, 1, 2);
    }

    @Test
    public void test() {
        // construction des graphes
        Graph graph1 = graphBuilder.buildVoltageLevelGraph(vl1.getId(), false, true);
        Graph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId(), false, true);
        Graph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId(), false, true);

        BaseVoltageColor baseVoltageColor = BaseVoltageColor.fromInputStream(getClass().getResourceAsStream("/base-voltages.yml"));
        NominalVoltageDiagramStyleProvider styleProvider = new NominalVoltageDiagramStyleProvider(baseVoltageColor, network);

        Node node1 = graph1.getNode("bbs1");
        Optional<String> nodeStyle1 = styleProvider.getCssNodeStyleAttributes(node1, false);
        assertFalse(nodeStyle1.isPresent());

        Node node2 = graph2.getNode("bbs2");
        Optional<String> nodeStyle2 = styleProvider.getCssNodeStyleAttributes(node2, false);
        assertFalse(nodeStyle2.isPresent());

        Node node3 = graph3.getNode("bbs3");
        Optional<String> nodeStyle3 = styleProvider.getCssNodeStyleAttributes(node3, false);
        assertFalse(nodeStyle3.isPresent());

        Edge edge = graph1.getEdges().get(12);
        Map<String, String> wireStyle = styleProvider.getSvgWireStyleAttributes(edge, false);
        assertEquals(ImmutableMap.of("stroke", "#ff0000", "stroke-width", "1"), wireStyle);

        Node fict3WTNode = graph1.getNode("FICT_vl1_3WT_fictif");
        Map<String, String> node3WTStyle = styleProvider.getSvgNodeStyleAttributes(fict3WTNode, new ComponentSize(14, 12), "WINDING1", true);
        assertFalse(node3WTStyle.isEmpty());
        assertTrue(node3WTStyle.containsKey("stroke"));
        assertEquals("#a020f0", node3WTStyle.get("stroke"));

        Node f2WTNode = graph1.getNode("2WT_ONE");
        Map<String, String> node2WTStyle = styleProvider.getSvgNodeStyleAttributes(f2WTNode, new ComponentSize(13, 8), "WINDING1", true);
        assertFalse(node2WTStyle.isEmpty());
        assertTrue(node2WTStyle.containsKey("stroke"));
        assertEquals("#ff0000", node2WTStyle.get("stroke"));

        Map<String, String> attributesArrow = styleProvider.getSvgArrowStyleAttributes(1);
        assertEquals(3, attributesArrow.size());
        assertTrue(attributesArrow.containsKey("fill"));
        assertEquals("black", attributesArrow.get("fill"));
        assertTrue(attributesArrow.containsKey("stroke"));
        assertEquals("black", attributesArrow.get("stroke"));
        assertTrue(attributesArrow.containsKey("fill-opacity"));
        assertEquals("1", attributesArrow.get("fill-opacity"));

        attributesArrow = styleProvider.getSvgArrowStyleAttributes(2);
        assertEquals(3, attributesArrow.size());
        assertTrue(attributesArrow.containsKey("fill"));
        assertEquals("blue", attributesArrow.get("fill"));
        assertTrue(attributesArrow.containsKey("stroke"));
        assertEquals("blue", attributesArrow.get("stroke"));
        assertTrue(attributesArrow.containsKey("fill-opacity"));
        assertEquals("1", attributesArrow.get("fill-opacity"));

        // Layout parameters :
        //
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(80)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(true)
                .setShowInternalNodes(false)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50)
                .setDrawStraightWires(false)
                .setHorizontalSnakeLinePadding(30)
                .setVerticalSnakeLinePadding(30)
                .setHighlightLineState(true);

        DiagramLabelProvider noFeederValueProvider = new DefaultDiagramLabelProvider(
                Network.create("empty", ""), componentLibrary, layoutParameters) {
            @Override
            public InitialValue getInitialValue(Node node) {
                InitialValue initialValue;
                if (node.getType() == Node.NodeType.BUS) {
                    initialValue = new InitialValue(null, null, null, null, null, null);
                } else {
                    initialValue = new InitialValue(Direction.UP, Direction.DOWN, null, null, null, null);
                }
                return initialValue;
            }
        };

        assertEquals(toString("/vl1_nominal_voltage_style.svg"), toSVG(graph1, "/vl1_nominal_voltage_style.svg", layoutParameters, noFeederValueProvider, styleProvider));
        assertEquals(toString("/vl2_nominal_voltage_style.svg"), toSVG(graph2, "/vl2_nominal_voltage_style.svg", layoutParameters, noFeederValueProvider, styleProvider));
        assertEquals(toString("/vl3_nominal_voltage_style.svg"), toSVG(graph3, "/vl3_nominal_voltage_style.svg", layoutParameters, noFeederValueProvider, styleProvider));
    }
}
