/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TopologicalStyleTest extends AbstractTestCase {

    VoltageLevel vl1;
    VoltageLevel vl2;
    VoltageLevel vl3;
    private FileSystem fileSystem;
    private Path tmpDir;

    @Before
    public void setUp() throws IOException {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.newSubstation().setId("s").setCountry(Country.FR).add();

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
        createSwitch(vl2, "b2WT_2", "b2WT_2", SwitchKind.BREAKER, true, false, true, 1, 2);

        // 3WT between the 3 voltage levels
        createThreeWindingsTransformer(substation, "3WT", "3WT", vl1.getId(), vl2.getId(), vl3.getId(),
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                5, 3, 1,
                "3WT_1", 2, ConnectablePosition.Direction.TOP,
                "3WT_2", 1, ConnectablePosition.Direction.TOP,
                "3WT_3", 0, ConnectablePosition.Direction.TOP);
        createSwitch(vl1, "d3WT_1", "d3WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, false, true, 5, 6);
        createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, false, true, 3, 4);
        createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, false, true, 1, 2);

        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
    }

    @Test
    public void test() throws IOException {
        // construction des graphes
        Graph graph1 = graphBuilder.buildVoltageLevelGraph(vl1.getId(), false, true);
        Graph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId(), false, true);
        Graph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId(), false, true);

        Path config = tmpDir.resolve("base-voltages.yml");
        Files.copy(getClass().getResourceAsStream("/base-voltages.yml"), config);

        TopologicalStyleProvider styleProvider = new TopologicalStyleProvider(config, network);

        Node node1 = graph1.getNode("bbs1");
        Optional<String> nodeStyle1 = styleProvider.getNodeStyle(node1, false, false);
        assertTrue(nodeStyle1.isPresent());
        assertEquals(" #idbbs1 {stroke:#FF0000;}", nodeStyle1.get());

        Node node2 = graph2.getNode("bbs2");
        Optional<String> nodeStyle2 = styleProvider.getNodeStyle(node2, false, false);
        assertTrue(nodeStyle2.isPresent());
        assertEquals(" #idbbs2 {stroke:#218B21;}", nodeStyle2.get());

        Node node3 = graph3.getNode("bbs3");
        Optional<String> nodeStyle3 = styleProvider.getNodeStyle(node3, false, false);
        assertTrue(nodeStyle3.isPresent());
        assertEquals(" #idbbs3 {stroke:#A020EF;}", nodeStyle3.get());

        Edge edge = graph1.getEdges().get(0);
        String idWireStyle = styleProvider.getIdWireStyle(edge);
        assertEquals("wire_vl1", idWireStyle);
        edge = graph1.getEdges().get(12);
        idWireStyle = styleProvider.getIdWireStyle(edge);
        assertEquals("wire_vl1", idWireStyle);

        Optional<String> wireStyle = styleProvider.getWireStyle(edge, vl1.getId(), 12);
        assertTrue(wireStyle.isPresent());
        assertEquals(" #idvl1_95_Wire12 {stroke:#FF0000;stroke-width:1;fill-opacity:0;}", wireStyle.get());

        Node fict3WTNode = graph1.getNode("FICT_vl1_3WT_fictif");
        Map<String, String> node3WTStyle = styleProvider.getNodeSVGStyle(fict3WTNode, new ComponentSize(14, 12), "WINDING1", true);
        assertTrue(node3WTStyle.isEmpty());

        Node f2WTNode = graph1.getNode("2WT_ONE");
        Map<String, String> node2WTStyle = styleProvider.getNodeSVGStyle(f2WTNode, new ComponentSize(13, 8), "WINDING1", true);
        assertTrue(node2WTStyle.isEmpty());

        Optional<String> color = styleProvider.getColor(400, null);
        assertFalse(color.isPresent());

        Map<String, String> attributesArrow = styleProvider.getAttributesArrow(1);
        assertEquals(3, attributesArrow.size());
        assertTrue(attributesArrow.containsKey("fill"));
        assertEquals("black", attributesArrow.get("fill"));
        assertTrue(attributesArrow.containsKey("stroke"));
        assertEquals("black", attributesArrow.get("stroke"));
        assertTrue(attributesArrow.containsKey("fill-opacity"));
        assertEquals("1", attributesArrow.get("fill-opacity"));

        attributesArrow = styleProvider.getAttributesArrow(2);
        assertEquals(3, attributesArrow.size());
        assertTrue(attributesArrow.containsKey("fill"));
        assertEquals("blue", attributesArrow.get("fill"));
        assertTrue(attributesArrow.containsKey("stroke"));
        assertEquals("blue", attributesArrow.get("stroke"));
        assertTrue(attributesArrow.containsKey("fill-opacity"));
        assertEquals("1", attributesArrow.get("fill-opacity"));
    }
}
