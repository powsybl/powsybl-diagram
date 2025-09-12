/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding.geometry;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.PointInteger;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.layout.pathfinding.geometry.Headings.isRightAngle;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class PathUtils {

    private PathUtils() { }

    /**
     * Transform a path to only its changes in direction (since between those changes, it's only direct lines, so the information would be redundant otherwise)
     * @param notSmoothPath a path that has all its point
     * @return virtually the same path, but with only the changes in direction
     */
    public static List<PointInteger> makeSmoothPath(List<PointInteger> notSmoothPath) {
        List<PointInteger> smoothPath = new ArrayList<>();
        smoothPath.add(notSmoothPath.get(0));
        // start from the second point, stop at the point before the last
        for (int i = 1; i < notSmoothPath.size() - 1; ++i) {
            PointInteger currentPoint = notSmoothPath.get(i);
            PointInteger previousPoint = notSmoothPath.get(i - 1);
            PointInteger nextPoint = notSmoothPath.get(i + 1);
            if (isRightAngle(previousPoint, currentPoint, nextPoint)) {
                smoothPath.add(currentPoint);
            }
        }
        smoothPath.add(notSmoothPath.get(notSmoothPath.size() - 1)); //getLast is only after JDK 21, this code is written on JDK 17
        return smoothPath;
    }

    /**
     * Convert a {@code List<PointInteger>} to a {@code List<Point>}.
     * Yes that's it, nothing more
     * @param path the path using point integer
     * @return same path but using point
     */
    public static List<Point> convertToPointPath(List<PointInteger> path) {
        List<Point> pointPath = new ArrayList<>();
        for (PointInteger pointInteger : path) {
            pointPath.add(new Point(pointInteger));
        }
        return pointPath;
    }
}

