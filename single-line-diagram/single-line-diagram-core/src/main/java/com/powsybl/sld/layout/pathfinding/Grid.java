/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.pathfinding;

import java.io.*;
import java.util.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public class Grid {
    private final int[][] values;
    private final int width;
    private final int height;

    private static final int NOT_WALKABLE = -1;
    private static final int WALKABLE = 0;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.values = new int[width][height];
        for (int[] tile : values) {
            Arrays.fill(tile, NOT_WALKABLE);
        }
    }

    public void setFreePath(int x, int y) {
        // Make sure we are not out of bounds
        values[Math.min(x, width - 1)][Math.min(y, height - 1)] = WALKABLE;
    }

    public void setObstacle(int x, int y) {
        // Make sure we are not out of bounds
        values[Math.min(x, width - 1)][Math.min(y, height - 1)] = NOT_WALKABLE;
    }

    public void setObstacles(List<com.powsybl.sld.layout.pathfinding.Point> path) {
        path.forEach(p -> setObstacle(p.x(), p.y()));
    }

    public boolean isValid(Point point) {
        return point.x() >= 0 && point.x() < width && point.y() >= 0 && point.y() < height && values[point.x()][point.y()] != -1;
    }

    public void dumpToFile(String filename) {
        Objects.requireNonNull(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Transpose from row / col to x / y
            int[][] toDump = transpose(values);

            for (int[] ints : toDump) {
                for (int y = 0; y < toDump[0].length; y++) {
                    String str = ints[y] == 0 ? " " : "X";
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

    private static int[][] transpose(int[][] mat) {
        int[][] result = new int[mat[0].length][mat.length];
        for (int i = 0; i < mat.length; ++i) {
            for (int j = 0; j < mat[0].length; ++j) {
                result[j][i] = mat[i][j];
            }
        }
        return result;
    }
}
