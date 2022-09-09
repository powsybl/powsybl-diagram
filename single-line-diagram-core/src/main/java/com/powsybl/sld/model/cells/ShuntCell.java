/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.blocks.BodyPrimaryBlock;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.coordinate.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ShuntCell extends AbstractCell {

    private final Map<Side, ExternCell> sideCells = new EnumMap<>(Side.class);

    private ShuntCell(int cellNumber, List<Node> nodes) {
        super(cellNumber, CellType.SHUNT, nodes);
        if (!(nodes.get(0) instanceof ConnectivityNode) || !(nodes.get(nodes.size() - 1) instanceof ConnectivityNode)) {
            throw new PowsyblException("the first and last nodes of a shunt cell shall be InternalNode");
        }
        ((ConnectivityNode) nodes.get(0)).setShunt(true);
        ((ConnectivityNode) nodes.get(nodes.size() - 1)).setShunt(true);
    }

    public static ShuntCell create(int cellNumber, List<Node> nodes) {
        return new ShuntCell(cellNumber, nodes);
    }

    @Override
    public void accept(CellVisitor cellVisitor) {
        cellVisitor.visit(this);
    }

    @Override
    public Direction getDirection() {
        return Direction.UNDEFINED;
    }

    @Override
    public BodyPrimaryBlock getRootBlock() {
        return (BodyPrimaryBlock) super.getRootBlock();
    }

    @Override
    public void setRootBlock(Block rootBlock) {
        if (rootBlock instanceof BodyPrimaryBlock) {
            super.setRootBlock(rootBlock);
        } else {
            throw new PowsyblException("ShuntCell can only be composed of a single BodyPrimaryBlock");
        }
    }

    public void putSideCell(Side side, ExternCell externCell) {
        sideCells.put(side, externCell);
    }

    public void alignExternCells() {
        if (sideCells.get(Side.LEFT).getOrder().orElse(-1) > sideCells.get(Side.RIGHT).getOrder().orElse(-1)) {
            reverse();
        }
    }

    public void alignDirections(Side side) {
        sideCells.get(side.getFlip()).setDirection(sideCells.get(side).getDirection());
    }

    private void reverse() {
        ExternCell cell = sideCells.get(Side.LEFT);
        sideCells.put(Side.LEFT, sideCells.get(Side.RIGHT));
        sideCells.put(Side.RIGHT, cell);
        Collections.reverse(nodes);
        getRootBlock().reverseBlock();
    }

    public ExternCell getSideCell(Side side) {
        return sideCells.get(side);
    }

    public Node getSideShuntNode(Side side) {
        if (side == Side.UNDEFINED) {
            return null;
        }
        return side == Side.LEFT ? nodes.get(0) : nodes.get(nodes.size() - 1);
    }

    public Position getSidePosition(Side side) {
        return sideCells.get(side).getRootBlock().getPosition();
    }

    public List<ExternCell> getSideCells() {
        return new ArrayList<>(sideCells.values());
    }

    public int getHSpan() {
        return getRootBlock().getPosition().getSpan(H) / 2 - 1;
    }

    public List<BusNode> getParentBusNodes() {
        return getSideCells().stream().flatMap(c -> c.getBusNodes().stream()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ShuntCell(" + nodes + " )";
    }
}
