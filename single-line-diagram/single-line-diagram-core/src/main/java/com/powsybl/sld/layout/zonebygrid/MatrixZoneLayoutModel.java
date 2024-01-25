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

import java.util.*;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayoutModel {

    private final Matrix matrix;

    private Grid pathFinderGrid;

    public MatrixZoneLayoutModel(String[][] ids) {
        this.matrix = new Matrix(ids.length, ids[0].length);
    }

    public void addSubstationGraph(SubstationGraph graph, int row, int col) {
        this.matrix.set(row, col, new MatrixCell(graph, row, col));
    }

    private int getX(int col, double snakelineMargin) {
        int matrixCellWidth = 0;
        for (int c = 0; c < col; c++) {
            matrixCellWidth += matrix.getMatrixCellWidth(c);
        }
        return ((col + 1) * (int) snakelineMargin) + matrixCellWidth;
    }

    private int getY(int row, double snakelineMargin) {
        int matrixCellHeight = 0;
        for (int r = 0; r < row; r++) {
            matrixCellHeight += matrix.getMatrixCellHeight(r);
        }
        return (row + 1) * (int) snakelineMargin + matrixCellHeight;
    }

    public List<Point> buildSnakeline(PathFinder pathfinder,
                                      Point p1, Direction d1,
                                      Point p2, Direction d2,
                                      double snakelineMargin) {
        insertFreePathInSubstation(p1, d1, snakelineMargin);
        insertFreePathInSubstation(p2, d2, snakelineMargin);

        // Use path finding algo
        return pathfinder.findShortestPath(pathFinderGrid, p1, p2);
    }

    private void insertFreePathInSubstation(Point p, Direction d, double snakelineMargin) {
        int dy = 1;

        int x1 = (int) p.getX();
        int y1 = (int) p.getY();
        int ss1Y = y1 + (int) snakelineMargin * (d == Direction.TOP ? -1 : 1);
        int min1Y = Math.max(Math.min(y1, ss1Y), 0);
        int max1Y = Math.max(y1, ss1Y) + dy;
        for (int y = min1Y; y < max1Y; y++) {
            pathFinderGrid.setAvailability(x1, y, true);
        }
    }

    public void computePathFindingGrid(ZoneGraph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        int width = (int) graph.getWidth();
        int height = (int) graph.getHeight();

        pathFinderGrid = new Grid(width, height);

        // Horizontal hallways lines
        computeHorizontalHallwaysAvailability(width, layoutParameters);

        // Vertical hallways lines
        computeVerticalHallwaysAvailability(width, height, layoutParameters);

        // Make available all matrix cells
        computeMatrixCellsAvailability(layoutParameters);

        // Make unavailable all voltagelevels
        computeSubstationsAvailability(layoutParameters);
    }

    private void computeSubstationsAvailability(LayoutParameters layoutParameters) {
        // For each not empty cells
        matrix.stream().filter(c -> !c.isEmpty()).forEach(cell -> {
            BaseGraph graph = cell.graph();
            graph.getVoltageLevelStream().forEach(vlGraph -> {
                double elementaryWidth = layoutParameters.getCellWidth() / 2; // the elementary step within a voltageLevel Graph is half a cell width
                double widthNoPadding = vlGraph.getMaxH() * elementaryWidth;
                double heightNoPadding = vlGraph.getInnerHeight(layoutParameters.getVerticalSpaceBus());
                int xGraph = (int) vlGraph.getX();
                int yGraph = (int) vlGraph.getY();

                for (int x = xGraph; x < xGraph + widthNoPadding; x++) {
                    for (int y = yGraph; y < yGraph + heightNoPadding; y++) {
                        pathFinderGrid.setAvailability(x, y, false);
                    }
                }
            });
        });
    }

    private void computeMatrixCellsAvailability(LayoutParameters layoutParameters) {
        int snakelineMargin = (int) layoutParameters.getZoneLayoutSnakeLinePadding();
        // Make empty cells available for snakeline computation
        List<MatrixCell> allCells = matrix.stream().toList();
        allCells.forEach(cell -> {
            double matrixCellWidth = matrix.getMatrixCellWidth(cell.col());
            double matrixCellHeight = matrix.getMatrixCellHeight(cell.row());
            int ssX = getX(cell.col(), snakelineMargin);
            int ssY = getY(cell.row(), snakelineMargin);

            for (int x = ssX - snakelineMargin; x < ssX + matrixCellWidth + snakelineMargin; x++) {
                for (int y = ssY - snakelineMargin; y < ssY + matrixCellHeight + snakelineMargin; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
            for (int x = ssX - snakelineMargin; x < ssX + matrixCellWidth + snakelineMargin; x += layoutParameters.getVerticalSnakeLinePadding()) {
                for (int y = ssY - snakelineMargin; y < ssY + matrixCellHeight + snakelineMargin; y++) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
        });
    }

    private void computeHorizontalHallwaysAvailability(int width, LayoutParameters layoutParameters) {
        int snakelineMargin = (int) layoutParameters.getZoneLayoutSnakeLinePadding();
        int nextY = 0;
        for (int r = 0; r < matrix.rowCount(); r++) {
            double matrixCellHeight = matrix.getMatrixCellHeight(r);
            for (int x = 0; x < width; x++) {
                for (int y = nextY; y < nextY + snakelineMargin; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
            nextY += snakelineMargin + matrixCellHeight;
        }
        // Last snakelineMargin
        for (int x = 0; x < width; x++) {
            for (int y = nextY; y < nextY + snakelineMargin; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                pathFinderGrid.setAvailability(x, y, true);
            }
        }
    }

    private void computeVerticalHallwaysAvailability(int width, int height, LayoutParameters layoutParameters) {
        int snakelineMargin = (int) layoutParameters.getZoneLayoutSnakeLinePadding();
        for (int c = 0; c < matrix.columnCount(); c++) {
            double matrixCellWidth = matrix.getMatrixCellWidth(c);
            for (int y = 0; y < height; y++) {
                for (int hx = 0; hx < width; hx += snakelineMargin + matrixCellWidth) {
                    for (int x = hx; x < hx + snakelineMargin; x += layoutParameters.getVerticalSnakeLinePadding()) {
                        pathFinderGrid.setAvailability(x, y, true);
                    }
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
