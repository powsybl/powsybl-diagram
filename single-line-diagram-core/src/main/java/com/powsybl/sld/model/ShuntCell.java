/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.coordinate.Side;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.coordinate.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ShuntCell extends AbstractCell {
    private Map<Side, ExternCell> cells = new EnumMap<>(Side.class);

    private ShuntCell(int cellNumber, List<Node> nodes) {
        super(cellNumber, CellType.SHUNT, nodes);
    }

    public static ShuntCell create(int cellNumber, ExternCell cell1, ExternCell cell2, List<Node> nodes) {
        ShuntCell shuntCell = new ShuntCell(cellNumber, nodes);
        if (cell1.getNodes().contains(nodes.get(0)) && cell2.getNodes().contains(nodes.get(nodes.size() - 1))) {
            shuntCell.cells.put(Side.LEFT, cell1);
            shuntCell.cells.put(Side.RIGHT, cell2);
        } else if (cell2.getNodes().contains(nodes.get(0)) && cell1.getNodes().contains(nodes.get(nodes.size() - 1))) {
            shuntCell.cells.put(Side.LEFT, cell2);
            shuntCell.cells.put(Side.RIGHT, cell1);
        } else {
            throw new PowsyblException("ShuntCell list of nodes incoherent with the connected externCells");
        }
        cell1.setShuntCell(shuntCell);
        cell2.setShuntCell(shuntCell);
        return shuntCell;
    }

    public void calculateCoord(VoltageLevelGraph vlGraph, LayoutParameters layoutParam) {
        if (getRootBlock() instanceof BodyPrimaryBlock) {
            Position lPos = getSidePosition(Side.LEFT);
            ((BodyPrimaryBlock) getRootBlock())
                    .coordShuntCase(layoutParam, lPos.get(H) + lPos.getSpan(H), getSidePosition(Side.RIGHT).get(H));
        } else {
            throw new PowsyblException("ShuntCell can only be composed of a single BodyPrimaryBlock");
        }
    }

    public void alignExternCells() {
        if (cells.get(Side.LEFT).getOrder().orElse(-1) > cells.get(Side.RIGHT).getOrder().orElse(-1)) {
            reverse();
        }
    }

    public void alignDirections(Side side) {
        cells.get(side.getFlip()).setDirection(cells.get(side).getDirection());
    }

    private void reverse() {
        ExternCell cell = cells.get(Side.LEFT);
        cells.put(Side.LEFT, cells.get(Side.RIGHT));
        cells.put(Side.RIGHT, cell);
        Collections.reverse(nodes);
        getRootBlock().reverseBlock();
    }

    public ExternCell getSideCell(Side side) {
        return cells.get(side);
    }

    public ExternCell getOtherSideCell(ExternCell cell) {
        if (cell == cells.get(Side.LEFT)) {
            return cells.get(Side.RIGHT);
        } else if (cell == cells.get(Side.RIGHT)) {
            return cells.get(Side.LEFT);
        }
        return null;
    }

    public FictitiousNode getSideShuntNode(Side side) {
        if (side == Side.UNDEFINED) {
            return null;
        }
        return (FictitiousNode) (side == Side.LEFT ? nodes.get(0) : nodes.get(nodes.size() - 1));
    }

    Position getSidePosition(Side side) {
        return cells.get(side).getRootBlock().getPosition();
    }

    public List<ExternCell> getCells() {
        return new ArrayList<>(cells.values());
    }

    public int getLength() {
        return getRootBlock().getPosition().getSpan(H) / 2 - 1;
    }

    public List<BusNode> getParentBusNodes() {
        return getCells().stream().flatMap(c -> c.getBusNodes().stream()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ShuntCell(" + nodes + " )";
    }
}
