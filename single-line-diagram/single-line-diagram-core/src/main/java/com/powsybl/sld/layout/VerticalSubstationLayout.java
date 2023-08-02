/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.coordinate.Direction;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class VerticalSubstationLayout extends AbstractSubstationLayout {

    private final InfosNbSnakeLinesVertical infosNbSnakeLines;
    private double maxVoltageLevelWidth;

    protected VerticalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        super(graph, vLayoutFactory);
        this.infosNbSnakeLines = infosNbSnakeLines;
    }

    public VerticalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        this(graph, vLayoutFactory, InfosNbSnakeLinesVertical.create(graph));
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected void calculateCoordVoltageLevels(LayoutParameters layoutParameters) {

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double xVoltageLevels = diagramPadding.getLeft() + voltageLevelPadding.getLeft();
        double substationWidth = 0;
        double y = diagramPadding.getTop();

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            // Calculate the objects coordinates inside the voltageLevel graph
            Layout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());

            substationWidth = Math.max(substationWidth, vlGraph.getWidth());
            y += vlGraph.getHeight();
        }

        double substationHeight = y - diagramPadding.getTop();
        getGraph().setSize(substationWidth, substationHeight);

        maxVoltageLevelWidth = substationWidth;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);

        adaptPaddingToSnakeLines(layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        double widthSnakeLinesLeft = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double xVoltageLevels = widthSnakeLinesLeft + diagramPadding.getLeft() + voltageLevelPadding.getLeft();
        double y = diagramPadding.getTop()
            + getGraph().getVoltageLevelStream().findFirst().map(vlg -> getHeightHorizontalSnakeLines(vlg.getId(), TOP, layoutParameters)).orElse(0.);

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());
            y += vlGraph.getHeight() + getHeightHorizontalSnakeLines(vlGraph.getId(), BOTTOM, layoutParameters);
        }

        double widthSnakeLinesRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
        double substationWidth = getGraph().getWidth() + widthSnakeLinesLeft + widthSnakeLinesRight;
        double substationHeight = y - diagramPadding.getTop();
        getGraph().setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

    private double getHeightHorizontalSnakeLines(String vlGraphId, Direction direction, LayoutParameters layoutParameters) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vlGraphId, direction) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                     boolean increment) {
        List<Point> polyline;
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        if (getGraph().getVoltageLevelGraph(node1) == getGraph().getVoltageLevelGraph(node2)) { // in the same VL (so far always horizontal layout)
            VoltageLevelGraph vlGraph = getGraph().getVoltageLevelGraph(node1);
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
            polyline = calculatePolylineSnakeLineForHorizontalLayout(layoutParam, nodes, increment, infosNbSnakeLinesH, yMin, yMax);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(TOP);
            Integer updatedNbLinesLeft = infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().get(graphId);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, BOTTOM, updatedNbLinesBottom);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, TOP, updatedNbLinesTop);
            infosNbSnakeLines.getNbSnakeLinesLeftRight().put(Side.LEFT, updatedNbLinesLeft);
        } else if (getGraph().getAllNodesStream().anyMatch(node -> node == node1) && getGraph().getAllNodesStream().anyMatch(node -> node == node2)) { // in the same SS
            polyline = new ArrayList<>();
            polyline.add(getGraph().getShiftedPoint(node1));
            addMiddlePoints(layoutParam, node1, node2, increment, polyline);
            polyline.add(getGraph().getShiftedPoint(node2));
        } else { // in the same Zone
            polyline = new ArrayList<>();
        }
        return polyline;
    }

    protected void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2, boolean increment, List<Point> polyline) {
        Direction dNode1 = getNodeDirection(node1, 1);
        Direction dNode2 = getNodeDirection(node2, 2);

        // increment not needed for 3WT for the common node
        String vl1 = getGraph().getVoltageLevelInfos(node1).getId();
        int nbSnakeLines1 = increment
            ? infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl1, dNode1)
            : infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vl1, dNode1);
        double decal1V = getVerticalShift(layoutParam, dNode1, nbSnakeLines1);

        Point p1 = getGraph().getShiftedPoint(node1);
        Point p2 = getGraph().getShiftedPoint(node2);

        if (facingNodes(node1, node2)) {
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
            int nbSnakeLines2 = infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl2, dNode2);
            double decal2V = getVerticalShift(layoutParam, dNode2, nbSnakeLines2);

            double ySnakeLine1 = getYSnakeLine(node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = getYSnakeLine(node2, dNode2, decal2V, layoutParam);

            Side side = getSide(increment);
            double xSnakeLine = getXSnakeLine(node1, side, layoutParam);
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

    private double getXSnakeLine(Node node, Side side, LayoutParameters layoutParam) {
        double shiftLeftRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1) - 1, 0) * layoutParam.getHorizontalSnakeLinePadding();
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

    private boolean facingNodes(Node node1, Node node2) {
        Direction dNode1 = getNodeDirection(node1, 1);
        Direction dNode2 = getNodeDirection(node2, 2);
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        return (dNode1 == BOTTOM && dNode2 == TOP && getGraph().graphAdjacents(vlGraph1, vlGraph2))
                || (dNode1 == TOP && dNode2 == BOTTOM && getGraph().graphAdjacents(vlGraph2, vlGraph1));
    }
}
