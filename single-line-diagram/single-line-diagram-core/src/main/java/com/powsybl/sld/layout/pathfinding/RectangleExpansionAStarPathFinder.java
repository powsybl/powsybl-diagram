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
import com.powsybl.sld.layout.pathfinding.geometry.PathUtils;
import com.powsybl.sld.layout.pathfinding.geometry.PointHeading;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.PointInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

/**
 * Based on "Rectangle expansion A* pathfinding for grid maps", Zhang An, Li Chong , Bi Wenhao, Chinese Journal of Aeronautics, (2016), 29(5): 1385–1396
 * A few modifications have been made to adapt to our problem:
 * Only 4 directions instead of 8
 * Nodes are all points of the border instead of segments, since our problem is not turn symmetric (and thus it sometimes matter which node we take before to avoid turns later)
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class RectangleExpansionAStarPathFinder implements PathFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RectangleExpansionAStarPathFinder.class);
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
        this.availabilityGrid = availabilityGrid;
        PointInteger startInteger = new PointInteger(start);
        PathNode startNode = new PathNode(new PointHeading(startInteger, null), null, 0, 0);
        PointInteger goalInteger = new PointInteger(goal);
        Map<PointHeading, PathNode> visitedNodes = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingDouble(PathNode::getTotalCost));
        // doesn't really matter which direction we go in
        PointInteger[] startingRectangle = createStartingRectangle(startInteger, Headings.UP);
        if (pointIsInsideRectangle(startingRectangle, goalInteger)) {
            List<PointInteger> path = makePathFragment(startInteger, goalInteger, null);
            availabilityGrid.makeWirePathFromIncompletePath(path);
            return PathUtils.convertToPointPath(path); // no need to make smooth, the path only has 2 or 3 points in that case
        }
        // main loop, generate all new nodes to start
        List<PointHeading> startingPointHeadings = generateSuccessorPointsStartingRectangle(startingRectangle);
        if (startingPointHeadings.isEmpty()) {
            LOGGER.error("Starting point {} is blocked on all 4 sides, no path can be found to the goal", startInteger);
            return List.of();
        }
        for (PointHeading pointHeading : startingPointHeadings) {
            int cost = costOfMovement(startInteger, pointHeading.getPoint());
            queue.add(new PathNode(
                    pointHeading,
                    startNode,
                    cost,
                    pointHeading.getPoint().manhattanDistance(goalInteger)
            ));
        }
        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            PointHeading currentPointHeading = current.getPointHeading();
            if (currentPointHeading.getPoint().equals(goalInteger)) {
                List<PointInteger> path = rebuildPath(current);
                // update the grid with the chosen path
                availabilityGrid.makeWirePathFromIncompletePath(path);
                // make the path smooth (ie only keeping the right angles) and convert to List<Point>
                return PathUtils.convertToPointPath(PathUtils.makeSmoothPath(path));
            }
            // check if we already visited this point with a path of lower cost
            PathNode currentAlreadyVisited = visitedNodes.get(currentPointHeading);
            if (currentAlreadyVisited != null && currentAlreadyVisited.getPathCost() < current.getPathCost()) {
                continue;
            }
            PointInteger[] newRectangle = expandRectangleFromPoint(
                    currentPointHeading.getPoint(),
                    Headings.rotateLeft(currentPointHeading.getHeading()),
                    currentPointHeading.getHeading(),
                    true
            );
            List<PointHeading> pointsToExpand = generateSuccessorPoints(newRectangle, currentPointHeading.getHeading());
            if (pointIsInsideRectangle(newRectangle, goalInteger)) {
                //not technically always correct, but we keep the same heading as the previous point.
                // Currently, the only way to access a goal point is through a long narrow corridor, so we know any point that reaches the goal will be aligned with it
                pointsToExpand.add(new PointHeading(goalInteger, currentPointHeading.getHeading()));
            }
            for (PointHeading newPoint : pointsToExpand) {
                int cost = costOfMovement(currentPointHeading.getPoint(), newPoint.getPoint());
                PathNode pointAlreadyVisited = visitedNodes.get(newPoint);
                PathNode pathToNewPoint = new PathNode(
                        newPoint,
                        current,
                        cost,
                        cost
                );
                if (pointAlreadyVisited == null || cost < pointAlreadyVisited.getPathCost()) {
                    queue.add(pathToNewPoint);
                    visitedNodes.put(newPoint, pathToNewPoint);
                }
            }
        }
        return List.of();
    }

    /**
     * Create the starting rectangle that contains the startingPoint. This is the biggest rectangle possible that contains the starting point without a change in grid state.
     * Contrary to expandRectangleFromPoint, this expands in all 4 directions instead of 3
     * @param startingPoint the point to start from
     * @param firstExpansionHeading the first direction to expand into
     * @return corners of the starting rectangle, in trigonometric order
     */
    private PointInteger[] createStartingRectangle(PointInteger startingPoint, PointInteger firstExpansionHeading) {
        PointInteger orthogonalHeading = Headings.rotateLeft(firstExpansionHeading);
        PointInteger[] firstRectangle = expandRectangleFromPoint(startingPoint, firstExpansionHeading, orthogonalHeading, false);
        PointInteger oppositeOrthogonalHeading = Headings.getOppositeHeading(orthogonalHeading);
        PointInteger[] secondRectangle = expandRectangleFromPoint(startingPoint, firstExpansionHeading, oppositeOrthogonalHeading, false);
        return mergeRectangles(firstRectangle, secondRectangle);
    }

    /**
     * Expand the starting rectangle on all 4 sides towards the exterior of the rectangle, and return
     * all the points that are not "not available" (meaning, available, wire, around wire). This is different from
     * the other generateSuccessorPoints() function, because at the start, we don't have a rectangle heading, all directions are available
     * @param corners the corners of the starting rectangle to expand, in trigonometric order
     * @return points around the edges of the rectangle that are available, wire or around wire, and the headings (the expansion direction of the rectangle on the given segment)
     */
    private List<PointHeading> generateSuccessorPointsStartingRectangle(PointInteger[] corners) {
        List<PointHeading> successorPoints = new ArrayList<>();
        // find one side of the rectangle without a width or height of 1 to get the normal vector for the next segment
        // if we have a width of 1, then two corners points will be on the same position, meaning we can't deduce the direction of the segment
        PointInteger segmentExpansionDirection = new PointInteger(0, 0);
        int i;
        for (i = 0; i < 3; ++i) {
            PointInteger segmentDirection = Headings.getNormalizedDirection(corners[i], corners[(i + 1) % 4]);
            if (!segmentDirection.equals(segmentExpansionDirection)) {
                segmentExpansionDirection = segmentDirection;
                break;
            }
        }
        if (segmentExpansionDirection.getX() == 0 && segmentExpansionDirection.getY() == 0) {
            //the starting rectangle is of width and height 1, so basically the starting rectangle is the starting point, which is blocked on all 4 directions
            LOGGER.error("The starting point seems to be blocked on all 4 directions");
            return List.of();
        }
        // we have the expansion direction for the segment starting at index i + 1
        // 1 -- 0 <-- the direction of the segment (0, 1) is the expansion direction for the segment (1, 2)
        // |    |
        // 2 -- 3
        ++i;
        for (int k = i; k < i + 4; ++k) {
            successorPointsOnOneSide(corners[k % 4], corners[(k + 1) % 4], segmentExpansionDirection, successorPoints);
            // rotate for the next segment
            segmentExpansionDirection = Headings.rotateLeft(segmentExpansionDirection);
        }
        return successorPoints;
    }

    /**
     * Expand a rectangle in the direction of heading, starting from the side of the rectangle represented by [firstSegmentStart, firstSegmentEnd], both end included
     * @param firstSegmentStart one corner of the rectangle
     * @param firstSegmentEnd another corner of the rectangle, which must share a segment with firstSegmentStart
     * @param heading the direction in which to expand
     * @return the 4 corners of a rectangle, with the first two being the firstSegmentStart and firstSegmentEnd. All points inside this rectangle are inside the availability grid and all have the same grid state
     */
    private PointInteger[] expandRectangleFromSegment(PointInteger firstSegmentStart, PointInteger firstSegmentEnd, PointInteger heading) {
        PointInteger[] rectangleCorners = new PointInteger[4];
        // write corners in the trigonometric order
        rectangleCorners[0] = firstSegmentStart;
        rectangleCorners[1] = firstSegmentEnd;
        rectangleCorners[2] = firstSegmentEnd;
        rectangleCorners[3] = firstSegmentStart;
        PointInteger currentSegmentStart = firstSegmentStart;
        PointInteger segmentDirection = Headings.getNormalizedDirection(firstSegmentStart, firstSegmentEnd);
        // need to shift by 1 to be able to include the corner in the loop after, otherwise we stop 1 too early
        PointInteger currentSegmentEnd = firstSegmentEnd;
        final byte startingState = availabilityGrid.getState(firstSegmentStart);
        // we'll eventually get out of the loop because the isInBounds condition will get false at one point
        while (true) {
            currentSegmentStart = currentSegmentStart.getShiftedPoint(heading);
            currentSegmentEnd = currentSegmentEnd.getShiftedPoint(heading);
            boolean arrivedAtSegmentEnd = false;
            PointInteger segmentPoint = currentSegmentStart;
            while (!arrivedAtSegmentEnd) {
                arrivedAtSegmentEnd = segmentPoint.equals(currentSegmentEnd);
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
     * Build a rectangle that expands in segmentExpansionDirection, -segmentExpansionDirection and forwardExpansionDirection, with the first segment containing the firstSegmentPoint.
     * Choose which direction to expand first in with the expandForwardFirst boolean. This expands in 3 directions.
     * @param firstSegmentPoint a point from which we start to expand forward or expand the segment. It will always belong to the segment defined by the two first points of the return array
     * @param segmentExpansionDirection start building the first segment by expanding in this direction and in the opposite direction
     * @param forwardExpansionDirection expand forward to find the segment opposite of the first segment
     * @param expandForwardFirst false if we expand the segment first, true if we expand forward first
     * @return the 4 corners of a rectangle, with the segment defined by the first two points containing the firstSegmentPoint.
     * All points inside this rectangle are inside the availability grid and all have the same grid state
     */
    private PointInteger[] expandRectangleFromPoint(PointInteger firstSegmentPoint, PointInteger segmentExpansionDirection, PointInteger forwardExpansionDirection, boolean expandForwardFirst) {
        final byte startingState = availabilityGrid.getState(firstSegmentPoint);
        if (expandForwardFirst) {
            PointInteger firstHeadingPoint = firstSegmentPoint.getShiftedPoint(segmentExpansionDirection);
            // find the longest segment traversing the startingPoint with the same state as the startingPoint, in both the expansion heading and the opposite heading
            while (availabilityGrid.isAvailable(firstHeadingPoint) && availabilityGrid.getState(firstHeadingPoint) == startingState) {
                firstHeadingPoint = firstHeadingPoint.getShiftedPoint(segmentExpansionDirection);
            }

            PointInteger oppositeSegmentHeading = Headings.getOppositeHeading(segmentExpansionDirection);
            firstHeadingPoint = firstHeadingPoint.getShiftedPoint(oppositeSegmentHeading); // we need to go back once because the last point we checked doesn't meet the conditions

            PointInteger firstOppositeHeadingPoint = firstSegmentPoint.getShiftedPoint(oppositeSegmentHeading);
            while (availabilityGrid.isAvailable(firstOppositeHeadingPoint) && availabilityGrid.getState(firstOppositeHeadingPoint) == startingState) {
                firstOppositeHeadingPoint = firstOppositeHeadingPoint.getShiftedPoint(oppositeSegmentHeading);
            }
            // go back since last point found doesn't meet conditions
            firstOppositeHeadingPoint = firstOppositeHeadingPoint.getShiftedPoint(segmentExpansionDirection);

            return expandRectangleFromSegment(firstHeadingPoint, firstOppositeHeadingPoint, forwardExpansionDirection);

        } else {
            PointInteger forwardHeadingPoint = firstSegmentPoint.getShiftedPoint(forwardExpansionDirection);
            while (availabilityGrid.isAvailable(forwardHeadingPoint) && availabilityGrid.getState(forwardHeadingPoint) == startingState) {
                forwardHeadingPoint = forwardHeadingPoint.getShiftedPoint(forwardExpansionDirection);
            }
            forwardHeadingPoint = forwardHeadingPoint.getShiftedPoint(Headings.getOppositeHeading(forwardExpansionDirection));
            PointInteger[] rectangleSegmentDirection = expandRectangleFromSegment(firstSegmentPoint, forwardHeadingPoint, segmentExpansionDirection);

            PointInteger oppositeSegmentHeading = Headings.getOppositeHeading(segmentExpansionDirection);
            PointInteger[] rectangleOppositeSegmentDirection = expandRectangleFromSegment(firstSegmentPoint, forwardHeadingPoint, oppositeSegmentHeading);

            return mergeRectangles(rectangleSegmentDirection, rectangleOppositeSegmentDirection);
        }
    }

    /**
     * Expand the rectangle on the last 3 sides of the rectangle by 1 towards the exterior of the rectangle
     * and return all the points + headings that are not "not available" (meaning, available, wire, around wire)
     * @param rectangleCorners the corners of the rectangle to expand, in trigonometric order
     * @param originalRectangleHeading the heading of the rectangle, this should be of magnitude 1
     * @return points around the edges of the rectangle that are available, wire or around wire,
     * with their heading (the expansion direction of the side of the rectangle),
     * except for the points on the first side of the rectangle
     */
    private List<PointHeading> generateSuccessorPoints(PointInteger[] rectangleCorners, PointInteger originalRectangleHeading) {
        List<PointHeading> successorPoints = new ArrayList<>();
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
     * @param expansionDirection the direction in which to search the points, this should be of magnitude 1
     * @param successorPoints the list of points + headings to fill
     */
    private void successorPointsOnOneSide(PointInteger firstCorner, PointInteger secondCorner, PointInteger expansionDirection, List<PointHeading> successorPoints) {
        PointInteger currentPoint = firstCorner.getShiftedPoint(expansionDirection);
        PointInteger lastPoint = secondCorner.getShiftedPoint(expansionDirection);
        PointInteger borderDirection = Headings.getNormalizedDirection(currentPoint, lastPoint);
        // need to move the last point by 1 to go up to the last corner
        lastPoint = lastPoint.getShiftedPoint(borderDirection);

        while (!currentPoint.equals(lastPoint)) {
            // cannot directly use the isNotAvailable check, because in the case where it point is out of bounds, that means we would add it as a valid point, which is not what we want
            if (availabilityGrid.isInBounds(currentPoint) && availabilityGrid.getState(currentPoint) != AvailabilityGrid.NOT_AVAILABLE) {
                successorPoints.add(new PointHeading(currentPoint, expansionDirection));
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

    /**
     * Checks if two points are either on the same line or the same column
     * @param first one point to check
     * @param second the other point we want to check with
     * @return true if the two points share an axis, false otherwise
     */
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
        return Headings.rotateLeft(
                Headings.getNormalizedDirection(corners[0], corners[1])
        ).equals(
                Headings.getNormalizedDirection(corners[1], corners[2])
        );
    }

    /**
     * Determine if the area of a rectangle is 0
     * @param corners the corners of the rectangle
     * @return true if the area of the rectangle is 0, false otherwise
     */
    private boolean isRectangleOfSizeZero(PointInteger[] corners) {
        for (int i = 0; i < 3; ++i) {
            if (corners[i].equals(corners[(i + 1) % 4])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Merge two adjacent rectangles, they need to share an edge
     * @param firstRectangle the first rectangle to merge, has to be in trigonometric order
     * @param secondRectangle the second rectangle to merge, has to be in trigonometric order
     * @return a rectangle in trigonometric order, where the area is the same as the area of the first and second rectangle, and all corners belong to either the first or second rectangle
     */
    private PointInteger[] mergeRectangles(PointInteger[] firstRectangle, PointInteger[] secondRectangle) {
        // if either of the two rectangles is of size 0, to prevent issues with order of points return the other rectangle
        // we don't lose any information that way anyway
        if (isRectangleOfSizeZero(firstRectangle)) {
            return secondRectangle;
        }
        if (isRectangleOfSizeZero(secondRectangle)) {
            return firstRectangle;
        }
        // find the segment in common between the two rectangles
        int[] indexes = findCommonSegment(firstRectangle, secondRectangle);
        int firstRectangleIndex = indexes[0];
        int secondRectangleIndex = indexes[1];

        if (firstRectangleIndex == -1 || secondRectangleIndex == -1) {
            LOGGER.error("Could not merge the two rectangles {} and {}", firstRectangle, secondRectangle);
            return new PointInteger[0];
        }

        // merge the two rectangles
        // both rectangles are in trigonometric order
        // 3 ----- 2  0 ----- 3
        // |       |  |       |
        // 0 ----- 1  1 ----- 2
        // the find common segment would give the index 1 and 1, for the first segment the two points that are not on the common segment are 3 and 0
        // for the second rectangle, that's 2 and 3
        return makeRectangleTrigonometricOrder(new PointInteger[] {
                firstRectangle[(firstRectangleIndex + 2) % 4],
                firstRectangle[(firstRectangleIndex + 3) % 4],
                secondRectangle[(secondRectangleIndex + 1) % 4],
                secondRectangle[(secondRectangleIndex + 2) % 4]
        });
    }

    /**
     * Find the index of a corner for rectangles such as points are in the same position for the two indexes on the two rectangles,
     * and that the following point on the first rectangle is equal to the previous point on the second rectangle
     * 3 ----- 2  2 ----- 1
     * |       |  |       |
     * 0 ----- 1  3 ----- 0
     * so for example, if the first rectangle is on the left, then this function would return 1 and 3, since first.1 = second.3
     * and first.2 = second.2
     * @param firstRectangle the first of the two rectangle we search the common segment for
     * @param secondRectangle the second of the two rectangle we search the common segment for
     * @return indexes such as first.firstIndex = second.secondIndex and first.(firstIndex + 1) = second.(secondIndex - 1) (with modulo when needed)
     * return [-1, -1] when an index can't be found
     */
    private static int[] findCommonSegment(PointInteger[] firstRectangle, PointInteger[] secondRectangle) {
        int firstRectangleIndex;
        int secondRectangleIndex;

        for (firstRectangleIndex = 0; firstRectangleIndex < 3; ++firstRectangleIndex) {
            for (secondRectangleIndex = 0; secondRectangleIndex < 3; ++secondRectangleIndex) {
                if (
                        firstRectangle[firstRectangleIndex].equals(secondRectangle[secondRectangleIndex])
                        && firstRectangle[(firstRectangleIndex + 1) % 4].equals(secondRectangle[(secondRectangleIndex - 1 + 4) % 4])
                ) {
                    // both rectangles in trigonometric order, meaning the segment in common has a reverse index order
                    // 2 ----- 1  0 ----- 3
                    // |       |  |       |
                    // 3 ----- 0  1 ----- 2
                    // which is why we use first + 1 but use second - 1; for the second we also do a + 4 because we want to keep the index positive
                    return new int[] {firstRectangleIndex, secondRectangleIndex};
                }
            }
        }
        // no index found
        return new int[] {-1, -1};
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

    private List<PointInteger> rebuildPath(PathNode lastNode) {
        PathNode current = lastNode;
        List<PointInteger> path = new ArrayList<>();
        while (current.getParentNode() != null) {
            List<PointInteger> pathFragment = makePathFragment(
                    current.getPointHeading().getPoint(),
                    current.getParentNode().getPointHeading().getPoint(),
                    Headings.getOppositeHeading(current.getPointHeading().getHeading()) // need the opposite since we are doing the path in reverse
                    );
            path.addAll(pathFragment);
            current = current.getParentNode();
        }
        Collections.reverse(path);
        return path;
    }
}

