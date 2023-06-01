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
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VerticalZoneLayout extends AbstractZoneLayout {

    private final InfosNbSnakeLinesVertical infosNbSnakeLines;
    private double maxVoltageLevelWidth;

    protected VerticalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.infosNbSnakeLines = infosNbSnakeLines;
    }

    public VerticalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        this(graph, sLayoutFactory, vLayoutFactory, InfosNbSnakeLinesVertical.create(graph));
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();

        double zoneWidth = 0;
        double y = 0;

        for (SubstationGraph subGraph : getGraph().getSubstations()) {
            // Calculate the objects coordinates inside the substation graph
            Layout vLayout = sLayoutFactory.create(subGraph, vLayoutFactory);
            vLayout.run(layoutParameters);

            zoneWidth = Math.max(zoneWidth, subGraph.getWidth());
            y += subGraph.getHeight();
        }

        double zoneHeight = y - diagramPadding.getTop();
        getGraph().setSize(zoneWidth, zoneHeight);

        maxVoltageLevelWidth = zoneWidth;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        manageAllSnakeLines(layoutParameters);

        adaptPaddingToSnakeLines(layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        VerticalLayout.adaptPaddingToSnakeLines(getGraph(), layoutParameters, infosNbSnakeLines);
        manageAllSnakeLines(layoutParameters);
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        List<Point> polyline;
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
            polyline = calculatePolylineSnakeLineForHorizontalLayout(getGraph(), layoutParam, node1, node2, increment, infosNbSnakeLinesH, yMin, yMax);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLinesH.getNbSnakeLinesTopBottom().get(TOP);
            Integer updatedNbLinesLeft = infosNbSnakeLinesH.getNbSnakeLinesVerticalBetween().get(graphId);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, BOTTOM, updatedNbLinesBottom);
            infosNbSnakeLines.setNbSnakeLinesTopBottom(graphId, TOP, updatedNbLinesTop);
            infosNbSnakeLines.getNbSnakeLinesLeftRight().put(Side.LEFT, updatedNbLinesLeft);

            return polyline;

        } else {
            polyline = new ArrayList<>();
            polyline.add(getGraph().getShiftedPoint(node1));
            addMiddlePoints(layoutParam, node1, node2, increment, polyline);
            polyline.add(getGraph().getShiftedPoint(node2));
            return polyline;
        }
    }

    protected void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2, boolean increment, List<Point> polyline) {
        Direction dNode1 = getNodeDirection(getGraph(), node1, 1);
        Direction dNode2 = getNodeDirection(getGraph(), node2, 2);

        // increment not needed for 3WT for the common node
        String vl1 = getGraph().getVoltageLevelInfos(node1).getId();
        int nbSnakeLines1 = increment
                ? infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl1, dNode1)
                : infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vl1, dNode1);
        double decal1V = VerticalLayout.getVerticalShift(layoutParam, dNode1, nbSnakeLines1);

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
            double decal2V = VerticalLayout.getVerticalShift(layoutParam, dNode2, nbSnakeLines2);

            double ySnakeLine1 = VerticalLayout.getYSnakeLine(getGraph(), node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = VerticalLayout.getYSnakeLine(getGraph(), node2, dNode2, decal2V, layoutParam);

            Side side = VerticalLayout.getSide(increment);
            double xSnakeLine = VerticalLayout.getXSnakeLine(getGraph(), node1, side, layoutParam, infosNbSnakeLines, maxVoltageLevelWidth);
            polyline.addAll(Point.createPointsList(p1.getX(), ySnakeLine1,
                    xSnakeLine, ySnakeLine1,
                    xSnakeLine, ySnakeLine2,
                    p2.getX(), ySnakeLine2));
        }
    }

    private boolean facingNodes(Node node1, Node node2) {
        Direction dNode1 = getNodeDirection(getGraph(), node1, 1);
        Direction dNode2 = getNodeDirection(getGraph(), node2, 2);
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        boolean isVl1Vl2Adjacent = false;
        boolean isVl2Vl1Adjacent = false;
        for (SubstationGraph substation : getGraph().getSubstations()) {
            isVl1Vl2Adjacent |= substation.graphAdjacents(vlGraph1, vlGraph2);
            isVl2Vl1Adjacent |= substation.graphAdjacents(vlGraph2, vlGraph1);
        }
        return (dNode1 == BOTTOM && dNode2 == TOP && isVl1Vl2Adjacent)
                || (dNode1 == TOP && dNode2 == BOTTOM && isVl2Vl1Adjacent);
    }
}
