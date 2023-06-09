/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.AbstractBaseGraph;
import com.powsybl.sld.model.graphs.BaseGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class VerticalLayoutUtil {

    private VerticalLayoutUtil() {
    }

    public static void adaptPaddingToSnakeLines(AbstractBaseGraph graph, LayoutParameters layoutParameters, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        double widthSnakeLinesLeft = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double xVoltageLevels = widthSnakeLinesLeft + diagramPadding.getLeft() + voltageLevelPadding.getLeft();
        double y = diagramPadding.getTop()
                + graph.getVoltageLevelStream().findFirst().map(vlg -> getHeightHorizontalSnakeLines(vlg.getId(), TOP, layoutParameters, infosNbSnakeLines)).orElse(0.);

        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());
            y += vlGraph.getHeight() + getHeightHorizontalSnakeLines(vlGraph.getId(), BOTTOM, layoutParameters, infosNbSnakeLines);
        }

        double widthSnakeLinesRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
        double substationWidth = graph.getWidth() + widthSnakeLinesLeft + widthSnakeLinesRight;
        double substationHeight = y - diagramPadding.getTop();
        graph.setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
    }

    private static double getHeightHorizontalSnakeLines(String vlGraphId, Direction direction, LayoutParameters layoutParameters, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vlGraphId, direction) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }

    public static double getVerticalShift(LayoutParameters layoutParam, Direction dNode1, int nbSnakeLines1) {
        return (nbSnakeLines1 - 1) * layoutParam.getVerticalSnakeLinePadding()
                + (dNode1 == Direction.TOP ? layoutParam.getVoltageLevelPadding().getTop() : layoutParam.getVoltageLevelPadding().getBottom());
    }

    /**
     * Dispatching the snake lines to the right and to the left
     */
    public static Side getSide(boolean increment) {
        return increment ? Side.LEFT : Side.RIGHT;
    }

    public static double getXSnakeLine(BaseGraph graph, Node node, Side side, LayoutParameters layoutParam, InfosNbSnakeLinesVertical infosNbSnakeLines, double maxVoltageLevelWidth) {
        double shiftLeftRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1) - 1, 0) * layoutParam.getHorizontalSnakeLinePadding();
        return graph.getVoltageLevelGraph(node).getX() - layoutParam.getVoltageLevelPadding().getLeft()
                + (side == Side.LEFT ? -shiftLeftRight : shiftLeftRight + maxVoltageLevelWidth);
    }

    public static double getYSnakeLine(BaseGraph graph, Node node, Direction dNode1, double decalV, LayoutParameters layoutParam) {
        double y = graph.getShiftedPoint(node).getY();
        if (dNode1 == BOTTOM) {
            return y + decalV;
        } else {
            List<VoltageLevelGraph> vls = graph.getVoltageLevels();
            int iVl = vls.indexOf(graph.getVoltageLevelGraph(node));
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

    public static List<Point> calculatePolylineSnakeLine(VerticalLayout layout,
                                                         BaseGraph graph, InfosNbSnakeLinesVertical infosNbSnakeLines,
                                                         LayoutParameters layoutParam, Node node1, Node node2,
                                                         boolean increment) {
        List<Point> polyline;
        if (graph.getVoltageLevelGraph(node1) == graph.getVoltageLevelGraph(node2)) { // in the same VL (so far always horizontal layout)
            VoltageLevelGraph vlGraph = graph.getVoltageLevelGraph(node1);
            String graphId = vlGraph.getId();

            InfosNbSnakeLinesHorizontal infosNbSnakeLinesH = InfosNbSnakeLinesHorizontal.create(vlGraph);

            // Reset the horizontal layout numbers to current graph numbers
            int currentNbBottom = infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(graphId, BOTTOM);
            int currentNbTop = infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(graphId, TOP);
            int currentNbLeft = infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT);
            infosNbSnakeLinesH.getNbSnakeLinesTopBottom().put(BOTTOM, currentNbBottom);
            infosNbSnakeLinesH.getNbSnakeLinesTopBottom().put(TOP, currentNbTop);
            infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().put(graphId, currentNbLeft);

            double yMin = vlGraph.getY();
            double yMax = vlGraph.getY() + vlGraph.getInnerHeight(layoutParam.getVerticalSpaceBus());

            // Calculate the snakeline as an horizontal layout
            polyline = AbstractLayout.calculatePolylineSnakeLineForHorizontalLayout(graph, layoutParam, node1, node2, increment, infosNbSnakeLinesH, yMin, yMax);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(TOP);
            Integer updatedNbLinesLeft = infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().get(graphId);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, BOTTOM, updatedNbLinesBottom);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, TOP, updatedNbLinesTop);
            infosNbSnakeLines.getNbSnakeLinesLeftRight().put(Side.LEFT, updatedNbLinesLeft);
        } else {
            polyline = new ArrayList<>();
            polyline.add(graph.getShiftedPoint(node1));
            layout.addMiddlePoints(layoutParam, node1, node2, increment, polyline);
            polyline.add(graph.getShiftedPoint(node2));
        }
        return polyline;
    }

    public static void addMiddlePoints(VerticalLayout layout,
                                       BaseGraph graph,
                                       InfosNbSnakeLinesVertical infosNbSnakeLines, double maxVoltageLevelWidth,
                                       LayoutParameters layoutParam, Node node1, Node node2,
                                       boolean increment,
                                       List<Point> polyline) {
        Direction dNode1 = AbstractLayout.getNodeDirection(graph, node1, 1);
        Direction dNode2 = AbstractLayout.getNodeDirection(graph, node2, 2);

        // increment not needed for 3WT for the common node
        String vl1 = graph.getVoltageLevelInfos(node1).getId();
        int nbSnakeLines1 = increment
                ? infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl1, dNode1)
                : infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vl1, dNode1);
        double decal1V = VerticalLayoutUtil.getVerticalShift(layoutParam, dNode1, nbSnakeLines1);

        Point p1 = graph.getShiftedPoint(node1);
        Point p2 = graph.getShiftedPoint(node2);

        if (layout.facingNodes(node1, node2)) {
            // if the two nodes are facing each other, no need to add more than 2 points (and one point is enough if same abscissa)
            double ySnakeLine = Math.min(p1.getY(), p2.getY()) + decal1V;
            if (p1.getX() != p2.getX()) {
                polyline.add(new Point(p1.getX(), ySnakeLine));
                polyline.add(new Point(p2.getX(), ySnakeLine));
            } else {
                polyline.add(new Point(p1.getX(), ySnakeLine));
            }
        } else {
            String vl2 = graph.getVoltageLevelInfos(node2).getId();
            int nbSnakeLines2 = infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl2, dNode2);
            double decal2V = VerticalLayoutUtil.getVerticalShift(layoutParam, dNode2, nbSnakeLines2);

            double ySnakeLine1 = VerticalLayoutUtil.getYSnakeLine(graph, node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = VerticalLayoutUtil.getYSnakeLine(graph, node2, dNode2, decal2V, layoutParam);

            Side side = VerticalLayoutUtil.getSide(increment);
            double xSnakeLine = VerticalLayoutUtil.getXSnakeLine(graph, node1, side, layoutParam, infosNbSnakeLines, maxVoltageLevelWidth);
            polyline.addAll(Point.createPointsList(p1.getX(), ySnakeLine1,
                    xSnakeLine, ySnakeLine1,
                    xSnakeLine, ySnakeLine2,
                    p2.getX(), ySnakeLine2));
        }
    }
}
