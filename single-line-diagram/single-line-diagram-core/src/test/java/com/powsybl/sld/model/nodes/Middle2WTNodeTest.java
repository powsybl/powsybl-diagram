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
class Middle2WTNodeTest {

    @Test
    void testSetOrientationFromSnakeLines() {
        VoltageLevelInfos vl1 = new VoltageLevelInfos("vl1", "VL 1", 225);
        VoltageLevelInfos vl2 = new VoltageLevelInfos("vl2", "VL 2", 400);
        Middle2WTNode node = new Middle2WTNode("node", "Node", vl1, vl2, "Test");
        Point transfoPoint = new Point(10, 0);

        // Horizontal: coord2 o-----OO-----o coord1
        // Orientation.LEFT case: coord1.x > coord2.x
        List<Point> pol1 = List.of(new Point(20, 0), transfoPoint);
        List<Point> pol2 = List.of(new Point(0, 0), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2));
        assertEquals(Orientation.LEFT, node.getOrientation());

        // Horizontal: coord1 o-----OO-----o coord2
        // Orientation.RIGHT case: coord1.x < coord2.x
        pol1 = List.of(new Point(0, 0), transfoPoint);
        pol2 = List.of(new Point(20, 0), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2));
        assertEquals(Orientation.RIGHT, node.getOrientation());

        // Vertical:
        // coord2 o
        //        |
        //        8
        //        |
        // coord1 o
        // Orientation.UP case: coord2.y < coord1.y
        pol1 = List.of(new Point(0, 20), transfoPoint);
        pol2 = List.of(new Point(0, 0), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2));
        assertEquals(Orientation.UP, node.getOrientation());

        // Vertical:
        // coord1 o
        //        |
        //        8
        //        |
        // coord2 o
        // Orientation.DOWN case: coord2.y > coord1.y
        pol1 = List.of(new Point(0, 0), transfoPoint);
        pol2 = List.of(new Point(0, 20), transfoPoint);
        node.setOrientationFromSnakeLines(List.of(pol1, pol2));
        assertEquals(Orientation.DOWN, node.getOrientation());
    }
}
