/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractComposedBlock extends AbstractBlock implements ComposedBlock {

    List<Block> subBlocks;

    AbstractComposedBlock(Type type, List<Block> subBlocks) {
        super(type);
        if (subBlocks.isEmpty()) {
            throw new IllegalArgumentException("Empty block list");
        }
        subBlocks.forEach(b -> b.setParentBlock(this));
    }

    @Override
    public Graph getGraph() {
        return subBlocks.get(0).getGraph();
    }

    public List<Block> getSubBlocks() {
        return subBlocks;
    }

    @Override
    public boolean isEmbedingNodeType(Node.NodeType type) {
        return subBlocks.stream().anyMatch(b -> b.isEmbedingNodeType(type));
    }

    @Override
    public int getOrder() {
        return getExtremityNode(Block.Extremity.END).getType() == Node.NodeType.FEEDER ?
                ((FeederNode) getExtremityNode(Block.Extremity.END)).getOrder() : 0;
    }

    @Override
    public Node getExtremityNode(Extremity extremity) {
        if (extremity == Extremity.START) {
            return subBlocks.get(0).getExtremityNode(Extremity.START);
        }
        if (extremity == Extremity.END) {
            return subBlocks.get(subBlocks.size() - 1).getExtremityNode(Extremity.END);
        }
        return null;
    }

    @Override
    public void reverseBlock() {
        Collections.reverse(subBlocks);
        subBlocks.forEach(Block::reverseBlock);
    }

    @Override
    public void setOrientation(Orientation orientation) {
        super.setOrientation(orientation);
        subBlocks.forEach(sub -> sub.setOrientation(orientation));
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeFieldName("subBlocks");
        generator.writeStartArray();
        for (Block subBlock : subBlocks) {
            subBlock.writeJson(generator);
        }
        generator.writeEndArray();
    }

    @Override
    public String toString() {
        return "BodyParallelBlock(subBlocks=" + subBlocks + ")";
    }
}
