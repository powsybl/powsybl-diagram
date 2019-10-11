/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.layout.LayoutParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractParallelBlock extends AbstractComposedBlock implements ParallelBlock {

    AbstractParallelBlock(List<Block> subBlocks, Cell cell, boolean allowMerge) {
        super(Type.PARALLEL, subBlocks);
        this.subBlocks = new ArrayList<>();
        subBlocks.forEach(child -> {
            if (child.getType() == Type.PARALLEL && allowMerge) {
                this.subBlocks.addAll(((ParallelBlock) child).getSubBlocks());
            } else {
                this.subBlocks.add(child);
            }
        });
        setCell(cell);

        Node node0s = subBlocks.get(0).getExtremityNode(Block.Extremity.START);
        Node node0e = subBlocks.get(0).getExtremityNode(Block.Extremity.END);
        subBlocks.forEach(b -> {
            b.setParentBlock(this);
            if (b.getExtremityNode(Block.Extremity.START) != node0s && b.getExtremityNode(Block.Extremity.END) != node0e) {
                b.reverseBlock();
            }
        });

        setCardinality(Extremity.START, this.subBlocks.size());
        setCardinality(Extremity.END, this.subBlocks.size());
    }

    abstract double initX0();

    abstract double intitXStep();

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        final double x0Final = initX0();
        final double xPxStepFinal = intitXStep();
        subBlocks.forEach(sub -> {
            sub.setX(x0Final + (sub.getPosition().getH() + (double) sub.getPosition().getHSpan() / 2) * xPxStepFinal);
            sub.setXSpan(xPxStepFinal * sub.getPosition().getHSpan());
            sub.setY(getCoord().getY());
            sub.setYSpan(getCoord().getYSpan());
            sub.calculateCoord(layoutParam);
        });
    }

}
