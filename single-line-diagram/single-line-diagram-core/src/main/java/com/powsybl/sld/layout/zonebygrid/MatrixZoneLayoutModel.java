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

import java.util.*;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayoutModel {

    private final Matrix matrix;

    private Grid pathFinderGrid;

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
                                      String ss2Id, Point p2, Direction d2) {
        matrix.get(ss1Id).ifPresent(matrixCell -> insertFreePathInSubstation(matrixCell.row(), p1, d1));
        matrix.get(ss2Id).ifPresent(matrixCell -> insertFreePathInSubstation(matrixCell.row(), p2, d2));

        // Use path finding algo
        return pathfinder.findShortestPath(pathFinderGrid, p1, p2);
    }

    private void insertFreePathInSubstation(int row, Point p, Direction d) {
        int x1 = (int) p.getX();
        int y1 = (int) p.getY();
        int ssY = matrix.getY(row);
        int min1Y = ssY - snakelinePadding;
        int max1Y = y1;
        if (d == Direction.BOTTOM) {
            min1Y = y1;
            max1Y = ssY + (int) matrix.getMatrixCellHeight(row) + snakelinePadding;
        }
        for (int y = min1Y; y <= max1Y; y++) {
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
        computeVerticalHallwaysAvailability(height, layoutParameters);

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

            // Make unavailable all multi term nodes (3wt, 2wt, etc...) center
            graph.getMultiTermNodes().forEach(node -> {
                pathFinderGrid.setAvailability(node.getCoordinates(), false);
                node.getAdjacentEdges().forEach(edge -> {
                    if (edge instanceof BranchEdge branch) {
                        List<Point> points = Grid.getPointsAlongSnakeline(branch.getSnakeLine());
                        points.forEach(p -> pathFinderGrid.setAvailability(p, false));
                    }
                });
            });
            graph.getLineEdges().forEach(s -> Grid.getPointsAlongSnakeline(s.getSnakeLine()).forEach(p -> pathFinderGrid.setAvailability(p, false)));
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
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }

            // Vertical lines
            int stepV = (int) layoutParameters.getVerticalSnakeLinePadding();
            int deltaV = (matrixCellWidth % stepV) / 2;
            for (int x = ssX + deltaV + stepV; x < ssX + matrixCellWidth - deltaV; x += stepV) {
                for (int y = ssY; y < ssY + matrixCellHeight; y++) {
                    pathFinderGrid.setAvailability(x, y, true);
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
                for (int y = matrix.getY(r); y >= matrix.getY(r) - snakelinePadding; y -= layoutParameters.getHorizontalSnakeLinePadding()) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
            nextY += snakelinePadding + matrix.getMatrixCellHeight(r);
        }
        // Last snakelineMargin
        for (int x = startX; x < endX; x++) {
            for (int y = nextY; y <= nextY + snakelinePadding; y += layoutParameters.getHorizontalSnakeLinePadding()) {
                pathFinderGrid.setAvailability(x, y, true);
            }
        }
    }

    private void computeVerticalHallwaysAvailability(int height, LayoutParameters layoutParameters) {
        int startY = (int) layoutParameters.getDiagramPadding().getTop();
        int endY = height - startY - (int) layoutParameters.getDiagramPadding().getBottom();
        int nextX = 0;
        for (int c = 0; c < matrix.columnCount(); c++) {
            for (int y = startY; y < endY; y++) {
                for (int x = matrix.getX(c); x >= matrix.getX(c) - snakelinePadding; x -= layoutParameters.getVerticalSnakeLinePadding()) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
            nextX += snakelinePadding + matrix.getMatrixCellWidth(c);
        }
        // Last snakelineMargin
        for (int y = startY; y < endY; y++) {
            for (int x = nextX; x <= nextX + snakelinePadding; x += layoutParameters.getVerticalSnakeLinePadding()) {
                pathFinderGrid.setAvailability(x, y, true);
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
