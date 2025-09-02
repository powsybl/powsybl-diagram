/**
 * Copyright (c) 2023-2025, RTE (http://www.rte-france.com)
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

    private AvailabilityGrid pathFinderGrid = null;
    private final PathFinder pathfinder = new AStarPathFinder();

    @BeforeEach
    public void setUp() throws IOException {
        pathFinderGrid = new AvailabilityGrid(12, 12, 5);
    }

    @Test
    void testNoSmoothPath() {
        final List<Point> expectedSnakeline = new ArrayList<>();
        expectedSnakeline.add(new Point(0, 0));
        for (int i = 0; i < 12; ++i) {
            for (int j = 0; j < 12; ++j) {
                pathFinderGrid.makeNotAvailable(i, j);
            }
        }
        for (int i = 0; i < 11; ++i) {
            pathFinderGrid.makeAvailable(i + 1, i);
            expectedSnakeline.add(new Point(i + 1, i));
            pathFinderGrid.makeAvailable(i + 1, i + 1);
            expectedSnakeline.add(new Point(i + 1, i + 1));
        }
        pathFinderGrid.makeAvailable(0, 0);
        List<Point> snakeline = pathfinder.findShortestPath(pathFinderGrid,
                new Point(0, 0),
                new Point(11, 11));
        assertEquals(expectedSnakeline.size(), snakeline.size());
        for (int i = 0; i < expectedSnakeline.size(); i++) {
            assertEquals(expectedSnakeline.get(i).toString(), snakeline.get(i).toString());
        }
    }

    @Test
    void testSmoothPath() {
        final List<Point> expectedSnakeline = new ArrayList<>();
        // Make available right border
        for (int y = 0; y < 12; y++) {
            pathFinderGrid.makeAvailable(11, y);
        }
        // Make available top border
        for (int x = 1; x < 12; x++) {
            pathFinderGrid.makeAvailable(x, 0);
        }

        expectedSnakeline.add(new Point(0, 0)); // first point
        expectedSnakeline.add(new Point(11, 0)); // right angle point
        expectedSnakeline.add(new Point(11, 11)); // last point

        List<Point> snakeline = pathfinder.findShortestPath(pathFinderGrid,
                new Point(0, 0),
                new Point(11, 11));
        assertEquals(expectedSnakeline.size(), snakeline.size());
        for (int i = 0; i < expectedSnakeline.size(); i++) {
            assertEquals(expectedSnakeline.get(i).toString(), snakeline.get(i).toString());
        }
    }

    @Test
    void testAllAvailablePath() {
        for (int x = 0; x < 12; ++x) {
            for (int y = 0; y < 12; ++y) {
                pathFinderGrid.makeAvailable(x, y);
            }
        }
        List<Point> snakeline = pathfinder.findShortestPath(
                pathFinderGrid,
                new Point(0, 0),
                new Point(11, 11)
        );
        assertEquals(3, snakeline.size());
    }
}
