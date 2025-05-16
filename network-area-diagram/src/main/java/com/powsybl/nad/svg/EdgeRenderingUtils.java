/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.metadata.DiagramMetadata;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public final class EdgeRenderingUtils {

    private EdgeRenderingUtils() {
    }

    public static EdgePoints getEdgePoints(Point edgeStart1, Point edgeStart2, List<Point> bentLinesPoints) {
        Objects.requireNonNull(edgeStart1);
        Objects.requireNonNull(edgeStart2);
        Objects.requireNonNull(bentLinesPoints);
        List<Point> points = new ArrayList<>(bentLinesPoints);
        points.add(0, edgeStart1);
        points.add(edgeStart2);
        double distance = IntStream.range(0, points.size() - 1)
                                   .mapToDouble(i -> points.get(i).distance(points.get(i + 1)))
                                   .sum();
        List<Point> points1 = new ArrayList<>(Arrays.asList(edgeStart1));
        List<Point> points2 = new ArrayList<>();
        double partialDistance = 0;
        boolean middleAdded = false;
        for (int i = 0; i < points.size() - 1; i++) {
            Point point = points.get(i);
            Point nextPoint = points.get(i + 1);
            partialDistance += point.distance(nextPoint);
            if (partialDistance < distance / 2) {
                points1.add(nextPoint);
            } else {
                if (!middleAdded) {
                    Point edgeMiddle = nextPoint.atDistance(partialDistance - distance / 2, point);
                    points1.add(edgeMiddle);
                    points2.add(edgeMiddle);
                    middleAdded = true;
                }
                points2.add(nextPoint);
            }
        }
        return new EdgePoints(points1, Lists.reverse(points2));
    }

    public static Map<String, List<Point>> getBentLinesPoints(InputStream metadataIs) {
        Objects.requireNonNull(metadataIs);
        return DiagramMetadata.parseJson(metadataIs).getBentLinesPoints();
    }

    public static Map<String, List<Point>> getBentLinesPoints(Path metadataFile) {
        Objects.requireNonNull(metadataFile);
        return DiagramMetadata.parseJson(metadataFile).getBentLinesPoints();
    }

    public static Map<String, List<Point>> getBentLinesPoints(Reader metadataReader) {
        Objects.requireNonNull(metadataReader);
        return DiagramMetadata.parseJson(metadataReader).getBentLinesPoints();
    }
}
