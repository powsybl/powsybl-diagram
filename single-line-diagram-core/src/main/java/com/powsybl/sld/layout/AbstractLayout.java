/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.coordinate.Point;
import com.powsybl.sld.model.*;

import java.util.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractLayout implements Layout {

    public abstract AbstractBaseGraph getGraph();

    protected abstract void manageSnakeLines(LayoutParameters layoutParameters);

    protected void manageSnakeLines(AbstractBaseGraph graph, LayoutParameters layoutParameters) {
        for (Node multiNode : graph.getMultiTermNodes()) {
            List<Edge> adjacentEdges = multiNode.getAdjacentEdges();
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();
            if (adjacentNodes.size() == 2) {
                List<Point> pol = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), true);
                List<List<Point>> pollingSplit = splitPolyline2(pol, multiNode);
                ((BranchEdge) adjacentEdges.get(0)).setSnakeLine(pollingSplit.get(0));
                ((BranchEdge) adjacentEdges.get(1)).setSnakeLine(pollingSplit.get(1));
            } else if (adjacentNodes.size() == 3) {
                List<Point> pol1 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), true);
                List<Point> pol2 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(1), adjacentNodes.get(2), false);
                List<List<Point>> pollingSplit = splitPolyline3(pol1, pol2, multiNode);
                for (int i = 0; i < 3; i++) {
                    ((BranchEdge) adjacentEdges.get(i)).setSnakeLine(pollingSplit.get(i));
                }
            }
        }

        for (BranchEdge lineEdge : graph.getLineEdges()) {
            List<Node> adjacentNodes = lineEdge.getNodes();
            lineEdge.setSnakeLine(calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), true));
        }
    }

    protected abstract List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                              boolean increment);

    protected static BusCell.Direction getNodeDirection(Node node, int nb) {
        if (node.getType() != Node.NodeType.FEEDER) {
            throw new PowsyblException("Node " + nb + " is not a feeder node");
        }
        BusCell.Direction dNode = node.getCell() != null ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.TOP;
        if (dNode != BusCell.Direction.TOP && dNode != BusCell.Direction.BOTTOM) {
            throw new PowsyblException("Node " + nb + " cell direction not TOP or BOTTOM");
        }
        return dNode;
    }

    /*
     * Calculate polyline points of a snakeLine
     * This is a default implementation of 'calculatePolylineSnakeLine' for a horizontal layout
     */
    protected static List<Point> calculatePolylineSnakeLineForHorizontalLayout(LayoutParameters layoutParam, Node node1, Node node2,
                                                                               boolean increment, InfosNbSnakeLinesHorizontal infosNbSnakeLines) {
        List<Point> pol = new ArrayList<>();
        pol.add(node1.getDiagramCoordinates());
        addMiddlePoints(layoutParam, node1, node2, infosNbSnakeLines, increment, pol);
        pol.add(node2.getDiagramCoordinates());
        return pol;
    }

    private static void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2,
                                        InfosNbSnakeLinesHorizontal infosNbSnakeLines, boolean increment,
                                        List<Point> pol) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = infosNbSnakeLines.getNbSnakeLinesTopBottom();

        double x1 = node1.getDiagramX();
        double x2 = node2.getDiagramX();
        double y1 = node1.getDiagramY();
        double y2 = node2.getDiagramY();

        if (dNode1 == dNode2) {
            if (increment) {
                nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
            }
            double decalV = getVerticalShift(layoutParam, dNode1, nbSnakeLinesTopBottom);
            double yDecal = Math.max(y1 + decalV, y2 + decalV);
            pol.add(new Point(x1, yDecal));
            pol.add(new Point(x2, yDecal));
        } else {
            if (increment) {
                nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                nbSnakeLinesTopBottom.compute(dNode2, (k, v) -> v + 1);
            }

            VoltageLevelGraph rightestVoltageLevel = node1.getVoltageLevelGraph().getX() > node2.getVoltageLevelGraph().getX() ? node1.getVoltageLevelGraph() : node2.getVoltageLevelGraph();
            double xMaxGraph = rightestVoltageLevel.getX();
            String idMaxGraph = rightestVoltageLevel.getId();

            LayoutParameters.Padding vlPadding = layoutParam.getVoltageLevelPadding();
            double decal1V = getVerticalShift(layoutParam, dNode1, nbSnakeLinesTopBottom);
            double decal2V = getVerticalShift(layoutParam, dNode2, nbSnakeLinesTopBottom);
            double xBetweenGraph = xMaxGraph - vlPadding.getLeft()
                - (infosNbSnakeLines.getNbSnakeLinesVerticalBetween().compute(idMaxGraph, (k, v) -> v + 1) - 1) * layoutParam.getHorizontalSnakeLinePadding();

            pol.addAll(Point.createPointsList(x1, y1 + decal1V,
                xBetweenGraph, y1 + decal1V,
                xBetweenGraph, y2 + decal2V,
                x2, y2 + decal2V));
        }
    }

    private static double getVerticalShift(LayoutParameters layoutParam, BusCell.Direction dNode1, Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom) {
        if (dNode1 == BusCell.Direction.BOTTOM) {
            return Math.max(nbSnakeLinesTopBottom.get(dNode1) - 1, 0) * layoutParam.getVerticalSnakeLinePadding() + layoutParam.getVoltageLevelPadding().getBottom();
        } else {
            return -Math.max(nbSnakeLinesTopBottom.get(dNode1) - 1, 0) * layoutParam.getVerticalSnakeLinePadding() - layoutParam.getVoltageLevelPadding().getTop();
        }
    }

    protected List<List<Point>> splitPolyline2(List<Point> points, Node multiNode) {
        int iMiddle0 = points.size() / 2 - 1;
        int iMiddle1 = points.size() / 2;

        Point pointSplit = points.get(iMiddle0).getMiddlePoint(points.get(iMiddle1));
        multiNode.setCoordinates(pointSplit);

        List<Point> part1 = new ArrayList<>(points.subList(0, iMiddle1));
        part1.add(new Point(pointSplit));

        // we need to reverse the order for the second part as the edges are always from middleTwtNode to twtLegNode
        LinkedList<Point> part2 = new LinkedList<>();
        points.stream().skip(iMiddle1).forEach(part2::addFirst);
        part2.add(new Point(pointSplit));

        return Arrays.asList(part1, part2);
    }

    protected List<List<Point>> splitPolyline3(List<Point> points1, List<Point> points2, Node coord) {
        // for the first new edge, we keep all the original first polyline points, except the last one
        List<Point> part1 = new ArrayList<>(points1.subList(0, points1.size() - 1));

        // for the second new edge, we keep the last two points of the original first polyline (in reverse order
        // as the edges are always from middleTwtNode to twtLegNode
        // we need to create a new point to avoid having a point shared between part1 and part2
        List<Point> part2 = Arrays.asList(points1.get(points1.size() - 1), new Point(points1.get(points1.size() - 2)));

        // the third new edge is made with the original second polyline, except the first point (in reverse order
        // as the edges are always from middleTwtNode to twtLegNode)
        LinkedList<Point> part3 = new LinkedList<>();
        points2.stream().skip(1).forEach(part3::addFirst);

        // the fictitious node point is the second to last point of the original first polyline (or the second of the original second polyline)
        coord.setCoordinates(points2.get(1));

        return Arrays.asList(part1, part2, part3);
    }

    protected static double getWidthVerticalSnakeLines(String vlGraphId, LayoutParameters layoutParameters, InfosNbSnakeLinesHorizontal infosNbSnakeLines) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesVerticalBetween().get(vlGraphId) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }

    protected static double getHeightSnakeLines(LayoutParameters layoutParameters, BusCell.Direction top, InfosNbSnakeLinesHorizontal infosNbSnakeLines) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesTopBottom().get(top) - 1, 0) * layoutParameters.getVerticalSnakeLinePadding();
    }
}
