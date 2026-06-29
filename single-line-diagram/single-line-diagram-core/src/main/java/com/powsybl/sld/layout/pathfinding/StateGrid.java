/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.IntPoint;
import com.powsybl.sld.model.coordinate.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class StateGrid {
    private final int width;
    private final int height;
    private final boolean[][] grid;

    public StateGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new boolean[width][height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                grid[x][y] = true;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean getState(int x, int y) {
        return grid[y][x];
    }

    private void setState(int x, int y, boolean state) {
        grid[y][x] = state;
    }

    public void setAvailable(int x, int y) {
        setState(x, y, true);
    }

    public void setUnavailable(int x, int y) {
        setState(x, y, false);
    }

    public void setUnavailable(List<IntPoint> points) {
        points.forEach(point -> setUnavailable(point.x(), point.y()));
    }

    /**
     * Get all the points that belong to a snakeline defined by the list of points that are changes in direction
     * @param pathDirectionChangePoints the points representing the snakeline, that is only the points at right angles (we don't have the points between those right angles)
     * @return the list of points that is all the points of the snakeline, not just the minimal information needed to represent it
     */
    public static List<IntPoint> getPointsAlongSnakeline(List<Point> pathDirectionChangePoints) {
        List<IntPoint> allPoints = new ArrayList<>();
        //only go to size - 2 because the last line segment is between size - 2 and size - 1
        for (int i = 0; i < pathDirectionChangePoints.size() - 1; ++i) {
            IntPoint firstSegmentPoint = new IntPoint(pathDirectionChangePoints.get(i));
            IntPoint secondSegmentPoint = new IntPoint(pathDirectionChangePoints.get(i + 1));
            addSegmentPoints(allPoints, firstSegmentPoint, secondSegmentPoint);
        }
        // add the last point
        allPoints.add(new IntPoint(pathDirectionChangePoints.getLast()));
        return allPoints;
    }

    /**
     * Add to the list of allPoints all the points with integer coordinates in the segment [firstSegmentPoint, secondSegmentPoint[ (start included, end excluded)
     * This function assumes that both points are either on the same column or the same line.
     * Note : if in the future this is not the case, change this to implement the Bresenham Algorithm
     * @param allPoints the list of all the points so far
     * @param firstSegmentPoint the first point of the segment
     * @param secondSegmentPoint the second point of the segment
     */
    private static void addSegmentPoints(List<IntPoint> allPoints, IntPoint firstSegmentPoint, IntPoint secondSegmentPoint) {
        IntPoint unitSegmentDirection = firstSegmentPoint.getUnitSegmentDirection(secondSegmentPoint);
        IntPoint currentPoint = new IntPoint(firstSegmentPoint);
        while (!currentPoint.equals(secondSegmentPoint)) {
            allPoints.add(currentPoint);
            currentPoint = currentPoint.getShiftedPoint(unitSegmentDirection);
        }
    }

}
