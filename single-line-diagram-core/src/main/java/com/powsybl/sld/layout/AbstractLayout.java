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
import java.util.List;
import java.util.Map;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractLayout {

    public abstract AbstractBaseGraph getGraph();

    protected abstract void manageSnakeLines(LayoutParameters layoutParameters);

    protected void manageSnakeLines(AbstractBaseGraph graph, LayoutParameters layoutParameters, InfosNbSnakeLines infos) {
        for (Node multiNode : graph.getMultiTermNodes()) {
            List<Edge> adjacentEdges = multiNode.getAdjacentEdges();
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();
            if (adjacentNodes.size() == 2) {
                List<Point> pol = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), infos, true);
                Point coordNodeFict = new Point(-1, -1);
                ((TwtEdge) adjacentEdges.get(0)).setSnakeLine(splitPolyline2(pol, 1, coordNodeFict));
                ((TwtEdge) adjacentEdges.get(1)).setSnakeLine(splitPolyline2(pol, 2, null));
                multiNode.setX(coordNodeFict.getX(), false);
                multiNode.setY(coordNodeFict.getY(), false);
            } else if (adjacentNodes.size() == 3) {
                List<Point> pol1 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), infos, true);
                List<Point> pol2 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(1), adjacentNodes.get(2), infos, false);
                Point coordNodeFict = new Point(-1, -1);
                ((TwtEdge) adjacentEdges.get(0)).setSnakeLine(splitPolyline3(pol1, pol2, 1, coordNodeFict));
                ((TwtEdge) adjacentEdges.get(1)).setSnakeLine(splitPolyline3(pol1, pol2, 2, null));
                ((TwtEdge) adjacentEdges.get(2)).setSnakeLine(splitPolyline3(pol1, pol2, 3, null));
                multiNode.setX(coordNodeFict.getX(), false);
                multiNode.setY(coordNodeFict.getY(), false);
            }
        }

        for (LineEdge lineEdge : graph.getLineEdges()) {
            List<Node> adjacentNodes = lineEdge.getNodes();
            lineEdge.setSnakeLine(calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), infos, true));
        }
    }

    protected abstract List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                               InfosNbSnakeLines infosNbSnakeLines, boolean increment);

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
                                                                                InfosNbSnakeLines infosNbSnakeLines, boolean increment) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        double xMaxGraph;
        String idMaxGraph;

        if (node1.getGraph().getX() > node2.getGraph().getX()) {
            xMaxGraph = node1.getGraph().getX();
            idMaxGraph = node1.getGraph().getVoltageLevelInfos().getId();
        } else {
            xMaxGraph = node2.getGraph().getX();
            idMaxGraph = node2.getGraph().getVoltageLevelInfos().getId();
        }

        double x1 = node1.getX();
        double y1 = node1.getY();
        double initY1 = node1.getInitY() != -1 ? node1.getInitY() : y1;
        double x2 = node2.getX();
        double y2 = node2.getY();
        double initY2 = node2.getInitY() != -1 ? node2.getInitY() : y2;

        HorizontalInfoCalcPoints info = new HorizontalInfoCalcPoints();
        info.setLayoutParam(layoutParam);
        info.setdNode1(dNode1);
        info.setdNode2(dNode2);
        info.setNbSnakeLinesTopBottom(infosNbSnakeLines.getNbSnakeLinesTopBottom());
        info.setNbSnakeLinesBetween(infosNbSnakeLines.getNbSnakeLinesBetween());
        info.setX1(x1);
        info.setX2(x2);
        info.setY1(y1);
        info.setInitY1(initY1);
        info.setY2(y2);
        info.setInitY2(initY2);
        info.setxMaxGraph(xMaxGraph);
        info.setIdMaxGraph(idMaxGraph);
        info.setIncrement(increment);

        return calculatePolylinePoints(info);
    }

    public static List<Point> calculatePolylinePoints(HorizontalInfoCalcPoints info) {
        List<Point> pol;

        LayoutParameters layoutParam = info.getLayoutParam();
        BusCell.Direction dNode1 = info.getdNode1();
        BusCell.Direction dNode2 = info.getdNode2();
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = info.getNbSnakeLinesTopBottom();
        Map<String, Integer> nbSnakeLinesBetween = info.getNbSnakeLinesBetween();
        double x1 = info.getX1();
        double x2 = info.getX2();
        double y1 = info.getY1();
        double initY1 = info.getInitY1();
        double y2 = info.getY2();
        double initY2 = info.getInitY2();
        double xMaxGraph = info.getxMaxGraph();
        String idMaxGraph = info.getIdMaxGraph();

        switch (dNode1) {
            case BOTTOM:
                if (dNode2 == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                    }
                    double decalV = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double yDecal = Math.max(initY1 + decalV, initY2 + decalV);

                    pol = Point.createPointsList(x1, y1,
                        x1, yDecal,
                        x2, yDecal,
                        x2, y2);

                } else {  // BOTTOM to TOP
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.compute(dNode2, (k, v) -> v + 1);
                    }
                    nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);

                    double decal1V = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = nbSnakeLinesTopBottom.get(dNode2) * layoutParam.getVerticalSnakeLinePadding();
                    double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                    pol = Point.createPointsList(x1, y1,
                        x1, initY1 + decal1V,
                        xBetweenGraph, initY1 + decal1V,
                        xBetweenGraph, initY2 - decal2V,
                        x2, initY2 - decal2V,
                        x2, y2);
                }
                break;

            case TOP:
                if (dNode2 == BusCell.Direction.TOP) {  // TOP to TOP
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                    }
                    double decalV = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double yDecal = Math.min(initY1 - decalV, initY2 - decalV);

                    pol = Point.createPointsList(x1, y1,
                        x1, yDecal,
                        x2, yDecal,
                        x2, y2);
                } else {  // TOP to BOTTOM
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.compute(dNode2, (k, v) -> v + 1);
                    }
                    nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);
                    double decal1V = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = nbSnakeLinesTopBottom.get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                    double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                    pol = Point.createPointsList(x1, y1,
                        x1, initY1 - decal1V,
                        xBetweenGraph, initY1 - decal1V,
                        xBetweenGraph, initY2 + decal2V,
                        x2, initY2 + decal2V,
                        x2, y2);
                }
                break;
            default:
                pol = new ArrayList<>();
        }
        return pol;
    }

    protected List<Point> splitPolyline2(List<Point> points, int numPart, Point coord) {
        List<Point> res = new ArrayList<>();

        int iMiddle0 = points.size() / 2 - 1;
        int iMiddle1 = points.size() / 2;
        Point pointSplit = points.get(iMiddle0).getMiddlePoint(points.get(iMiddle1));
        if (numPart == 1) {
            res.addAll(points.subList(0, iMiddle1));
            res.add(pointSplit);
        } else {
            res.add(pointSplit);
            res.addAll(points.subList(iMiddle1, points.size()));
        }

        if (coord != null) {
            coord.setX(pointSplit.getX());
            coord.setY(pointSplit.getY());
        }

        return res;
    }

    protected List<Point> splitPolyline3(List<Point> points1, List<Point> points2, int numPart, Point coord) {
        List<Point> res = new ArrayList<>();

        if (numPart == 1) {
            // for the first new edge, we keep all the original first polyline points, except the last one
            res.addAll(points1.subList(0, points1.size() - 1));
            if (coord != null) {
                // the fictitious node point is the last point of the new edge polyline
                Point fictitiousNodePoint = points1.get(points1.size() - 2);
                coord.setX(fictitiousNodePoint.getX());
                coord.setY(fictitiousNodePoint.getY());
            }
        } else if (numPart == 2) {
            // for the second new edge, we keep the last two points of the original first polyline
            res.addAll(points1.subList(points1.size() - 2, points1.size()));
        } else {
            // the third new edge is made with the original second polyline, except the first point
            res.addAll(points2.subList(1, points2.size()));
        }

        return res;
    }

}
