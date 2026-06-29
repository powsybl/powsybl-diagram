/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding.geometry;

import com.powsybl.sld.model.coordinate.IntPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class OrthogonalPolygon {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrthogonalPolygon.class);

    private final List<IntPoint> corners;
    private final int cornerNumber;

    /**
     * Build an orthogonal polygon from a list of points (at least 4 points). The points must have the following properties:
     * <ul>
     *     <li>X-aligned or Y-aligned with the previous and following point</li>
     *     <li>distinct</li>
     *     <li>represent only segment corners</li>
     * </ul>
     * Regarding this last requirement, points that do not represent a corner will be ignored.
     * An exception will be thrown if:
     * <ul>
     *     <li>any two points are not X-aligned or Y-aligned</li>
     *     <li>there are less than 4 points (either because less than 4 were provided, or some points were ignored because not corners, thus resulting in less than 4 points)</li>
     * </ul>
     * @param tentativeCorners the corners of the polygon (some of its points might be ignored, read above for conditions)
     */
    public OrthogonalPolygon(List<IntPoint> tentativeCorners) {
        if (tentativeCorners.size() <= 3) {
            throw new IllegalArgumentException("An orthogonal polygon needs at least 4 corners");
        }
        if (checkCornerListIsAdjacentAndDistinct(tentativeCorners)) {
            //ensure no point at the middle of a segment
            this.corners = keepCornersOnly(tentativeCorners);
            this.cornerNumber = this.corners.size();
            if (cornerNumber <= 3) {
                throw new IllegalArgumentException("An orthogonal polygon needs at least 4 corners, some provided points were not corners and ignoring those doesn't leave enough corners for the polygon");
            }
        } else {
            throw new IllegalArgumentException("All corners of the orthogonal polygon must be x-aligned or y-aligned and distinct with the previous and next corner of the list (X-aligned or Y-aligned)");
        }
    }

    private boolean checkCornerListIsAdjacentAndDistinct(List<IntPoint> corners) {
        //cannot use cornerNumber, not init yet
        int n = corners.size();
        for (int i = 0; i < n; ++i) {
            IntPoint segmentStart = corners.get(i);
            IntPoint segmentEnd = corners.get((i + 1) % n);
            if (segmentStart.x() != segmentEnd.x() && segmentStart.y() != segmentEnd.y()) {
                //corners are not aligned on either the x or y-axis
                return false;
            }
            if (segmentStart.x() == segmentEnd.x() && segmentStart.y() == segmentEnd.y()) {
                //corners are identical
                return false;
            }
        }
        return true;
    }

    /**
     * Keeps only the points of the point list that result in a direction change
     * @param pointList points of the polygon segment that are adjacent and non-repeating
     * @return a list where no point is inside a segment, only corner points are kept
     */
    private List<IntPoint> keepCornersOnly(List<IntPoint> pointList) {
        List<IntPoint> cornersOnly = new ArrayList<>(pointList.size());
        IntPoint first = pointList.getFirst();
        cornersOnly.add(first);
        IntPoint second = pointList.get(1);
        int previousDx = IntPoint.dx(first, second);
        int previousDy = IntPoint.dy(first, second);
        int n = pointList.size();
        //go around the shape, checking that each corners corresponds to a change in the direction of the segment
        for (int i = 1; i < n; ++i) {
            IntPoint start = pointList.get(i);
            IntPoint end = pointList.get((i + 1) % n);
            int dx = IntPoint.dx(start, end);
            int dy = IntPoint.dy(start, end);
            if (dx != previousDx || dy != previousDy) {
                cornersOnly.add(start);
                previousDx = dx;
                previousDy = dy;
            } else {
                LOGGER.warn("The point ({}, {}) was not a corner, it has been ignored", start.x(), start.y());
            }
        }
        return cornersOnly;
    }

    /**
     * Checks if a point of coordinates <code>(x, y)</code> is inside the polygon (edges included)
     * @return true if the point <code>(x, y)</code> is inside the polygon (edges included), false otherwise
     */
    public boolean contains(int x, int y) {
        if (isOnEdges(x, y)) {
            return true;
        }
        //ray-cast algorithm check, only need to check vertical edges since the polygon is orthogonal, and we already checked to not be on the edges
        boolean inside = false;
        for (int i = 0; i < cornerNumber; ++i) {
            IntPoint segmentStart = corners.get(i);
            IntPoint segmentEnd = corners.get((i + 1) % cornerNumber);
            if (segmentStart.y() != segmentEnd.y() && segmentStart.x() > x && isNumberBetweenUnorderedBounds(y, segmentStart.y(), segmentEnd.y())) {
                //vertical segment in the positive x direction that would cross a ray
                inside = !inside;
            }
        }
        return inside;
    }

    /**
     * Check if a segment starting from <code>start</code> and ending at <code>end</code> crosses the polygon (edges included).
     * <code>start</code> and <code>end</code> must be aligned along one axis and not be at the same position
     * @param start the start of the segment
     * @param end the end of the segment
     * @return true if any point of the segment [start, end] is inside the polygon (edges included)
     */
    public boolean segmentCrossesPolygon(IntPoint start, IntPoint end) {
        //true only if both points on the same position or if points are not axis aligned
        if ((start.x() == end.x()) == (start.y() == end.y())) {
            throw new IllegalArgumentException("start and end points must have distinct coordinates and be aligned on an axis");
        }
        if (isOnEdges(start) || isOnEdges(end)) {
            return true;
        }

        for (int i = 0; i < cornerNumber; ++i) {
            IntPoint polygonSegmentStart = corners.get(i);
            IntPoint polygonSegmentEnd = corners.get((i + 1) % cornerNumber);
            if (segmentCrossesSegment(start, end, polygonSegmentStart, polygonSegmentEnd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if at least one point is in common between segment1 and segment2, false otherwise
     */
    private boolean segmentCrossesSegment(IntPoint segment1Start, IntPoint segment1End, IntPoint segment2Start, IntPoint segment2End) {
        int dx1 = IntPoint.dx(segment1Start, segment1End);
        int dy1 = IntPoint.dy(segment1Start, segment1End);
        int dx2 = IntPoint.dx(segment2Start, segment2End);
        int dy2 = IntPoint.dy(segment2Start, segment2End);
        //since we are in a grid, either segment are parallel or perpendicular, calculate scalar product to know
        if (dx1 * dx2 + dy1 * dy2 != 0) {
            //parallel
            if (segment1Start.x() == segment2Start.x()) {
                //vertical
                return intervalsOverlap(segment1Start.y(), segment1End.y(), segment2Start.y(), segment2End.y());
            } else if (segment1Start.y() == segment2Start.y()) {
                //horizontal
                return intervalsOverlap(segment1Start.x(), segment1End.x(), segment2Start.x(), segment2End.x());
            } else {
                //parallel but not aligned
                return false;
            }
        } else {
            //perpendicular
            if (segment1Start.x() == segment1End.x()) {
                //vertical
                return isNumberBetweenUnorderedBounds(segment1Start.x(), segment2Start.x(), segment2End.x())
                    && isNumberBetweenUnorderedBounds(segment2Start.y(), segment1Start.y(), segment1End.y());
            } else if (segment1Start.y() == segment1End.y()) {
                //horizontal
                return isNumberBetweenUnorderedBounds(segment2Start.x(), segment1Start.x(), segment1End.x())
                    && isNumberBetweenUnorderedBounds(segment1Start.y(), segment2Start.y(), segment2End.y());
            } else {
                return false;
            }
        }
    }

    private boolean intervalsOverlap(int interval1Start, int interval1End, int interval2Start, int interval2End) {
        //forced to copy because of checkstyle not allowing assigning to a parameter
        int interval1StartCopy = interval1Start;
        int interval1EndCopy = interval1End;
        int interval2StartCopy = interval2Start;
        int interval2EndCopy = interval2End;
        // swap to ensure order
        // if we didn't, we could have a situation like this:
        // start1 -- end2 -- end1 -- start2
        // where neither start is inside the other interval
        if (interval1Start > interval1End) {
            interval1EndCopy = interval1Start;
            interval1StartCopy = interval1End;
        }
        if (interval2Start > interval2End) {
            interval2EndCopy = interval2Start;
            interval2StartCopy = interval2End;
        }
        //now that we know the order of intervals, we only need 2 checks
        return interval2StartCopy <= interval1StartCopy && interval1StartCopy <= interval2EndCopy
            || interval1StartCopy <= interval2StartCopy && interval2StartCopy <= interval1EndCopy;
    }

    private boolean isOnEdges(IntPoint point) {
        return isOnEdges(point.x(), point.y());
    }

    private boolean isOnEdges(int x, int y) {
        for (int i = 0; i < cornerNumber; ++i) {
            if (isPointOnSegment(x, y, corners.get(i), corners.get((i + 1) % cornerNumber))) {
                return true;
            }
        }
        return false;
    }

    private boolean isPointOnSegment(int x, int y,
        IntPoint segmentStart, IntPoint segmentEnd) {
        if (segmentStart.x() == segmentEnd.x()) {
            // vertical
            if (x != segmentStart.x()) {
                return false;
            }
            return isNumberBetweenUnorderedBounds(y, segmentStart.y(), segmentEnd.y());
        } else if (segmentStart.y() == segmentEnd.y()) {
            // horizontal
            if (y != segmentStart.y()) {
                return false;
            }
            return isNumberBetweenUnorderedBounds(x, segmentEnd.x(), segmentEnd.x());
        } else {
            //segment is not axis aligned
            return false;
        }
    }

    private boolean isNumberBetweenUnorderedBounds(int number, int bound1, int bound2) {
        int minBound = Math.min(bound1, bound2);
        int maxBound = Math.max(bound1, bound2);
        return number >= minBound && number <= maxBound;
    }
}
