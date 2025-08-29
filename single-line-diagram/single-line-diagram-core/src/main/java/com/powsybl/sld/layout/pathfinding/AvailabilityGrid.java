/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.PointInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * A grid that represents how each pixel is occupied, used for pathfinding. By default, all positions are considered as available
 * Note that this is a much bigger matrix than the matrix of cells (with each cell representing a substation)
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class AvailabilityGrid {
    /**
     * This space is not available at all
     */
    public static final byte NOT_AVAILABLE = 0;
    /**
     * There is already a wire there
     */
    public static final byte WIRE = 1;
    /**
     * The area around a wire (distance TBD). This is only the area lateral of the wire, not in front of it
     */
    public static final byte AROUND_WIRE = 2;
    /**
     * No restriction on this space
     */
    public static final byte AVAILABLE = 3;

    private final byte[][] grid;
    private final int snakelinePadding;

    public AvailabilityGrid(int width, int height, int snakelinePadding) {
        this.grid = new byte[height][width];
        this.snakelinePadding = snakelinePadding;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                // technically not necessary since by default every element will already be 0 which is the value of NOT_AVAILABLE
                // but in case we change the value of NOT_AVAILABLE it will be necessary
                grid[i][j] = AVAILABLE;
            }
        }
    }

    /**
     * Makes a deep copy of the availabilityGrid passed and creates a new one
     * @param availabilityGrid the availabilityGrid to be deep-copied
     */
    public AvailabilityGrid(AvailabilityGrid availabilityGrid) {
        this.snakelinePadding = availabilityGrid.snakelinePadding;
        int height = availabilityGrid.grid.length;
        if (height > 0) {
            int width = availabilityGrid.grid[0].length;
            this.grid = new byte[height][width];
            for (int i = 0; i < height; ++i) {
                grid[i] = availabilityGrid.grid[i].clone();
            }
        } else {
            this.grid = new byte[0][0];
        }
    }

    public byte[][] getGrid() {
        return grid;
    }

    public static boolean isRightAngle(PointInteger previous, PointInteger current, PointInteger next) {
        // Check if the angle is a right angle using dot product
        int vectorABx = current.getX() - previous.getX();
        int vectorABy = current.getY() - previous.getY();
        int vectorBCx = next.getX() - current.getX();
        int vectorBCy = next.getY() - current.getY();

        // Dot product of vectors AB and BC
        int dotProduct = vectorABx * vectorBCx + vectorABy * vectorBCy;

        // Check if the dot product is zero (cosine of 90 degrees)
        return dotProduct == 0;
    }

    public static boolean isRightAngle(PathNode currentNode, PointInteger next) {
        PathNode parentNode = currentNode.getParent();
        return parentNode != null && isRightAngle(parentNode.getPointInteger(), currentNode.getPointInteger(), next);
    }

    public void makeNotAvailable(int x, int y) {
        grid[y][x] = NOT_AVAILABLE;
    }

    public void makeNotAvailable(PointInteger point) {
        makeNotAvailable(point.getX(), point.getY());
    }

    public boolean isNotAvailable(int x, int y) {
        return grid[y][x] == NOT_AVAILABLE;
    }

    public boolean isNotAvailable(PointInteger point) {
        return isNotAvailable(point.getX(), point.getY());
    }

    public void makeWire(int x, int y) {
        grid[y][x] = WIRE;
    }

    public void makeWire(PointInteger point) {
        makeWire(point.getX(), point.getY());
    }

    public boolean isWire(int x, int y) {
        return grid[y][x] == WIRE;
    }

    public boolean isWire(PointInteger point) {
        return isWire(point.getX(), point.getY());
    }

    public void makeAroundWire(int x, int y) {
        grid[y][x] = AROUND_WIRE;
    }

    public void makeAroundWire(PointInteger point) {
        makeAroundWire(point.getX(), point.getY());
    }

    public boolean isAroundWire(int x, int y) {
        return grid[y][x] == AROUND_WIRE;
    }

    public boolean isAroundWire(PointInteger point) {
        return isAroundWire(point.getX(), point.getY());
    }

    public void makeAvailable(int x, int y) {
        grid[y][x] = AVAILABLE;
    }

    public boolean isAvailable(int x, int y) {
        return grid[y][x] == AVAILABLE;
    }

    public boolean isAvailable(PointInteger point) {
        return isAvailable(point.getX(), point.getY());
    }

    public void makeWirePath(List<PointInteger> path) {
        for (PointInteger pointInteger : path) {
            this.makeWire(pointInteger.getX(), pointInteger.getY());
        }
    }
    public int getSnakelinePadding() {
        return snakelinePadding;
    }
}

