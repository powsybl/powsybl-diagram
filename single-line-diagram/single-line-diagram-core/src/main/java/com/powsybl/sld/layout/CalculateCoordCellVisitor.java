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
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.Node;

import static com.powsybl.sld.model.coordinate.Position.Dimension.*;
import static com.powsybl.sld.model.blocks.Block.Extremity.*;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
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
        if (cell.getShape().checkIsNotShape(Shape.ONE_LEG, Shape.UNDEFINED, Shape.UNHANDLED_PATTERN)) {
            calculateInternCellRootCoord(cell.getBodyBlock(), cell.getDirection(), cell.getShape(), false);
        }
        cell.getLegs().values().forEach(lb -> calculateInternCellRootCoord(lb, cell.getDirection(), cell.getShape(), true));
    }

    @Override
    public void visit(ExternCell cell) {
        calculateExternCellRootCoord(cell.getRootBlock(), layoutContext, cell.getDirection());
    }

    @Override
    public void visit(ShuntCell cell) {
        Position lPos = cell.getSidePosition(Side.LEFT);
        coordShuntCase(cell.getRootBlock(), lPos.get(H) + lPos.getSpan(H), cell.getSidePosition(Side.RIGHT).get(H));
    }

    private void calculateInternCellRootCoord(Block block, Direction direction, Shape shape, boolean leg) {
        Position position = block.getPosition();

        double spanX = getRootSpanX(position);
        double x = getRootX(position, spanX);
        if (!leg && shape != Shape.FLAT) {
            x += layoutParameters.getCellWidth() / 2;
        }

        double spanY = position.getSpan(V) / 2. * layoutParameters.getInternCellHeight();
        double dyToBus = spanY / 2 + layoutParameters.getInternCellHeight() * (1 + position.get(V)) / 2.;
        double y = 0;
        if (direction == Direction.BOTTOM) {
            y = layoutContext.getLastBusY() + dyToBus;
        } else if (direction == Direction.TOP) {
            y = layoutContext.getFirstBusY() - dyToBus;
        } else if (direction == Direction.MIDDLE) {
            y = layoutContext.getFirstBusY() + (position.get(V) - 1) * layoutParameters.getVerticalSpaceBus();
        }

        block.setCoord(x, y, spanX, spanY);
        block.accept(new CalculateCoordBlockVisitor(layoutParameters));
    }

    private void calculateExternCellRootCoord(Block block, LayoutContext layoutContext, Direction direction) {
        Position position = block.getPosition();

        double spanX = getRootSpanX(position);
        double x = getRootX(position, spanX);

        // The Y span of root block does not consider the space needed for the FeederPrimaryBlock (feeder span)
        // nor the one needed for the LegPrimaryBlock (layoutParam.getStackHeight())
        double spanY = position.getSpan(V) != 0
                ? layoutContext.getExternCellHeight(direction) - layoutParameters.getStackHeight() - layoutParameters.getFeederSpan()
                : 0;
        double dyToBus = spanY / 2 + layoutParameters.getStackHeight();
        double y = direction == Direction.BOTTOM
                ? layoutContext.getLastBusY() + dyToBus
                : layoutContext.getFirstBusY() - dyToBus;

        block.setCoord(x, y, spanX, spanY);
        block.accept(new CalculateCoordBlockVisitor(layoutParameters));
    }

    private double getRootSpanX(Position position) {
        return position.getSpan(H) / 2. * layoutParameters.getCellWidth();
    }

    private double getRootX(Position position, double spanX) {
        return hToX(layoutParameters, position.get(H)) + spanX / 2;
    }

    private double hToX(LayoutParameters layoutParameters, int h) {
        return layoutParameters.getCellWidth() * h / 2;
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
            node.setCoordinates(x0 + (i - 1.5) * dx, y);
        }
    }

}
