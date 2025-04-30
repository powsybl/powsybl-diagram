/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class EdgeTest {
    @Test
    void testEdgeLength() {
        double delta = 0.01;
        Point point1 = new Point(2, -3);
        Point point2 = new Point(23.3, 5.6);
        Point point3 = new Point(-2.1, -7.54);
        Edge[] testEdges = {
            new Edge(point1, point2),
            new Edge(point3, point1),
            new Edge(point1, point2),
            new Edge(point2, point1),
            new Edge(point2, point2),
            new Edge(point2, point3),
        };
        double[] expectedLengths = {
            22.970,
            6.117,
            22.970,
            22.970,
            0.0,
            28.598,
        };
        for (int i = 0; i < testEdges.length; i++) {
            assertEquals(testEdges[i].length(), expectedLengths[i], delta);

        }
    }

    @Test
    void checkNotEqual() {
        Point point = new Point(1, 2);
        Edge testEdge = new Edge(point, new Point(2.3, -8.6));
        assertNotEquals(testEdge, point);
    }
}
