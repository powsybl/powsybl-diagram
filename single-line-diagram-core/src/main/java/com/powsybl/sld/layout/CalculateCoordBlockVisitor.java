/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.blocks.*;
import com.powsybl.sld.model.coordinate.Coord;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.nodes.Node;

import static com.powsybl.sld.model.coordinate.Orientation.*;
import static com.powsybl.sld.model.coordinate.Position.Dimension.*;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class CalculateCoordBlockVisitor implements BlockVisitor {
    private final LayoutParameters layoutParameters;
    private final LayoutContext layoutContext;

    private CalculateCoordBlockVisitor(LayoutParameters layoutParameters, LayoutContext layoutContext) {
        this.layoutParameters = layoutParameters;
        this.layoutContext = layoutContext;
    }

    public static CalculateCoordBlockVisitor create(LayoutParameters layoutParameters, LayoutContext layoutContext) {
        return new CalculateCoordBlockVisitor(layoutParameters, layoutContext);
    }

    @Override
    public void visit(BodyPrimaryBlock block) {
        if (block.getPosition().getOrientation().isVertical()) {
            int sign = block.getOrientation() == UP ? 1 : -1;
            double y0 = block.getCoord().get(Y) + sign * block.getCoord().getSpan(Y) / 2;
            double yPxStep = block.getPosition().getSpan(V) == 0 ? 0
                    : sign * block.getCoord().getSpan(Y) / (block.getNodes().size() - 1);
            int v = 0;
            for (Node node : block.getNodes()) {
                node.setCoordinates(block.getCoord().get(X), y0 - yPxStep * v);
                node.setRotationAngle(null);
                v++;
            }
        } else {
            double x0 = block.getCoord().get(X) - block.getCoord().getSpan(X) / 2;
            if (layoutContext.isInternCell() && !layoutContext.isFlat()) {
                x0 += layoutParameters.getCellWidth() / 2;
            }
            double xPxStep = block.getCoord().getSpan(X) / (block.getNodes().size() - 1);
            int h = 0;
            for (Node node : block.getNodes()) {
                node.setCoordinates(x0 + xPxStep * h, block.getCoord().get(Y));
                node.setRotationAngle(90.);
                h++;
            }
        }
    }

    @Override
    public void visit(LegPrimaryBlock block) {
        if (block.getPosition().getOrientation().isVertical()) {
            block.getNodeOnBus().setCoordinates(block.getCoord().get(X), block.getBusNode().getY());
            block.getLegNode().setX(block.getCoord().get(X));
            if (layoutContext.isInternCell() && layoutContext.isUnileg()) {
                block.getLegNode().setY(block.getCoord().get(Y)
                        + (block.getOrientation() == UP ? -1 : 1) * layoutParameters.getInternCellHeight());
            }
        } else {
            block.getNodeOnBus().setCoordinates(block.getCoord().get(X) + block.getCoord().getSpan(X) / 2,
                    block.getBusNode().getY());
            block.getLegNode().setY(block.getBusNode().getY());
        }
    }

    @Override
    public void visit(FeederPrimaryBlock block) {
        if (block.getPosition().getOrientation().isVertical()) {
            double yFeeder = block.getConnectedNode().getY()
                    + block.getOrientation().progressionSign() * layoutParameters.getFeederSpan();
            block.getFeederNode().setCoordinates(block.getCoord().get(X), yFeeder);
        } else {
            // Will never happen
        }
    }

    @Override
    public void visit(UndefinedBlock block) {
        replicateCoordInSubblocks(block, X);
        replicateCoordInSubblocks(block, Y);
        block.getSubBlocks().forEach(b -> b.accept(this));
    }

    @Override
    public void visit(BodyParallelBlock block) {
        if (block.getPosition().getOrientation().isVertical()) {
            translatePosInCoord(block, Y, X, H, 1);
        } else {
            translatePosInCoord(block, X, Y, V, 1);
        }
    }

    @Override
    public void visit(SerialBlock block) {
        if (block.getPosition().getOrientation().isVertical()) {
            translatePosInCoord(block, X, Y, V, block.getOrientation().progressionSign());
            block.getChainingNodes().forEach(n -> n.setX(block.getCoord().get(X)));
        } else {
            translatePosInCoord(block, Y, X, H, block.getOrientation().progressionSign());
            block.getChainingNodes().forEach(n -> n.setY(block.getCoord().get(Y)));
        }
    }

    @Override
    public void visit(LegParralelBlock block) {
        if (block.getPosition().getOrientation().isVertical()) {
            translatePosInCoord(block, Y, X, H, 1);
        } else {
            // case HORIZONTAL cannot happen
        }
    }

    void translatePosInCoord(ComposedBlock block, Coord.Dimension cDimSteady,
            Coord.Dimension cDimVariable, Position.Dimension pDim, int sign) {
        replicateCoordInSubblocks(block, cDimSteady);
        distributeCoordInSubblocs(block, pDim, cDimVariable, sign);
        block.getSubBlocks().forEach(sub -> sub.accept(this));
    }

    void replicateCoordInSubblocks(ComposedBlock block, Coord.Dimension dim) {
        block.getCoord().getSegment(dim)
                .replicateMe(block.getSubBlocks().stream().map(b -> b.getCoord().getSegment(dim)));
    }

    void distributeCoordInSubblocs(ComposedBlock block, Position.Dimension pDim, Coord.Dimension cDim, int sign) {
        // Computes the step, avoiding the division by 0 for 0-span composed block (e.g.
        // LegPrimaryBlock + Feeder)
        double init = block.getCoord().get(cDim) - sign * block.getCoord().getSpan(cDim) / 2;
        int pSpan = block.getPosition().getSpan(pDim);
        double step = pSpan == 0 ? 0 : block.getCoord().getSpan(cDim) / pSpan;
        block.getSubBlocks().forEach(sub -> {
            sub.getCoord().set(cDim, init
                    + sign * step * (sub.getPosition().get(pDim) + (double) sub.getPosition().getSpan(pDim) / 2));
            sub.getCoord().setSpan(cDim, sub.getPosition().getSpan(pDim) * step);
        });
    }
}
