/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.blocks.BlockVisitor;
import com.powsybl.sld.model.blocks.BodyParallelBlock;
import com.powsybl.sld.model.blocks.BodyPrimaryBlock;
import com.powsybl.sld.model.blocks.ComposedBlock;
import com.powsybl.sld.model.blocks.FeederPrimaryBlock;
import com.powsybl.sld.model.blocks.LegParallelBlock;
import com.powsybl.sld.model.blocks.LegPrimaryBlock;
import com.powsybl.sld.model.blocks.SerialBlock;
import com.powsybl.sld.model.blocks.UndefinedBlock;
import com.powsybl.sld.model.coordinate.Coord;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.nodes.Middle3WTNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.Node.NodeType;

import java.util.List;

import static com.powsybl.sld.model.coordinate.Coord.Dimension.X;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.Y;
import static com.powsybl.sld.model.coordinate.Orientation.UP;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
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
        List<Node> blockNodes = block.getNodes();
        if (blockNodes.size() == 1) {
            blockNodes.getFirst().setCoordinates(block.getCoord().get(X), block.getCoord().get(Y));
            return;
        }

        if (block.getPosition().getOrientation().isVertical()) {
            int sign = block.getOrientation() == UP ? 1 : -1;
            double y0 = block.getCoord().get(Y) + sign * block.getCoord().getSpan(Y) / 2;
            double yPxStep = sign * block.getCoord().getSpan(Y) / (blockNodes.size() - 1);
            double swStep = sign * block.getCoord().getSpan(Y) / (blockNodes.size());

            for (int i = 0; i < blockNodes.size(); i++) {
                Node n = blockNodes.get(i);
                if (n.getType() == NodeType.SWITCH && n.getAdjacentNodes().stream().anyMatch(m -> m instanceof Middle3WTNode)) {
                    n.setCoordinates(block.getCoord().get(X), y0 - yPxStep * i + swStep);
                } else {
                    n.setCoordinates(block.getCoord().get(X), y0 - yPxStep * i);
                }
            }
        } else {
            double x0 = block.getCoord().get(X) - block.getCoord().getSpan(X) / 2;
            if (layoutContext.isInternCell() && !layoutContext.isFlat()) {
                x0 += layoutParameters.getCellWidth() / 2;
            }
            double xPxStep = block.getCoord().getSpan(X) / (blockNodes.size() - 1);
            double swStep = block.getCoord().getSpan(X) / (blockNodes.size());
            for (int i = 0; i < blockNodes.size(); i++) {
                Node n = blockNodes.get(i);
                if (n.getType() == NodeType.SWITCH && n.getAdjacentNodes().stream().anyMatch(m -> m instanceof Middle3WTNode)) {
                    n.setCoordinates(x0 + swStep + xPxStep * i, block.getCoord().get(Y));
                } else {
                    n.setCoordinates(x0 + xPxStep * i, block.getCoord().get(Y));
                }
            }
        }
    }

    @Override
    public void visit(LegPrimaryBlock block) {
        Coord blockCoord = block.getCoord();
        if (block.getPosition().getOrientation().isVertical()) {
            block.getNodeOnBus().setCoordinates(blockCoord.get(X), block.getBusNode().getY());
            block.getLegNode().setCoordinates(blockCoord.get(X), blockCoord.get(Y));
            if (layoutContext.isInternCell() && layoutContext.isUnileg()) {
                block.getLegNode().setY(blockCoord.get(Y)
                        + (block.getOrientation() == UP ? -1 : 1) * layoutParameters.getInternCellHeight());
            }
        } else {
            block.getNodeOnBus().setCoordinates(blockCoord.get(X) + blockCoord.getSpan(X) / 2,
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
    public void visit(LegParallelBlock block) {
        if (block.getPosition().getOrientation().isVertical()) {
            translatePosInCoord(block, Y, X, H, 1);
        } else {
            // case HORIZONTAL cannot happen
        }
    }

    <T extends Block> void translatePosInCoord(ComposedBlock<T> block, Coord.Dimension cDimSteady,
            Coord.Dimension cDimVariable, Position.Dimension pDim, int sign) {
        replicateCoordInSubblocks(block, cDimSteady);
        distributeCoordInSubblocs(block, pDim, cDimVariable, sign);
        block.getSubBlocks().forEach(sub -> sub.accept(this));
    }

    <T extends Block> void replicateCoordInSubblocks(ComposedBlock<T> block, Coord.Dimension dim) {
        block.getCoord().getSegment(dim)
                .replicateMe(block.getSubBlocks().stream().map(b -> b.getCoord().getSegment(dim)));
    }

    <T extends Block> void distributeCoordInSubblocs(ComposedBlock<T> block, Position.Dimension pDim, Coord.Dimension cDim, int sign) {
        // Computes the step, avoiding the division by 0 for 0-span composed block (e.g.
        // LegPrimaryBlock + Feeder)
        double init = block.getCoord().get(cDim) - sign * block.getCoord().getSpan(cDim) / 2;
        int pSpan = block.getPosition().getSpan(pDim);
        double step = pSpan == 0 ? 0 : block.getCoord().getSpan(cDim) / pSpan;
        block.getSubBlocks().forEach(sub -> {
            double value = init + sign * step * (sub.getPosition().get(pDim) + (double) sub.getPosition().getSpan(pDim) / 2);
            double span = sub.getPosition().getSpan(pDim) * step;
            sub.getCoord().set(cDim, value, span);
        });
    }
}
