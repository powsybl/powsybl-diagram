/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.BaseGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractLayout implements Layout {

    public abstract BaseGraph getGraph();

    protected abstract void manageSnakeLines(LayoutParameters layoutParameters);

    protected void manageSnakeLines(BaseGraph graph, LayoutParameters layoutParameters) {
        for (MiddleTwtNode multiNode : graph.getMultiTermNodes()) {
            List<Edge> adjacentEdges = multiNode.getAdjacentEdges();
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();
            if (multiNode instanceof Middle2WTNode) {
                List<Point> pol = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), true);
                List<List<Point>> pollingSplit = splitPolyline2(pol, multiNode);
                ((BranchEdge) adjacentEdges.get(0)).setSnakeLine(pollingSplit.get(0));
                ((BranchEdge) adjacentEdges.get(1)).setSnakeLine(pollingSplit.get(1));
                handle2wtNodeOrientation((Middle2WTNode) multiNode, pollingSplit);
            } else if (multiNode instanceof Middle3WTNode) {
                List<Point> pol1 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), true);
                List<Point> pol2 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(1), adjacentNodes.get(2), false);
                List<List<Point>> pollingSplit = splitPolyline3(pol1, pol2, multiNode);
                for (int i = 0; i < 3; i++) {
                    ((BranchEdge) adjacentEdges.get(i)).setSnakeLine(pollingSplit.get(i));
                }
                handle3wtNodeOrientation((Middle3WTNode) multiNode, pollingSplit);
            }
        }

        for (BranchEdge lineEdge : graph.getLineEdges()) {
            List<Node> adjacentNodes = lineEdge.getNodes();
            lineEdge.setSnakeLine(calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), true));
        }
    }

    private void handle2wtNodeOrientation(Middle2WTNode node, List<List<Point>> pollingSplit) {
        List<Point> pol1 = pollingSplit.get(0);
        List<Point> pol2 = pollingSplit.get(1);

        // Orientation.LEFT example:
        // coord1 o-----OO-----o coord2
        Point coord1 = pol1.get(pol1.size() - 2); // point linked to winding1
        Point coord2 = pol2.get(pol2.size() - 2); // point linked to winding2

        if (coord1.getX() == coord2.getX()) {
            node.setOrientation(coord2.getY() > coord1.getY() ? Orientation.DOWN : Orientation.UP);
        } else {
            node.setOrientation(coord1.getX() < coord2.getX() ? Orientation.RIGHT : Orientation.LEFT);
        }
    }

    /**
     * Deduce the node orientation based on the lines coordinates supporting the svg component.
     * As we are dealing with straight lines, we always have two out of three snake lines which are in line, the third
     * one being perpendicular.
     */
    private void handle3wtNodeOrientation(Middle3WTNode node, List<List<Point>> snakeLines) {
        List<Point> snakeLineLeg1 = snakeLines.get(0); // snakeline from leg1 feeder node to 3wt
        List<Point> snakeLineLeg2 = snakeLines.get(1); // snakeline with simply two points going from leg2 feeder node to 3wt
        List<Point> snakeLineLeg3 = snakeLines.get(2); // snakeline from leg3 feeder node to 3wt

        // Orientation.UP example:
        // line going  _____OO_____ line going
        //   to leg1        O        to leg3
        //                  |
        //                  o leg2
        Point leg1 = snakeLineLeg1.get(snakeLineLeg1.size() - 2);
        Point leg2 = snakeLineLeg2.get(snakeLineLeg2.size() - 2);
        Point leg3 = snakeLineLeg3.get(snakeLineLeg3.size() - 2);

        if (leg1.getY() == leg3.getY()) {
            // General case
            node.setOrientation(leg2.getY() < leg1.getY() ? Orientation.DOWN : Orientation.UP);
            setWindingOrder(node,
                () -> leg1.getX() < leg3.getX(),
                Arrays.asList(Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_RIGHT,
                Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_LEFT));
        } else if (leg2.getX() == leg1.getX()) {
            // Specific case of leg1 and leg2 facing feeder nodes with same abscissa
            node.setOrientation(leg3.getX() > leg1.getX() ? Orientation.LEFT : Orientation.RIGHT);
            setWindingOrder(node,
                () -> leg3.getX() > leg1.getX() == leg1.getY() > leg2.getY(),
                Arrays.asList(Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.DOWN,
                Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.DOWN));
        } else if (leg2.getX() == leg3.getX()) {
            // Specific case of leg2 and leg3 facing feeder nodes with same abscissa
            node.setOrientation(leg1.getX() > leg3.getX() ? Orientation.LEFT : Orientation.RIGHT);
            setWindingOrder(node,
                () -> leg1.getX() > leg3.getX() == leg2.getY() > leg3.getY(),
                Arrays.asList(Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.UPPER_RIGHT,
                Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.UPPER_LEFT));
        }
    }

    private void setWindingOrder(Middle3WTNode node,
                                 BooleanSupplier cond,
                                 List<Middle3WTNode.Winding> windings) {
        if (cond.getAsBoolean()) {
            node.setWindingOrder(windings.get(0), windings.get(1), windings.get(2));
        } else {
            node.setWindingOrder(windings.get(3), windings.get(4), windings.get(5));
        }
    }

    protected abstract List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                              boolean increment);

    protected Direction getNodeDirection(Node node, int nb) {
        if (node.getType() != Node.NodeType.FEEDER) {
            throw new PowsyblException("Node " + nb + " is not a feeder node");
        }
        Direction dNode = getGraph().getCell(node).map(Cell::getDirection).orElse(Direction.TOP);
        if (dNode != Direction.TOP && dNode != Direction.BOTTOM) {
            throw new PowsyblException("Node " + nb + " cell direction not TOP or BOTTOM");
        }
        return dNode;
    }

    /*
     * Calculate polyline points of a snakeLine
     * This is a default implementation of 'calculatePolylineSnakeLine' for a horizontal layout
     */
    protected List<Point> calculatePolylineSnakeLineForHorizontalLayout(LayoutParameters layoutParam, Node node1, Node node2,
                                                                        boolean increment, InfosNbSnakeLinesHorizontal infosNbSnakeLines,
                                                                        double yMin, double yMax) {
        List<Point> pol = new ArrayList<>();
        pol.add(getGraph().getShiftedPoint(node1));
        addMiddlePoints(layoutParam, new Pair<>(node1, node2), infosNbSnakeLines, increment, pol, new Pair<>(yMin, yMax));
        pol.add(getGraph().getShiftedPoint(node2));
        return pol;
    }

    private void addMiddlePoints(LayoutParameters layoutParam,
                                 Pair<Node, Node> nodes,
                                 InfosNbSnakeLinesHorizontal infosNbSnakeLines, boolean increment,
                                 List<Point> pol,
                                 Pair<Double, Double> yMinMax) {
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        double yMin = yMinMax.getFirst();
        double yMax = yMinMax.getSecond();

        Direction dNode1 = getNodeDirection(node1, 1);
        Direction dNode2 = getNodeDirection(node2, 2);

        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);

        Map<Direction, Integer> nbSnakeLinesTopBottom = infosNbSnakeLines.getNbSnakeLinesTopBottom();

        double x1 = node1.getX() + vlGraph1.getX();
        double x2 = node2.getX() + vlGraph2.getX();
        double y1 = dNode1 == Direction.BOTTOM ? yMax : yMin;
        double y2 = dNode2 == Direction.BOTTOM ? yMax : yMin;

        if (dNode1 == dNode2) {
            if (increment) {
                nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
            }
            double decalV = getVerticalShift(layoutParam, dNode1, nbSnakeLinesTopBottom);
            double yDecal = y1 + decalV;
            pol.add(new Point(x1, yDecal));
            pol.add(new Point(x2, yDecal));
        } else {
            if (increment) {
                nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                nbSnakeLinesTopBottom.compute(dNode2, (k, v) -> v + 1);
            }

            VoltageLevelGraph rightestVoltageLevel = vlGraph1.getX() > vlGraph2.getX() ? vlGraph1 : vlGraph2;
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

    private static double getVerticalShift(LayoutParameters layoutParam, Direction dNode1, Map<Direction, Integer> nbSnakeLinesTopBottom) {
        if (dNode1 == Direction.BOTTOM) {
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

    protected static double getHeightSnakeLines(LayoutParameters layoutParameters, Direction top, InfosNbSnakeLinesHorizontal infosNbSnakeLines) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesTopBottom().get(top) - 1, 0) * layoutParameters.getVerticalSnakeLinePadding();
    }
}
