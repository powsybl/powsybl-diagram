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

    private final Map<String, MatrixCell> cellsById = new HashMap<>();
    private final List<MatrixCell> emptyCells = new ArrayList<>();

    private int matrixNbRow = 0;

    private int matrixNbCol = 0;

    private int matrixCellHeight = -1;

    private int matrixCellWidth = -1;

    private Grid pathFinderGrid;

    public void addSubstationSubgraph(SubstationGraph graph, int col, int row) {
        if (graph != null) {
            cellsById.put(graph.getId(), new MatrixCell(graph, col, row));
            matrixCellHeight = Math.max(matrixCellHeight, (int) graph.getHeight());
            matrixCellWidth = Math.max(matrixCellWidth, (int) graph.getWidth());
        } else {
            emptyCells.add(new MatrixCell(null, col, row));
        }
        matrixNbRow = Math.max(matrixNbRow, row + 1);
        matrixNbCol = Math.max(matrixNbCol, col + 1);
    }

    public int getMatrixCellWidth() {
        return matrixCellWidth;
    }

    public int getMatrixCellHeight() {
        return matrixCellHeight;
    }

    private int getX(int col, double snakelineMargin) {
        return ((col + 1) * (int) snakelineMargin) + (col * matrixCellWidth);
    }

    private int getY(int row, double snakelineMargin) {
        return (row + 1) * (int) snakelineMargin + row * matrixCellHeight;
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

        // Matrix cells grid lines
        computeMatrixCellsAvailability(layoutParameters);

        // Horizontal hallways lines
        computeHorizontalHallwaysAvailability(width, height, layoutParameters);

        // Vertical hallways lines
        computeVerticalHallwaysAvailability(width, height, layoutParameters);

        // Substations are not available
        computeSubstationsAvailability(layoutParameters);
    }

    private void computeSubstationsAvailability(LayoutParameters layoutParameters) {
        cellsById.values().forEach(cell -> {
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
        List<MatrixCell> allCells = new ArrayList<>(cellsById.values().stream().toList());
        // Make empty cells available for snakeline computation
        allCells.addAll(emptyCells);
        allCells.forEach(cell -> {
            int ssX = getX(cell.col(), snakelineMargin);
            int ssY = getY(cell.row(), snakelineMargin);
            for (int x = ssX - snakelineMargin; x < ssX - snakelineMargin + matrixCellWidth + snakelineMargin; x++) {
                for (int y = ssY - snakelineMargin; y < ssY - snakelineMargin + matrixCellHeight + snakelineMargin; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
            for (int x = ssX - snakelineMargin; x < ssX - snakelineMargin + matrixCellWidth + snakelineMargin; x += layoutParameters.getVerticalSnakeLinePadding()) {
                for (int y = ssY - snakelineMargin; y < ssY - snakelineMargin + matrixCellHeight + snakelineMargin; y++) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
        });
    }

    private void computeHorizontalHallwaysAvailability(int width, int height, LayoutParameters layoutParameters) {
        int snakelineMargin = (int) layoutParameters.getZoneLayoutSnakeLinePadding();
        for (int r = 0; r < matrixNbRow; r++) {
            for (int x = 0; x < width; x++) {
                for (int hy = 0; hy < height; hy += snakelineMargin + matrixCellHeight) {
                    for (int y = hy; y < hy + snakelineMargin; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                        pathFinderGrid.setAvailability(x, y, true);
                    }
                }
            }
        }
    }

    private void computeVerticalHallwaysAvailability(int width, int height, LayoutParameters layoutParameters) {
        int snakelineMargin = (int) layoutParameters.getZoneLayoutSnakeLinePadding();
        for (int c = 0; c < matrixNbCol; c++) {
            for (int y = 0; y < height; y++) {
                for (int hx = 0; hx < width; hx += snakelineMargin + matrixCellWidth) {
                    for (int x = hx; x < hx + snakelineMargin; x += layoutParameters.getVerticalSnakeLinePadding()) {
                        pathFinderGrid.setAvailability(x, y, true);
                    }
                }
            }
        }
    }

    public boolean contains(String id) {
        Objects.requireNonNull(id);
        return cellsById.containsKey(id);
    }
}
