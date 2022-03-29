/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import java.util.HashSet;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;

import com.powsybl.sld.model.blocks.*;
import com.powsybl.sld.model.coordinate.Coord;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.nodes.Node;

import static com.powsybl.sld.model.nodes.Node.NodeType.*;
import static com.powsybl.sld.model.coordinate.Orientation.*;
import static com.powsybl.sld.model.coordinate.Position.Dimension.*;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class BlockCalculationVisitors {
    LayoutParameters layoutParameters;

    public BlockCalculationVisitors(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
    }

    public CalculateCellHeightVisitor createCalculateCellHeight(Set<Node> encounteredNodes) {
        return new CalculateCellHeightVisitor(encounteredNodes);
    }

    public CalculateCellHeightVisitor createCalculateCellHeight() {
        return new CalculateCellHeightVisitor(new HashSet<>());
    }

    public CalculateCoord createCalculateCoord(LayoutContext layoutContext) {
        return new CalculateCoord(layoutContext);
    }

    public class CalculateCellHeightVisitor implements BlockVisitor {
        private double blockHeight;
        private Set<Node> encounteredNodes;

        public CalculateCellHeightVisitor(Set<Node> encounteredNodes) {
            this.encounteredNodes = encounteredNodes;
        }

        public CalculateCellHeightVisitor() {
            this.encounteredNodes = new HashSet<>();
        }

        public double getBlockHeight() {
            return blockHeight;
        }

        @Override
        public void visit(LegPrimaryBlock block) {
            blockHeight = 0;
        }

        @Override
        public void visit(FeederPrimaryBlock block) {
            blockHeight = 0;
        }

        @Override
        public void visit(BodyPrimaryBlock block) {
            // we do not consider the exact height of components as the maximum height will
            // later be split up equally
            // between nodes
            double componentHeight = layoutParameters.getMaxComponentHeight()
                    + layoutParameters.getMinSpaceBetweenComponents();

            // we increment the height only if the node is not a bus node and has not been
            // already encountered
            long nbNodes = block.getNodes().stream().filter(n -> !encounteredNodes.contains(n) && n.getType() != BUS)
                    .count();

            this.blockHeight = (nbNodes - 1) * componentHeight;
        }

        @Override
        public void visit(SerialBlock block) {
            calculateSubHeight(block, (a, b) -> a + b);
        }

        @Override
        public void visit(LegParralelBlock block) {
            calculateSubHeight(block, Math::max);
        }

        @Override
        public void visit(BodyParallelBlock block) {
            calculateSubHeight(block, Math::max);
        }

        @Override
        public void visit(UndefinedBlock block) {
            calculateSubHeight(block, Math::max);
        }

        private void calculateSubHeight(ComposedBlock block, DoubleBinaryOperator op) {
            blockHeight = 0.;
            for (int i = 0; i < block.getSubBlocks().size(); i++) {
                Block sub = block.getSubBlocks().get(i);
                // Here, when the subBlocks are positioned in parallel we calculate the max
                // height of all these subBlocks
                // when the subBlocks are serial, we calculate the sum
                CalculateCellHeightVisitor cch = createCalculateCellHeight(encounteredNodes);
                sub.accept(cch);
                blockHeight = op.applyAsDouble(blockHeight, cch.blockHeight);
            }
        }
    }

    public class CalculateCoord implements BlockVisitor {
        LayoutContext layoutContext;

        CalculateCoord(LayoutContext layoutContext) {
            this.layoutContext = layoutContext;
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
}
