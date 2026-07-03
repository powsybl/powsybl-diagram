/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.zonebygrid;

import com.powsybl.sld.model.graphs.SubstationGraph;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayoutModel {

    private final Matrix matrix;

    public MatrixZoneLayoutModel(String[][] ids) {
        this.matrix = new Matrix(ids.length, ids[0].length);
    }

    public void addSubstationGraph(SubstationGraph graph, int row, int col) {
        this.matrix.set(row, col, new MatrixCell(graph, row, col));
    }

    public Matrix getMatrix() {
        return matrix;
    }
}
