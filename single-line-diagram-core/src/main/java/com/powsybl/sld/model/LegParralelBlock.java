/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.layout.LayoutParameters;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LegParralelBlock extends AbstractParallelBlock implements LegBlock {

    public LegParralelBlock(List<Block> subBlocks, Cell cell, boolean allowMerge) {
        super(Type.LEGPARALLEL, subBlocks, cell, allowMerge);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return subBlocks.stream().map(b -> ((LegPrimaryBlock) b).getBusNode()).collect(Collectors.toList());
    }

    @Override
    public void sizing() {
        subBlocks.forEach(Block::sizing);
        if (getPosition().getOrientation() == Orientation.VERTICAL) {
            getPosition().setVSpan(0);
            List<LegPrimaryBlock> subBlocksCopy = subBlocks.stream()
                    .map(LegPrimaryBlock.class::cast).collect(Collectors.toList());
            int h = 0;
            while (!subBlocksCopy.isEmpty()) {
                LegPrimaryBlock b = subBlocksCopy.get(0);
                b.getPosition().setHV(h, 0);
                if (b.getStackableBlocks().isEmpty()) {
                    b.getPosition().setHV(h, 0);
                    h += b.getPosition().getHSpan();
                } else {
                    final int finalH = h;
                    b.getStackableBlocks().forEach(sb -> sb.getPosition().setHV(finalH, 0));
                    h++;
                    subBlocksCopy.removeAll(b.getStackableBlocks());
                }
                subBlocksCopy.remove(b);
            }
            getPosition().setHSpan(h);
        }
        // case HORIZONTAL cannot happen
    }

    @Override
    public double initX0() {
        return getCoord().getX()
                - (getPosition().getHSpan() == 1 ? 0 : getCoord().getXSpan() / 2);
    }

    @Override
    public double intitXStep() {
        return getPosition().getHSpan() == 1 ? 0 : getCoord().getXSpan() / getPosition().getHSpan();
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        // case HORIZONTAL cannot happen
    }

}
