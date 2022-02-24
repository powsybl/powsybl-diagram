/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model.blocks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutContext;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.nodes.BusConnection;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.powsybl.sld.model.blocks.Block.Extremity.*;
import static com.powsybl.sld.model.blocks.Block.Type.*;
import static com.powsybl.sld.model.nodes.Node.NodeType.*;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.*;
import static com.powsybl.sld.model.coordinate.Orientation.*;
import static com.powsybl.sld.model.coordinate.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LegPrimaryBlock extends AbstractPrimaryBlock implements LegBlock {

    private final List<LegPrimaryBlock> stackableBlocks = new ArrayList<>();

    public LegPrimaryBlock(List<Node> nodes) {
        super(LEGPRIMARY, nodes);
        if (getExtremityNode(END).getType() == BUS) {
            super.reverseBlock();
        }
        if (!checkConsistency()) {
            throw new PowsyblException("LegPrimaryBlock not consistent");
        }
    }

    private boolean checkConsistency() {
        return nodes.size() == 3
                && nodes.get(0).getType() == BUS
                && checkMiddleNode(nodes.get(1))
                && (nodes.get(2).getType() == FICTITIOUS || nodes.get(2).getType() == Node.NodeType.SHUNT);
    }

    private boolean checkMiddleNode(Node node) {
        return node instanceof BusConnection
            || (node instanceof SwitchNode && ((SwitchNode) node).getKind() == SwitchNode.SwitchKind.DISCONNECTOR);
    }

    public BusNode getBusNode() {
        return (BusNode) getExtremityNode(START);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return Collections.singletonList(getBusNode());
    }

    private Node getNodeOnBus() {
        return nodes.get(1);
    }

    public void addStackableBlock(LegPrimaryBlock block) {
        stackableBlocks.add(block);
    }

    public List<LegPrimaryBlock> getStackableBlocks() {
        return new ArrayList<>(stackableBlocks);
    }

    @Override
    public void reverseBlock() {
        // Will never happen.
    }

    @Override
    public void sizing() {
        if (getOrientation().isHorizontal()) {
            getPosition().setSpan(H, 0);
            getPosition().setSpan(V, 0);
        } else {
            getPosition().setSpan(H, 2);
            getPosition().setSpan(V, 0);
        }
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        // LegPrimaryBlock has a 0 vertical span (see sizing above) and its height should not be included in external
        // cell height. Indeed, its height is fixed and corresponds to the layoutParameters.getStackSize().
        return 0.;
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam, LayoutContext layoutContext) {
        getNodeOnBus().setCoordinates(getCoord().get(X) + getCoord().getSpan(X) / 2, getBusNode().getY());
        getLegNode().setY(getBusNode().getY());
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam, LayoutContext layoutContext) {
        getNodeOnBus().setCoordinates(getCoord().get(X), getBusNode().getY());

        getLegNode().setX(getCoord().get(X));
        if (layoutContext.isInternCell() && layoutContext.isUnileg()) {
            getLegNode().setY(getCoord().get(Y) + (getOrientation() == UP ? -1 : 1) * layoutParam.getInternCellHeight());
        }
    }

}
