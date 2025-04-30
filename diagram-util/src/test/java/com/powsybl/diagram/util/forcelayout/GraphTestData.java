/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.Edge;
import com.powsybl.diagram.util.forcelayout.geometry.Point;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class GraphTestData {

    private GraphTestData() {
        throw new AssertionError("Instantiating utility class");
    }

    private static final Point[] POINTS = {
        new Point(1, 2),
        new Point(-3.14, 2.78),
        new Point(1.414, 2345),
        new Point(26.1, -746),
        new Point(45, 45),
    };

    private static final Edge[] EDGES = {
        new Edge(POINTS[0], POINTS[1]),
        new Edge(POINTS[2], POINTS[1]),
        new Edge(POINTS[0], POINTS[3]),
        new Edge(POINTS[0], POINTS[2]),
    };

    public static Point[] getPoints() {
        return POINTS;
    }

    public static Edge[] getEdges() {
        return EDGES;
    }
}
