/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.blocks.BodyPrimaryBlock;
import com.powsybl.sld.model.cells.ArchCell;
import com.powsybl.sld.model.cells.CellVisitor;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Coord;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.Node;

import static com.powsybl.sld.model.blocks.Block.Extremity.END;
import static com.powsybl.sld.model.blocks.Block.Extremity.START;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.X;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.Y;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

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
        cell.getLegs().values().forEach(lb -> calculateRootCoord(lb, layoutContext));
        calculateRootCoord(cell.getBodyBlock(), layoutContext);
    }

    @Override
    public void visit(ExternCell cell) {
        calculateRootCoord(cell.getRootBlock(), layoutContext);
    }

    @Override
    public void visit(ArchCell cell) {
        Block rootBlock = cell.getRootBlock();
        Position position = rootBlock.getPosition();
        setCoordX(rootBlock.getCoord(), position);

        // pillar block Coord has been calculated beforehand
        rootBlock.getCoord().set(Y, cell.getPillarBlock().getCoord());
        CalculateCoordBlockVisitor cc = CalculateCoordBlockVisitor.create(layoutParameters, layoutContext);
        rootBlock.accept(cc);
    }

    @Override
    public void visit(ShuntCell cell) {
        Position lPos = cell.getSidePosition(Side.LEFT);
        coordShuntCase(cell.getRootBlock(), lPos.get(H) + lPos.getSpan(H), cell.getSidePosition(Side.RIGHT).get(H));
    }

    private void calculateRootCoord(Block block, LayoutContext layoutContext) {
        if (block == null) {
            return;
        }

        Position position = block.getPosition();
        setCoordX(block.getCoord(), position);

        double spanY = getRootSpanYCoord(position, layoutParameters, layoutContext.maxInternCellHeight(), layoutContext.isInternCell());
        double valueY = getRootYCoord(position, layoutParameters, spanY, layoutContext);
        block.getCoord().set(Y, valueY, spanY);

        CalculateCoordBlockVisitor cc = CalculateCoordBlockVisitor.create(layoutParameters, layoutContext);
        block.accept(cc);
    }

    private void setCoordX(Coord coord, Position position) {
        double spanX = position.getSpan(H) / 2. * layoutParameters.getCellWidth();
        double valueX = hToX(layoutParameters, position.get(H)) + spanX / 2;
        coord.set(X, valueX, spanX);
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
        double dyToBus;
        if (layoutContext.isInternCell() && !layoutContext.isFlat()) {
            dyToBus = spanY / 2 + layoutParam.getInternCellHeight() * (1 + position.get(V)) / 2.;
        } else {
            dyToBus = spanY / 2 + layoutParam.getStackHeight();
        }
        return switch (layoutContext.direction()) {
            case BOTTOM -> layoutContext.lastBusY() + dyToBus;
            case TOP -> layoutContext.firstBusY() - dyToBus;
            case MIDDLE -> layoutContext.firstBusY() + (position.get(V) - 1) * layoutParam.getVerticalSpaceBus();
            default -> 0;
        };
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
