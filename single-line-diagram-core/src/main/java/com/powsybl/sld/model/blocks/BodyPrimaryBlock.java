/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import com.powsybl.sld.model.nodes.Node;

import java.util.List;

import static com.powsybl.sld.model.blocks.Block.Type.BODYPRIMARY;
import static com.powsybl.sld.model.coordinate.Orientation.*;
import static com.powsybl.sld.model.coordinate.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class BodyPrimaryBlock extends AbstractPrimaryBlock {

    private BodyPrimaryBlock(List<Node> nodes) {
        super(BODYPRIMARY, nodes);
    }

    public static BodyPrimaryBlock createBodyPrimaryBlockInBusCell(List<Node> nodes) {
        return new BodyPrimaryBlock(nodes);
    }

    public static BodyPrimaryBlock createBodyPrimaryBlockForShuntCell(List<Node> nodes) {
        BodyPrimaryBlock bpy = new BodyPrimaryBlock(nodes);
        bpy.setOrientation(RIGHT);
        return bpy;
    }

    @Override
    public void sizing() {
        if (getPosition().getOrientation().isVertical()) {
            getPosition().setSpan(H, 2);
            // in the case of vertical Blocks the x Spanning is a ratio of the nb of edges of the blocks/overall edges
            getPosition().setSpan(V, 2 * (nodes.size() - 1));
        } else {
            // in the case of horizontal Blocks having 1 switch/1 position => 1 hPos / 2 edges rounded to the superior int
            getPosition().setSpan(H, 2 * Math.max(1, nodes.size() - 2));
            getPosition().setSpan(V, 2);
        }
    }

    @Override
    public void accept(BlockVisitor blockVisitor) {
        blockVisitor.visit(this);
    }
}
