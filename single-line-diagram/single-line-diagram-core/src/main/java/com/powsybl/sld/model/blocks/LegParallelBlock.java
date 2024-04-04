/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LegParallelBlock extends AbstractParallelBlock<LegPrimaryBlock> implements LegBlock {

    public LegParallelBlock(List<LegPrimaryBlock> subBlocks, boolean allowMerge) {
        super(Type.LEGPARALLEL, subBlocks, allowMerge);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return subBlocks.stream().map(LegPrimaryBlock::getBusNode).toList();
    }

    @Override
    public void sizing() {
        subBlocks.forEach(Block::sizing);
        if (getPosition().getOrientation().isVertical()) {
            getPosition().setSpan(V, 0);
            List<LegPrimaryBlock> subBlocksCopy = new ArrayList<>(subBlocks);
            int h = 0;
            while (!subBlocksCopy.isEmpty()) {
                LegPrimaryBlock b = subBlocksCopy.get(0);
                Position pos = b.getPosition();
                pos.set(H, h);
                pos.set(V, 0);
                if (b.getStackableBlocks().isEmpty()) {
                    h += b.getPosition().getSpan(H);
                } else {
                    final int finalH = h;
                    b.getStackableBlocks().forEach(sb -> {
                        Position position = sb.getPosition();
                        position.set(H, finalH);
                        position.set(V, 0);
                    });
                    h += b.getPosition().getSpan(H);
                    subBlocksCopy.removeAll(b.getStackableBlocks());
                }
                subBlocksCopy.remove(b);
            }
            getPosition().setSpan(H, h);
        }
        // case HORIZONTAL cannot happen
    }

    @Override
    public void accept(BlockVisitor blockVisitor) {
        blockVisitor.visit(this);
    }
}
