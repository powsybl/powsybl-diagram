/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class PointTest {

    @Test
    void applyForce() {
        Vector2D force1 = new Vector2D(3, 7);
        Vector2D force2 = new Vector2D(2, -5);
        Vector2D force3 = new Vector2D(-12, -6.6);
        Vector2D force4 = new Vector2D(-2, 4);
        Point point = new Point(6, 90);
        point.applyForce(force1);
        assertEquals(3, point.getForces().getX());
        assertEquals(7, point.getForces().getY());
        point.applyForce(force2);
        assertEquals(5, point.getForces().getX());
        assertEquals(2, point.getForces().getY());
        point.applyForce(force3);
        assertEquals(-7, point.getForces().getX());
        assertEquals(-4.6, point.getForces().getY(), 0.001);
        point.applyForce(force4);
        assertEquals(-9, point.getForces().getX());
        assertEquals(-0.6, point.getForces().getY(), 0.001);
    }

    @Test
    void positionTest() {
        Point point = new Point(42, -71);
        assertEquals(42, point.getPosition().getX());
        assertEquals(-71, point.getPosition().getY());
        point.setPosition(new Vector2D(-3, 0.9));
        assertEquals(-3, point.getPosition().getX());
        assertEquals(0.9, point.getPosition().getY());
    }

    @Test
    void velocityTest() {
        Point point = new Point(434, 8989);
        assertEquals(0, point.getVelocity().getX());
        assertEquals(0, point.getVelocity().getY());
        point.setVelocity(new Vector2D(4, 90909));
        assertEquals(4, point.getVelocity().getX());
        assertEquals(90909, point.getVelocity().getY());
    }

    @Test
    void getEnergy() {
        Point point = new Point(56, 5, 6.4);
        point.setVelocity(new Vector2D(4, -6));
        assertEquals(166.4, point.getEnergy(), 0.1);
    }

    @Test
    void getMass() {
        Point point = new Point(0, 0);
        assertEquals(1, point.getMass());
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            new Point(1, 2, -6);
        });
        assertTrue(exception.getMessage().contains("Point with a negative mass is not allowed"));
        Point point2 = new Point(-3, -5, 0.01);
        assertEquals(0.01, point2.getMass());
    }

    @Test
    void resetForces() {
        Point point = new Point(-7.6, -12474);
        point.applyForce(new Vector2D(59598, 7887));
        point.applyForce(new Vector2D(233, -66767));
        assertNotEquals(0, point.getForces().getX());
        assertNotEquals(0, point.getForces().getY());
        point.resetForces();
        assertEquals(0, point.getForces().getX());
        assertEquals(0, point.getForces().getY());
    }

    @Test
    void distanceTo() {
        Point point1 = new Point(2, 4);
        Point point2 = new Point(-6, -2);
        double distance = point1.distanceTo(point2);
        assertEquals(10, distance);
        double otherDistance = point2.distanceTo(point1);
        assertEquals(distance, otherDistance);
        assertEquals(0, point1.distanceTo(point1));
        assertEquals(0, point2.distanceTo(point2));
    }
}
