/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import java.util.Comparator;
import java.util.List;

import static com.powsybl.sld.model.blocks.Block.Type.BODYPARALLEL;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BodyParallelBlock extends AbstractParallelBlock<Block> {

    public BodyParallelBlock(List<Block> subBlocks, boolean allowMerge) {
        super(BODYPARALLEL, subBlocks, allowMerge);
    }

    @Override
    public void sizing() {
        subBlocks.forEach(Block::sizing);
        if (getPosition().getOrientation().isVertical()) {
            getPosition().getSegment(V).mergeEnvelop(getSegments(V));
            subBlocks.sort(Comparator.comparingInt(Block::getOrder));
            getPosition().getSegment(H).glue(getSegments(H));
        } else {
            getPosition().getSegment(H).mergeEnvelop(getSegments(H));
            getPosition().getSegment(V).glue(getSegments(V));
        }
    }

    @Override
    public void accept(BlockVisitor blockVisitor) {
        blockVisitor.visit(this);
    }

}
