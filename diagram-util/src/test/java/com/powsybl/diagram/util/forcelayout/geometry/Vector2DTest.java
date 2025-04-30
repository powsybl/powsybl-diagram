/*
 * Copyright (c) 2020-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class Vector2DTest {

    @Test
    void test() {
        Vector2D vector = new Vector2D(1.0, 2.0);
        assertEquals(2.24, vector.magnitude(), 0.01);
        assertEquals(5.0, vector.magnitudeSquare(), 0.0);
        assertEquals(1.0, vector.x(), 0.0);
        assertEquals(2.0, vector.y(), 0.0);

        vector = vector.normalize();
        assertEquals(1.0, vector.magnitude(), 0.1);
        assertEquals(1.0, vector.magnitudeSquare(), 0.1);
        assertEquals(0.44, vector.x(), 0.01);
        assertEquals(0.89, vector.y(), 0.01);

        vector = vector.divide(2.0);
        assertEquals(0.49, vector.magnitude(), 0.01);
        assertEquals(0.24, vector.magnitudeSquare(), 0.01);
        assertEquals(0.22, vector.x(), 0.01);
        assertEquals(0.44, vector.y(), 0.01);

        vector = vector.multiply(2.0);
        assertEquals(1.0, vector.magnitude(), 0.1);
        assertEquals(1.0, vector.magnitudeSquare(), 0.1);
        assertEquals(0.44, vector.x(), 0.01);
        assertEquals(0.89, vector.y(), 0.01);

        vector = vector.add(vector);
        assertEquals(1.99, vector.magnitude(), 0.01);
        assertEquals(3.99, vector.magnitudeSquare(), 0.01);
        assertEquals(0.89, vector.x(), 0.01);
        assertEquals(1.78, vector.y(), 0.01);
    }

    @Test
    void testSubtract() {
        Vector2D v1 = new Vector2D(23, -13.5);
        Vector2D v2 = new Vector2D(-2.3, -5.7);
        Vector2D expected = new Vector2D(25.3, -7.8);
        Vector2D got = v1.subtract(v2);
        double delta = 0.01;
        assertEquals(expected.x(), got.x(), delta);
        assertEquals(expected.y(), got.y(), delta);
    }

    @Test
    void testUnitVector() {
        double delta = 0.0001;
        Point point1 = new Point(2.3, 4.5);
        Point point2 = new Point(7.2, -3.14);
        Point point3 = new Point(-97.87, -53.6);
        Point point4 = new Point(-0.13, 5.95);
        Vector2D[] resultVectors = {
                Vector2D.calculateUnitVector(point1, point2),
                Vector2D.calculateUnitVector(point2, point3),
                Vector2D.calculateUnitVector(point3, point4),
                Vector2D.calculateUnitVector(point4, point1),
        };
        Vector2D[] expectedVectors = {
            new Vector2D(0.53987, -0.84175),
            new Vector2D(-0.90143, -0.43292),
            new Vector2D(0.85398, 0.5203),
            new Vector2D(0.85874, -0.51242),
        };
        for (int i = 0; i < resultVectors.length; i++) {
            // there is an equal Vector2D but there is no way to put a delta for this, check x and y separately
            assertEquals(expectedVectors[i].x(), resultVectors[i].x(), delta);
            assertEquals(expectedVectors[i].y(), resultVectors[i].y(), delta);
        }

    }
}
