/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.blocks.BodyPrimaryBlock;
import com.powsybl.sld.model.cells.*;
import com.powsybl.sld.model.cells.InternCell.Shape;
import com.powsybl.sld.model.coordinate.Coord;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.Node;

import static com.powsybl.sld.model.coordinate.Position.Dimension.*;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.*;
import static com.powsybl.sld.model.blocks.Block.Extremity.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class CalculateCoordCellVisitor implements CellVisitor {
    private final LayoutParameters layoutParameters;
    private final LayoutContext layoutContext;

    CalculateCoordCellVisitor(LayoutParameters layoutParameters, LayoutContext layoutContext) {
        this.layoutParameters = layoutParameters;
        this.layoutContext = layoutContext;
    }

    @Override
    public void visit(InternCell cell) {
        if (cell.getShape().checkIsNotShape(Shape.UNILEG, Shape.UNDEFINED, Shape.UNHANDLEDPATTERN)) {
            calculateRootCoord(cell.getBodyBlock(), layoutContext);
        }
        cell.getLegs().values().forEach(lb -> calculateRootCoord(lb, layoutContext));
    }

    @Override
    public void visit(ExternCell cell) {
        calculateRootCoord(cell.getRootBlock(), layoutContext);
    }

    @Override
    public void visit(ShuntCell cell) {
        Position lPos = cell.getSidePosition(Side.LEFT);
        coordShuntCase(cell.getRootBlock(), lPos.get(H) + lPos.getSpan(H), cell.getSidePosition(Side.RIGHT).get(H));
    }

    private void calculateRootCoord(Block block, LayoutContext layoutContext) {
        Position position = block.getPosition();
        Coord coord = block.getCoord();
        double spanX = position.getSpan(H) / 2. * layoutParameters.getCellWidth();
        coord.setSpan(X, spanX);
        coord.set(X, hToX(layoutParameters, position.get(H)) + spanX / 2);

        double spanY = getRootSpanYCoord(position, layoutParameters, layoutContext.getMaxInternCellHeight(), layoutContext.isInternCell());
        coord.setSpan(Y, spanY);
        coord.set(Y, getRootYCoord(position, layoutParameters, spanY, layoutContext));
        CalculateCoordBlockVisitor cc = CalculateCoordBlockVisitor.create(layoutParameters, layoutContext);
        block.accept(cc);
    }

    private double hToX(LayoutParameters layoutParameters, int h) {
        return layoutParameters.getCellWidth() * h / 2;
    }

    private double getRootSpanYCoord(Position position, LayoutParameters layoutParam, double externCellHeight, boolean isInternCell) {
        double ySpan;
        if (isInternCell) {
            ySpan = position.getSpan(V) / 2. * layoutParam.getInternCellHeight();
        } else {
            // The Y span of root block does not consider the space needed for the FeederPrimaryBlock (feeder span)
            // nor the one needed for the LegPrimaryBlock (layoutParam.getStackHeight())
            ySpan = externCellHeight - layoutParam.getStackHeight() - layoutParam.getFeederSpan();
        }
        return ySpan;
    }

    private double getRootYCoord(Position position, LayoutParameters layoutParam, double spanY, LayoutContext layoutContext) {
        double dyToBus = 0;
        if (layoutContext.isInternCell() && !layoutContext.isFlat()) {
            dyToBus = spanY / 2 + layoutParam.getInternCellHeight() * (1 + position.get(V)) / 2.;
        } else {
            dyToBus = spanY / 2 + layoutParam.getStackHeight();
        }
        switch (layoutContext.getDirection()) {
            case BOTTOM:
                return layoutContext.getLastBusY() + dyToBus;
            case TOP:
                return layoutContext.getFirstBusY() - dyToBus;
            case MIDDLE:
                return layoutContext.getFirstBusY() + (position.get(V) - 1) * layoutParam.getVerticalSpaceBus();
            default:
                return 0;
        }
    }

    public void coordShuntCase(BodyPrimaryBlock block, int hLeft, int hRight) {
        double x0 = hToX(layoutParameters, hLeft);
        double y0 = block.getExtremityNode(START).getY();
        double x1 = hToX(layoutParameters, hRight);
        double y1 = block.getExtremityNode(END).getY();
        double y = (y0 + y1) / 2;

        block.getNodes().get(1).setCoordinates(x0, y);
        block.getNodes().get(block.getNodes().size() - 2).setCoordinates(x1, y);

        double dx = (x1 - x0) / (block.getNodes().size() - 4);
        for (int i = 2; i < block.getNodes().size() - 2; i++) {
            Node node = block.getNodes().get(i);
            node.setCoordinates(x0  + (i - 1.5) * dx, y);
        }
    }

}
