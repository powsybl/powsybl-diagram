/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.test.Networks;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Luma Zamarreno {@literal <zamarrenolm at aia.es>}
 */
class FixedLayoutTest {

    @Test
    void testCurrentLimits() {
        Network network = Networks.createTwoVoltageLevels();

        Map<String, Point> expected = Map.of(
                "dl1", new Point(0, 0),
                "vl1", new Point(1, 0),
                "vl2", new Point(2, 1));
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout fixedLayout = new FixedLayoutFactory(expected).create();
        fixedLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(expected.keySet(), actual.keySet());
        expected.keySet().forEach(k -> {
            Point pexpected = expected.get(k);
            Point pactual = actual.get(k);
            assertNotNull(pactual);
            assertEquals(pexpected.getX(), pactual.getX(), 0);
            assertEquals(pexpected.getY(), pactual.getY(), 0);
        });
    }

    @Test
    void testMissingVL8InitialPosition() {
        Network network = IeeeCdfNetworkFactory.create9();
        Map<String, Point> initialPositions = new HashMap<>();
        initialPositions.put("VL1", new Point(900, -900));
        initialPositions.put("VL2", new Point(-300, 250));
        initialPositions.put("VL3", new Point(400, 200));
        initialPositions.put("VL5", new Point(500, 500));
        initialPositions.put("VL6", new Point(700, 700));

        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new FixedLayoutFactory(initialPositions, new ForceLayoutFactory()).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(initialPositions.get("VL1").getX(), actual.get("VL1").getX());
        assertEquals(initialPositions.get("VL1").getY(), actual.get("VL1").getY());
        assertEquals(initialPositions.get("VL2").getX(), actual.get("VL2").getX());
        assertEquals(initialPositions.get("VL2").getY(), actual.get("VL2").getY());
        assertEquals(initialPositions.get("VL3").getX(), actual.get("VL3").getX());
        assertEquals(initialPositions.get("VL3").getY(), actual.get("VL3").getY());
        assertEquals(initialPositions.get("VL5").getX(), actual.get("VL5").getX());
        assertEquals(initialPositions.get("VL5").getY(), actual.get("VL5").getY());
        assertEquals(initialPositions.get("VL6").getX(), actual.get("VL6").getX());
        assertEquals(initialPositions.get("VL6").getY(), actual.get("VL6").getY());
    }

    @Test
    void testBasicFixedLayoutFallback() {
        Network network = IeeeCdfNetworkFactory.create9();
        Map<String, Point> initialPositions = new HashMap<>();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new FixedLayoutFactory(initialPositions).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(0, actual.get("VL1").getX());
        assertEquals(0, actual.get("VL1").getY());
        assertEquals(0, actual.get("VL2").getX());
        assertEquals(0, actual.get("VL2").getY());
        assertEquals(0, actual.get("VL3").getX());
        assertEquals(0, actual.get("VL3").getY());
        assertEquals(0, actual.get("VL5").getX());
        assertEquals(0, actual.get("VL5").getY());
        assertEquals(0, actual.get("VL6").getX());
        assertEquals(0, actual.get("VL6").getY());
        assertEquals(0, actual.get("VL8").getX());
        assertEquals(0, actual.get("VL8").getY());
    }

    @Test
    void testMetadataUtils() throws URISyntaxException, IOException {
        Network network = Networks.createTwoVoltageLevels();
        Path metadataFile = Paths.get(getClass().getResource("/two-voltage-levels_metadata.json").toURI());

        Layout layout = new FixedLayoutFactory(new HashMap<>()).create();
        testEmptyLayout(layout, network);

        layout = LayoutFactoryUtils.create(metadataFile).create();
        testMetadataLayout(layout, network);
        layout = LayoutFactoryUtils.create(metadataFile, BasicFixedLayout::new).create();
        testMetadataLayout(layout, network);

        try (InputStream metadataIS = Files.newInputStream(metadataFile)) {
            layout = LayoutFactoryUtils.create(metadataIS).create();
            testMetadataLayout(layout, network);
        }
        try (InputStream metadataIS = Files.newInputStream(metadataFile)) {
            layout = LayoutFactoryUtils.create(metadataIS, BasicFixedLayout::new).create();
            testMetadataLayout(layout, network);
        }

        try (Reader metadataReader = Files.newBufferedReader(metadataFile)) {
            layout = LayoutFactoryUtils.create(metadataReader).create();
            testMetadataLayout(layout, network);
        }
        try (Reader metadataReader = Files.newBufferedReader(metadataFile)) {
            layout = LayoutFactoryUtils.create(metadataReader, BasicFixedLayout::new).create();
            testMetadataLayout(layout, network);
        }
    }

    void testEmptyLayout(Layout layout, Network network) {
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        layout.run(graph, new LayoutParameters());
        Map<String, Point> nodePositions = graph.getNodePositions();
        checkNodePosition(nodePositions.get("dl1"), 0, 0);
        checkNodePosition(nodePositions.get("vl1"), 0, 0);
        checkNodePosition(nodePositions.get("vl2"), 0, 0);
        Map<String, TextPosition> textNodesPositions = getTextNodesPositions(graph);
        checkNodeShift(nodePositions.get("vl1"), textNodesPositions.get("vl1").topLeftPosition(), 100, -40);
        checkNodeShift(nodePositions.get("vl1"), textNodesPositions.get("vl1").edgeConnection(), 100, -15);
        checkNodeShift(nodePositions.get("vl2"), textNodesPositions.get("vl2").topLeftPosition(), 100, -40);
        checkNodeShift(nodePositions.get("vl2"), textNodesPositions.get("vl2").edgeConnection(), 100, -15);
    }

    void testMetadataLayout(Layout layout, Network network) {
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        layout.run(graph, new LayoutParameters());
        Map<String, Point> nodePositions = graph.getNodePositions();
        checkNodePosition(nodePositions.get("dl1"), -49.12, 317.14);
        checkNodePosition(nodePositions.get("vl1"), -56.06, -318.7);
        checkNodePosition(nodePositions.get("vl2"), -230.42, 1.18);
        Map<String, TextPosition> textNodesPositions = getTextNodesPositions(graph);
        checkNodeShift(nodePositions.get("vl1"), textNodesPositions.get("vl1").topLeftPosition(), 80, -30);
        checkNodeShift(nodePositions.get("vl1"), textNodesPositions.get("vl1").edgeConnection(), 80, -5);
        checkNodeShift(nodePositions.get("vl2"), textNodesPositions.get("vl2").topLeftPosition(), 80, -30);
        checkNodeShift(nodePositions.get("vl2"), textNodesPositions.get("vl2").edgeConnection(), 80, -5);
    }

    Map<String, TextPosition> getTextNodesPositions(Graph graph) {
        Map<String, TextPosition> textNodesPositions = new HashMap<>();
        graph.getTextEdgesMap()
             .values()
             .forEach(nodePair -> textNodesPositions.put(nodePair.getFirst().getEquipmentId(),
                                                         new TextPosition(nodePair.getSecond().getPosition(),
                                                                          nodePair.getSecond().getEdgeConnection())));
        return textNodesPositions;
    }

    void checkNodePosition(Point point, double x, double y) {
        assertEquals(x, point.getX());
        assertEquals(y, point.getY());
    }

    void checkNodeShift(Point point, Point shiftedPoint, double shiftX, double shiftY) {
        Point expectedPoint = point.shift(shiftX, shiftY);
        assertEquals(expectedPoint.getX(), shiftedPoint.getX());
        assertEquals(expectedPoint.getY(), shiftedPoint.getY());
    }
}
