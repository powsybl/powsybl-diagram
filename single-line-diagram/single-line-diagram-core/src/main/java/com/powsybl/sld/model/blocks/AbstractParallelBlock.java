/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import com.powsybl.sld.model.nodes.Node;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.model.blocks.Block.Extremity.END;
import static com.powsybl.sld.model.blocks.Block.Extremity.START;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
abstract class AbstractParallelBlock<T extends Block> extends AbstractComposedBlock<T> {

    AbstractParallelBlock(Type type, List<T> subBlocks, boolean allowMerge) {
        super(type, subBlocks);
        this.subBlocks = new ArrayList<>();
        for (T child : subBlocks) {
            if (child.getType().isParallel() && allowMerge) {
                this.subBlocks.addAll(((ComposedBlock) child).getSubBlocks());
            } else {
                this.subBlocks.add(child);
            }
        }

        Node node0s = subBlocks.get(0).getExtremityNode(START);
        Node node0e = subBlocks.get(0).getExtremityNode(END);
        for (Block b : this.subBlocks) {
            b.setParentBlock(this);
            if (b.getExtremityNode(START) != node0s && b.getExtremityNode(END) != node0e) {
                b.reverseBlock();
            }
        }

        setCardinality(START, this.subBlocks.stream().mapToInt(c -> c.getCardinality(START)).sum());
        setCardinality(END, this.subBlocks.stream().mapToInt(c -> c.getCardinality(END)).sum());
    }

    @Override
    public void replaceEndingNode(Node newEndingNode) {
        for (Block subBlock : subBlocks) {
            subBlock.replaceEndingNode(newEndingNode);
        }
    }
}
