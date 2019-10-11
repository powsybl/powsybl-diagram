/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model;

import com.powsybl.sld.layout.LayoutParameters;

import java.util.List;
import java.util.Objects;

/**
 * A block group that cannot be correctly decomposed anymore.
 * All subBlocks are superposed.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UndefinedBlock extends AbstractComposedBlock {

    public UndefinedBlock(List<Block> subBlocks) {
        super(Type.UNDEFINED, subBlocks);
        this.subBlocks = Objects.requireNonNull(subBlocks);
    }

    public UndefinedBlock(List<Block> subBlocks, Cell cell) {
        this(subBlocks);
        setCell(cell);
    }

    @Override
    public void sizing() {
        for (Block block : subBlocks) {
            block.sizing();
        }
        if (getPosition().getOrientation() == Orientation.VERTICAL) {
            // TODO
        } else {
            throw new UnsupportedOperationException("Horizontal layout of undefined  block not supported");
        }
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        for (Block block : subBlocks) {
            block.setX(getCoord().getX());
            block.setY(getCoord().getY());
            block.setXSpan(getCoord().getXSpan());
            block.setYSpan(getCoord().getYSpan());
            block.coordVerticalCase(layoutParam);
        }
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        throw new UnsupportedOperationException("Horizontal layout of undefined  block not supported");
    }

    @Override
    public String toString() {
        return "UndefinedBlock(subBlocks=" + subBlocks + ")";
    }
}
