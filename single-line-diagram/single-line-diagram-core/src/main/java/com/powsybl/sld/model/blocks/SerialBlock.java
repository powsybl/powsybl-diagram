/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.powsybl.sld.model.blocks.Block.Type.SERIAL;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class SerialBlock extends AbstractComposedBlock<Block> {

    /**
     * Constructor
     * A layout.block chain is oriented in order to have.
     * Lower - embedding BusNode if only one of both layout.block embed a BusNode
     * Upper - (as a consequence) can embed a BusNode only if Lower as one
     */

    public SerialBlock(List<Block> blocks) {
        super(SERIAL, blocks);
        if (blocks.size() == 1 && blocks.get(0).getType() == SERIAL) {
            subBlocks = ((SerialBlock) blocks.get(0)).getSubBlocks();
        } else {
            subBlocks = new ArrayList<>(blocks);
        }
        postConstruct();
    }

    public SerialBlock(Block block) {
        this(Collections.singletonList(block));
    }

    @Override
    public void replaceEndingNode(Node newEndingNode) {
        getUpperBlock().replaceEndingNode(newEndingNode);
    }

    @Override
    public int getOrder() {
        return getExtremityNode(Block.Extremity.END).getType() == Node.NodeType.FEEDER ?
                getExtremityNode(Extremity.END).getOrder().orElse(-1) : 0;
    }

    @Override
    public void accept(BlockVisitor blockVisitor) {
        blockVisitor.visit(this);
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

    public boolean addSubBlock(VoltageLevelGraph vlGraph, Block block) {
        // Looking for a common extremity node between current serial block and given block
        for (Extremity myExtremity : Extremity.values()) {
            Node extremityNode = getExtremityNode(myExtremity);
            if (extremityNode instanceof FeederNode || extremityNode instanceof BusNode) {
                continue;
            }
            for (Extremity itsExtremity : Extremity.values()) {
                if (extremityNode == block.getExtremityNode(itsExtremity)
                        && extremityNode.getCardinality(vlGraph) == getCardinality(extremityNode) + block.getCardinality(itsExtremity)) {
                    insertBlock(block, myExtremity);
                    return true;
                }
            }
        }
        return false;
    }

    public Optional<Extremity> whichExtremity(Block block) {
        if (block.equals(subBlocks.get(0))) {
            return Optional.of(Extremity.START);
        }
        if (block.equals(subBlocks.get(subBlocks.size() - 1))) {
            return Optional.of(Extremity.END);
        }
        return Optional.empty();
    }

    public Block extractBody(Collection<Block> blocks) {
        List<Block> subBlocksCopy = new ArrayList<>(subBlocks);
        subBlocksCopy.removeAll(blocks);
        if (subBlocksCopy.size() == 1) {
            return subBlocksCopy.get(0);
        } else {
            return new SerialBlock(subBlocksCopy);
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

    public List<Node> getChainingNodes() {
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
            getPosition().getSegment(H).mergeEnvelop(getSegments(H));
            getPosition().getSegment(V).glue(getSegments(V));
        } else {
            getPosition().getSegment(V).mergeEnvelop(getSegments(V));
            getPosition().getSegment(H).glue(getSegments(H));
        }
    }
}
