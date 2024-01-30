/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.zonebygrid;

import com.powsybl.sld.model.graphs.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class Matrix {
    // [row] [col]
    private final MatrixCell[][] cells;

    public Matrix(int rows, int cols) {
        cells = new MatrixCell[rows][cols];
    }

    public Matrix set(int row, int col, MatrixCell cell) {
        cells[row][col] = cell;
        return this;
    }

    public Optional<MatrixCell> get(String id) {
        return stream().filter(c -> c.getId().equals(id)).findAny();
    }

    public MatrixCell get(int row, int col) {
        return cells[row][col];
    }

    public Stream<MatrixCell> stream() {
        return Arrays.stream(cells).flatMap(Arrays::stream);
    }

    public double getMatrixCellHeight(int row) {
        return Stream.of(cells[row]).filter(cell -> !cell.isEmpty()).mapToDouble(cell -> cell.graph().getHeight()).max().orElse(0.0);
    }

    public double getMatrixCellWidth(int col) {
        double maxWidth = 0.0;
        for (int row = 0; row < rowCount(); row++) {
            BaseGraph graph = cells[row][col].graph();
            if (graph != null) {
                maxWidth = Math.max(maxWidth, graph.getWidth());
            }
        }
        return maxWidth;
    }

    public int rowCount() {
        return cells.length;
    }

    public int columnCount() {
        return cells[0].length;
    }
}
