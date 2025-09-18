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
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.coordinate.Position.Dimension.H;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class ShuntCell extends AbstractCell {

    private final Map<Side, ExternCell> sideCells = new EnumMap<>(Side.class);

    private ShuntCell(int cellNumber, List<Node> nodes) {
        super(cellNumber, CellType.SHUNT, nodes);
        if (!(nodes.get(0) instanceof ConnectivityNode) || !(nodes.get(nodes.size() - 1) instanceof ConnectivityNode)) {
            throw new PowsyblException("the first and last nodes of a shunt cell shall be ConnectivityNode");
        }
        ((ConnectivityNode) nodes.get(0)).setShunt(true);
        ((ConnectivityNode) nodes.get(nodes.size() - 1)).setShunt(true);
    }

    /**
     * @param vlGraph the VoltageLevelGraph
     * @param shuntNodes a list of nodes that constitute a ShuntCell: the first and last nodes are both {@link ConnectivityNode}
     * @return a ShuntCell
     */
    public static ShuntCell create(VoltageLevelGraph vlGraph, List<Node> shuntNodes) {
        int cellNumber = vlGraph.getNextCellNumber();

        List<Node> extended = new ArrayList<>(shuntNodes);
        ConnectivityNode iNode1 = vlGraph.insertConnectivityNode(shuntNodes.get(0), shuntNodes.get(1), "Shunt " + cellNumber + ".1");
        extended.add(1, iNode1);

        ConnectivityNode iNode2 = vlGraph.insertConnectivityNode(extended.get(extended.size() - 1), extended.get(extended.size() - 2), "Shunt " + cellNumber + ".2");
        extended.add(extended.size() - 1, iNode2);

        ShuntCell shuntCell = new ShuntCell(cellNumber, extended);
        vlGraph.addCell(shuntCell);
        return shuntCell;
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
        return getRootBlock().getPosition().getSpan(H) - 4; // as 2 nodes are added in Shunts * 2 1/2 row = 4
    }

    public List<BusNode> getParentBusNodes() {
        return getSideCells().stream().flatMap(c -> c.getBusNodes().stream()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ShuntCell(" + nodes + " )";
    }
}
