/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.pathfinding.StateGrid;
import com.powsybl.sld.layout.pathfinding.ZoneLayoutPathFinderFactory;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.IntPoint;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.BaseGraph;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.MiddleTwtNode;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * Zone layout that places substations at given top-left coordinates (x, y).
 * <p>
 * Each substation is assigned a desired top-left position returned by the abstract
 * method {@code computeSubstationPositions}. Child classes are responsible for implementing
 * this method and making sure the substations do not overlap (including margin for snakelines).
 *
 * @author Frédéric Sabot {@literal <frederic.sabot at haulogy.net>}
 */
public abstract class AbstractPositionedZoneLayout extends AbstractZoneLayout {

    /**
     * Returns the final top-left position for each substation.
     * Sub-layouts have already been run by the time this method is called.
     */
    protected abstract List<Pair<String, Point>> computeSubstationPositions(LayoutParameters layoutParameters);

    /** Path-finding grid built in manageSnakeLines, used in calculatePolylineSnakeLine. */
    private StateGrid pathFinderGrid;

    protected AbstractPositionedZoneLayout(ZoneGraph graph,
                                   ZoneLayoutPathFinderFactory pathFinderFactory,
                                   SubstationLayoutFactory sLayoutFactory,
                                   VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.pathFinder = Objects.requireNonNull(pathFinderFactory).create();
    }

    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        getGraph().getSubstations().forEach(sg -> layoutBySubstation.get(sg).run(layoutParameters));
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        List<Pair<String, Point>> positions = computeSubstationPositions(layoutParameters);
        for (Pair<String, Point> entry : positions) {
            String id = entry.getFirst();
            SubstationGraph sGraph = getGraph().getSubstationGraph(id);
            if (sGraph == null) {
                throw new PowsyblException("Substation '" + id + "' was not found in zone graph '" + getGraph().getId() + "'");
            }
            Point topLeft = entry.getSecond();
            move(sGraph, topLeft.getX(), topLeft.getY());
        }

        double zoneWidth = getGraph().getSubstations().stream()
            .mapToDouble(sg -> getSubstationOrigin(sg).getX() + sg.getWidth()).max().orElse(0);
        double zoneHeight = getGraph().getSubstations().stream()
            .mapToDouble(sg -> getSubstationOrigin(sg).getY() + sg.getHeight()).max().orElse(0);
        getGraph().setSize(
            diagramPadding.left() + zoneWidth + diagramPadding.right(),
            diagramPadding.top() + zoneHeight + diagramPadding.bottom()
        );
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        computePathFindingGrid(layoutParameters);
        manageSnakeLines(getGraph(), layoutParameters);
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Pair<Node, Node> nodes,
                                                     boolean increment) {
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        SubstationGraph ss1Graph = getGraph().getSubstationGraph(node1).orElse(null);
        SubstationGraph ss2Graph = getGraph().getSubstationGraph(node2).orElse(null);

        if (ss1Graph == null || ss2Graph == null
                || !containsSubstation(ss1Graph.getId())
                || !containsSubstation(ss2Graph.getId())) {
            return new ArrayList<>();
        }

        Point p1 = vlGraph1.getShiftedPoint(node1);
        Point p2 = vlGraph2.getShiftedPoint(node2);
        Direction dNode1 = getNodeDirection(node1, 1);
        Direction dNode2 = getNodeDirection(node2, 2);

        Point p1OutsideVl = getPathFindingPointOutsideVoltageLevel(p1, dNode1, layoutParameters);
        Point p2OutsideVl = getPathFindingPointOutsideVoltageLevel(p2, dNode2, layoutParameters);

        List<Point> polyline = new ArrayList<>();
        polyline.add(p1);
        polyline.add(p1OutsideVl);
        polyline.addAll(pathFinder.findShortestPath(pathFinderGrid, p1OutsideVl, p2OutsideVl));
        polyline.add(p2OutsideVl);
        polyline.add(p2);
        return polyline;
    }

    // -------------------------------------------------------------------------
    // Path-finding grid
    // -------------------------------------------------------------------------

    private void computePathFindingGrid(LayoutParameters layoutParameters) {
        int width = (int) getGraph().getWidth();
        int height = (int) getGraph().getHeight();
        pathFinderGrid = new StateGrid(width, height);

        makeVoltageLevelsUnavailable(layoutParameters);
    }

    private void makeVoltageLevelsUnavailable(LayoutParameters layoutParameters) {
        // For each not empty cell, make it not available
        for (BaseGraph graph : getGraph().getSubstations()) {
            graph.getVoltageLevelStream().forEach(vlGraph -> {
                double elementaryWidth = layoutParameters.getCellWidth() / 2; // the elementary step within a voltageLevel Graph is half a cell width
                double widthNoPadding = vlGraph.getMaxH() * elementaryWidth;
                double heightNoPadding = vlGraph.getInnerHeight(layoutParameters.getVerticalSpaceBus());
                int xGraph = (int) vlGraph.getX();
                int yGraph = (int) vlGraph.getY();

                LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();

                for (int x = xGraph - ((int) vlPadding.left() - 1); x < xGraph + widthNoPadding + (int) vlPadding.right(); x++) {
                    for (int y = yGraph - ((int) vlPadding.top() - 1); y < yGraph + heightNoPadding + (int) vlPadding.bottom(); y++) {
                        pathFinderGrid.setUnavailable(x, y);
                    }
                }
            });
        }

        makeWTSnakelineWire(graph);
    }

    /** Returns the top-left corner of a substation as the minimum VL coordinate. */
    private Point getSubstationOrigin(SubstationGraph sg) {
        double x = sg.getVoltageLevels().stream().mapToDouble(VoltageLevelGraph::getX).min().orElse(0);
        double y = sg.getVoltageLevels().stream().mapToDouble(VoltageLevelGraph::getY).min().orElse(0);
        return new Point(x, y);
    }

    private boolean containsSubstation(String id) {
        return getGraph().getSubstationGraph(id) != null;
    }

    /**
     * Make unavailable all multi term nodes (3wt, 2wt, etc...), as well as the snakeline of those nodes
     * @param graph the graph of the SLD, representing the different substations
     */
    private void makeWTSnakelineWire(BaseGraph graph) {
        for (MiddleTwtNode node : graph.getMultiTermNodes()) {
            for (Edge edge : node.getAdjacentEdges()) {
                if (edge instanceof BranchEdge branchEdge) {
                    List<IntPoint> points = StateGrid.getPointsAlongSnakeline(branchEdge.getSnakeLine());
                    pathFinderGrid.setUnavailable(points);
                }
            }
        }
    }

    /**
     * Gets a point outside the unavailable area of the voltage level to which the given <code>point</code> belongs, in the <code>direction</code>.
     * @param point the point from which to make the grid available (generally the end of a connection, where we want a snakeline to start or end)
     * @param direction the direction of the connection (this should only be top or bottom)
     * @param layoutParameters parameters of the layout, used to know the padding of the voltage level
     */
    private Point getPathFindingPointOutsideVoltageLevel(Point point, Direction direction, LayoutParameters layoutParameters) {
        LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();
        // remember that the y-axis is oriented downwards, meaning the smallest y is the one closest to the top
        double pointY = switch (direction) {
            case TOP -> point.getY() - vlPadding.top();
            case BOTTOM -> point.getY() + vlPadding.bottom();
            default -> throw new IllegalArgumentException(
                String.format("Unknown direction for inserting a free path in substation: Point: %s | Direction: %s", point, direction));
        };
        return new Point(point.getX(), pointY);

    }
}
