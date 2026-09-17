/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.Node;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class WireConnectionTest {

    private Node createConnectivityNode(double x, double y) {
        Node node = new ConnectivityNode("id", "type");
        node.setCoordinates(x, y);
        return node;
    }

    @ParameterizedTest
    @MethodSource("calculatePolylinePointsProvider")
    void testCalculatePolylinePoints(AnchorOrientation orientationA, AnchorOrientation orientationB, List<Double> expectedPoints) {
        AnchorPoint apA = new AnchorPoint(0, 0, orientationA);
        AnchorPoint apB = new AnchorPoint(0, 0, orientationB);

        Node nodeA = createConnectivityNode(10, 10);
        Node nodeB = createConnectivityNode(50, 50);

        WireConnection wc = new WireConnection(apA, apB);
        List<Point> pol = wc.calculatePolylinePoints(nodeA, nodeB, false, new Point(0, 0));

        assertEquals(expectedPoints, Point.pointsToDoubles(pol));
    }

    private static Stream<Arguments> calculatePolylinePointsProvider() {
        return Stream.of(
            Arguments.of(AnchorOrientation.VERTICAL, AnchorOrientation.VERTICAL, List.of(10.0, 10.0, 50.0, 30.0, 10.0, 30.0, 50.0, 50.0)),
            Arguments.of(AnchorOrientation.VERTICAL, AnchorOrientation.HORIZONTAL, List.of(10.0, 10.0, 10.0, 50.0, 50.0, 50.0)),
            Arguments.of(AnchorOrientation.HORIZONTAL, AnchorOrientation.HORIZONTAL, List.of(10.0, 10.0, 30.0, 50.0, 30.0, 10.0, 50.0, 50.0)),
            Arguments.of(AnchorOrientation.HORIZONTAL, AnchorOrientation.VERTICAL, List.of(10.0, 10.0, 50.0, 10.0, 50.0, 50.0)),
            Arguments.of(AnchorOrientation.NONE, AnchorOrientation.HORIZONTAL, List.of(10.0, 10.0, 10.0, 50.0, 50.0, 50.0)),
            Arguments.of(AnchorOrientation.NONE, AnchorOrientation.VERTICAL, List.of(10.0, 10.0, 50.0, 10.0, 50.0, 50.0))
        );
    }

    @Test
    void testSearchBestAnchorPointsWithHorizontalBusNode() {
        VoltageLevelInfos infos = new VoltageLevelInfos("vl", "VL", 225);
        VoltageLevelGraph graph = new VoltageLevelGraph(infos, null) {
            @Override
            public Direction getDirection(Node node) {
                return node.getDirection();
            }
        };
        SldComponentLibrary componentLibrary = new ConvergenceComponentLibrary();

        BusNode busNode = new BusNode("bus", "Bus", false);
        busNode.setOrientation(Orientation.RIGHT); // Horizontal
        busNode.setCoordinates(10, 10);
        busNode.setPxWidth(100);

        Node otherNode = new ConnectivityNode("other", "Type");

        // Case 1: Direction.MIDDLE -> returns two horizontal anchor points (ends of bus)
        otherNode.setDirection(Direction.MIDDLE);
        otherNode.setCoordinates(200, 10); // Distance to (10, 10) is 190, to (110, 10) is 90
        WireConnection wc = WireConnection.searchBestAnchorPoints(componentLibrary, graph, busNode, otherNode);
        assertEquals(AnchorOrientation.HORIZONTAL, wc.getAnchorPoint1().getOrientation());
        assertEquals(100, wc.getAnchorPoint1().getX());

        // Case 2: Direction.UNDEFINED but satisfies undefinedMiddleDirection (Y match, X outside bus range)
        otherNode.setDirection(Direction.UNDEFINED);
        otherNode.setCoordinates(-50, 10); // Distance to (10, 10) is 60, to (110, 10) is 160
        wc = WireConnection.searchBestAnchorPoints(componentLibrary, graph, busNode, otherNode);
        assertEquals(AnchorOrientation.HORIZONTAL, wc.getAnchorPoint1().getOrientation());
        assertEquals(0, wc.getAnchorPoint1().getX());

        // Case 3: Other direction (e.g. TOP) -> returns vertical anchor point aligned with other node's X
        otherNode.setDirection(Direction.TOP);
        otherNode.setCoordinates(40, 50);
        wc = WireConnection.searchBestAnchorPoints(componentLibrary, graph, busNode, otherNode);
        assertEquals(AnchorOrientation.VERTICAL, wc.getAnchorPoint1().getOrientation());
        assertEquals(30, wc.getAnchorPoint1().getX()); // otherNode.getX() - busNode.getX() = 40 - 10 = 30
    }

    @Test
    void testSearchBestAnchorPointsWithVerticalBusNode() {
        VoltageLevelInfos infos = new VoltageLevelInfos("vl", "VL", 225);
        VoltageLevelGraph graph = new VoltageLevelGraph(infos, null) {
            @Override
            public Direction getDirection(Node node) {
                return node.getDirection();
            }
        };
        SldComponentLibrary componentLibrary = new ConvergenceComponentLibrary();

        BusNode busNode = new BusNode("bus", "Bus", false);
        busNode.setOrientation(Orientation.DOWN); // Vertical
        busNode.setCoordinates(10, 10);
        busNode.setPxWidth(100);

        Node otherNode = new ConnectivityNode("other", "Type");

        // Case 1: Direction.MIDDLE -> returns two vertical anchor points (ends of bus)
        otherNode.setDirection(Direction.MIDDLE);
        otherNode.setCoordinates(10, 200); // Distance to (10, 10) is 190, to (10, 110) is 90
        WireConnection wc = WireConnection.searchBestAnchorPoints(componentLibrary, graph, busNode, otherNode);
        assertEquals(AnchorOrientation.VERTICAL, wc.getAnchorPoint1().getOrientation());
        assertEquals(100, wc.getAnchorPoint1().getY());

        // Case 2a: Direction.UNDEFINED but satisfies undefinedMiddleDirection (X match, Y outside bus range)
        otherNode.setDirection(Direction.UNDEFINED);
        otherNode.setCoordinates(10, -50); // Distance to (10, 10) is 60, to (10, 110) is 160
        wc = WireConnection.searchBestAnchorPoints(componentLibrary, graph, busNode, otherNode);
        assertEquals(AnchorOrientation.VERTICAL, wc.getAnchorPoint1().getOrientation());
        assertEquals(0, wc.getAnchorPoint1().getY());

        // Case 2b: Direction.UNDEFINED but does not satisfy undefinedMiddleDirection (X match, Y within bus range)
        otherNode.setDirection(Direction.UNDEFINED);
        otherNode.setCoordinates(10, 100); // Distance to (10, 10) is 190, to (10, 110) is 90
        wc = WireConnection.searchBestAnchorPoints(componentLibrary, graph, busNode, otherNode);
        assertEquals(AnchorOrientation.HORIZONTAL, wc.getAnchorPoint1().getOrientation());
        assertEquals(90, wc.getAnchorPoint1().getY());

        // Case 3: Other direction (e.g. TOP) -> returns horizontal anchor point aligned with other node's Y
        otherNode.setDirection(Direction.TOP);
        otherNode.setCoordinates(50, 40);
        wc = WireConnection.searchBestAnchorPoints(componentLibrary, graph, busNode, otherNode);
        assertEquals(AnchorOrientation.HORIZONTAL, wc.getAnchorPoint1().getOrientation());
        assertEquals(30, wc.getAnchorPoint1().getY()); // otherNode.getY() - busNode.getY() = 40 - 10 = 30
    }
}
