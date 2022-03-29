/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model.blocks;

import java.util.List;
import java.util.Objects;

import static com.powsybl.sld.model.coordinate.Coord.Dimension.*;

/**
 * A block group that cannot be correctly decomposed anymore.
 * All subBlocks are superposed.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class UndefinedBlock extends AbstractComposedBlock {

    public UndefinedBlock(List<Block> subBlocks) {
        super(Type.UNDEFINED, subBlocks);
        this.subBlocks = Objects.requireNonNull(subBlocks);
    }

    @Override
    public void sizing() {
        for (Block block : subBlocks) {
            block.sizing();
        }
        if (getPosition().getOrientation().isVertical()) {
            // better do nothing
        } else {
            throw new UnsupportedOperationException("Horizontal layout of undefined  block not supported");
        }
    }

    @Override
    public void accept(BlockVisitor blockVisitor) {
        blockVisitor.visit(this);
    }
}
