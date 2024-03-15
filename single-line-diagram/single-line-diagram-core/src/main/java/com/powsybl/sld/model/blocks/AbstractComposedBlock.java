/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.nodes.Node;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public abstract class AbstractComposedBlock<T extends Block> extends AbstractBlock implements ComposedBlock {

    List<T> subBlocks;

    AbstractComposedBlock(Type type, List<T> subBlocks) {
        super(type);
        if (subBlocks.isEmpty()) {
            throw new IllegalArgumentException("Empty block list");
        }
        subBlocks.forEach(b -> b.setParentBlock(this));
    }

    public List<T> getSubBlocks() {
        return subBlocks;
    }

    @Override
    public boolean isEmbeddingNodeType(Node.NodeType type) {
        return subBlocks.stream().anyMatch(b -> b.isEmbeddingNodeType(type));
    }

    @Override
    public List<Block> findBlockEmbeddingNode(Node node) {
        return subBlocks.stream().flatMap(b -> b.findBlockEmbeddingNode(node).stream()).collect(Collectors.toList());
    }

    @Override
    public Node getExtremityNode(Extremity extremity) {
        if (extremity == Extremity.START) {
            return subBlocks.get(0).getExtremityNode(Extremity.START);
        } else {
            return subBlocks.get(subBlocks.size() - 1).getExtremityNode(Extremity.END);
        }
    }

    @Override
    public void reverseBlock() {
        Collections.reverse(subBlocks);
        subBlocks.forEach(Block::reverseBlock);
    }

    @Override
    public void setOrientation(Orientation orientation) {
        super.setOrientation(orientation);
        setOrientation(orientation, true);
    }

    @Override
    public void setOrientation(Orientation orientation, boolean recursively) {
        super.setOrientation(orientation);
        if (recursively) {
            subBlocks.forEach(sub -> sub.setOrientation(orientation));
        }
    }

    public Stream<Position.Segment> getSegments(Position.Dimension dimension) {
        return subBlocks.stream().map(b -> b.getPosition().getSegment(dimension));
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeFieldName("subBlocks");
        generator.writeStartArray();
        for (Block subBlock : subBlocks) {
            subBlock.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + subBlocks;
    }
}
