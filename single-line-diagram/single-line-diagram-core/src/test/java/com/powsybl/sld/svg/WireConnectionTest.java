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
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.Node;
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
}
