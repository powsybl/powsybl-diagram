/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.pathfinding.ZoneLayoutPathFinderFactory;
import com.powsybl.sld.layout.zonebygrid.Matrix;
import com.powsybl.sld.layout.zonebygrid.MatrixCell;
import com.powsybl.sld.layout.zonebygrid.MatrixZoneLayoutModel;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.BaseGraph;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayout extends AbstractPositionedZoneLayout {

    private final String[][] matrixUserDefinition;

    protected MatrixZoneLayout(ZoneGraph graph, String[][] matrixUserDefinition, ZoneLayoutPathFinderFactory pathFinderFactory,
                               SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, pathFinderFactory, sLayoutFactory, vLayoutFactory);
        this.matrixUserDefinition = Objects.requireNonNull(matrixUserDefinition);
    }

    @Override
    protected List<Pair<String, Point>> computeSubstationPositions(LayoutParameters layoutParameters) {
        // Build matrix model
        MatrixZoneLayoutModel model = new MatrixZoneLayoutModel(matrixUserDefinition);
        for (int row = 0; row < matrixUserDefinition.length; row++) {
            for (int col = 0; col < matrixUserDefinition[row].length; col++) {
                String id = matrixUserDefinition[row][col];
                SubstationGraph sGraph = getGraph().getSubstationGraph(id);
                if (sGraph == null && !id.isEmpty()) {
                    throw new PowsyblException("Substation '" + id + "' was not found in zone graph '" + getGraph().getId() + "'");
                }
                model.addSubstationGraph(sGraph, row, col);
            }
        }

        Matrix matrix = model.getMatrix();
        // Height by rows
        int maxHeightRow = 0;
        // Width by col
        int maxWidthCol = 0;
        // Snakeline hallway (horizontal & vertical)
        int snakelineMargin = layoutParameters.getZoneLayoutSnakeLinePadding();
        // Zone size
        int nbRows = matrix.rowCount();
        int nbCols = matrix.columnCount();
        // Move each substation into its matrix position
        List<Pair<String, Point>> positions = new ArrayList<>();
        for (int row = 0; row < nbRows; row++) {
            maxWidthCol = 0;
            for (int col = 0; col < nbCols; col++) {
                MatrixCell cell = matrix.get(row, col);
                BaseGraph graph = cell.graph();
                if (graph != null) {
                    // Compute delta in order to center substations into own matrix cell
                    int deltaX = (int) (matrix.getMatrixCellWidth(col) % graph.getWidth()) / 2;
                    int deltaY = (int) (matrix.getMatrixCellHeight(row) % graph.getHeight()) / 2;
                    double dx = maxWidthCol + (col + 1.0) * snakelineMargin;
                    double dy = maxHeightRow + (row + 1.0) * snakelineMargin;
                    positions.add(Pair.of(((SubstationGraph) graph).getId(), new Point(dx + deltaX, dy + deltaY)));
                }
                maxWidthCol += (int) matrix.getMatrixCellWidth(col);
            }
            maxHeightRow += (int) matrix.getMatrixCellHeight(row);
        }
        return positions;
    }
}
