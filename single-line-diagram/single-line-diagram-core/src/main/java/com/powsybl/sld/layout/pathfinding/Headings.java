/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.PointInteger;

/**
 * Utility class for movements allowed on the grid
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class Headings {

    private Headings() { }

    /**
     * The UP direction. Since the line of y 0 is the top line in a matrix, and the line at height - 1 is at the bottom, going up is decreasing the y
     */
    public static final PointInteger UP = new PointInteger(0, -1);
    /**
     * The DOWN direction. Since the line of y 0 is the top line in a matrix, and the line at height - 1 is at the bottom, going down is increasing the y
     */
    public static final PointInteger DOWN = new PointInteger(0, 1);
    /**
     * The LEFT direction
     */
    public static final PointInteger LEFT = new PointInteger(-1, 0);
    /**
     * The RIGHT direction
     */
    public static final PointInteger RIGHT = new PointInteger(1, 0);

    /**
     * All the possible movements, ie the 4 cardinal directions
     */
    public static final PointInteger[] ALL_HEADINGS = {
        UP,
        DOWN,
        LEFT,
        RIGHT
    };

    /**
     * Get the direction opposite of the given heading
     * @param heading the direction we want the opposite of, this can be null
     * @return the opposite direction, or null if heading is null. The magnitude of the result is the same as the given heading
     */
    public static PointInteger getOppositeHeading(PointInteger heading) {
        //it's correct to use == and not .equals() because we use a static variable and expect to only use those for directions
        if (heading == UP) {
            return DOWN;
        } else if (heading == DOWN) {
            return UP;
        } else if (heading == LEFT) {
            return RIGHT;
        } else if (heading == RIGHT) {
            return LEFT;
        } else if (heading == null) {
            return null;
        } else {
            return heading.getOpposite();
        }
    }

    /**
     * Check if the vector defined by the firstHeading makes a right angle with the secondHeading
     * @param firstHeading the first direction
     * @param secondHeading the second direction
     * @return if firstDirection is orthogonal to secondHeading
     */
    public static boolean isRightAngle(PointInteger firstHeading, PointInteger secondHeading) {
        int dotProduct = firstHeading.getX() * secondHeading.getX() + firstHeading.getY() + secondHeading.getY();
        return dotProduct == 0;
    }

    public static boolean isRightAngle(PointInteger previousPoint, PointInteger currentPoint, PointInteger nextPoint) {
        return previousPoint != null
                && currentPoint != null
                && nextPoint != null
                && isRightAngle(previousPoint.getDirection(currentPoint), currentPoint.getDirection(nextPoint));
    }
}

