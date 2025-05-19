/**
 * Copyright (c) 2020-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class BoundingBoxTest {

    @Test
    void test() {
        List<Point> points = new ArrayList<>();
        points.add(new Point(1.0, 2.0));
        points.add(new Point(3.0, 4.0));
        points.add(new Point(5.0, 6.0));
        points.add(new Point(1.0, 2.0));
        BoundingBox bbox = BoundingBox.computeBoundingBox(points);

        assertEquals(4.0, bbox.getWidth(), 0.0);
        assertEquals(4.0, bbox.getHeight(), 0.0);
        assertEquals(1.0, bbox.getLeft(), 0.0);
        assertEquals(2.0, bbox.getTop(), 0.0);
        assertEquals(5.0, bbox.getRight(), 0.0);
        assertEquals(6.0, bbox.getBottom(), 0.0);
    }

    @Test
    void addBoundingBox() {
        List<Point> points = new ArrayList<>();
        points.add(new Point(1.0, 2.0));
        points.add(new Point(3.0, 4.0));
        points.add(new Point(5.0, 6.0));
        points.add(new Point(1.0, 2.0));
        BoundingBox bbox = BoundingBox.computeBoundingBox(points);

        List<Point> otherPoints = new ArrayList<>();
        otherPoints.add(points.get(1));
        otherPoints.add(new Point(5, 9));
        otherPoints.add(new Point(0, 0));
        otherPoints.add(new Point(-4, -1.1));
        otherPoints.add(new Point(5, 6));
        otherPoints.add(new Point(4, -2));

        BoundingBox otherBbox = BoundingBox.computeBoundingBox(otherPoints);
        BoundingBox finalBbox = BoundingBox.addBoundingBoxes(bbox, otherBbox);

        assertEquals(9, finalBbox.getWidth(), 0.0);
        assertEquals(11, finalBbox.getHeight(), 0.0);
        assertEquals(-4, finalBbox.getLeft(), 0.0);
        assertEquals(-2, finalBbox.getTop(), 0.0);
        assertEquals(5.0, finalBbox.getRight(), 0.0);
        assertEquals(9, finalBbox.getBottom(), 0.0);
    }
}
