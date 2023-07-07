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
import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public abstract class AbstractBaseLayout extends AbstractLayout {

    protected Optional<InfosNbSnakeLinesHorizontal> getInfosNbSnakeLinesHorizontal() {
        return Optional.empty();
    }

    protected Optional<InfosNbSnakeLinesVertical> getInfosNbSnakeLinesVertical() {
        return Optional.empty();
    }

    protected OptionalDouble getMaxVoltageLevelWidth() {
        return OptionalDouble.empty();
    }

    protected Optional<Boolean> facingNodes(Node node1, Node node2) {
        return Optional.empty();
    }

    protected List<Point> calculatePolylineSnakeLine(BaseGraph graph,
                                                     LayoutParameters layoutParam,
                                                     Pair<Node, Node> nodes,
                                                     boolean increment) {
        List<Point> polyline;
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        if (graph.getVoltageLevelGraph(node1) == graph.getVoltageLevelGraph(node2)) { // in the same VL (so far always horizontal layout)
            VoltageLevelGraph vlGraph = graph.getVoltageLevelGraph(node1);
            String graphId = vlGraph.getId();

            InfosNbSnakeLinesHorizontal infosNbSnakeLinesH = InfosNbSnakeLinesHorizontal.create(vlGraph);
            InfosNbSnakeLinesVertical infosNbSnakeLinesV = getInfosNbSnakeLinesVertical().get();

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
            polyline = AbstractLayout.calculatePolylineSnakeLineForHorizontalLayout(graph, layoutParam, new Pair<>(node1, node2), increment, infosNbSnakeLinesH, yMin, yMax);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(TOP);
            Integer updatedNbLinesLeft = infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().get(graphId);
            infosNbSnakeLinesV.setNbSnakeLinesTopBottom(graphId, BOTTOM, updatedNbLinesBottom);
            infosNbSnakeLinesV.setNbSnakeLinesTopBottom(graphId, TOP, updatedNbLinesTop);
            infosNbSnakeLinesV.getNbSnakeLinesLeftRight().put(Side.LEFT, updatedNbLinesLeft);
        } else {
            polyline = new ArrayList<>();
            polyline.add(graph.getShiftedPoint(node1));
            addMiddlePoints(graph, layoutParam, node1, node2, increment, polyline);
            polyline.add(graph.getShiftedPoint(node2));
        }
        return polyline;
    }

    protected void adaptPaddingToSnakeLinesForVertical(AbstractBaseGraph graph, LayoutParameters layoutParameters) {
        InfosNbSnakeLinesVertical infosNbSnakeLinesV = getInfosNbSnakeLinesVertical().get();
        double widthSnakeLinesLeft = Math.max(infosNbSnakeLinesV.getNbSnakeLinesLeftRight().get(Side.LEFT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double xVoltageLevels = widthSnakeLinesLeft + diagramPadding.getLeft() + voltageLevelPadding.getLeft();
        double y = diagramPadding.getTop()
                + graph.getVoltageLevelStream().findFirst().map(vlg -> getHeightHorizontalSnakeLines(vlg.getId(), TOP, layoutParameters, infosNbSnakeLinesV)).orElse(0.);

        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());
            y += vlGraph.getHeight() + getHeightHorizontalSnakeLines(vlGraph.getId(), BOTTOM, layoutParameters, infosNbSnakeLinesV);
        }

        double widthSnakeLinesRight = Math.max(infosNbSnakeLinesV.getNbSnakeLinesLeftRight().get(Side.RIGHT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
        double substationWidth = graph.getWidth() + widthSnakeLinesLeft + widthSnakeLinesRight;
        double substationHeight = y - diagramPadding.getTop();
        graph.setSize(substationWidth, substationHeight);

        infosNbSnakeLinesV.reset();
    }

    protected void adaptPaddingToSnakeLinesForHorizontal(AbstractBaseGraph graph, LayoutParameters layoutParameters) {
        InfosNbSnakeLinesHorizontal infosNbSnakeLinesH = getInfosNbSnakeLinesHorizontal().get();
        double heightSnakeLinesTop = AbstractLayout.getHeightSnakeLines(layoutParameters, TOP, infosNbSnakeLinesH);

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double topPadding = heightSnakeLinesTop + diagramPadding.getTop() + voltageLevelPadding.getTop();
        double x = diagramPadding.getLeft();

        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            x += AbstractLayout.getWidthVerticalSnakeLines(vlGraph.getId(), layoutParameters, infosNbSnakeLinesH);
            vlGraph.setCoord(x + voltageLevelPadding.getLeft(), computeCoordY(graph, layoutParameters, topPadding, vlGraph));
            x += vlGraph.getWidth();
        }

        double zoneWidth = x - diagramPadding.getLeft();
        double heightSnakeLinesBottom = AbstractLayout.getHeightSnakeLines(layoutParameters, BOTTOM, infosNbSnakeLinesH);
        double zoneHeight = graph.getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;

        graph.setSize(zoneWidth, zoneHeight);

        infosNbSnakeLinesH.reset();
    }

    static double computeCoordY(AbstractBaseGraph graph, LayoutParameters layoutParameters, double topPadding, VoltageLevelGraph vlGraph) {
        double y;
        // Find maximum voltage level top height
        double maxTopExternCellHeight = graph.getVoltageLevelStream().mapToDouble(g -> g.getExternCellHeight(Direction.TOP)).max().orElse(0.0);
        // Get gap between current voltage level and maximum height one
        double delta = maxTopExternCellHeight - vlGraph.getExternCellHeight(Direction.TOP);
        // Find maximum voltage level maxV
        double maxV = graph.getVoltageLevelStream().mapToDouble(VoltageLevelGraph::getMaxV).max().orElse(0.0);
        // Get all busbar section height
        double bbsHeight = layoutParameters.getVerticalSpaceBus() * (maxV - vlGraph.getMaxV());

        switch (layoutParameters.getBusbarsAlignment()) {
            case FIRST: {
                // Align on First busbar section
                y = topPadding + delta;
                break;
            }
            case LAST: {
                // Align on Last busbar section
                y = topPadding + delta + bbsHeight;
                break;
            }
            case MIDDLE: {
                // Align on middle of all busbar section
                y = topPadding + delta + bbsHeight / 2;
                break;
            }
            case NONE: // None alignment
            default:
                y = topPadding;
        }
        return y;
    }

    protected void addMiddlePoints(BaseGraph graph,
                                   LayoutParameters layoutParam, Node node1, Node node2,
                                   boolean increment,
                                   List<Point> polyline) {
        InfosNbSnakeLinesVertical infosNbSnakeLinesV = getInfosNbSnakeLinesVertical().get();
        Direction dNode1 = AbstractLayout.getNodeDirection(graph, node1, 1);
        Direction dNode2 = AbstractLayout.getNodeDirection(graph, node2, 2);

        // increment not needed for 3WT for the common node
        String vl1 = graph.getVoltageLevelInfos(node1).getId();
        int nbSnakeLines1 = increment
                ? infosNbSnakeLinesV.incrementAndGetNbSnakeLinesTopBottom(vl1, dNode1)
                : infosNbSnakeLinesV.getNbSnakeLinesHorizontalBetween(vl1, dNode1);
        double decal1V = getVerticalShift(layoutParam, dNode1, nbSnakeLines1);

        Point p1 = graph.getShiftedPoint(node1);
        Point p2 = graph.getShiftedPoint(node2);

        if (facingNodes(node1, node2).get()) {
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
            int nbSnakeLines2 = infosNbSnakeLinesV.incrementAndGetNbSnakeLinesTopBottom(vl2, dNode2);
            double decal2V = getVerticalShift(layoutParam, dNode2, nbSnakeLines2);

            double ySnakeLine1 = getYSnakeLine(graph, node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = getYSnakeLine(graph, node2, dNode2, decal2V, layoutParam);

            Side side = getSide(increment);
            double xSnakeLine = getXSnakeLine(graph, node1, side, layoutParam, infosNbSnakeLinesV, getMaxVoltageLevelWidth().getAsDouble());
            polyline.addAll(Point.createPointsList(p1.getX(), ySnakeLine1,
                    xSnakeLine, ySnakeLine1,
                    xSnakeLine, ySnakeLine2,
                    p2.getX(), ySnakeLine2));
        }
    }

    private static double getHeightHorizontalSnakeLines(String vlGraphId, Direction direction, LayoutParameters layoutParameters, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vlGraphId, direction) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }

    private static double getVerticalShift(LayoutParameters layoutParam, Direction dNode1, int nbSnakeLines1) {
        return (nbSnakeLines1 - 1) * layoutParam.getVerticalSnakeLinePadding()
                + (dNode1 == Direction.TOP ? layoutParam.getVoltageLevelPadding().getTop() : layoutParam.getVoltageLevelPadding().getBottom());
    }

    /**
     * Dispatching the snake lines to the right and to the left
     */
    private static Side getSide(boolean increment) {
        return increment ? Side.LEFT : Side.RIGHT;
    }

    private static double getXSnakeLine(BaseGraph graph, Node node, Side side, LayoutParameters layoutParam, InfosNbSnakeLinesVertical infosNbSnakeLines, double maxVoltageLevelWidth) {
        double shiftLeftRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1) - 1, 0) * layoutParam.getHorizontalSnakeLinePadding();
        return graph.getVoltageLevelGraph(node).getX() - layoutParam.getVoltageLevelPadding().getLeft()
                + (side == Side.LEFT ? -shiftLeftRight : shiftLeftRight + maxVoltageLevelWidth);
    }

    private static double getYSnakeLine(BaseGraph graph, Node node, Direction dNode1, double decalV, LayoutParameters layoutParam) {
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
}
