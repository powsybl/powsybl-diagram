/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.layout.algo.*;
import com.powsybl.sld.layout.zonebygrid.*;
import com.powsybl.sld.model.coordinate.*;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public class MatrixZoneLayoutModel {

    private final Map<String, MatrixCell> cellsById = new HashMap<>();
    private final List<MatrixCell> emptyCells = new ArrayList<>();

    private int matrixCellHeight = -1;

    private int matrixCellWidth = -1;

    private int snakelineHallwayWidth = 1;

    private float[][] privateTiles;

    private static final float WALKABLE = 1.0f;

    private static final float NOT_WALKABLE = 0.0f;

    protected MatrixZoneLayoutModel(int hallway) {
        this.snakelineHallwayWidth = hallway;
    }

    public MatrixZoneLayoutModel addGraph(BaseGraph graph, int x, int y) {
        if (graph != null) {
            cellsById.put(graph.getId(), new MatrixCell(graph, x, y));
            setCellHeight(Math.max(matrixCellHeight, (int) graph.getHeight()));
            setCellWidth(Math.max(matrixCellWidth, (int) graph.getWidth()));
        } else {
            emptyCells.add(new MatrixCell(null, x, y));
        }
        return this;
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

    public MatrixZoneLayoutModel setCellWidth(int width) {
        this.matrixCellWidth = width;
        return this;
    }

    public MatrixZoneLayoutModel setCellHeight(int height) {
        this.matrixCellHeight = height;
        return this;
    }

    public MatrixZoneLayoutModel setHallway(int width) {
        this.snakelineHallwayWidth = width;
        return this;
    }

    public int getX(String id) {
        MatrixCell cell = cellsById.get(id);
        int col = cell.x();
        return getX(col);
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
        int row = cell.y();
        return getY(row, direction);
    }

    public List<Point> findPath(String ss1Id, Point p1, Direction d1,
                                String ss2Id, Point p2, Direction d2) {
        Grid grid = buildPathFindingGrid(ss1Id, p1, d1,
                ss2Id, p2, d2);
        com.powsybl.sld.layout.algo.Point start = new com.powsybl.sld.layout.algo.Point((int) p1.getY(), (int) p1.getX());
        com.powsybl.sld.layout.algo.Point target = new com.powsybl.sld.layout.algo.Point((int) p2.getY(), (int) p2.getX());
        // Last argument will make this search be 4 directional
        List<com.powsybl.sld.layout.algo.Point> path = PathFinding.findPath(grid, start, target, false);
        List<com.powsybl.sld.layout.algo.Point> simplifiedPoints = PathFinding.simplify(path, 1.0);

        List<Point> polyline = new ArrayList<>();
        for (com.powsybl.sld.layout.algo.Point point : simplifiedPoints) {
            polyline.add(new Point(point.y(), point.x()));
        }

        // Only debug file
        // dumpGridToFile("out.txt");

        return polyline;
    }

    private Grid buildPathFindingGrid(String id1, Point p1, Direction d1,
                                      String id2, Point p2, Direction d2) {
        int x1 = (int) p1.getX();
        int y1 = (int) p1.getY();
        int ss1Y = getY(id1, d1);
        for (int y = Math.min(y1, ss1Y); y <= Math.max(y1, ss1Y); y++) {
            privateTiles[y][x1] = WALKABLE;
        }

        int x2 = (int) p2.getX();
        int y2 = (int) p2.getY();
        int ss2Y = getY(id2, d2);
        for (int y = Math.min(y2, ss2Y); y <= Math.max(y2, ss2Y); y++) {
            privateTiles[y][x2] = WALKABLE;
        }
        return new Grid(privateTiles.length, privateTiles.length, privateTiles);
    }

    public void computeDefaultTiles(ZoneGraph graph) {
        Objects.requireNonNull(graph);
        int height = (int) graph.getHeight();
        int width = (int) graph.getWidth();

        // FIXME: not sure for equality -> maybe w & h inversion ?
        // Width & height must be equal for tiles
        int max = Math.max(height, width);
        privateTiles = new float[max][max];
        for (float[] tile : privateTiles) {
            Arrays.fill(tile, WALKABLE);
        }
        cellsById.keySet().forEach(id -> {
            int ssX = getX(id);
            int ssY = getY(id);
            for (int xx = ssX; xx < ssX + matrixCellWidth; xx++) {
                for (int yy = ssY; yy < ssY + matrixCellHeight; yy++) {
                    privateTiles[yy][xx] = NOT_WALKABLE;
                }
            }
        });
        emptyCells.forEach(cell -> {
            int ssX = getX(cell.x());
            int ssY = getY(cell.y());
            for (int xx = ssX; xx < ssX + matrixCellWidth; xx++) {
                for (int yy = ssY; yy < ssY + matrixCellHeight; yy++) {
                    privateTiles[yy][xx] = NOT_WALKABLE;
                }
            }
        });
    }

    public void dumpGridToFile(String filename) {
        Objects.requireNonNull(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (float[] privateTile : privateTiles) {
                for (float b : privateTile) {
                    String str = b == WALKABLE ? " " : "X";
                    str += "";
                    writer.write(str);
                }
                writer.write("\n");
                writer.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean gridContains(String id) {
        Objects.requireNonNull(id);
        return cellsById.containsKey(id);
    }

    public void forEachCellGrid(Consumer<? super String> consumer) {
        cellsById.keySet().forEach(consumer);
    }
}
