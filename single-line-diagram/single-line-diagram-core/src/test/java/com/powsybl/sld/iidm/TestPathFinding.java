/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.sld.layout.pathfinding.*;
import com.powsybl.sld.model.coordinate.Point;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
class TestPathFinding extends AbstractTestCaseIidm {

    private Grid pathFinderGrid = null;
    private final PathFinder pathfinder = new DijkstraPathFinder();

    @BeforeEach
    public void setUp() throws IOException {
        pathFinderGrid = new Grid(12, 12);
    }

    @Test
    void testNoSmoothPath() {
        final List<Point> expectedSnakeline = new ArrayList<>();
        pathFinderGrid.setAvailability(0, 0, true);
        expectedSnakeline.add(new Point(0, 0));
        for (int i = 0; i < 12 - 1; i++) {
            pathFinderGrid.setAvailability(i + 1, i, true);
            expectedSnakeline.add(new Point(i + 1, i));
            pathFinderGrid.setAvailability(i + 1, i + 1, true);
            expectedSnakeline.add(new Point(i + 1, i + 1));
        }
        List<Point> snakeline = pathfinder.toSnakeLine(pathfinder.findShortestPath(pathFinderGrid,
                0, 0,
                11, 11));
        assertEquals(expectedSnakeline.size(), snakeline.size());
        for (int i = 0; i < expectedSnakeline.size(); i++) {
            assertEquals(expectedSnakeline.get(i).toString(), snakeline.get(i).toString());
        }
    }

    @Test
    void testSmoothPath() {
        final List<Point> expectedSnakeline = new ArrayList<>();
        // Make available left & right borders
        for (int y = 0; y < 12; y++) {
            pathFinderGrid.setAvailability(0, y, true);
            expectedSnakeline.add(new Point(0, y));
            pathFinderGrid.setAvailability(11, y, true);
        }
        // Make available up & down borders
        for (int x = 1; x < 12; x++) {
            pathFinderGrid.setAvailability(x, 0, true);
            pathFinderGrid.setAvailability(x, 11, true);
            expectedSnakeline.add(new Point(x, 11));
        }
        List<Point> snakeline = pathfinder.toSnakeLine(pathfinder.findShortestPath(pathFinderGrid,
                0, 0,
                11, 11));
        assertEquals(expectedSnakeline.size(), snakeline.size());
        for (int i = 0; i < expectedSnakeline.size(); i++) {
            assertEquals(expectedSnakeline.get(i).toString(), snakeline.get(i).toString());
        }
    }
}
