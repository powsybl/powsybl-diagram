/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InternCell extends AbstractBusCell {

    public enum Shape {
        UNDEFINED, UNILEG, FLAT, MAYBEFLAT, VERTICAL, CROSSOVER
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(InternCell.class);

    private static final Side BODY_SIDE = Side.LEFT;

    private Shape shape;
    private Map<Side, LegBlock> legs;
    private Block body;
    private boolean exceptionIfPatternNotHandled;

    public InternCell(Graph graph, boolean exceptionIfPatternNotHandled) {
        super(graph, CellType.INTERN);
        setDirection(Direction.TOP);
        shape = Shape.UNDEFINED;
        this.exceptionIfPatternNotHandled = exceptionIfPatternNotHandled;
    }

    public void organizeBlocks() {
        legs = new EnumMap<>(Side.class);
        List<LegBlock> candidateLegs = searchLegs();
        if (getRootBlock().getType() == Block.Type.SERIAL && candidateLegs.size() == 2) {
            SerialBlock serialRootBlock = (SerialBlock) getRootBlock();
            assignLeg(serialRootBlock, candidateLegs.get(0));
            assignLeg(serialRootBlock, candidateLegs.get(1));
            body = serialRootBlock.extractBody(new ArrayList<>(legs.values()));
            body.setOrientation(Orientation.HORIZONTAL);
        } else {
            if (candidateLegs.size() != 1) {
                if (exceptionIfPatternNotHandled) {
                    throw new PowsyblException("InternCell pattern not recognized");
                } else {
                    LOGGER.error("InternCell pattern not handled");
                    legs.put(Side.UNDEFINED, candidateLegs.get(0));
                }
            }
        }
        if (candidateLegs.size() == 1) {
            shape = Shape.UNILEG;
            legs.put(Side.UNDEFINED, candidateLegs.get(0));
        } else if (getBusNodes().size() == 2) {
            shape = Shape.MAYBEFLAT;
        }
    }

    private void assignLeg(SerialBlock sb, LegBlock candidateLeg) {
        Block.Extremity extremity = sb.whichExtremity(candidateLeg);
        if (extremity != Block.Extremity.NONE) {
            legs.put(extremityToSide(extremity), candidateLeg);
        } else {
            throw new PowsyblException("Unable to identify legs of internCell");
        }
    }

    private Side extremityToSide(Block.Extremity extremity) {
        if (extremity == Block.Extremity.START) {
            return Side.LEFT;
        }
        if (extremity == Block.Extremity.END) {
            return Side.RIGHT;
        }
        return Side.UNDEFINED;
    }

    private List<LegBlock> searchLegs() {
        List<LegBlock> candidateLegs = new ArrayList<>();
        List<LegPrimaryBlock> plbCopy = new ArrayList<>(getPrimaryLegBlocks());
        while (!plbCopy.isEmpty()) {
            LegPrimaryBlock lpb = plbCopy.get(0);
            Block parentBlock = lpb.getParentBlock();
            if (parentBlock instanceof LegParralelBlock) {
                candidateLegs.add((LegBlock) parentBlock);
                plbCopy.removeAll(((LegParralelBlock) parentBlock).subBlocks);
            } else {
                candidateLegs.add(lpb);
                plbCopy.remove(lpb);
            }
        }
        return candidateLegs;
    }

    public void identifyIfFlat() {
        List<BusNode> buses = getBusNodes();
        if (shape != Shape.MAYBEFLAT) {
            return;
        }
        Position pos1 = buses.get(0).getStructuralPosition();
        Position pos2 = buses.get(1).getStructuralPosition();
        if (Math.abs(pos2.getH() - pos1.getH()) == 1 && pos2.getV() == pos1.getV()) {
            setDirection(Direction.FLAT);
            shape = Shape.FLAT;
            getRootBlock().setOrientation(Orientation.HORIZONTAL);
        } else {
            shape = Shape.CROSSOVER;
        }
    }

    public boolean checkShape(Shape shape) {
        return this.shape == shape;
    }

    public boolean checkIsNotShape(Shape... shapes) {
        for (Shape s : shapes) {
            if (this.shape == s) {
                return false;
            }
        }
        return true;
    }

    public void reverseCell() {
        body.reverseBlock();
        if (legs.get(Side.LEFT) != null) {
            LegBlock tmp = legs.get(Side.LEFT);
            legs.put(Side.LEFT, legs.get(Side.RIGHT));
            legs.put(Side.RIGHT, tmp);
        }
    }

    public int getSideHPos(Side side) {
        return getSideToLeg(side).getPosition().getH();
    }

    @Override
    public void blockSizing() {
        legs.values().forEach(Block::sizing);
        if (shape != Shape.UNILEG && shape != Shape.UNDEFINED) {
            body.sizing();
        }
    }

    @Override
    public int newHPosition(int hPosition) {
        int h = hPosition;
        if (shape == Shape.UNILEG) {
            legs.get(Side.UNDEFINED).getPosition().setH(h);
            h += legs.get(Side.UNDEFINED).getPosition().getHSpan();
        } else {
            legs.get(Side.LEFT).getPosition().setH(h);
            h += legs.get(Side.LEFT).getPosition().getHSpan();
            if (shape == Shape.FLAT) {
                body.getPosition().setHV(h, legs.get(Side.LEFT).getBusNodes().get(0).getStructuralPosition().getV());
            } else {
                h -= 1;
                body.getPosition().setHV(h, 1);
            }
            h += body.getPosition().getHSpan();
            legs.get(Side.RIGHT).getPosition().setH(h);
            h += legs.get(Side.RIGHT).getPosition().getHSpan();
        }
        return h;
    }

    public int newHPosition(int hPosition, Side side) {
        int h = hPosition;
        if (side == Side.LEFT) {
            legs.get(Side.LEFT).getPosition().setH(h);
            h += legs.get(Side.LEFT).getPosition().getHSpan();
        }
        if (side == BODY_SIDE) {
            h -= 1;
            body.getPosition().setHV(h, 1);
            h += body.getPosition().getHSpan();
        }
        if (side == Side.RIGHT) {
            legs.get(Side.RIGHT).getPosition().setH(h);
            h += legs.get(Side.RIGHT).getPosition().getHSpan();
        }
        return h;
    }

    @Override
    public void calculateCoord(LayoutParameters layoutParam) {
        legs.values().forEach(lb -> lb.calculateRootCoord(layoutParam));
        if (shape != Shape.UNILEG && shape != Shape.UNDEFINED) {
            body.calculateRootCoord(layoutParam);
        }
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
        if (shape == Shape.FLAT) {
            setDirection(Direction.FLAT);
        }
    }
}
