/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractLayout {

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
        pol.add(node1.getCoordinates());
        addMiddlePoints(layoutParam, node1, node2, infosNbSnakeLines, increment, pol);
        pol.add(node2.getCoordinates());
        return pol;
    }

    private static void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2,
                                        InfosNbSnakeLinesHorizontal infosNbSnakeLines, boolean increment,
                                        List<Point> pol) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = infosNbSnakeLines.getNbSnakeLinesTopBottom();

        double x1 = node1.getCoordinates().getX();
        double x2 = node2.getCoordinates().getX();
        double y1 = node1.getCoordinates().getY();
        double y2 = node2.getCoordinates().getY();

        if (dNode1 == dNode2) {
            if (increment) {
                nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
            }
            double decalV = getSgn(dNode1) * nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
            double yDecal = Math.max(y1 + decalV, y2 + decalV);
            pol.add(new Point(x1, yDecal));
            pol.add(new Point(x2, yDecal));
        } else {
            if (increment) {
                nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                nbSnakeLinesTopBottom.compute(dNode2, (k, v) -> v + 1);
            }

            VoltageLevelGraph rightestVoltageLevel = node1.getGraph().getX() > node2.getGraph().getX() ? node1.getGraph() : node2.getGraph();
            double xMaxGraph = rightestVoltageLevel.getX();
            String idMaxGraph = rightestVoltageLevel.getId();

            double decal1V = getSgn(dNode1) * nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
            double decal2V = getSgn(dNode2) * nbSnakeLinesTopBottom.get(dNode2) * layoutParam.getVerticalSnakeLinePadding();
            double xBetweenGraph = xMaxGraph -
                (infosNbSnakeLines.getNbSnakeLinesVerticalBetween().compute(idMaxGraph, (k, v) -> v + 1) * layoutParam.getHorizontalSnakeLinePadding());

            pol.addAll(Point.createPointsList(x1, y1 + decal1V,
                xBetweenGraph, y1 + decal1V,
                xBetweenGraph, y2 + decal2V,
                x2, y2 + decal2V));
        }
    }

    private static double getSgn(BusCell.Direction direction) {
        return direction == BusCell.Direction.BOTTOM ? 1 : -1;
    }

    protected List<List<Point>> splitPolyline2(List<Point> points, Node multiNode) {
        int iMiddle0 = points.size() / 2 - 1;
        int iMiddle1 = points.size() / 2;

        Point pointSplit = points.get(iMiddle0).getMiddlePoint(points.get(iMiddle1));
        multiNode.setCoordinates(pointSplit);

        List<Point> part1 = new ArrayList<>(points.subList(0, iMiddle1));
        part1.add(new Point(pointSplit));

        List<Point> part2 = new ArrayList<>();
        part2.add(new Point(pointSplit));
        part2.addAll(points.subList(iMiddle1, points.size()));

        return Arrays.asList(part1, part2);
    }

    protected List<List<Point>> splitPolyline3(List<Point> points1, List<Point> points2, Node coord) {
        // for the first new edge, we keep all the original first polyline points, except the last one
        List<Point> part1 = new ArrayList<>(points1.subList(0, points1.size() - 1));

        // for the second new edge, we keep the last two points of the original first polyline
        // we need to create a new point to avoid having a point shared between part1 and part2
        List<Point> part2 = Arrays.asList(new Point(points1.get(points1.size() - 2)), points1.get(points1.size() - 1));

        // the third new edge is made with the original second polyline, except the first point
        List<Point> part3 = new ArrayList<>(points2.subList(1, points2.size()));

        // the fictitious node point is the second to last point of the original first polyline (or the second of the original seond polyline)
        coord.setCoordinates(points2.get(1));

        return Arrays.asList(part1, part2, part3);
    }

}
