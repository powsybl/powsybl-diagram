/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;

import static com.powsybl.sld.model.Position.Dimension.*;

import java.util.*;

import static com.powsybl.sld.model.Orientation.UP;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class SerialBlock extends AbstractComposedBlock {

    /**
     * Constructor
     * A layout.block chain is oriented in order to have.
     * Lower - embedding BusNode if only one of both layout.block embed a BusNode
     * Upper - (as a consequence) can embed a BusNode only if Lower as one
     */

    public SerialBlock(List<Block> blocks, Cell cell) {
        super(Type.SERIAL, blocks);
        subBlocks = new ArrayList<>(blocks);
        setCell(cell);
        postConstruct();
    }

    public SerialBlock(Block block, Cell cell) {
        this(Collections.singletonList(block), cell);
    }

    @Override
    public int getOrder() {
        return getExtremityNode(Block.Extremity.END).getType() == Node.NodeType.FEEDER ?
                ((FeederNode) getExtremityNode(Block.Extremity.END)).getOrder() : 0;
    }

    private void postConstruct() {
        if (subBlocks.size() != 1) {
            for (int i = 0; i < subBlocks.size() - 1; i++) {
                alignChaining(subBlocks.get(i), subBlocks.get(i + 1));
            }

            if (getLowerBlock().isEmbeddingNodeType(Node.NodeType.FEEDER)
                    || getUpperBlock().isEmbeddingNodeType(Node.NodeType.BUS)) {
                reverseBlock();
            }
        }

        setCardinality(Extremity.START, getLowerBlock().getCardinality(Extremity.START));
        setCardinality(Extremity.END, getUpperBlock().getCardinality(Extremity.END));
    }

    private void alignChaining(Block block1, Block block2) {
        if (block1.getExtremityNode(Extremity.END) == block2.getExtremityNode(Extremity.START)) {
            return;
        }
        if (block1.getExtremityNode(Extremity.END) == block2.getExtremityNode(Extremity.END)) {
            block2.reverseBlock();
            return;
        }
        if (block1.getExtremityNode(Extremity.START) == block2.getExtremityNode(Extremity.END)) {
            block1.reverseBlock();
            block2.reverseBlock();
            return;
        }
        if (block1.getExtremityNode(Extremity.START) == block2.getExtremityNode(Extremity.START)) {
            block1.reverseBlock();
            return;
        }
        throw new PowsyblException("unconsistent chaining in SerialBlock");
    }

    public boolean addSubBlock(Block block) {
        for (Extremity myExtremity : Extremity.values()) {
            if (getExtremityNode(myExtremity) instanceof FictitiousNode) {
                FictitiousNode commonNode = (FictitiousNode) getExtremityNode(myExtremity);
                for (Extremity itsExtremity : Extremity.values()) {
                    if (commonNode == block.getExtremityNode(itsExtremity)
                            && commonNode.getCardinality() == getCardinality(commonNode) + block.getCardinality(commonNode)
                    ) {
                        insertBlock(block, myExtremity);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    Optional<Extremity> whichExtremity(Block block) {
        if (block.equals(subBlocks.get(0))) {
            return Optional.of(Extremity.START);
        }
        if (block.equals(subBlocks.get(subBlocks.size() - 1))) {
            return Optional.of(Extremity.END);
        }
        return Optional.empty();
    }

    Block extractBody(Collection<Block> blocks) {
        List<Block> subBlocksCopy = new ArrayList<>(subBlocks);
        subBlocksCopy.removeAll(blocks);
        if (subBlocksCopy.size() == 1) {
            return subBlocksCopy.get(0);
        } else {
            return new SerialBlock(subBlocksCopy, getCell());
        }
    }

    private void insertBlock(Block block, Extremity myExtremity) {
        block.setParentBlock(this);
        if (myExtremity == Extremity.START) {
            subBlocks.add(0, block);
        } else {
            subBlocks.add(block);
        }
        postConstruct();
    }

    public Block getUpperBlock() {
        return subBlocks.get(subBlocks.size() - 1);
    }

    public Block getLowerBlock() {
        return subBlocks.get(0);
    }

    private List<Node> getChainingNodes() {
        List<Node> result = new ArrayList<>();
        for (int i = 0; i < subBlocks.size() - 1; i++) {
            result.add(subBlocks.get(i).getEndingNode());
        }
        return result;
    }

    @Override
    public void sizing() {
        subBlocks.forEach(Block::sizing);
        if (getPosition().getOrientation().isVertical()) {
            getPosition().getHSeg().mergeEnvelop(getSegments(H));
            getPosition().getVSeg().glue(getSegments(V));
        } else {
            getPosition().getVSeg().mergeEnvelop(getSegments(V));
            getPosition().getHSeg().glue(getSegments(H));
        }
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        double y0;
        double yPxStep;
        int sign = getOrientation() == UP ? 1 : -1;
        y0 = getCoord().getY() + sign * getCoord().getYSpan() / 2;
        yPxStep = -sign * getCoord().getYSpan() / getPosition().getVSpan();

        for (Block sub : subBlocks) {
            sub.setX(getCoord().getX());
            sub.setXSpan(getCoord().getXSpan());

            sub.setYSpan(
                    getCoord().getYSpan() * ((double) sub.getPosition().getVSpan() / getPosition().getVSpan()));
            sub.setY(y0 + yPxStep * (sub.getPosition().getV() + (double) sub.getPosition().getVSpan() / 2));

            sub.calculateCoord(layoutParam);
        }
        getChainingNodes().forEach(n -> n.setX(getCoord().getX()));
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        double x0 = getCoord().getX() - getCoord().getXSpan() / 2;
        double xPxStep = getCoord().getXSpan() / getPosition().getHSpan();

        for (int i = 0; i < subBlocks.size(); i++) {
            Block sub = subBlocks.get(i);
            sub.setX(x0 + (sub.getPosition().getH() + (double) sub.getPosition().getHSpan() / 2) * xPxStep);
            sub.setXSpan(sub.getPosition().getHSpan() * xPxStep);
            sub.setY(getCoord().getY());
            sub.setYSpan(getCoord().getYSpan());
            sub.calculateCoord(layoutParam);
        }
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        double blockHeight = 0.;
        for (int i = 0; i < subBlocks.size(); i++) {
            Block sub = subBlocks.get(i);
            // Here, the subBlocks are positioned serially, so we add the height of all these subBlocks
            blockHeight += sub.calculateHeight(encounteredNodes, layoutParameters);
        }
        return blockHeight;
    }

}
