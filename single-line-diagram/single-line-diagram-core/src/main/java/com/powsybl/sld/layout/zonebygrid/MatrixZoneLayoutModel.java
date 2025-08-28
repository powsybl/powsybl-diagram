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
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayoutModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixZoneLayoutModel.class);

    private final Matrix matrix;

    private AvailabilityGrid pathFinderGrid;

    private final int snakelinePadding;

    public MatrixZoneLayoutModel(String[][] ids, LayoutParameters layoutParameters) {
        this.matrix = new Matrix(ids.length, ids[0].length, layoutParameters);
        this.snakelinePadding = layoutParameters.getZoneLayoutSnakeLinePadding();
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
        return pathfinder.findShortestPath(pathFinderGrid, p1, p2);
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
        int pathMinX = pointX - snakelinePadding;
        int pathMaxX = pointX + snakelinePadding;
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

    public void computePathFindingGrid(ZoneGraph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        int width = (int) graph.getWidth();
        int height = (int) graph.getHeight();

        pathFinderGrid = new AvailabilityGrid(width, height);

        // Horizontal hallways lines
        computeHorizontalHallwaysAvailability(width, layoutParameters);

        // Vertical hallways lines
        computeVerticalHallwaysAvailability(height, layoutParameters);

        // Make available empty matrix cells
        computeMatrixCellsAvailability(layoutParameters);

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

            // Make unavailable all multi term nodes (3wt, 2wt, etc...) center
            graph.getMultiTermNodes().forEach(node -> {
                Point nodePoint = node.getCoordinates();
                int x = (int) nodePoint.getX();
                int y = (int) nodePoint.getY();
                pathFinderGrid.makeNotAvailable(x, y);
                node.getAdjacentEdges().forEach(edge -> {
                    if (edge instanceof BranchEdge branch) {
                        List<Point> points = Grid.getPointsAlongSnakeline(branch.getSnakeLine());
                        points.forEach(p -> pathFinderGrid.makeNotAvailable((int) p.getX(), (int) p.getY()));
                    }
                });
            });
            graph.getLineEdges().forEach(s -> Grid.getPointsAlongSnakeline(s.getSnakeLine()).forEach(p -> pathFinderGrid.makeNotAvailable((int) p.getX(), (int) p.getY())));
        });
    }

    private void computeMatrixCellsAvailability(LayoutParameters layoutParameters) {
        // Make empty cells available for snakeline computation
        List<MatrixCell> allCells = matrix.stream().toList();
        allCells.forEach(cell -> {
            int matrixCellWidth = (int) matrix.getMatrixCellWidth(cell.col());
            int matrixCellHeight = (int) matrix.getMatrixCellHeight(cell.row());
            int ssX = matrix.getX(cell.col());
            int ssY = matrix.getY(cell.row());
            // Horizontal lines
            int stepH = (int) layoutParameters.getHorizontalSnakeLinePadding();
            int deltaH = (matrixCellHeight % stepH) / 2;
            for (int x = ssX; x < ssX + matrixCellWidth; x++) {
                for (int y = ssY + deltaH + stepH; y < ssY + matrixCellHeight - deltaH; y += stepH) {
                    pathFinderGrid.makeAvailable(x, y);
                }
            }

            // Vertical lines
            int stepV = (int) layoutParameters.getVerticalSnakeLinePadding();
            int deltaV = (matrixCellWidth % stepV) / 2;
            for (int x = ssX + deltaV + stepV; x < ssX + matrixCellWidth - deltaV; x += stepV) {
                for (int y = ssY; y < ssY + matrixCellHeight; y++) {
                    pathFinderGrid.makeAvailable(x, y);
                }
            }
        });
    }

    private void computeHorizontalHallwaysAvailability(int width, LayoutParameters layoutParameters) {
        int startX = (int) layoutParameters.getDiagramPadding().getLeft();
        int endX = width - startX - (int) layoutParameters.getDiagramPadding().getRight();
        int nextY = 0;
        for (int r = 0; r < matrix.rowCount(); r++) {
            for (int x = startX; x < endX; x++) {
                for (int y = matrix.getY(r); y > matrix.getY(r) - snakelinePadding; y -= layoutParameters.getHorizontalSnakeLinePadding()) {
                    pathFinderGrid.makeAvailable(x, y);
                }
            }
            nextY += snakelinePadding + matrix.getMatrixCellHeight(r);
        }
        // Last snakelineMargin
        for (int x = startX; x < endX; x++) {
            for (int y = nextY; y < nextY + snakelinePadding; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                pathFinderGrid.makeAvailable(x, y);
            }
        }
    }

    private void computeVerticalHallwaysAvailability(int height, LayoutParameters layoutParameters) {
        int startY = (int) layoutParameters.getDiagramPadding().getTop();
        int endY = height - startY - (int) layoutParameters.getDiagramPadding().getBottom();
        int nextX = 0;
        for (int c = 0; c < matrix.columnCount(); c++) {
            for (int y = startY; y < endY; y++) {
                for (int x = matrix.getX(c); x > matrix.getX(c) - snakelinePadding; x -= layoutParameters.getVerticalSnakeLinePadding()) {
                    pathFinderGrid.makeAvailable(x, y);
                }
            }
            nextX += snakelinePadding + matrix.getMatrixCellWidth(c);
        }
        // Last snakelineMargin
        for (int y = startY; y < endY; y++) {
            for (int x = nextX; x < nextX + snakelinePadding; x += layoutParameters.getVerticalSnakeLinePadding()) {
                pathFinderGrid.makeAvailable(x, y);
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
