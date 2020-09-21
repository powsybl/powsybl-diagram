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
import java.util.Set;

import static com.powsybl.sld.model.Block.Extremity.*;
import static com.powsybl.sld.model.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
abstract class AbstractParallelBlock extends AbstractComposedBlock implements ParallelBlock {

    AbstractParallelBlock(Type type, List<Block> subBlocks, Cell cell, boolean allowMerge) {
        super(type, subBlocks);
        this.subBlocks = new ArrayList<>();
        subBlocks.forEach(child -> {
            if (child.getType().isParallel() && allowMerge) {
                this.subBlocks.addAll(((ParallelBlock) child).getSubBlocks());
            } else {
                this.subBlocks.add(child);
            }
        });
        setCell(cell);

        Node node0s = subBlocks.get(0).getExtremityNode(START);
        Node node0e = subBlocks.get(0).getExtremityNode(END);
        subBlocks.forEach(b -> {
            b.setParentBlock(this);
            if (b.getExtremityNode(START) != node0s && b.getExtremityNode(END) != node0e) {
                b.reverseBlock();
            }
        });

        setCardinality(START, this.subBlocks.size());
        setCardinality(END, this.subBlocks.size());
    }

    @Override
    public double initX0() {
        return getCoord().getX() - getCoord().getXSpan() / 2;
    }

    @Override
    public double intitXStep() {
        return getCoord().getXSpan() / getPosition().getSpan(H);
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        final double x0Final = initX0();
        final double xPxStepFinal = intitXStep();
        subBlocks.forEach(sub -> {
            sub.setX(x0Final + (sub.getPosition().get(H) + (double) sub.getPosition().getSpan(H) / 2) * xPxStepFinal);
            sub.setXSpan(xPxStepFinal * sub.getPosition().getSpan(H));
            sub.setY(getCoord().getY());
            sub.setYSpan(getCoord().getYSpan());
            sub.calculateCoord(layoutParam);
        });
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        double blockHeight = 0.;
        for (int i = 0; i < subBlocks.size(); i++) {
            Block sub = subBlocks.get(i);
            // Here, the subBlocks are positioned in parallel, so we calculate the max height of all these subBlocks
            blockHeight = Math.max(blockHeight, sub.calculateHeight(encounteredNodes, layoutParameters));
        }
        return blockHeight;
    }
}
