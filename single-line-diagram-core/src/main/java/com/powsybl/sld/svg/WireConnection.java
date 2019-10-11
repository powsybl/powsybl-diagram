/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.AnchorPointProvider;
import com.powsybl.sld.model.BaseNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class WireConnection {

    private AnchorPoint anchorPoint1;

    private AnchorPoint anchorPoint2;

    WireConnection(AnchorPoint anchorPoint1, AnchorPoint anchorPoint2) {
        this.anchorPoint1 = Objects.requireNonNull(anchorPoint1);
        this.anchorPoint2 = Objects.requireNonNull(anchorPoint2);
    }

    /**
     * Calculates the distance between two points.
     *
     * @param x1 x1
     * @param y1 y1
     * @param x2 x2
     * @param y2 y2
     * @return distance
     */
    private static double calculateDistancePoint(double x1, double y1, double x2, double y2) {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    }

    private static List<AnchorPoint> getAnchorPoints(AnchorPointProvider anchorPointProvider, BaseNode node) {
        return anchorPointProvider.getAnchorPoints(node.getComponentType(), node.getId())
                .stream()
                .map(anchorPoint -> node.isRotated() ? anchorPoint.rotate(node.getRotationAngle()) : anchorPoint)
                .collect(Collectors.toList());
    }

    public static WireConnection searchBetterAnchorPoints(AnchorPointProvider anchorPointProvider,
                                                          BaseNode node1,
                                                          BaseNode node2) {
        Objects.requireNonNull(anchorPointProvider);
        Objects.requireNonNull(node1);
        Objects.requireNonNull(node2);

        List<AnchorPoint> anchorPoints1 = getAnchorPoints(anchorPointProvider, node1);
        List<AnchorPoint> anchorPoints2 = getAnchorPoints(anchorPointProvider, node2);
        AnchorPoint betterAnchorPoint1 = anchorPoints1.get(0);
        AnchorPoint betterAnchorPoint2 = anchorPoints2.get(0);

        double currentDistance = calculateDistancePoint(node1.getX() + betterAnchorPoint1.getX(),
                                                        node1.getY() + betterAnchorPoint1.getY(),
                                                        node2.getX() + betterAnchorPoint2.getX(),
                                                        node2.getY() + betterAnchorPoint2.getY());

        for (AnchorPoint anchorPoint1 : anchorPoints1) {
            for (AnchorPoint anchorPoint2 : anchorPoints2) {
                double distance = calculateDistancePoint(node1.getX() + anchorPoint1.getX(),
                                                         node1.getY() + anchorPoint1.getY(),
                                                         node2.getX() + anchorPoint2.getX(),
                                                         node2.getY() + anchorPoint2.getY());
                if (distance < currentDistance) {
                    betterAnchorPoint1 = anchorPoint1;
                    betterAnchorPoint2 = anchorPoint2;
                    currentDistance = distance;
                }
            }
        }

        return new WireConnection(betterAnchorPoint1, betterAnchorPoint2);
    }

    public AnchorPoint getAnchorPoint1() {
        return anchorPoint1;
    }

    public AnchorPoint getAnchorPoint2() {
        return anchorPoint2;
    }

    /*
     * Calculating the polyline points for the voltageLevel graph edge
     */
    public List<Double> calculatePolylinePoints(BaseNode node1, BaseNode node2, boolean straight) {
        double x1 = node1.getX() + getAnchorPoint1().getX();
        double y1 = node1.getY() + getAnchorPoint1().getY();
        double x2 = node2.getX() + getAnchorPoint2().getX();
        double y2 = node2.getY() + getAnchorPoint2().getY();

        if (straight || (x1 == x2 || y1 == y2)) {
            return Arrays.asList(x1, y1, x2, y2);
        }
        List<Double> pol = new ArrayList<>();
        switch (anchorPoint1.getOrientation()) {
            case VERTICAL:
                if (anchorPoint2.getOrientation() == AnchorOrientation.VERTICAL) {
                    double mid = (y1 + y2) / 2;
                    pol.addAll(Arrays.asList(x1, y1, x1, mid, x2, mid, x2, y2));
                } else {
                    pol.addAll(Arrays.asList(x1, y1, x1, y2, x2, y2));
                }
                break;
            case HORIZONTAL:
                if (anchorPoint2.getOrientation() == AnchorOrientation.HORIZONTAL) {
                    double mid = (x1 + x2) / 2;
                    pol.addAll(Arrays.asList(x1, y1, mid, y1, mid, y2, x2, y2));
                } else {
                    pol.addAll(Arrays.asList(x1, y1, x2, y1, x2, y2));
                }
                break;
            case NONE:
                // Case none-none is not handled, it never happens (even if it happen it will execute another case)
                if (anchorPoint2.getOrientation() == AnchorOrientation.HORIZONTAL) {
                    pol.addAll(Arrays.asList(x1, y1, x1, y2, x2, y2));
                } else {
                    pol.addAll(Arrays.asList(x1, y1, x2, y1, x2, y2));
                }
                break;
            default:
                break;
        }
        return pol;
    }
}
