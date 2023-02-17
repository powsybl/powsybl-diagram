/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.util.forcelayout;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VectorTest {

    @Test
    public void test() {
        Vector vector = new Vector(1.0, 2.0);
        assertEquals(2.24, vector.magnitude(), 0.01);
        assertEquals(5.0, vector.magnitudeSquare(), 0.0);
        assertEquals(1.0, vector.getX(), 0.0);
        assertEquals(2.0, vector.getY(), 0.0);

        vector = vector.normalize();
        assertEquals(1.0, vector.magnitude(), 0.1);
        assertEquals(1.0, vector.magnitudeSquare(), 0.1);
        assertEquals(0.44, vector.getX(), 0.01);
        assertEquals(0.89, vector.getY(), 0.01);

        vector = vector.divide(2.0);
        assertEquals(0.49, vector.magnitude(), 0.01);
        assertEquals(0.24, vector.magnitudeSquare(), 0.01);
        assertEquals(0.22, vector.getX(), 0.01);
        assertEquals(0.44, vector.getY(), 0.01);

        vector = vector.multiply(2.0);
        assertEquals(1.0, vector.magnitude(), 0.1);
        assertEquals(1.0, vector.magnitudeSquare(), 0.1);
        assertEquals(0.44, vector.getX(), 0.01);
        assertEquals(0.89, vector.getY(), 0.01);

        vector = vector.add(vector);
        assertEquals(1.99, vector.magnitude(), 0.01);
        assertEquals(3.99, vector.magnitudeSquare(), 0.01);
        assertEquals(0.89, vector.getX(), 0.01);
        assertEquals(1.78, vector.getY(), 0.01);
    }
}
