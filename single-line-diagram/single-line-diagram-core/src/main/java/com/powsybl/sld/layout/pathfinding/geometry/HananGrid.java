/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding.geometry;

import com.powsybl.sld.model.coordinate.IntPoint;

import java.util.*;

/**
 * Used for fast path-finding over a pixel grid, this improves search speed.
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class HananGrid {
    private final NavigableSet<Integer> xCoordinates = new TreeSet<>();
    private final NavigableSet<Integer> yCoordinates = new TreeSet<>();
    private final int width;
    private final int height;
    private final List<OrthogonalPolygon> obstacles;

    public HananGrid(int width, int height) {
        this(width, height, new ArrayList<>());
    }

    public HananGrid(int width, int height, List<OrthogonalPolygon> obstacles) {
        this.width = width;
        this.height = height;
        this.obstacles = obstacles;
    }

    public enum LineDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public static final class HananPoint {
        private final IntPoint position;
        //used to know if a crossing of a line is perpendicular, or in the same direction
        private LineDirection lineDirection = null;

        public HananPoint(int x, int y) {
            this.position = new IntPoint(x, y);
        }

        public HananPoint(IntPoint position) {
            this.position = Objects.requireNonNull(position);
        }

        public IntPoint getPosition() {
            return position;
        }

        public LineDirection getLineDirection() {
            return lineDirection;
        }

        public void setLineDirection(LineDirection lineDirection) {
            this.lineDirection = lineDirection;
        }
    }

    public void addVerticalLine(int x) {
        if (x >= 0 && x < width) {
            xCoordinates.add(x);
        } else {
            throw new IllegalArgumentException(String.format("The provided x coordinate was %d but it must be >= 0 and strictly lower than the width %d", x, width));
        }
    }

    public void addHorizontalLine(int y) {
        if (y >= 0 && y < height) {
            yCoordinates.add(y);
        } else {
            throw new IllegalArgumentException(String.format("The provided y coordinate was %d but it must be >= 0 and strictly lower than the height %d", y, height));
        }
    }

    public void addObstacles(Collection<OrthogonalPolygon> newObstacles) {
        this.obstacles.addAll(newObstacles);
    }

    private boolean isVertexBlocked(IntPoint point) {
        for (OrthogonalPolygon obstacle : obstacles) {
            if (obstacle.contains(point.x(), point.y())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSegmentBlocked(IntPoint segmentStart, IntPoint segmentEnd) {
        for (OrthogonalPolygon obstacle : obstacles) {
            if (obstacle.segmentCrossesPolygon(segmentStart, segmentEnd)) {
                return true;
            }
        }
        return false;
    }

    private HananPoint getMove(HananPoint point, Integer x, Integer y) {
        if (x == null || y == null) {
            return null;
        }
        IntPoint segmentEnd = new IntPoint(x, y);
        if (isVertexBlocked(segmentEnd) || isSegmentBlocked(point.getPosition(), segmentEnd)) {
            return null;
        }
        return new HananPoint(segmentEnd);
    }

    public HananPoint up(HananPoint point) {
        //y-axis is down oriented, up is a smaller y
        return getMove(point, point.getPosition().x(), yCoordinates.lower(point.getPosition().y()));
    }

    public HananPoint down(HananPoint point) {
        //y-axis is down oriented, down is a bigger y
        return getMove(point, point.getPosition().x(), yCoordinates.higher(point.getPosition().y()));
    }

    public HananPoint left(HananPoint point) {
        return getMove(point, xCoordinates.lower(point.getPosition().x()), point.getPosition().y());
    }

    public HananPoint right(HananPoint point) {
        return getMove(point, xCoordinates.higher(point.getPosition().x()), point.getPosition().y());
    }
}
