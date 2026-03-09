/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class Middle3WTNodeTest {

    private Middle3WTNode create3WTNode() {
        VoltageLevelInfos vl1 = new VoltageLevelInfos("vl1", "VL 1", 225);
        VoltageLevelInfos vl2 = new VoltageLevelInfos("vl2", "VL 2", 63);
        VoltageLevelInfos vl3 = new VoltageLevelInfos("vl3", "VL 3", 150);
        var node = new Middle3WTNode("node", "Node", vl1, vl2, vl3, "3WT", false);
        node.setCoordinates(new Point(50, 100));
        return node;
    }

    private void assertNodeState(Middle3WTNode node, Orientation expectedOrientation, String upperLeftId, String upperRightId, String downId) {
        assertEquals(expectedOrientation, node.getOrientation());
        assertEquals(upperLeftId, node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_LEFT).id());
        assertEquals(upperRightId, node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_RIGHT).id());
        assertEquals(downId, node.getVoltageLevelInfos(Middle3WTNode.Winding.DOWN).id());
    }

    @Test
    void testSetOrientationFromSnakeLines_Case1_Horizontal() {
        Middle3WTNode middle3WTNode = create3WTNode();
        // line to leg1 ____OO____ line to leg3
        //                  O
        //                  |
        //            line to leg2
        List<Point> pol1 = List.of(new Point(10, 100), middle3WTNode.getCoordinates());
        List<Point> pol2 = List.of(new Point(50, 150), middle3WTNode.getCoordinates());
        List<Point> pol3 = List.of(new Point(90, 100), middle3WTNode.getCoordinates());

        middle3WTNode.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(middle3WTNode, Orientation.UP, "vl1", "vl3", "vl2");

        //               line to leg2
        //                     |
        //                     O
        // line to leg1  -----OO-----  line to leg3
        pol2 = List.of(new Point(50, 50), middle3WTNode.getCoordinates());
        middle3WTNode.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(middle3WTNode, Orientation.DOWN, "vl1", "vl3", "vl2");
    }

    @Test
    void testSetOrientationFromSnakeLines_Case2_Horizontal() {
        Middle3WTNode node = create3WTNode();
        Point transfoPoint = new Point(0, 0);
        // line to leg1 ____OO____ line to leg2
        //                  O
        //                  |
        //            line to leg3
        List<Point> pol1 = List.of(new Point(10, 100), transfoPoint);
        List<Point> pol2 = List.of(new Point(90, 100), transfoPoint);
        List<Point> pol3 = List.of(new Point(50, 150), transfoPoint);

        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.UP, "vl1", "vl2", "vl3");

        //               line to leg3
        //                     |
        //                     O
        // line to leg1  -----OO-----  line to leg2
        pol3 = List.of(new Point(50, 50), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.DOWN, "vl1", "vl2", "vl3");
    }

    @Test
    void testSetOrientationFromSnakeLines_Case3_Horizontal() {
        Middle3WTNode node = create3WTNode();
        Point transfoPoint = new Point(0, 0);
        // line to leg2 ____OO____ line to leg3
        //                  O
        //                  |
        //            line to leg1
        List<Point> pol1 = List.of(new Point(50, 150), transfoPoint);
        List<Point> pol2 = List.of(new Point(10, 100), transfoPoint);
        List<Point> pol3 = List.of(new Point(90, 100), transfoPoint);

        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.UP, "vl2", "vl3", "vl1");

        //               line to leg1
        //                     |
        //                     O
        // line to leg2  -----OO-----  line to leg3
        pol1 = List.of(new Point(50, 50), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.DOWN, "vl2", "vl3", "vl1");
    }

    @Test
    void testSetOrientationFromSnakeLines_Case4_Vertical() {
        Middle3WTNode node = create3WTNode();
        Point transfoPoint = new Point(0, 0);
        // line to leg1
        //      |
        //      8o --- line to leg3
        //      |
        // line to leg2
        List<Point> pol1 = List.of(new Point(100, 10), transfoPoint);
        List<Point> pol2 = List.of(new Point(100, 90), transfoPoint);
        List<Point> pol3 = List.of(new Point(150, 50), transfoPoint);

        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.LEFT, "vl2", "vl1", "vl3");

        //           line to leg1
        //                   |
        // line to leg3 --- o8
        //                   |
        //           line to leg2
        pol3 = List.of(new Point(50, 50), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.RIGHT, "vl1", "vl2", "vl3");
    }

    @Test
    void testSetOrientationFromSnakeLines_Case5_Vertical() {
        Middle3WTNode node = create3WTNode();
        Point transfoPoint = new Point(0, 0);
        // line to leg3
        //      |
        //      8o --- line to leg1
        //      |
        // line to leg2
        List<Point> pol1 = List.of(new Point(150, 50), transfoPoint);
        List<Point> pol2 = List.of(new Point(100, 90), transfoPoint);
        List<Point> pol3 = List.of(new Point(100, 10), transfoPoint);

        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.LEFT, "vl2", "vl3", "vl1");

        //           line to leg3
        //                   |
        // line to leg1 --- o8
        //                   |
        //           line to leg2
        pol1 = List.of(new Point(50, 50), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.RIGHT, "vl3", "vl2", "vl1");
    }

    @Test
    void testSetOrientationFromSnakeLines_Case6_Vertical() {
        Middle3WTNode node = create3WTNode();
        Point transfoPoint = new Point(0, 0);
        // line to leg1
        //      |
        //      8o --- line to leg2
        //      |
        // line to leg3
        List<Point> pol1 = List.of(new Point(100, 10), transfoPoint);
        List<Point> pol2 = List.of(new Point(150, 50), transfoPoint);
        List<Point> pol3 = List.of(new Point(100, 90), transfoPoint);

        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.LEFT, "vl3", "vl1", "vl2");

        //           line to leg1
        //                   |
        // line to leg2 --- o8
        //                   |
        //           line to leg3
        pol2 = List.of(new Point(50, 50), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2, pol3));
        assertNodeState(node, Orientation.RIGHT, "vl1", "vl3", "vl2");
    }
}
