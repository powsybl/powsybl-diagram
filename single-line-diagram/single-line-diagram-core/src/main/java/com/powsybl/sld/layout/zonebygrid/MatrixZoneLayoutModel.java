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
 * @author Thomas Adam <tadam at neverhack.com>
 */
public class MatrixZoneLayoutModel {

    private final Map<String, MatrixCell> cellsById = new HashMap<>();
    private final List<MatrixCell> emptyCells = new ArrayList<>();

    private int matrixNbRow = 0;

    private int matrixNbCol = 0;

    private int matrixCellHeight = -1;

    private int matrixCellWidth = -1;

    private final int snakelineHallwayWidth;

    private Grid pathFinderGrid;

    public MatrixZoneLayoutModel(int hallway) {
        this.snakelineHallwayWidth = hallway;
    }

    public void addGraph(SubstationGraph graph, int col, int row) {
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

    public int getSnakelineHallwayWidth() {
        return snakelineHallwayWidth;
    }

    public int getMatrixCellWidth() {
        return matrixCellWidth;
    }

    public int getMatrixCellHeight() {
        return matrixCellHeight;
    }

    public int getX(String id) {
        MatrixCell cell = cellsById.get(id);
        return getX(cell.col());
    }

    public int getX(int col) {
        return ((col + 1) * snakelineHallwayWidth) + (col * matrixCellWidth);
    }

    public int getY(String id) {
        return getY(id, Direction.TOP);
    }

    public int getY(int row) {
        return getY(row, Direction.TOP);
    }

    public int getY(int row, Direction direction) {
        return (row + 1) * snakelineHallwayWidth + (row + (direction == Direction.TOP ? 0 : 1)) * matrixCellHeight;
    }

    public int getY(String id, Direction direction) {
        MatrixCell cell = cellsById.get(id);
        int row = cell.row();
        return getY(row, direction);
    }

    public List<Point> buildSnakeline(PathFinder pathfinder,
                                      String ss1Id, Point p1, Direction d1,
                                      String ss2Id, Point p2, Direction d2) {
        insertFreePathInSubstation(ss1Id, p1, d1, ss2Id, p2, d2);

        // Use path finding algo
        List<Point> snakeLine = pathfinder.toSnakeLine(pathfinder.findShortestPath(pathFinderGrid,
                (int) p1.getX(), (int) p1.getY(),
                (int) p2.getX(), (int) p2.getY(),
                true));

        // Only for debug
        pathFinderGrid.dumpToFile("out.txt");

        return snakeLine;
    }

    private void insertFreePathInSubstation(String id1, Point p1, Direction d1,
                                            String id2, Point p2, Direction d2) {
        int dy = 1;

        int x1 = (int) p1.getX();
        int y1 = (int) p1.getY();
        int ss1Y = getY(id1, d1) + snakelineHallwayWidth * (d1 == Direction.TOP ? -1 : 1);
        int min1Y = Math.max(Math.min(y1, ss1Y), 0);
        int max1Y = Math.max(y1, ss1Y) + dy;
        for (int y = min1Y; y < max1Y; y++) {
            pathFinderGrid.setAvailability(x1, y, true);
        }

        int x2 = (int) p2.getX();
        int y2 = (int) p2.getY();
        int ss2Y = getY(id2, d2) + snakelineHallwayWidth * (d2 == Direction.TOP ? -1 : 1);
        int min2Y = Math.max(Math.min(y2, ss2Y), 0);
        int max2Y = Math.max(y2, ss2Y) + dy;
        for (int y = min2Y; y < max2Y; y++) {
            pathFinderGrid.setAvailability(x2, y, true);
        }
    }

    public void computePathFindingGrid(ZoneGraph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        int width = (int) graph.getWidth();
        int height = (int) graph.getHeight();

        pathFinderGrid = new Grid(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pathFinderGrid.setAvailability(x, y, false);
            }
        }

        // Substations
        computeSubstationsAvailability();

        // Empty cells
        computeEmptyCellsAvailability();

        // Horizontal hallways
        computeHorizontalHallwaysAvailability(width, height, layoutParameters);

        // Vertical hallways
        computeVerticalHallwaysAvailability(width, height, layoutParameters);
    }

    private void computeSubstationsAvailability() {
        cellsById.keySet().forEach(id -> {
            int ssX = getX(id);
            int ssY = getY(id);
            for (int x = ssX; x < ssX + matrixCellWidth; x++) {
                for (int y = ssY; y < ssY + matrixCellHeight; y++) {
                    pathFinderGrid.setAvailability(x, y, false);
                }
            }
        });
    }

    private void computeEmptyCellsAvailability() {
        emptyCells.forEach(cell -> {
            int ssX = getX(cell.col());
            int ssY = getY(cell.row());
            for (int x = ssX; x < ssX + matrixCellWidth; x++) {
                for (int y = ssY; y < ssY + matrixCellHeight; y++) {
                    pathFinderGrid.setAvailability(x, y, false);
                }
            }
        });
    }

    private void computeHorizontalHallwaysAvailability(int width, int height, LayoutParameters layoutParameters) {
        for (int r = 0; r < matrixNbRow; r++) {
            for (int x = 0; x < width; x++) {
                for (int hy = 0; hy < height; hy += snakelineHallwayWidth + matrixCellHeight) {
                    for (int y = hy; y < hy + snakelineHallwayWidth; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                        pathFinderGrid.setAvailability(x, y, true);
                    }
                }
            }
        }
    }

    private void computeVerticalHallwaysAvailability(int width, int height, LayoutParameters layoutParameters) {
        for (int c = 0; c < matrixNbCol; c++) {
            for (int y = 0; y < height; y++) {
                for (int hx = 0; hx < width; hx += snakelineHallwayWidth + matrixCellWidth) {
                    for (int x = hx; x < hx + snakelineHallwayWidth; x += layoutParameters.getVerticalSnakeLinePadding()) {
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
