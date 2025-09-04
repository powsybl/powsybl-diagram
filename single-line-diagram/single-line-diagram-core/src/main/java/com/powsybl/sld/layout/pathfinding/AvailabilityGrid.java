/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.Point;
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
    private final int height;
    private final int width;

    public AvailabilityGrid(int width, int height, int snakelinePadding) {
        this.grid = new byte[height][width];
        this.height = height;
        this.width = width;
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
        this.height = availabilityGrid.grid.length;
        if (height > 0) {
            this.width = availabilityGrid.grid[0].length;
            this.grid = new byte[height][width];
            for (int i = 0; i < height; ++i) {
                grid[i] = availabilityGrid.grid[i].clone();
            }
        } else {
            this.width = 0;
            this.grid = new byte[0][0];
        }
    }

    public byte[][] getGrid() {
        return grid;
    }

    /**
     * Checks if the lines formed by previous-current and current-next make a right angle
     * @param previous the previous point
     * @param current the current point
     * @param next the next point
     * @return if previous-current-next is a right angle
     */
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

    /**
     * Checks if the lines formed by currentNode.parent - currentNode and currentNode-next make a right angle.
     * currentNode.parent can be null and this operation will return false in that case
     * @param currentNode the current node of the search
     * @param next the next point
     * @return if parent-currentNode-next make a right angle
     */
    public static boolean isRightAngle(PathNode currentNode, PointInteger next) {
        PathNode parentNode = currentNode.getParent();
        return parentNode != null && isRightAngle(parentNode.getPointInteger(), currentNode.getPointInteger(), next);
    }

    /**
     * @param x the column index
     * @param y the line index
     * @return if the coordinates (x,y) are the grid
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public void makeNotAvailable(int x, int y) {
        grid[y][x] = NOT_AVAILABLE;
    }

    public void makeNotAvailable(PointInteger point) {
        makeNotAvailable(point.getX(), point.getY());
    }

    public boolean isNotAvailable(int x, int y) {
        return isInBounds(x, y) && grid[y][x] == NOT_AVAILABLE;
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
        return isInBounds(x, y) && grid[y][x] == WIRE;
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
        return isInBounds(x, y) && grid[y][x] == AROUND_WIRE;
    }

    public boolean isAroundWire(PointInteger point) {
        return isAroundWire(point.getX(), point.getY());
    }

    /**
     * Makes all point in direction and -direction starting from (x, y) (excluded) as AROUND_WIRE, for snakeLinePadding (from the class constructor)
     * It does so only if those points are AVAILABLE (this does not overwrite NOT_AVAILABLE and WIRE by accident)
     * @param x starting point X (usually wir;e)
     * @param y starting point Y (usually wire)
     * @param direction the direction in which to apply around wire, will also be applied in the opposite direction
     */
    public void makeAroundWireInBothDirections(int x, int y, PointInteger direction) {
        List<PointInteger> candidatePoints = new ArrayList<>();
        PointInteger oppositeDirection = direction.getOpposite();
        PointInteger startingPoint = new PointInteger(x, y);
        PointInteger pointInDirection = startingPoint.getShiftedPoint(direction);
        PointInteger pointInOppositeDirection = startingPoint.getShiftedPoint(oppositeDirection);
        for (int i = 0; i < snakelinePadding; ++i) {
            candidatePoints.add(pointInDirection);
            candidatePoints.add(pointInOppositeDirection);
            pointInDirection = pointInDirection.getShiftedPoint(direction);
            pointInOppositeDirection = pointInOppositeDirection.getShiftedPoint(oppositeDirection);
        }
        for (PointInteger candidate : candidatePoints) {
            if (isAvailable(candidate)) {
                makeAroundWire(candidate);
            }
        }
    }

    public void makeAvailable(int x, int y) {
        grid[y][x] = AVAILABLE;
    }

    public boolean isAvailable(int x, int y) {
        return isInBounds(x, y) && grid[y][x] == AVAILABLE;
    }

    public boolean isAvailable(PointInteger point) {
        return isAvailable(point.getX(), point.getY());
    }

    /**
     * Makes all the points in the path as WIRE
     * Also makes all points perpendicular to the path as AROUND_WIRE, for snakeLinePadding length
     * @param path the path of the wire, this function assumes all if two points are next to each other in the list,
     *             they are next to each other in the 2D space (ie no jumping to different points of the path)
     *             this condition is needed to be able to properly calculate the perpendicular of the path to set AROUND_WIRE correctly
     */
    public void makeWirePath(List<PointInteger> path) {
        PointInteger perpendicularOfPath = new PointInteger(0, 0);
        // iterate over all the points except the last one, because we can't calculate a direction of the path for the last point
        for (int i = 0; i < path.size() - 1; ++i) {
            PointInteger currentPoint = path.get(i);
            this.makeWire(currentPoint);
            perpendicularOfPath = currentPoint.getDirection(path.get(i + 1)); // this is the direction of the path
            // rotate to get perpendicular
            perpendicularOfPath.rotate();
            makeAroundWireInBothDirections(currentPoint.getX(), currentPoint.getY(), perpendicularOfPath);
        }
        // finish with the last point
        PointInteger lastPoint = path.get(path.size() - 1);
        this.makeWire(lastPoint);
        // there is no next point, we can't calculate a direction of path, use the perpendicular of the previous point
        // since there won't be a turn at the last point
        makeAroundWireInBothDirections(lastPoint.getX(), lastPoint.getY(), perpendicularOfPath);
    }

    public int getSnakelinePadding() {
        return snakelinePadding;
    }

    /**
     * Get all the points of the grid that belong to a certain snakeline
     * @param pathDirectionChangePoints the points representing the snakeline, that is only the points at right angles (we don't have the points between those right angles)
     * @return the list of points that is all the points of the snakeline, not just the minimal information needed to represent it
     */
    public static List<PointInteger> getPointsAlongSnakeline(List<Point> pathDirectionChangePoints) {
        List<PointInteger> allPoints = new ArrayList<>();
        //only go to size - 2 because the last line segment is between size - 2 and size - 1
        for (int i = 0; i < pathDirectionChangePoints.size() - 1; ++i) {
            PointInteger firstSegmentPoint = new PointInteger(pathDirectionChangePoints.get(i));
            PointInteger secondSegmentPoint = new PointInteger(pathDirectionChangePoints.get(i + 1));
            addSegmentPoints(allPoints, firstSegmentPoint, secondSegmentPoint);
        }
        // add the last point
        allPoints.add(new PointInteger(pathDirectionChangePoints.get(pathDirectionChangePoints.size() - 1)));
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
    private static void addSegmentPoints(List<PointInteger> allPoints, PointInteger firstSegmentPoint, PointInteger secondSegmentPoint) {
        PointInteger segmentDirection = firstSegmentPoint.getDirection(secondSegmentPoint);
        PointInteger normalizedSegmentDirection = getNormalizedSegmentDirection(segmentDirection);
        PointInteger currentPoint = new PointInteger(firstSegmentPoint);
        while (!currentPoint.equals(secondSegmentPoint)) {
            allPoints.add(currentPoint);
            currentPoint = currentPoint.getShiftedPoint(normalizedSegmentDirection);
        }
    }

    /**
     * Calculate the segment direction so that it only corresponds to UP, DOWN, LEFT or RIGHT directions, with a vector or magnitude 1
     * @param segmentDirection the direction we want to calculate the normalized direction of
     * @return a vector of magnitude 1, as long as segmentDirection is only in one of the cardinal direction (ie no diagonal movements)
     */
    private static PointInteger getNormalizedSegmentDirection(PointInteger segmentDirection) {
        int deltaX = segmentDirection.getX();
        int deltaY = segmentDirection.getY();
        // normalize x and y to only move by one each time, we get a vector like (0, 1), (0, -1), (1, 0) or (-1, 0)
        if (deltaX != 0) {
            // just get the direction of the X change, if it's negative or positive
            deltaX = deltaX > 0 ? 1 : -1;
        }
        if (deltaY != 0) {
            deltaY = deltaY > 0 ? 1 : -1;
        }
        return new PointInteger(deltaX, deltaY);
    }
}

