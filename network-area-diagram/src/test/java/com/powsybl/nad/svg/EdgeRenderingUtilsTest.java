/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.powsybl.nad.model.Point;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public class EdgeRenderingUtilsTest {

    @Test
    void testGetEdgePoints() {
        Point edgeStart1 = new Point(0, 0);
        Point edgeStart2 = new Point(1000, 0);

        List<Point> bendingPoints = new ArrayList<>();
        EdgePoints edgePoints = EdgeRenderingUtils.getEdgePoints(edgeStart1, edgeStart2, bendingPoints);
        assertEquals(2, edgePoints.points1().size());
        assertEquals(0, edgePoints.points1().get(0).getX());
        assertEquals(0, edgePoints.points1().get(0).getY());
        assertEquals(500, edgePoints.points1().get(1).getX());
        assertEquals(0, edgePoints.points1().get(1).getY());
        assertEquals(2, edgePoints.points2().size());
        assertEquals(1000, edgePoints.points2().get(0).getX());
        assertEquals(0, edgePoints.points2().get(0).getY());
        assertEquals(500, edgePoints.points2().get(1).getX());
        assertEquals(0, edgePoints.points2().get(1).getY());

        bendingPoints.add(new Point(100, 0));
        edgePoints = EdgeRenderingUtils.getEdgePoints(edgeStart1, edgeStart2, bendingPoints);
        assertEquals(3, edgePoints.points1().size());
        assertEquals(0, edgePoints.points1().get(0).getX());
        assertEquals(0, edgePoints.points1().get(0).getY());
        assertEquals(100, edgePoints.points1().get(1).getX());
        assertEquals(0, edgePoints.points1().get(1).getY());
        assertEquals(500, edgePoints.points1().get(2).getX());
        assertEquals(0, edgePoints.points1().get(2).getY());
        assertEquals(2, edgePoints.points2().size());
        assertEquals(1000, edgePoints.points2().get(0).getX());
        assertEquals(0, edgePoints.points2().get(0).getY());
        assertEquals(500, edgePoints.points2().get(1).getX());
        assertEquals(0, edgePoints.points2().get(1).getY());

        bendingPoints.add(new Point(300, 0));
        edgePoints = EdgeRenderingUtils.getEdgePoints(edgeStart1, edgeStart2, bendingPoints);
        assertEquals(4, edgePoints.points1().size());
        assertEquals(0, edgePoints.points1().get(0).getX());
        assertEquals(0, edgePoints.points1().get(0).getY());
        assertEquals(100, edgePoints.points1().get(1).getX());
        assertEquals(0, edgePoints.points1().get(1).getY());
        assertEquals(300, edgePoints.points1().get(2).getX());
        assertEquals(0, edgePoints.points1().get(2).getY());
        assertEquals(500, edgePoints.points1().get(3).getX());
        assertEquals(0, edgePoints.points1().get(3).getY());
        assertEquals(2, edgePoints.points2().size());
        assertEquals(1000, edgePoints.points2().get(0).getX());
        assertEquals(0, edgePoints.points2().get(0).getY());
        assertEquals(500, edgePoints.points2().get(1).getX());
        assertEquals(0, edgePoints.points2().get(1).getY());

        bendingPoints.add(new Point(600, 0));
        edgePoints = EdgeRenderingUtils.getEdgePoints(edgeStart1, edgeStart2, bendingPoints);
        assertEquals(4, edgePoints.points1().size());
        assertEquals(0, edgePoints.points1().get(0).getX());
        assertEquals(0, edgePoints.points1().get(0).getY());
        assertEquals(100, edgePoints.points1().get(1).getX());
        assertEquals(0, edgePoints.points1().get(1).getY());
        assertEquals(300, edgePoints.points1().get(2).getX());
        assertEquals(0, edgePoints.points1().get(2).getY());
        assertEquals(500, edgePoints.points1().get(3).getX());
        assertEquals(0, edgePoints.points1().get(3).getY());
        assertEquals(3, edgePoints.points2().size());
        assertEquals(1000, edgePoints.points2().get(0).getX());
        assertEquals(0, edgePoints.points2().get(0).getY());
        assertEquals(600, edgePoints.points2().get(1).getX());
        assertEquals(0, edgePoints.points2().get(1).getY());
        assertEquals(500, edgePoints.points2().get(2).getX());
        assertEquals(0, edgePoints.points2().get(2).getY());
    }

    @Test
    void testGetBentLinesPoints() throws URISyntaxException, IOException {
        Path metadataFile = Paths.get(getClass().getResource("/IEEE_9_zeroimpedance_metadata.json").toURI());

        Map<String, List<Point>> bentLinesPoints = EdgeRenderingUtils.getBentLinesPoints(metadataFile);
        checkBendingPoints(bentLinesPoints);

        try (InputStream metadataIS = Files.newInputStream(metadataFile)) {
            bentLinesPoints = EdgeRenderingUtils.getBentLinesPoints(metadataIS);
            checkBendingPoints(bentLinesPoints);
        }

        try (Reader metadataReader = Files.newBufferedReader(metadataFile)) {
            bentLinesPoints = EdgeRenderingUtils.getBentLinesPoints(metadataReader);
            checkBendingPoints(bentLinesPoints);
        }
    }

    void checkBendingPoints(Map<String, List<Point>> bentLinesPoints) {
        assertEquals(3, bentLinesPoints.keySet().size());
        assertTrue(bentLinesPoints.containsKey("L5-4-0"));
        assertTrue(bentLinesPoints.containsKey("L6-4-0"));
        assertTrue(bentLinesPoints.containsKey("L7-5-0"));
        assertEquals(1, bentLinesPoints.get("L5-4-0").size());
        assertEquals(3, bentLinesPoints.get("L6-4-0").size());
        assertEquals(2, bentLinesPoints.get("L7-5-0").size());
    }
}
