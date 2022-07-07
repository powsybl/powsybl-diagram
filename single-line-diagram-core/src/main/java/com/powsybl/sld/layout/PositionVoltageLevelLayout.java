/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.cells.*;
import com.powsybl.sld.model.cells.InternCell.Shape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static com.powsybl.sld.model.coordinate.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class PositionVoltageLevelLayout extends AbstractVoltageLevelLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionVoltageLevelLayout.class);

    public PositionVoltageLevelLayout(VoltageLevelGraph graph, LayoutParameters layoutParameters) {
        super(graph, layoutParameters);
    }

    /**
     * Calculate real coordinate of busNode and blocks connected to busbar
     */
    @Override
    public void run() {
        LOGGER.info("Running voltage level layout");
        calculateMaxCellHeight(layoutParameters);
        calculateBusNodeCoord(getGraph(), layoutParameters);
        calculateCellCoord(getGraph(), layoutParameters);

        setGraphCoord(layoutParameters);
        setGraphSize(layoutParameters);

        // Calculate all the coordinates for the middle nodes and the snake lines in the voltageLevel graph
        manageSnakeLines(layoutParameters);

        if (getGraph().isForVoltageLevelDiagram()) {
            adaptPaddingToSnakeLines(layoutParameters);
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
        double heightWithoutPadding = graph.getInnerHeight(layoutParam.getVerticalSpaceBus());

        LayoutParameters.Padding padding = layoutParam.getVoltageLevelPadding();
        double width = widthWithoutPadding + padding.getLeft() + padding.getRight();
        double height = heightWithoutPadding + padding.getTop() + padding.getBottom();

        getGraph().setSize(width, height);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParam) {
        VoltageLevelGraph graph = getGraph();
        double widthSnakeLinesLeft = getWidthVerticalSnakeLines(graph.getId(), layoutParam, infosNbSnakeLines);
        double heightSnakeLinesTop = getHeightSnakeLines(layoutParam, Direction.TOP, infosNbSnakeLines);
        double heightSnakeLinesBottom = getHeightSnakeLines(layoutParam, Direction.BOTTOM,  infosNbSnakeLines);
        double width = graph.getWidth() + widthSnakeLinesLeft;
        double height = graph.getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;
        graph.setSize(width, height);
        graph.setCoord(graph.getX() + widthSnakeLinesLeft, graph.getY() + heightSnakeLinesTop);

        infosNbSnakeLines.reset();

        manageSnakeLines(getGraph(), layoutParam);
    }

    private void calculateBusNodeCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        graph.getNodeBuses().forEach(nb -> calculateNodeCoord(nb, layoutParam, graph.getFirstBusY()));
    }

    private void calculateNodeCoord(BusNode busNode, LayoutParameters layoutParameters, double firstBusY) {
        double elementaryWidth = layoutParameters.getCellWidth() / 2;
        double busPadding = busNode.isFictitious() ? elementaryWidth : layoutParameters.getBusPadding();
        Position position = busNode.getPosition();
        busNode.setCoordinates(position.get(H) * elementaryWidth + busPadding,
            firstBusY + position.get(V) * layoutParameters.getVerticalSpaceBus());
        busNode.setPxWidth(position.getSpan(H) * elementaryWidth - 2 * busPadding);
    }

    private void calculateCellCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        graph.getBusCellStream().forEach(cell -> cell.accept(new CalculateCoordCellVisitor(layoutParam, createLayoutContext(graph, cell, layoutParam))));
        graph.getShuntCellStream().forEach(cell -> cell.accept(new CalculateCoordCellVisitor(layoutParam, null)));
    }

    private LayoutContext createLayoutContext(VoltageLevelGraph graph, BusCell cell, LayoutParameters layoutParam) {
        double firstBusY = graph.getFirstBusY();
        double lastBusY = graph.getLastBusY(layoutParam.getVerticalSpaceBus());
        Double externCellHeight = graph.getExternCellHeight(cell.getDirection());
        if (cell.getType() != Cell.CellType.INTERN) {
            return new LayoutContext(firstBusY, lastBusY, externCellHeight, cell.getDirection());
        } else {
            boolean isFlat = ((InternCell) cell).getShape() == Shape.FLAT;
            boolean isUnileg = ((InternCell) cell).getShape() == Shape.UNILEG;
            return new LayoutContext(firstBusY, lastBusY, externCellHeight, cell.getDirection(), true, isFlat, isUnileg);
        }
    }

    /**
     * Calculating the maximum height of all the extern cells in each direction (top and bottom).
     * If no extern cell found taking into account intern cells too.
     * This height does include the constant stack height.
     * @param layoutParam the layout parameters
     */
    private void calculateMaxCellHeight(LayoutParameters layoutParam) {
        Map<Direction, Double> maxCellHeight = new EnumMap<>(Direction.class);
        if (layoutParam.isAdaptCellHeightToContent()) {
            Map<Direction, Double> maxInternCellHeight = new EnumMap<>(Direction.class);
            // Initialize map with intern cells height
            // in order to keep intern cells visible if there are no extern cells
            getGraph().getInternCellStream().forEach(cell ->
                    maxInternCellHeight.merge(cell.getDirection(), calculateCellHeight(layoutParam, cell), Math::max));

            // when using the adapt cell height to content option, we have to calculate the
            // maximum height of all the extern cells in each direction (top and bottom)
            getGraph().getExternCellStream().forEach(cell ->
                    maxCellHeight.merge(cell.getDirection(), calculateCellHeight(layoutParam, cell), Math::max));

            // if needed, adjusting the maximum calculated cell height to the minimum extern cell height parameter
            EnumSet.allOf(Direction.class).forEach(d -> maxCellHeight.compute(d, (k, v) -> {
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
            maxCellHeight.put(Direction.TOP, layoutParam.getExternCellHeight() + layoutParam.getStackHeight());
            maxCellHeight.put(Direction.BOTTOM, layoutParam.getExternCellHeight() + layoutParam.getStackHeight());
        }

        getGraph().setMaxCellHeight(maxCellHeight);
    }

    double calculateCellHeight(LayoutParameters layoutParameters, BusCell cell) {
        CalculateCellHeightBlockVisitor cchbv = CalculateCellHeightBlockVisitor.create(layoutParameters);
        cell.getRootBlock().accept(cchbv);
        return cchbv.getBlockHeight();
    }
}
