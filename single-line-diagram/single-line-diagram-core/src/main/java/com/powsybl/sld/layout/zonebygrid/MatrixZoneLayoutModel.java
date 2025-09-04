/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.zonebygrid;

import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.pathfinding.*;
import com.powsybl.sld.model.coordinate.*;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayoutModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixZoneLayoutModel.class);

    private final Matrix matrix;

    private AvailabilityGrid pathFinderGrid;

    public MatrixZoneLayoutModel(String[][] ids, LayoutParameters layoutParameters) {
        this.matrix = new Matrix(ids.length, ids[0].length, layoutParameters);
    }

    public void addSubstationGraph(SubstationGraph graph, int row, int col) {
        this.matrix.set(row, col, new MatrixCell(graph, row, col));
    }

    public List<Point> buildSnakeline(PathFinder pathfinder,
                                      String ss1Id, Point p1, Direction d1,
                                      String ss2Id, Point p2, Direction d2,
                                      LayoutParameters layoutParameters) {
        matrix.get(ss1Id).ifPresent(matrixCell -> insertFreePathInSubstation(p1, d1, layoutParameters));
        matrix.get(ss2Id).ifPresent(matrixCell -> insertFreePathInSubstation(p2, d2, layoutParameters));

        // Use path finding algo
        return pathfinder.findBestPath(pathFinderGrid, p1, p2);
    }

    /**
     * Create a path that is available, starting from the point, in the given direction. By default, voltage levels are not available for tracing snakelines,
     * however for making a snakeline starting from a VL, a path needs to start inside the VL, so a line needs to be made from the point inside the voltage level,
     * to the outside of the VL (including the padding of the VL).
     * Note that only the places not available will become available, meaning areas that already have a wire will not become marked as available
     * @param point the point from which to make the grid available (generally the end of a connection, where we want a snakeline to start or end)
     * @param direction the direction of the connection (this should only be top or bottom)
     * @param layoutParameters parameters of the layout, used to know the padding of the voltage level
     */
    private void insertFreePathInSubstation(Point point, Direction direction, LayoutParameters layoutParameters) {
        LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();
        int pointX = (int) point.getX();
        // make it somewhat large, but should this even be done ? maybe it should only be a single line for getting out of the substation
        // making it a bit larger allows multiple snakeline to start from the same point without overlapping, but would that even happen ?
        int pathMinX = pointX - 1;
        int pathMaxX = pointX + 1;
        double pointY = point.getY();
        // all path are either in the top or bottom direction
        int pathMinY = 0;
        int pathMaxY = 0;
        // remember that the y-axis is oriented downwards, meaning the smallest y is the one closest to the top of the image
        switch (direction) {
            case TOP -> {
                pathMinY = (int) (pointY - vlPadding.getTop());
                pathMaxY = (int) pointY;
            }
            case BOTTOM -> {
                pathMinY = (int) pointY;
                pathMaxY = (int) (pointY + vlPadding.getBottom());
            }
            default -> {
                LOGGER.error("Unknown direction for inserting a free path in substation: Point: {} | Direction: {}", point, direction);
                return;
            }
        }

        // should this go to max or stop just before ?
        for (int y = pathMinY; y <= pathMaxY; ++y) {
            for (int x = pathMinX; x <= pathMaxX; ++x) {
                // this prevents marking wire and around wire area as available
                if (pathFinderGrid.isNotAvailable(x, y)) {
                    pathFinderGrid.makeAvailable(x, y);
                }
            }
        }
    }

    /**
     * Make unavailable or wire all areas that are already occupied, that is: voltage levels, WT and the snakelines of WT
     * @param graph the graph of the SLD, representing the different substations
     * @param layoutParameters the parameters of the layout
     */
    public void fillPathFindingGridStates(ZoneGraph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        int width = (int) graph.getWidth();
        int height = (int) graph.getHeight();

        // dividing by 10 means we can fit up to 10 snakelines between zones before they start going on the areas of one another
        // this is totally arbitrary
        pathFinderGrid = new AvailabilityGrid(width, height, layoutParameters.getZoneLayoutSnakeLinePadding() / 10);

        // Make unavailable all voltagelevels
        computeSubstationsAvailability(layoutParameters);
    }

    private void computeSubstationsAvailability(LayoutParameters layoutParameters) {
        // For each not empty cells, make it not available
        matrix.stream().filter(c -> !c.isEmpty()).forEach(cell -> {
            BaseGraph graph = cell.graph();
            graph.getVoltageLevelStream().forEach(vlGraph -> {
                double elementaryWidth = layoutParameters.getCellWidth() / 2; // the elementary step within a voltageLevel Graph is half a cell width
                double widthNoPadding = vlGraph.getMaxH() * elementaryWidth;
                double heightNoPadding = vlGraph.getInnerHeight(layoutParameters.getVerticalSpaceBus());
                int xGraph = (int) vlGraph.getX();
                int yGraph = (int) vlGraph.getY();

                LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();

                for (int x = xGraph - ((int) vlPadding.getLeft() - 1); x < xGraph + widthNoPadding + (int) vlPadding.getRight(); x++) {
                    for (int y = yGraph - ((int) vlPadding.getTop() - 1); y < yGraph + heightNoPadding + (int) vlPadding.getBottom(); y++) {
                        pathFinderGrid.makeNotAvailable(x, y);
                    }
                }
            });

            makeWTSnakelineWire(graph);
        });
    }

    /**
     * Make unavailable all multi term nodes (3wt, 2wt, etc...), as well as the snakeline of those nodes
     * @param graph the graph of the SLD, representing the different substations
     */
    private void makeWTSnakelineWire(BaseGraph graph) {
        for (MiddleTwtNode node : graph.getMultiTermNodes()) {
            for (Edge edge : node.getAdjacentEdges()) {
                if (edge instanceof BranchEdge branchEdge) {
                    List<PointInteger> points = AvailabilityGrid.getPointsAlongSnakeline(branchEdge.getSnakeLine());
                    pathFinderGrid.makeWirePath(points);
                }
            }
        }
    }

    public boolean contains(String otherId) {
        Objects.requireNonNull(otherId);
        return matrix.stream().map(MatrixCell::getId).anyMatch(id -> id.equals(otherId));
    }

    public Matrix getMatrix() {
        return matrix;
    }
}
