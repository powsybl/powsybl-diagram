/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.blocks.*;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class InternCell extends AbstractBusCell {

    public enum Shape {
        /**
         * Initial state
         */
        UNDEFINED,

        /**
         * Pattern not handled (more than two legs)
         */
        UNHANDLED_PATTERN,

        /**
         * Intermediary state:
         * <ul>
         *     <li>if, due to some extern cells, the bus nodes are not in the same {@link com.powsybl.sld.layout.LegBusSet}, the intern cell is {@link #ONE_LEG}</li>
         *     <li>if not, the shape is put to either {@link #MAYBE_FLAT} if connecting only two bus nodes, or {@link #UNDEFINED}</li>
         * </ul>
         */
        MAYBE_ONE_LEG,

        /**
         * Intermediary state: the intern cell has only one BusNode on each side and therefore might be {@link #FLAT}.
         * If the corresponding bus nodes positions make it impossible, it could be either {@link #CROSSOVER} or
         * {@link #VERTICAL}.
         */
        MAYBE_FLAT,

        /**
         * Final state: the corresponding intern cell is displayed as a single straight line between two busbar sections
         */
        FLAT,

        /**
         * Final state: the corresponding intern cell is connecting two subsections, it hops over a subsections gap and
         * might hop over some extern cells
         */
        CROSSOVER,

        /**
         * Final state: the corresponding intern cell is in a single subsection
         */
        VERTICAL,

        /**
         * Final state: <i>impaired</i> vertical intern cell, that is, with no equipments.
         * Calling it uni-leg might be misleading as it's one LegParallelBlock but two (or more) LegBlocks,
         * drawn as a single line only if stacked.
         */
        ONE_LEG;

        public boolean checkIsShape(Shape... shapes) {
            return Arrays.stream(shapes).anyMatch(s -> s == this);
        }

        public boolean checkIsNotShape(Shape... shapes) {
            return Arrays.stream(shapes).noneMatch(s -> s == this);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(InternCell.class);

    private static final Side BODY_SIDE = Side.LEFT;

    private Shape shape;
    private final Map<Side, LegBlock> legs;
    private Block body;

    public InternCell(int cellNumber, Collection<Node> nodes) {
        super(cellNumber, CellType.INTERN, nodes);
        legs = new EnumMap<>(Side.class);
        setDirection(Direction.UNDEFINED);
        shape = Shape.UNDEFINED;
    }

    @Override
    public void accept(CellVisitor cellVisitor) {
        cellVisitor.visit(this);
    }

    public void organizeBlocks(boolean exceptionIfPatternNotHandled) {
        List<LegBlock> candidateLegs = searchLegs();
        if (getRootBlock().getType() == Block.Type.SERIAL && candidateLegs.size() == 2) {
            SerialBlock serialRootBlock = (SerialBlock) getRootBlock();
            assignLeg(serialRootBlock, candidateLegs.get(0));
            assignLeg(serialRootBlock, candidateLegs.get(1));
            body = serialRootBlock.extractBody(new ArrayList<>(legs.values()));
            body.setOrientation(Orientation.RIGHT);

            // if one bus on each side, the intern cell is either flat or vertical
            // if more than one bus on one side, the intern cell is
            //  - either crossover (two sections): detected later in Subsection::identifyCrossOverAndCheckOrientation
            //  - or vertical (one section): detected later in Subsection::identifyVerticalInternCells
            if (candidateLegs.stream().map(LegBlock::getBusNodes).allMatch(bn -> bn.size() == 1)) {
                shape = Shape.MAYBE_FLAT;
            }
        } else {
            if (candidateLegs.size() == 1) {
                shape = Shape.MAYBE_ONE_LEG;
                LegBlock leg = candidateLegs.get(0);
                legs.put(Side.UNDEFINED, leg);
                leg.setOrientation(Orientation.UP);
            } else {
                if (exceptionIfPatternNotHandled) {
                    throw new PowsyblException("InternCell pattern not recognized");
                } else {
                    shape = Shape.UNHANDLED_PATTERN;
                    LOGGER.error("InternCell pattern not handled");
                    LegBlock leg = candidateLegs.get(0);
                    legs.put(Side.UNDEFINED, candidateLegs.get(0));
                    leg.setOrientation(Orientation.UP);
                }
            }
        }
    }

    public void replaceOneLegByMultiLeg() {
        LegBlock oneLeg = legs.get(Side.UNDEFINED); // non-null as once one-leg
        if (oneLeg instanceof LegParallelBlock legParallelBlock) {
            List<LegPrimaryBlock> subBlocks = legParallelBlock.getSubBlocks();
            if (subBlocks.size() == 2) {
                body = BodyPrimaryBlock.createBodyPrimaryBlockInBusCell(List.of(subBlocks.get(0).getEndingNode()));
                body.setOrientation(Orientation.RIGHT);
                SerialBlock serialRootBlock = new SerialBlock(List.of(subBlocks.get(0), body, subBlocks.get(1)));
                blocksSetting(serialRootBlock, getLegPrimaryBlocks(), List.of());
                legs.remove(Side.UNDEFINED);
                assignLeg(serialRootBlock, subBlocks.get(0));
                assignLeg(serialRootBlock, subBlocks.get(1));
            }
            if (subBlocks.stream().map(LegBlock::getBusNodes).allMatch(bn -> bn.size() == 1)) {
                shape = Shape.MAYBE_FLAT;
            }
            return;
        }
        // Fails to replace it by a multileg -> marks it one leg
        shape = Shape.ONE_LEG;
    }

    public void setFlat() {
        shape = Shape.FLAT;
        setDirection(Direction.MIDDLE);
        legs.values().forEach(l -> l.setOrientation(Orientation.RIGHT));
    }

    private void assignLeg(SerialBlock sb, LegBlock candidateLeg) {
        Optional<Block.Extremity> extremity = sb.whichExtremity(candidateLeg);
        if (extremity.isPresent()) {
            legs.put(extremityToSide(extremity.get()), candidateLeg);
            candidateLeg.setOrientation(Orientation.UP);
        } else {
            throw new PowsyblException("Unable to identify legs of internCell");
        }
    }

    private Side extremityToSide(Block.Extremity extremity) {
        switch (extremity) {
            case START: return Side.LEFT;
            case END: return Side.RIGHT;
            default: return Side.UNDEFINED;
        }
    }

    private List<LegBlock> searchLegs() {
        return getLegPrimaryBlocks().stream()
                .map(lpb -> lpb.getParentBlock() instanceof LegParallelBlock ? (LegParallelBlock) lpb.getParentBlock() : lpb)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean checkIsShape(Shape... shape) {
        return this.shape.checkIsShape(shape);
    }

    public boolean checkIsNotShape(Shape... shapes) {
        return shape.checkIsNotShape(shapes);
    }

    public void reverseCell() {
        body.reverseBlock();
        legs.computeIfPresent(Side.LEFT, (leftSide, leftLeg) -> {
            LegBlock rightLeg = legs.get(Side.RIGHT);
            legs.put(Side.RIGHT, leftLeg);
            return rightLeg;
        });
    }

    public int getSideHPos(Side side) {
        return getSideToLeg(side).getPosition().get(H);
    }

    @Override
    public void blockSizing() {
        legs.values().forEach(Block::sizing);
        if (shape.checkIsNotShape(Shape.ONE_LEG, Shape.UNDEFINED, Shape.UNHANDLED_PATTERN)) {
            body.sizing();
        }
    }

    @Override
    public int newHPosition(int hPosition) {
        int h = hPosition;
        if (shape == Shape.ONE_LEG) {
            legs.get(Side.UNDEFINED).getPosition().set(H, h);
            h += legs.get(Side.UNDEFINED).getPosition().getSpan(H);
        } else {
            legs.get(Side.LEFT).getPosition().set(H, h);
            h += legs.get(Side.LEFT).getPosition().getSpan(H);
            Position pos = body.getPosition();
            if (shape == Shape.FLAT) {
                pos.set(H, h);
                pos.set(V, legs.get(Side.LEFT).getBusNodes().get(0).getBusbarIndex());
            } else {
                h -= 2;
                pos.set(H, h);
                pos.set(V, 0);
            }
            h += pos.getSpan(H);
            legs.get(Side.RIGHT).getPosition().set(H, h);
            h += legs.get(Side.RIGHT).getPosition().getSpan(H);
        }
        return h;
    }

    public int newHPosition(int hPosition, Side side) {
        int h = hPosition;
        if (side == Side.LEFT) {
            legs.get(Side.LEFT).getPosition().set(H, h);
            h += legs.get(Side.LEFT).getPosition().getSpan(H);
        }
        if (side == BODY_SIDE) {
            h -= 2;
            Position pos = body.getPosition();
            pos.set(H, h);
            pos.set(V, 1);
            h += body.getPosition().getSpan(H);
        }
        if (side == Side.RIGHT) {
            legs.get(Side.RIGHT).getPosition().set(H, h);
            h += legs.get(Side.RIGHT).getPosition().getSpan(H);
        }
        return h;
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
        for (LegBlock leg : legs.values()) {
            leg.setOrientation(direction.toOrientation());
        }
    }

    public Map<Side, LegBlock> getLegs() {
        return legs;
    }

    public LegBlock getSideToLeg(Side side) {
        return legs.get(side);
    }

    public List<BusNode> getSideBusNodes(Side side) {
        return legs.get(side).getBusNodes();
    }

    public Block getBodyBlock() {
        return body;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
