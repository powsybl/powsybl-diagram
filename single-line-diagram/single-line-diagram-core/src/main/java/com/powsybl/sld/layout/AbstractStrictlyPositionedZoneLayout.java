/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.pathfinding.Grid;
import com.powsybl.sld.layout.pathfinding.ZoneLayoutPathFinderFactory;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * Zone layout that places substations at desired top-left coordinates (x, y).
 * <p>
 * Each substation is assigned a desired top-left position via a {@code List<Pair<String, Point>>}.
 * Child classes are responsible for running the sub-layouts and making sure the
 * substations do not overlap (including margin for snakelines).
 *
 * @author Frédéric Sabot {@literal <frederic.sabot at haulogy.net>}
 */
public abstract class AbstractStrictlyPositionedZoneLayout extends AbstractZoneLayout {

    /**
     * Returns the final top-left position for each substation, after running sub-layouts.
     * Implementations are responsible for calling {@code layoutBySubstation.get(sGraph).run(layoutParameters)}
     * for each substation before returning.
     */
    protected abstract List<Pair<String, Point>> computeSubstationPositions(LayoutParameters layoutParameters);

    /** Path-finding grid built in manageSnakeLines, used in calculatePolylineSnakeLine. */
    private Grid pathFinderGrid;

    protected AbstractStrictlyPositionedZoneLayout(ZoneGraph graph,
                                   ZoneLayoutPathFinderFactory pathFinderFactory,
                                   SubstationLayoutFactory sLayoutFactory,
                                   VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.pathFinder = Objects.requireNonNull(pathFinderFactory).create();
    }

    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        List<Pair<String, Point>> positions = computeSubstationPositions(layoutParameters);
        for (Pair<String, Point> entry : positions) {
            String id = entry.getFirst();
            SubstationGraph sGraph = getGraph().getSubstationGraph(id);
            if (sGraph == null) {
                throw new PowsyblException("Substation '" + id + "' was not found in zone graph '" + getGraph().getId() + "'");
            }
            // layoutBySubstation.get(sGraph).run(layoutParameters);  // Already run by child
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

        // Open exit corridors from each node into the margin hallway, then find path
        insertFreePathInSubstation(ss1Graph, p1, dNode1, layoutParameters);
        insertFreePathInSubstation(ss2Graph, p2, dNode2, layoutParameters);

        List<Point> polyline = new ArrayList<>();
        polyline.add(p1);
        polyline.addAll(pathFinder.findShortestPath(pathFinderGrid, p1, p2));
        polyline.add(p2);
        return polyline;
    }

    // -------------------------------------------------------------------------
    // Path-finding grid
    // -------------------------------------------------------------------------

    private void computePathFindingGrid(LayoutParameters layoutParameters) {
        int width = (int) getGraph().getWidth();
        int height = (int) getGraph().getHeight();
        pathFinderGrid = new Grid(width, height);

        int margin = layoutParameters.getZoneLayoutSnakeLinePadding();
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();

        // Mark the margin bands around every substation as available (hallways),
        // then mark substation interiors as unavailable.
        for (SubstationGraph sg : getGraph().getSubstations()) {
            Point origin = getSubstationOrigin(sg);
            int ssX = (int) origin.getX();
            int ssY = (int) origin.getY();
            int ssW = (int) sg.getWidth();
            int ssH = (int) sg.getHeight();

            // Horizontal hallway above and below this substation
            int hStep = (int) layoutParameters.getHorizontalSnakeLinePadding();
            int zoneW = (int) (width - diagramPadding.left() - diagramPadding.right());
            int startX = (int) diagramPadding.left();
            for (int x = startX; x < startX + zoneW; x++) {
                for (int y = ssY - margin; y < ssY; y += Math.max(1, hStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
                for (int y = ssY + ssH; y <= ssY + ssH + margin; y += Math.max(1, hStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }

            // Vertical hallway left and right of this substation
            int vStep = (int) layoutParameters.getVerticalSnakeLinePadding();
            int zoneH = (int) (height - diagramPadding.top() - diagramPadding.bottom());
            int startY = (int) diagramPadding.top();
            for (int y = startY; y < startY + zoneH; y++) {
                for (int x = ssX - margin; x < ssX; x += Math.max(1, vStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
                for (int x = ssX + ssW; x <= ssX + ssW + margin; x += Math.max(1, vStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
        }

        // Mark substation interiors (voltage levels + existing edges) unavailable
        for (SubstationGraph sg : getGraph().getSubstations()) {
            sg.getVoltageLevelStream().forEach(vlGraph -> {
                LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();
                double elementaryWidth = layoutParameters.getCellWidth() / 2;
                double wNoPad = vlGraph.getMaxH() * elementaryWidth;
                double hNoPad = vlGraph.getInnerHeight(layoutParameters.getVerticalSpaceBus());
                int xVl = (int) vlGraph.getX();
                int yVl = (int) vlGraph.getY();
                for (int x = xVl - ((int) vlPadding.left() - 1); x < xVl + wNoPad + (int) vlPadding.right(); x++) {
                    for (int y = yVl - ((int) vlPadding.top() - 1); y < yVl + hNoPad + (int) vlPadding.bottom(); y++) {
                        pathFinderGrid.setAvailability(x, y, false);
                    }
                }
            });
            sg.getMultiTermNodes().forEach(node -> {
                pathFinderGrid.setAvailability(node.getCoordinates(), false);
                node.getAdjacentEdges().forEach(edge -> {
                    if (edge instanceof BranchEdge branch) {
                        Grid.getPointsAlongSnakeline(branch.getSnakeLine())
                            .forEach(p -> pathFinderGrid.setAvailability(p, false));
                    }
                });
            });
            sg.getLineEdges().forEach(e ->
                Grid.getPointsAlongSnakeline(e.getSnakeLine())
                    .forEach(p -> pathFinderGrid.setAvailability(p, false)));
        }
    }

    /**
     * Opens a vertical exit corridor from node {@code p} into the margin hallway
     * above or below the substation, then a horizontal strip across the full
     * substation width — exactly as MatrixZoneLayoutModel does per cell.
     */
    private void insertFreePathInSubstation(SubstationGraph sg, Point p, Direction d,
                                            LayoutParameters layoutParameters) {
        LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();
        int margin = layoutParameters.getZoneLayoutSnakeLinePadding();

        double x1 = p.getX();
        double y1 = p.getY();
        double minY = d == Direction.BOTTOM ? y1 : y1 - vlPadding.top();
        double maxY = d == Direction.BOTTOM ? y1 + vlPadding.bottom() : y1;

        // Vertical corridor out of the voltage level
        for (int y = (int) minY; y <= (int) maxY; y++) {
            pathFinderGrid.setAvailability(x1, y, true);
        }

        // Horizontal strip across the substation + margin so paths can route laterally
        int ssX = (int) getSubstationOrigin(sg).getX();
        int ssW = (int) sg.getWidth();
        double exitY = d == Direction.TOP ? minY : maxY;
        for (int x = ssX - margin; x < ssX + ssW + margin; x++) {
            pathFinderGrid.setAvailability(x, exitY, true);
        }
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
}
