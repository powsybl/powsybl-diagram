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
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void testCalculatePolylinePoints_VerticalVertical() {
        AnchorPoint apA = new AnchorPoint(0, 0, AnchorOrientation.VERTICAL);
        AnchorPoint apB = new AnchorPoint(0, 0, AnchorOrientation.VERTICAL);

        Node nodeA = createConnectivityNode(10, 10);
        Node nodeB = createConnectivityNode(50, 50);

        WireConnection wc = new WireConnection(apA, apB);
        List<Point> pol = wc.calculatePolylinePoints(nodeA, nodeB, false, new Point(0, 0));

        assertEquals(List.of(10.0, 10.0, 50.0, 30.0, 10.0, 30.0, 50.0, 50.0), Point.pointsToDoubles(pol));
    }

    @Test
    void testCalculatePolylinePoints_VerticalHorizontal() {
        AnchorPoint apA = new AnchorPoint(0, 0, AnchorOrientation.VERTICAL);
        AnchorPoint apB = new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL);

        Node nodeA = createConnectivityNode(10, 10);
        Node nodeB = createConnectivityNode(50, 50);

        WireConnection wc = new WireConnection(apA, apB);
        List<Point> pol = wc.calculatePolylinePoints(nodeA, nodeB, false, new Point(0, 0));

        assertEquals(List.of(10.0, 10.0, 10.0, 50.0, 50.0, 50.0), Point.pointsToDoubles(pol));
    }

    @Test
    void testCalculatePolylinePoints_HorizontalHorizontal() {
        AnchorPoint apA = new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL);
        AnchorPoint apB = new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL);

        Node nodeA = createConnectivityNode(10, 10);
        Node nodeB = createConnectivityNode(50, 50);

        WireConnection wc = new WireConnection(apA, apB);
        List<Point> pol = wc.calculatePolylinePoints(nodeA, nodeB, false, new Point(0, 0));

        assertEquals(List.of(10.0, 10.0, 30.0, 50.0, 30.0, 10.0, 50.0, 50.0), Point.pointsToDoubles(pol));
    }

    @Test
    void testCalculatePolylinePoints_HorizontalVertical() {
        AnchorPoint apA = new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL);
        AnchorPoint apB = new AnchorPoint(0, 0, AnchorOrientation.VERTICAL);

        Node nodeA = createConnectivityNode(10, 10);
        Node nodeB = createConnectivityNode(50, 50);

        WireConnection wc = new WireConnection(apA, apB);
        List<Point> pol = wc.calculatePolylinePoints(nodeA, nodeB, false, new Point(0, 0));

        assertEquals(List.of(10.0, 10.0, 50.0, 10.0, 50.0, 50.0), Point.pointsToDoubles(pol));
    }

    @Test
    void testCalculatePolylinePoints_NoneHorizontal() {
        AnchorPoint apA = new AnchorPoint(0, 0, AnchorOrientation.NONE);
        AnchorPoint apB = new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL);

        Node nodeA = createConnectivityNode(10, 10);
        Node nodeB = createConnectivityNode(50, 50);

        WireConnection wc = new WireConnection(apA, apB);
        List<Point> pol = wc.calculatePolylinePoints(nodeA, nodeB, false, new Point(0, 0));

        assertEquals(List.of(10.0, 10.0, 10.0, 50.0, 50.0, 50.0), Point.pointsToDoubles(pol));
    }

    @Test
    void testCalculatePolylinePoints_NoneVertical() {
        AnchorPoint apA = new AnchorPoint(0, 0, AnchorOrientation.NONE);
        AnchorPoint apB = new AnchorPoint(0, 0, AnchorOrientation.VERTICAL);

        Node nodeA = createConnectivityNode(10, 10);
        Node nodeB = createConnectivityNode(50, 50);

        WireConnection wc = new WireConnection(apA, apB);
        List<Point> pol = wc.calculatePolylinePoints(nodeA, nodeB, false, new Point(0, 0));

        assertEquals(List.of(10.0, 10.0, 50.0, 10.0, 50.0, 50.0), Point.pointsToDoubles(pol));
    }
}
