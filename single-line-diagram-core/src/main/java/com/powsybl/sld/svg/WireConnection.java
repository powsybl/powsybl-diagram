/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.Component;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class WireConnection {

    private final AnchorPoint anchorPoint1;

    private final AnchorPoint anchorPoint2;

    private WireConnection(AnchorPoint anchorPoint1, AnchorPoint anchorPoint2) {
        this.anchorPoint1 = Objects.requireNonNull(anchorPoint1);
        this.anchorPoint2 = Objects.requireNonNull(anchorPoint2);
    }

    public static List<AnchorPoint> getAnchorPoints(ComponentLibrary componentLibrary, Node node) {
        String componentType = node.getComponentType();
        Orientation nodeOrientation = node.getOrientation();
        Component.Transformation transformation = componentLibrary.getTransformations(componentType).get(nodeOrientation);
        return componentLibrary.getAnchorPoints(componentType)
                .stream()
                .map(anchorPoint -> anchorPoint.transformAnchorPoint(nodeOrientation, transformation))
                .collect(Collectors.toList());
    }

    public static WireConnection searchBestAnchorPoints(ComponentLibrary componentLibrary, VoltageLevelGraph graph, Node node1, Node node2) {
        Objects.requireNonNull(componentLibrary);
        Objects.requireNonNull(node1);
        Objects.requireNonNull(node2);

        List<AnchorPoint> anchorPoints1 = node1 instanceof BusNode ? getBusNodeAnchorPoint(graph, (BusNode) node1, node2) : getAnchorPoints(componentLibrary, node1);
        List<AnchorPoint> anchorPoints2 = node2 instanceof BusNode ? getBusNodeAnchorPoint(graph, (BusNode) node2, node1) : getAnchorPoints(componentLibrary, node2);
        return searchBestAnchorPoints(node1.getCoordinates(), node2.getCoordinates(), anchorPoints1, anchorPoints2);
    }

    private static List<AnchorPoint> getBusNodeAnchorPoint(VoltageLevelGraph graph, BusNode busNode, Node otherNode) {
        Direction direction = graph.getDirection(otherNode);
        boolean undefinedMiddleDirection = direction == Direction.UNDEFINED
                && otherNode.getCoordinates().getY() == busNode.getCoordinates().getY()
                && (otherNode.getCoordinates().getX() < busNode.getCoordinates().getX()
                || otherNode.getCoordinates().getX() > busNode.getCoordinates().getX() + busNode.getPxWidth());
        if (direction == Direction.MIDDLE || undefinedMiddleDirection) {
            return Arrays.asList(
                    new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL),
                    new AnchorPoint(busNode.getPxWidth(), 0, AnchorOrientation.HORIZONTAL)
            );
        } else {
            return Collections.singletonList(
                    new AnchorPoint(otherNode.getX() - busNode.getX(), 0, AnchorOrientation.VERTICAL));
        }
    }

    public static AnchorPoint getBestAnchorPoint(ComponentLibrary componentLibrary, Graph graph, Node node, Point point) {
        Objects.requireNonNull(componentLibrary);
        Objects.requireNonNull(node);
        Objects.requireNonNull(point);

        List<AnchorPoint> anchorPoints1 = getAnchorPoints(componentLibrary, node);
        List<AnchorPoint> anchorPoints2 = Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
        return searchBestAnchorPoints(graph.getShiftedPoint(node), point, anchorPoints1, anchorPoints2).getAnchorPoint1();
    }

    private static WireConnection searchBestAnchorPoints(Point coord1, Point coord2,
                                                         List<AnchorPoint> anchorPoints1,
                                                         List<AnchorPoint> anchorPoints2) {
        AnchorPoint betterAnchorPoint1 = anchorPoints1.get(0);
        AnchorPoint betterAnchorPoint2 = anchorPoints2.get(0);

        double currentDistance = coord1.getShiftedPoint(betterAnchorPoint1).distanceSquare(
            coord2.getShiftedPoint(betterAnchorPoint2));
        for (AnchorPoint anchorPoint1 : anchorPoints1) {
            Point shiftedCoord1 = coord1.getShiftedPoint(anchorPoint1);
            for (AnchorPoint anchorPoint2 : anchorPoints2) {
                double distance = shiftedCoord1.distanceSquare(coord2.getShiftedPoint(anchorPoint2));
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
    public List<Point> calculatePolylinePoints(Node node1, Node node2, boolean straight, Point vlGraphCoord) {

        Point point1 = node1.getCoordinates().getShiftedPoint(vlGraphCoord).getShiftedPoint(getAnchorPoint1());
        Point point2 = node2.getCoordinates().getShiftedPoint(vlGraphCoord).getShiftedPoint(getAnchorPoint2());
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
