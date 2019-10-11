/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.layout.LayoutParameters;

import java.util.Comparator;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BodyParallelBlock extends AbstractParallelBlock {

    public BodyParallelBlock(List<Block> subBlocks, Cell cell, boolean allowMerge) {
        super(subBlocks, cell, allowMerge);
    }

    @Override
    public void sizing() {
        subBlocks.forEach(Block::sizing);
        if (getPosition().getOrientation() == Orientation.VERTICAL) {
            getPosition().setVSpan(subBlocks.stream().mapToInt(b -> b.getPosition().getVSpan()).max().orElse(0));
            subBlocks.sort(Comparator.comparingInt(Block::getOrder));
            getPosition().setHSpan(subBlocks.stream().mapToInt(b -> b.getPosition().getHSpan()).sum());
            int h = 0;
            for (Block block : subBlocks) {
                block.getPosition().setHV(h, 0);
                h += block.getPosition().getHSpan();
            }
        } else {
            getPosition().setVSpan(subBlocks.stream().mapToInt(b -> b.getPosition().getVSpan()).sum());
            getPosition().setHSpan(subBlocks.stream().mapToInt(b -> b.getPosition().getHSpan()).max().orElse(0));
            int v = 0;
            for (Block subBlock : subBlocks) {
                subBlock.getPosition().setHV(0, v);
                v += subBlock.getPosition().getVSpan();
            }
        }
    }

    @Override
    double initX0() {
        return getCoord().getX() - getCoord().getXSpan() / 2;
    }

    @Override
    double intitXStep() {
        return getCoord().getXSpan() / getPosition().getHSpan();
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        subBlocks.forEach(sub -> {
            sub.setX(getCoord().getX());
            sub.setXSpan(getCoord().getXSpan());
            sub.setY(getCoord().getY());
            sub.setYSpan(getCoord().getYSpan());
            sub.calculateCoord(layoutParam);
        });
    }

    @Override
    public String toString() {
        return "BodyParallelBlock(subBlocks=" + subBlocks + ")";
    }
}
