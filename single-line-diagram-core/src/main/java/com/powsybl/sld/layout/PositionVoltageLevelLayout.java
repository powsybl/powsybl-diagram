/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Cell;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class PositionVoltageLevelLayout extends AbstractVoltageLevelLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionVoltageLevelLayout.class);

    public PositionVoltageLevelLayout(VoltageLevelGraph graph) {
        super(graph);
    }

    /**
     * Calculate real coordinate of busNode and blocks connected to busbar
     */
    @Override
    public void run(LayoutParameters layoutParam) {
        LOGGER.info("Running voltage level layout");
        calculateMaxCellHeight(layoutParam);
        calculateBusNodeCoord(getGraph(), layoutParam);
        calculateCellCoord(getGraph(), layoutParam);

        setGraphCoord(layoutParam);
        setGraphSize(layoutParam);

        // Calculate all the coordinates for the middle nodes and the snake lines in the voltageLevel graph
        manageSnakeLines(layoutParam);

        if (getGraph().isForVoltageLevelDiagram()) {
            adaptPaddingToSnakeLines(layoutParam);
        }
    }

    private void setGraphCoord(LayoutParameters layoutParam) {
        LayoutParameters.Padding vlPadding = layoutParam.getVoltageLevelPadding();
        LayoutParameters.Padding dPadding = layoutParam.getDiagramPadding();
        getGraph().setCoord(dPadding.getLeft() + vlPadding.getLeft(), dPadding.getTop() + vlPadding.getTop());
    }

    private void setGraphSize(LayoutParameters layoutParam) {
        VoltageLevelGraph graph = getGraph();
        double elementaryWidth = layoutParam.getCellWidth() / 2; // the elementary step within a voltageLevel Graph is half a cell width
        double widthWithoutPadding = graph.getMaxH() * elementaryWidth;
        double heightWithoutPadding = graph.getExternCellHeight(BusCell.Direction.TOP)
            + layoutParam.getVerticalSpaceBus() * graph.getMaxV()
            + graph.getExternCellHeight(BusCell.Direction.BOTTOM);

        LayoutParameters.Padding padding = layoutParam.getVoltageLevelPadding();
        double width = widthWithoutPadding + padding.getLeft() + padding.getRight();
        double height = heightWithoutPadding + padding.getTop() + padding.getBottom();

        getGraph().setSize(width, height);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParam) {
        VoltageLevelGraph graph = getGraph();
        double widthSnakeLinesLeft = getWidthVerticalSnakeLines(graph.getId(), layoutParam, infosNbSnakeLines);
        double heightSnakeLinesTop = getHeightSnakeLines(layoutParam, BusCell.Direction.TOP, infosNbSnakeLines);
        double heightSnakeLinesBottom = getHeightSnakeLines(layoutParam, BusCell.Direction.BOTTOM,  infosNbSnakeLines);
        double width = graph.getWidth() + widthSnakeLinesLeft;
        double height = graph.getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;
        graph.setSize(width, height);
        graph.setCoord(graph.getX() + widthSnakeLinesLeft, graph.getY() + heightSnakeLinesTop);

        infosNbSnakeLines.reset();
        manageSnakeLines(getGraph(), layoutParam);
    }

    private void calculateBusNodeCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        graph.getNodeBuses().forEach(nb -> nb.calculateCoord(layoutParam));
    }

    private void calculateCellCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN
                        || cell.getType() == Cell.CellType.INTERN)
                .forEach(cell -> cell.calculateCoord(graph, layoutParam));
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.SHUNT)
                .forEach(cell -> cell.calculateCoord(graph, layoutParam));
    }

    /**
     * Calculating the maximum height of all the extern cells in each direction (top and bottom).
     * If no extern cell found taking into account intern cells too.
     * This height does not include the constant stack height.
     * @param layoutParam the layout parameters
     */
    private void calculateMaxCellHeight(LayoutParameters layoutParam) {
        Map<BusCell.Direction, Double> maxCellHeight = new EnumMap<>(BusCell.Direction.class);
        if (layoutParam.isAdaptCellHeightToContent()) {
            Map<BusCell.Direction, Double> maxInternCellHeight = new EnumMap<>(BusCell.Direction.class);
            // Initialize map with intern cells height
            // in order to keep intern cells visible if there are no extern cells
            getGraph().getCells().stream()
                    .filter(cell -> cell.getType() == Cell.CellType.INTERN)
                    .forEach(cell -> maxInternCellHeight.merge(((BusCell) cell).getDirection(), cell.calculateHeight(layoutParam), Math::max));

            // when using the adapt cell height to content option, we have to calculate the
            // maximum height of all the extern cells in each direction (top and bottom)
            getGraph().getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .forEach(cell -> maxCellHeight.merge(((BusCell) cell).getDirection(), cell.calculateHeight(layoutParam), Math::max));

            // if needed, adjusting the maximum calculated cell height to the minimum extern cell height parameter
            EnumSet.allOf(BusCell.Direction.class).forEach(d -> maxCellHeight.compute(d, (k, v) -> {
                Double vIntern = maxInternCellHeight.get(d);
                if (v == null && vIntern == null) {
                    return 0.;
                } else if (v == null) {
                    return vIntern + layoutParam.getStackHeight();
                } else {
                    return Math.max(v, layoutParam.getMinExternCellHeight()) + layoutParam.getFeederSpan() + layoutParam.getStackHeight();
                }
            }));
        } else {
            maxCellHeight.put(BusCell.Direction.TOP, layoutParam.getExternCellHeight() + layoutParam.getStackHeight());
            maxCellHeight.put(BusCell.Direction.BOTTOM, layoutParam.getExternCellHeight() + layoutParam.getStackHeight());
        }

        getGraph().setMaxCellHeight(maxCellHeight);
    }
}
