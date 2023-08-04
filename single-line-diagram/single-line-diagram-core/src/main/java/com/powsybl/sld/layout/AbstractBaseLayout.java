/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.*;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;
import org.jgrapht.alg.util.*;

import java.util.*;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public abstract class AbstractBaseLayout<T extends AbstractBaseGraph> extends AbstractLayout<T> {
    protected AbstractBaseLayout(T graph) {
        super(graph);
    }

    protected double maxVoltageLevelWidth;

    /*
     * Calculate polyline points of a snakeLine in vertical layout
     */
    protected List<Point> calculatePolylineSnakeLineForVerticalLayout(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                                      boolean increment,
                                                                      InfosNbSnakeLinesVertical infosNbSnakeLinesV,
                                                                      boolean facingNodes) {
        List<Point> polyline;
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        if (getGraph().getVoltageLevelGraph(node1) == getGraph().getVoltageLevelGraph(node2)) { // in the same VL (so far always horizontal layout)
            VoltageLevelGraph vlGraph = getGraph().getVoltageLevelGraph(node1);
            String graphId = vlGraph.getId();

            InfosNbSnakeLinesHorizontal infosNbSnakeLinesH = InfosNbSnakeLinesHorizontal.create(vlGraph);

            // Reset the horizontal layout numbers to current graph numbers
            int currentNbBottom = infosNbSnakeLinesV.getNbSnakeLinesHorizontalBetween(graphId, BOTTOM);
            int currentNbTop = infosNbSnakeLinesV.getNbSnakeLinesHorizontalBetween(graphId, TOP);
            int currentNbLeft = infosNbSnakeLinesV.getNbSnakeLinesLeftRight().get(Side.LEFT);
            infosNbSnakeLinesH.getNbSnakeLinesTopBottom().put(BOTTOM, currentNbBottom);
            infosNbSnakeLinesH.getNbSnakeLinesTopBottom().put(TOP, currentNbTop);
            infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().put(graphId, currentNbLeft);

            double yMin = vlGraph.getY();
            double yMax = vlGraph.getY() + vlGraph.getInnerHeight(layoutParam.getVerticalSpaceBus());

            // Calculate the snakeline as an horizontal layout
            polyline = calculatePolylineSnakeLineForHorizontalLayout(layoutParam, nodes, increment, infosNbSnakeLinesH, yMin, yMax);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(TOP);
            Integer updatedNbLinesLeft = infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().get(graphId);
            infosNbSnakeLinesV.setNbSnakeLinesTopBottom(graphId, BOTTOM, updatedNbLinesBottom);
            infosNbSnakeLinesV.setNbSnakeLinesTopBottom(graphId, TOP, updatedNbLinesTop);
            infosNbSnakeLinesV.getNbSnakeLinesLeftRight().put(Side.LEFT, updatedNbLinesLeft);
        } else if (getGraph().getAllNodesStream().anyMatch(node -> node == node1) && getGraph().getAllNodesStream().anyMatch(node -> node == node2)) { // in the same SS
            polyline = new ArrayList<>();
            polyline.add(getGraph().getShiftedPoint(node1));
            addMiddlePointsForVerticalLayout(layoutParam, nodes, increment, polyline, infosNbSnakeLinesV, facingNodes);
            polyline.add(getGraph().getShiftedPoint(node2));
        } else { // in the same Zone
            polyline = new ArrayList<>();
        }
        return polyline;
    }

    protected void addMiddlePointsForVerticalLayout(LayoutParameters layoutParam,
                                                    Pair<Node, Node> nodes,
                                                    boolean increment,
                                                    List<Point> polyline,
                                                    InfosNbSnakeLinesVertical infosNbSnakeLinesV,
                                                    boolean facingNodes) {
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        Direction dNode1 = getNodeDirection(node1, 1);
        Direction dNode2 = getNodeDirection(node2, 2);

        // increment not needed for 3WT for the common node
        String vl1 = getGraph().getVoltageLevelInfos(node1).getId();
        int nbSnakeLines1 = increment
                ? infosNbSnakeLinesV.incrementAndGetNbSnakeLinesTopBottom(vl1, dNode1)
                : infosNbSnakeLinesV.getNbSnakeLinesHorizontalBetween(vl1, dNode1);
        double decal1V = getVerticalShift(layoutParam, dNode1, nbSnakeLines1);

        Point p1 = getGraph().getShiftedPoint(node1);
        Point p2 = getGraph().getShiftedPoint(node2);

        if (facingNodes) {
            // if the two nodes are facing each other, no need to add more than 2 points (and one point is enough if same abscissa)
            double ySnakeLine = Math.min(p1.getY(), p2.getY()) + decal1V;
            if (p1.getX() != p2.getX()) {
                polyline.add(new Point(p1.getX(), ySnakeLine));
                polyline.add(new Point(p2.getX(), ySnakeLine));
            } else {
                polyline.add(new Point(p1.getX(), ySnakeLine));
            }
        } else {
            String vl2 = getGraph().getVoltageLevelInfos(node2).getId();
            int nbSnakeLines2 = infosNbSnakeLinesV.incrementAndGetNbSnakeLinesTopBottom(vl2, dNode2);
            double decal2V = getVerticalShift(layoutParam, dNode2, nbSnakeLines2);

            double ySnakeLine1 = getYSnakeLine(node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = getYSnakeLine(node2, dNode2, decal2V, layoutParam);

            Side side = getSide(increment);
            double xSnakeLine = getXSnakeLine(node1, side, layoutParam, infosNbSnakeLinesV);
            polyline.addAll(Point.createPointsList(p1.getX(), ySnakeLine1,
                    xSnakeLine, ySnakeLine1,
                    xSnakeLine, ySnakeLine2,
                    p2.getX(), ySnakeLine2));
        }
    }

    private double getVerticalShift(LayoutParameters layoutParam, Direction dNode1, int nbSnakeLines1) {
        return (nbSnakeLines1 - 1) * layoutParam.getVerticalSnakeLinePadding()
                + (dNode1 == Direction.TOP ? layoutParam.getVoltageLevelPadding().getTop() : layoutParam.getVoltageLevelPadding().getBottom());
    }

    /**
     * Dispatching the snake lines to the right and to the left
     */
    private Side getSide(boolean increment) {
        return increment ? Side.LEFT : Side.RIGHT;
    }

    private double getXSnakeLine(Node node, Side side, LayoutParameters layoutParam, InfosNbSnakeLinesVertical infosNbSnakeLinesV) {
        double shiftLeftRight = Math.max(infosNbSnakeLinesV.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1) - 1, 0) * layoutParam.getHorizontalSnakeLinePadding();
        return getGraph().getVoltageLevelGraph(node).getX() - layoutParam.getVoltageLevelPadding().getLeft()
                + (side == Side.LEFT ? -shiftLeftRight : shiftLeftRight + maxVoltageLevelWidth);
    }

    private double getYSnakeLine(Node node, Direction dNode1, double decalV, LayoutParameters layoutParam) {
        double y = getGraph().getShiftedPoint(node).getY();
        if (dNode1 == BOTTOM) {
            return y + decalV;
        } else {
            List<VoltageLevelGraph> vls = getGraph().getVoltageLevels();
            int iVl = vls.indexOf(getGraph().getVoltageLevelGraph(node));
            if (iVl == 0) {
                return y - decalV;
            } else {
                VoltageLevelGraph vlAbove = vls.get(iVl - 1);
                return vlAbove.getY()
                        + vlAbove.getHeight() - layoutParam.getVoltageLevelPadding().getTop() - layoutParam.getVoltageLevelPadding().getBottom()
                        + decalV;
            }
        }
    }
}
