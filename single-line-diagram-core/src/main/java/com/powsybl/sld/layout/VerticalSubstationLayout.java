/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.Side;

import java.util.ArrayList;
import java.util.List;

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
            + getGraph().getVoltageLevelStream().findFirst().map(vlg -> getHeightHorizontalSnakeLines(vlg.getId(), BusCell.Direction.TOP, layoutParameters)).orElse(0.);

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());
            y += vlGraph.getHeight() + getHeightHorizontalSnakeLines(vlGraph.getId(), BusCell.Direction.BOTTOM, layoutParameters);
        }

        double widthSnakeLinesRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
        double substationWidth = getGraph().getWidth() + widthSnakeLinesLeft + widthSnakeLinesRight;
        double substationHeight = y - diagramPadding.getTop();
        getGraph().setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

    private double getHeightHorizontalSnakeLines(String vlGraphId, BusCell.Direction direction, LayoutParameters layoutParameters) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vlGraphId, direction) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        List<Point> polyline;
        if (node1.getVoltageLevelGraph() == node2.getVoltageLevelGraph()) { // in the same VL (so far always horizontal layout)
            String graphId = node1.getVoltageLevelGraph().getId();

            InfosNbSnakeLinesHorizontal infosNbSnakeLinesH = InfosNbSnakeLinesHorizontal.create(node1.getVoltageLevelGraph());

            // Reset the horizontal layout numbers to current graph numbers
            int currentNbBottom = infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(graphId, BusCell.Direction.BOTTOM);
            int currentNbTop = infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(graphId, BusCell.Direction.TOP);
            int currentNbLeft = infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT);
            infosNbSnakeLinesH.getNbSnakeLinesTopBottom().put(BusCell.Direction.BOTTOM, currentNbBottom);
            infosNbSnakeLinesH.getNbSnakeLinesTopBottom().put(BusCell.Direction.TOP, currentNbTop);
            infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().put(graphId, currentNbLeft);

            double yMin = node1.getVoltageLevelGraph().getY();
            double yMax = node1.getVoltageLevelGraph().getY() + node1.getVoltageLevelGraph().getInnerHeight(layoutParam);

            // Calculate the snakeline as an horizontal layout
            polyline = calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, increment, infosNbSnakeLinesH, yMin, yMax);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(BusCell.Direction.BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(BusCell.Direction.TOP);
            Integer updatedNbLinesLeft = infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().get(graphId);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, BusCell.Direction.BOTTOM, updatedNbLinesBottom);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, BusCell.Direction.TOP, updatedNbLinesTop);
            infosNbSnakeLines.getNbSnakeLinesLeftRight().put(Side.LEFT, updatedNbLinesLeft);

            return polyline;

        } else {
            polyline = new ArrayList<>();
            polyline.add(node1.getDiagramCoordinates());
            addMiddlePoints(layoutParam, node1, node2, increment, polyline);
            polyline.add(node2.getDiagramCoordinates());
            return polyline;
        }
    }

    protected void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2, boolean increment, List<Point> polyline) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        // increment not needed for 3WT for the common node
        String vl1 = node1.getVoltageLevelGraph().getVoltageLevelInfos().getId();
        int nbSnakeLines1 = increment
            ? infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl1, dNode1)
            : infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vl1, dNode1);
        double decal1V = getVerticalShift(layoutParam, dNode1, nbSnakeLines1);

        double x1 = node1.getDiagramX();
        double x2 = node2.getDiagramX();

        if (facingNodes(node1, node2)) {
            // if the two nodes are facing each other, no need to add more than 2 points (and one point is enough if same abscissa)
            double ySnakeLine = Math.min(node1.getDiagramY(), node2.getDiagramY()) + decal1V;
            if (x1 != x2) {
                polyline.add(new Point(x1, ySnakeLine));
                polyline.add(new Point(x2, ySnakeLine));
            } else {
                polyline.add(new Point(x1, ySnakeLine));
            }
        } else {
            String vl2 = node2.getVoltageLevelGraph().getVoltageLevelInfos().getId();
            int nbSnakeLines2 = infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl2, dNode2);
            double decal2V = getVerticalShift(layoutParam, dNode2, nbSnakeLines2);

            double ySnakeLine1 = getYSnakeLine(node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = getYSnakeLine(node2, dNode2, decal2V, layoutParam);

            Side side = getSide(dNode1, increment);
            double xSnakeLine = getXSnakeLine(node1, side, layoutParam);
            polyline.addAll(Point.createPointsList(x1, ySnakeLine1,
                xSnakeLine, ySnakeLine1,
                xSnakeLine, ySnakeLine2,
                x2, ySnakeLine2));
        }
    }

    private double getVerticalShift(LayoutParameters layoutParam, BusCell.Direction dNode1, int nbSnakeLines1) {
        return (nbSnakeLines1 - 1) * layoutParam.getVerticalSnakeLinePadding()
            + (dNode1 == BusCell.Direction.TOP ? layoutParam.getVoltageLevelPadding().getTop() : layoutParam.getVoltageLevelPadding().getBottom());
    }

    /**
     * Dispatching the snake lines to the right and to the left
     */
    private Side getSide(BusCell.Direction dNode1, boolean increment) {
        return ((increment && dNode1 == BusCell.Direction.BOTTOM) || (!increment && dNode1 == BusCell.Direction.TOP)) ? Side.RIGHT : Side.LEFT;
    }

    private double getXSnakeLine(Node node, Side side, LayoutParameters layoutParam) {
        double shiftLeftRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1) - 1, 0) * layoutParam.getHorizontalSnakeLinePadding();
        return node.getVoltageLevelGraph().getX() - layoutParam.getVoltageLevelPadding().getLeft()
            + (side == Side.LEFT ? -shiftLeftRight : shiftLeftRight + maxVoltageLevelWidth);
    }

    private double getYSnakeLine(Node node, BusCell.Direction dNode1, double decalV, LayoutParameters layoutParam) {
        if (dNode1 == BusCell.Direction.BOTTOM) {
            return node.getDiagramY() + decalV;
        } else {
            List<VoltageLevelGraph> vls = getGraph().getVoltageLevels();
            int iVl = vls.indexOf(node.getVoltageLevelGraph());
            if (iVl == 0) {
                return node.getDiagramY() - decalV;
            } else {
                VoltageLevelGraph vlAbove = vls.get(iVl - 1);
                return vlAbove.getY()
                    + vlAbove.getHeight() - layoutParam.getVoltageLevelPadding().getTop() - layoutParam.getVoltageLevelPadding().getBottom()
                    + decalV;
            }
        }
    }

    private boolean facingNodes(Node node1, Node node2) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);
        return (dNode1 == BusCell.Direction.BOTTOM && dNode2 == BusCell.Direction.TOP && getGraph().graphAdjacents(node1.getVoltageLevelGraph(), node2.getVoltageLevelGraph()))
            || (dNode1 == BusCell.Direction.TOP && dNode2 == BusCell.Direction.BOTTOM && getGraph().graphAdjacents(node2.getVoltageLevelGraph(), node1.getVoltageLevelGraph()));
    }
}
