/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.coordinate.Point;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class WireConnection {

    private final AnchorPoint anchorPoint1;

    private final AnchorPoint anchorPoint2;

    private WireConnection(AnchorPoint anchorPoint1, AnchorPoint anchorPoint2) {
        this.anchorPoint1 = Objects.requireNonNull(anchorPoint1);
        this.anchorPoint2 = Objects.requireNonNull(anchorPoint2);
    }

    public static List<AnchorPoint> getAnchorPoints(ComponentLibrary componentLibrary, Node node) {
        return componentLibrary.getAnchorPoints(node.getComponentType())
                .stream()
                .map(anchorPoint -> node.isRotated() ? anchorPoint.createRotatedAnchorPoint(node.getRotationAngle()) : anchorPoint)
                .collect(Collectors.toList());
    }

    public static WireConnection searchBetterAnchorPoints(ComponentLibrary componentLibrary, Node node1, Node node2) {
        Objects.requireNonNull(node1);
        Objects.requireNonNull(node2);

        List<AnchorPoint> anchorPoints1 = node1 instanceof BusNode ? getBusNodeAnchorPoint((BusNode) node1, node2) : getAnchorPoints(componentLibrary, node1);
        List<AnchorPoint> anchorPoints2 = node2 instanceof BusNode ? getBusNodeAnchorPoint((BusNode) node2, node1) : getAnchorPoints(componentLibrary, node2);
        return searchBetterAnchorPoints(node1.getDiagramCoordinates(), node2.getDiagramCoordinates(), anchorPoints1, anchorPoints2);
    }

    private static List<AnchorPoint> getBusNodeAnchorPoint(BusNode busNode, Node otherNode) {
        Cell cell = otherNode.getCell();
        if (!(cell instanceof AbstractBusCell) // should not happen: otherNode linked to busNode should always be an AbstractBusCell
                || ((AbstractBusCell) cell).getDirection() == BusCell.Direction.UNDEFINED) { // should not happen
            return Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
        } else if (((AbstractBusCell) cell).getDirection() == BusCell.Direction.MIDDLE) {
            return Arrays.asList(
                    new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL),
                    new AnchorPoint(busNode.getPxWidth(), 0, AnchorOrientation.HORIZONTAL)
            );
        } else {
            return Collections.singletonList(
                    new AnchorPoint(otherNode.getX() - busNode.getX(), 0, AnchorOrientation.VERTICAL));
        }
    }

    public static WireConnection searchBetterAnchorPoints(ComponentLibrary componentLibrary, Node node1, Point coord2) {
        Objects.requireNonNull(componentLibrary);
        Objects.requireNonNull(node1);

        List<AnchorPoint> anchorPoints1 = getAnchorPoints(componentLibrary, node1);
        List<AnchorPoint> anchorPoints2 = Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
        return searchBetterAnchorPoints(node1.getDiagramCoordinates(), coord2, anchorPoints1, anchorPoints2);
    }

    private static WireConnection searchBetterAnchorPoints(Point coord1, Point coord2,
                                                           List<AnchorPoint> anchorPoints1,
                                                           List<AnchorPoint> anchorPoints2) {
        AnchorPoint betterAnchorPoint1 = anchorPoints1.get(0);
        AnchorPoint betterAnchorPoint2 = anchorPoints2.get(0);

        double currentDistance = coord1.getShiftedPoint(betterAnchorPoint1).distanceSquare(
            coord2.getShiftedPoint(betterAnchorPoint2));
        for (AnchorPoint anchorPoint1 : anchorPoints1) {
            for (AnchorPoint anchorPoint2 : anchorPoints2) {
                double distance = coord1.getShiftedPoint(anchorPoint1).distanceSquare(
                    coord2.getShiftedPoint(anchorPoint2));
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
    public List<Point> calculatePolylinePoints(Node node1, Node node2, boolean straight) {

        Point point1 = node1.getDiagramCoordinates().getShiftedPoint(getAnchorPoint1());
        Point point2 = node2.getDiagramCoordinates().getShiftedPoint(getAnchorPoint2());
        if (point1.getX() == point2.getX() && point1.getY() == point2.getY()) {
            return Collections.emptyList();
        }

        List<Point> pol = new ArrayList<>();
        pol.add(point1);
        if (!straight && (point1.getX() != point2.getX() && point1.getY() != point2.getY())) {
            addMiddlePoints(point1, point2, pol);
        }
        pol.add(point2);
        return pol;
    }

    private void addMiddlePoints(Point point1, Point point2, List<Point> pol) {
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();

        switch (anchorPoint1.getOrientation()) {
            case VERTICAL:
                if (anchorPoint2.getOrientation() == AnchorOrientation.VERTICAL) {
                    double mid = (y1 + y2) / 2;
                    pol.addAll(Point.createPointsList(x1, mid, x2, mid));
                } else {
                    pol.add(new Point(x1, y2));
                }
                break;
            case HORIZONTAL:
                if (anchorPoint2.getOrientation() == AnchorOrientation.HORIZONTAL) {
                    double mid = (x1 + x2) / 2;
                    pol.addAll(Point.createPointsList(mid, y1, mid, y2));
                } else {
                    pol.add(new Point(x2, y1));
                }
                break;
            case NONE:
                // Case none-none is not handled, it never happens (even if it happen it will execute another case)
                if (anchorPoint2.getOrientation() == AnchorOrientation.HORIZONTAL) {
                    pol.add(new Point(x1, y2));
                } else {
                    pol.add(new Point(x2, y1));
                }
                break;
            default:
                break;
        }
    }
}
