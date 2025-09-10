/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.layout.pathfinding.geometry.Headings;
import com.powsybl.sld.layout.pathfinding.geometry.PathNode;
import com.powsybl.sld.layout.pathfinding.geometry.PointHeading;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.PointInteger;

import java.util.*;

/**
 * Based on "Rectangle expansion A* pathfinding for grid maps", Zhang An, Li Chong , Bi Wenhao, Chinese Journal of Aeronautics, (2016), 29(5): 1385–1396
 * A few modifications have been made to adapt to our problem:
 * Only 4 directions instead of 8
 * Nodes are all points of the border instead of segments, since our problem is not turn symmetric (and thus it sometimes matter which node we take before to avoid turns later)
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class RectangleExpansionAStarPathFinder implements PathFinder {
    /**
     * The cost of turning, ie making a 90° turn
     */
    private static final int TURNING_COST = 100;
    /**
     * The cost of being too close to another wire
     */
    private static final int AROUND_WIRE_COST = 10;
    /**
     * The cost of having a wire cross another wire
     */
    private static final int CROSSING_COST = 200;

    /**
     * The grid that keeps track of which areas are available, using a wire or not
     */
    private AvailabilityGrid availabilityGrid;

    @Override
    public List<Point> findBestPath(AvailabilityGrid availabilityGrid, Point start, Point goal) {
        return List.of();
    }

    /**
     * Create the starting rectangle that contains the startingPoint. This is the biggest rectangle possible that contains the starting point without a change in grid state
     * @param startingPoint the point to start from
     * @param firstExpansionHeading the first direction to expand into
     * @return corners of the starting rectangle, in trigonometric order
     */
    private PointInteger[] createStartingRectangle(PointInteger startingPoint, PointInteger firstExpansionHeading) {
        PointInteger oppositeHeading = Headings.getOppositeHeading(firstExpansionHeading);
        final byte startingState = availabilityGrid.getState(startingPoint);
        PointInteger firstHeadingPoint = startingPoint.getShiftedPoint(firstExpansionHeading);
        // find the longest segment traversing the startingPoint with the same state as the startingPoint, in both the expansion heading and the opposite heading
        while (availabilityGrid.isAvailable(firstHeadingPoint) && availabilityGrid.getState(firstHeadingPoint) == startingState) {
            firstHeadingPoint = firstHeadingPoint.getShiftedPoint(firstExpansionHeading);
        }
        PointInteger firstOppositeHeadingPoint = startingPoint.getShiftedPoint(oppositeHeading);
        while (availabilityGrid.isAvailable(firstOppositeHeadingPoint) && availabilityGrid.getState(firstOppositeHeadingPoint) == startingState) {
            firstOppositeHeadingPoint = firstOppositeHeadingPoint.getShiftedPoint(firstExpansionHeading);
        }
        // make rectangles in both orthogonal direction, with one of their edge being the two extremities of the segment containing the starting point
        PointInteger orthogonalExpansionDirection = Headings.rotateLeft(firstExpansionHeading);
        PointInteger[] rectangleOrthogonal = expandRectangle(firstHeadingPoint, firstOppositeHeadingPoint, orthogonalExpansionDirection);
        PointInteger oppositeOrthogonalDirection = Headings.rotateRight(firstExpansionHeading);
        PointInteger[] oppositeRectangleOrthogonal = expandRectangle(firstHeadingPoint, firstOppositeHeadingPoint, oppositeOrthogonalDirection);
        // now merge the two rectangles to get the biggest rectangle that includes both orthogonal directions
        // both rectangles are in trigonometric order, and both rectangles start with the points of the first segment, but in opposite order
        // 2 ----- 1  0 ----- 3
        // |       |  |       |
        // 3 ----- 0  1 ----- 2
        // the 0 and 1 are actually the same points but order changes depending on the rectangle
        return makeRectangleTrigonometricOrder(new PointInteger[] {
            rectangleOrthogonal[2],
            rectangleOrthogonal[3],
            oppositeRectangleOrthogonal[2],
            oppositeRectangleOrthogonal[3]
        });
    }

    /**
     * Expand a rectangle in the direction of heading, starting from the side of the rectangle represented by [firstSegmentStart, firstSegmentEnd], both end included
     * @param firstSegmentStart one corner of the rectangle
     * @param firstSegmentEnd another corner of the rectangle, which must share a segment with firstSegmentStart
     * @param heading the direction in which to expand
     * @return the 4 corners of a rectangle, with the first two being the firstSegmentStart and firstSegmentEnd. All points inside this rectangle are inside the availability grid and all have the same grid state
     */
    private PointInteger[] expandRectangle(PointInteger firstSegmentStart, PointInteger firstSegmentEnd, PointInteger heading) {
        PointInteger[] rectangleCorners = new PointInteger[4];
        // write corners in the trigonometric order
        rectangleCorners[0] = firstSegmentStart;
        rectangleCorners[1] = firstSegmentEnd;
        rectangleCorners[2] = firstSegmentEnd;
        rectangleCorners[3] = firstSegmentStart;
        PointInteger currentSegmentStart = firstSegmentStart;
        //TODO this is not correct, we need the normalized direction
        PointInteger segmentDirection = firstSegmentStart.getDirection(firstSegmentEnd);
        // need to shift by 1 to be able to include the corner in the loop after, otherwise we stop 1 too early
        PointInteger currentSegmentEnd = firstSegmentEnd.getShiftedPoint(segmentDirection);
        final byte startingState = availabilityGrid.getState(firstSegmentStart);
        // we'll eventually get out of the loop because the isInBounds condition will get false at one point
        while (true) {
            currentSegmentStart = currentSegmentStart.getShiftedPoint(heading);
            currentSegmentEnd = currentSegmentEnd.getShiftedPoint(heading);
            PointInteger segmentPoint = currentSegmentStart;
            while (!segmentPoint.equals(currentSegmentEnd)) {
                if (!availabilityGrid.isInBounds(segmentPoint) || availabilityGrid.getState(segmentPoint) != startingState) {
                    return makeRectangleTrigonometricOrder(rectangleCorners);
                }
                segmentPoint = segmentPoint.getShiftedPoint(segmentDirection);
            }
            rectangleCorners[2] = currentSegmentEnd;
            rectangleCorners[3] = currentSegmentStart;
        }
    }

    /**
     * Expand the rectangle on the last 3 sides of the rectangle by 1 towards the exterior of the rectangle
     * and return all the points that are not "not available" (meaning, available, wire, around wire)
     * @param rectangleCorners the corners of the rectangle to expand, in trigonometric order
     * @return points around the edges of the rectangle that are available, wire or around wire, except for the points on the first side of the rectangle
     */
    private List<PointInteger> generateSuccessorPoints(PointInteger[] rectangleCorners, PointInteger originalRectangleHeading) {
        List<PointInteger> successorPoints = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            int secondCornerIndex = (i + 1) % 4;
            int thirdCornerIndex = (i + 2) % 4;
            // we could just get the direction by using the two corners of the previous segment
            // but this is more robust if the rectangle is of height 1 or width 1 (in which case we couldn't determine expansion direction based on the corners as they would overlap)
            PointInteger expansionDirection = switch (i) {
                case 0 -> Headings.rotateRight(originalRectangleHeading);
                case 2 -> Headings.rotateLeft(originalRectangleHeading);
                default -> originalRectangleHeading;
            };
            successorPointsOnOneSide(rectangleCorners[secondCornerIndex], rectangleCorners[thirdCornerIndex], expansionDirection, successorPoints);
        }

        return successorPoints;
    }

    /**
     * Get all the points directly adjacent to the segment defined by [firstCorner, secondCorner], in the expansionDirection, that are not "not available"
     * @param firstCorner corner of the segment
     * @param secondCorner the other corner of the segment
     * @param expansionDirection the direction in which to search the points
     * @param successorPoints the list of points to fill
     */
    private void successorPointsOnOneSide(PointInteger firstCorner, PointInteger secondCorner, PointInteger expansionDirection, List<PointInteger> successorPoints) {
        PointInteger currentPoint = firstCorner.getShiftedPoint(expansionDirection);
        PointInteger lastPoint = secondCorner.getShiftedPoint(expansionDirection);
        //TODO incorrect, we need the normalized direction
        PointInteger borderDirection = currentPoint.getDirection(lastPoint);
        // need to move the last point by 1 to go up to the last corner
        lastPoint = lastPoint.getShiftedPoint(borderDirection);

        while (!currentPoint.equals(lastPoint)) {
            // cannot directly use the isNotAvailable check, because in the case where it point is out of bounds, that means we would add it as a valid point, which is not what we want
            if (availabilityGrid.isInBounds(currentPoint) && availabilityGrid.getState(currentPoint) != AvailabilityGrid.NOT_AVAILABLE) {
                successorPoints.add(currentPoint);
            }
            currentPoint = currentPoint.getShiftedPoint(borderDirection);
        }
    }

    /**
     * Calculate the cost of moving from one point to another. We assume 3 things:<br>
     * - the state of all the points inside the rectangle is the same (no change in cost), the state of the endPoint can be different
     * - the cost of the startingPoint is already counted
     * - we make at most one turn to go from start to end (ie no going back)
     * @param startingPoint the point the movement starts at
     * @param endPoint the point the movement ends at
     * @return the cost of the move from start to end, cost of end point included, cost of start point excluded
     */
    private int costOfMovement(PointInteger startingPoint, PointInteger endPoint) {
        int zoneCost = getZoneCost(startingPoint);
        int pathCost = (startingPoint.manhattanDistance(endPoint) - 2) * zoneCost; // remove 2 for the first and last point
        // first point is already counted, and last point might be of different zone
        int endPointCost = getZoneCost(endPoint);
        pathCost += endPointCost;
        // if we are not in the same line or column, we need a turn
        if (!pointsAreAligned(startingPoint, endPoint)) {
            pathCost += TURNING_COST;
        }
        return pathCost;
    }

    private boolean pointsAreAligned(PointInteger first, PointInteger second) {
        return first.getX() == second.getX() || first.getY() == second.getY();
    }

    /**
     * Cost of the point depending on grid state
     * @param point the point we want the cost of
     * @return a value depending on the state of the grid at the coordinates of the given point
     */
    private int getZoneCost(PointInteger point) {
        return switch (availabilityGrid.getState(point)) {
            case AvailabilityGrid.AVAILABLE -> 1;
            case AvailabilityGrid.WIRE -> CROSSING_COST;
            case AvailabilityGrid.AROUND_WIRE -> AROUND_WIRE_COST;
            default -> 1000000; //unknown case, assume it's too expensive to go there
        };
    }

    /**
     * Determines whether the pointToCheck is inside the rectangles defined by the corners, borders included
     * @param corners corners defining the rectangle
     * @param pointToCheck the point we want to know if it's in the rectangle or not
     * @return true if the point is inside the rectangle or on the borders, false otherwise
     */
    private boolean pointIsInsideRectangle(PointInteger[] corners, PointInteger pointToCheck) {
        int xMax = Math.max(Math.max(Math.max(corners[0].getX(), corners[1].getX()), corners[2].getX()), corners[3].getX());
        int yMax = Math.max(Math.max(Math.max(corners[0].getY(), corners[1].getY()), corners[2].getY()), corners[3].getY());
        int xMin = Math.min(Math.min(Math.min(corners[0].getX(), corners[1].getX()), corners[2].getX()), corners[3].getX());
        int yMin = Math.min(Math.min(Math.min(corners[0].getY(), corners[1].getY()), corners[2].getY()), corners[3].getY());
        return xMin <= pointToCheck.getX() && pointToCheck.getX() <= xMax && yMin <= pointToCheck.getY() && pointToCheck.getY() <= yMax;
    }

    /**
     * Revert order of corners to make the order trigonometric, if it's not already trigonometric. The first segment stays the first segment
     * <pre>
     * {@code
     *      0 -- 1      1 -- 0
     *      |    |  --> |    |
     *      3 -- 2      2 -- 3
     * }
     * </pre>
     * to do that, we just exchange the first and second corner, as well as the third and fourth corner
     *
     * @param corners the corners of the rectangle, in continuous order
     * @return the corners in trigonometric order, the same array as the one that is passed (no new array is created)
     */
    private PointInteger[] makeRectangleTrigonometricOrder(PointInteger[] corners) {
        if (!isTrigonometricOrder(corners)) {
            PointInteger tempFirstCorner = corners[0];
            corners[0] = corners[1];
            corners[1] = tempFirstCorner;
            PointInteger tempThirdCorner = corners[2];
            corners[2] = corners[3];
            corners[3] = tempThirdCorner;
        }
        return corners;
    }

    /**
     * Checks if the corners of the rectangle are given in trigonometric order
     * @param corners the corners of the rectangle, in a continuous order (two consecutive corner describe a segment, no crossing)
     * @return true if the corners are in trigonometric order
     */
    private boolean isTrigonometricOrder(PointInteger[] corners) {
        // the order is trigonometric if we can get the direction of the second edge by rotating the direction of the first edge left
        //TODO incorrect, need normalized direction
        return Headings.rotateLeft(corners[0].getDirection(corners[1])).equals(corners[1].getDirection(corners[2]));
    }

    /**
     * Make a path between two points, where it requires at most 1 turn to go from one to the other, and the state of the grid is uniform in the area
     * @param start the point from which to start the path
     * @param end the point at which the path ends
     * @param startHeading the direction of the start of the path. This is useful if we need to make a turn, to know in which direction we travel first
     *                     if we didn't have this information, we could make too many turns for the overall path (meaning each path fragment would not flow well into the next one,
     *                     we need to keep the continuity of the direction as much as possible between fragments)<br>
     *                     Note that this heading can be null, in which case we will just find a turning point by using the x of the start and the y of the end
     * @return the list of points that constitute the path, there are 2 points (start and end) if the points are aligned, three if a turn is required, in which case
     * we have (start, turn, end), with start -> turn with the same direction as startHeading
     */
    private List<PointInteger> makePathFragment(PointInteger start, PointInteger end, PointInteger startHeading) {
        if (pointsAreAligned(start, end)) {
            return List.of(start, end);
        } else {
            PointInteger turnPoint = start;
            if (startHeading != null) {
                while (!pointsAreAligned(turnPoint, end)) {
                    turnPoint = turnPoint.getShiftedPoint(startHeading);
                }
            } else {
                turnPoint = new PointInteger(start.getX(), end.getY());
            }
            return List.of(start, turnPoint, end);
        }
    }
}

