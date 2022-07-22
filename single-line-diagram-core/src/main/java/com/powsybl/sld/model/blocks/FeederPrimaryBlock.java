/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model.blocks;

import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.List;

import static com.powsybl.sld.model.blocks.Block.Extremity.START;
import static com.powsybl.sld.model.blocks.Block.Type.FEEDERPRIMARY;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;
import static com.powsybl.sld.model.nodes.Node.NodeType.FEEDER;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class FeederPrimaryBlock extends AbstractPrimaryBlock {

    public FeederPrimaryBlock(List<Node> nodes) {
        super(FEEDERPRIMARY, nodes);
        if (getExtremityNode(START).getType() == FEEDER) {
            super.reverseBlock();
        }
    }

    public FeederNode getFeederNode() {
        return (FeederNode) nodes.get(1);
    }

    public Node getConnectedNode() {
        return nodes.get(0);
    }

    @Override
    public void reverseBlock() {
        // Will never happen.
    }

    @Override
    public void sizing() {
        // Orientation is always vertical (no horizontal feeder so far)
        // The span is equal to 0 as the height is constant
        getPosition().setSpan(H, 2);
        getPosition().setSpan(V, 0);
    }

    @Override
    public void accept(BlockVisitor blockVisitor) {
        blockVisitor.visit(this);
    }

    @Override
    public int getOrder() {
        return getFeederNode().getOrder().orElse(-1);
    }
}
