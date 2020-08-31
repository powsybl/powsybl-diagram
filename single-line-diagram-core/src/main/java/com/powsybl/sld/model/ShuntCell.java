/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShuntCell extends AbstractCell {
    private Map<Side, ExternCell> cells = new EnumMap<>(Side.class);

    public ShuntCell(Graph graph) {
        super(graph, CellType.SHUNT);
    }

    public static ShuntCell create(ExternCell cell1, ExternCell cell2, List<Node> nodes) {
        ShuntCell shuntCell = new ShuntCell(cell1.getGraph());
        if (cell1.getNodes().contains(nodes.get(0)) && cell2.getNodes().contains(nodes.get(nodes.size() - 1))) {
            shuntCell.cells.put(Side.LEFT, cell1);
            shuntCell.cells.put(Side.RIGHT, cell2);
        } else if (cell2.getNodes().contains(nodes.get(0)) && cell1.getNodes().contains(nodes.get(nodes.size() - 1))) {
            shuntCell.cells.put(Side.LEFT, cell2);
            shuntCell.cells.put(Side.RIGHT, cell1);
        } else {
            throw new PowsyblException("ShuntCell list of nodes incoherent with the connected externCells");
        }
        shuntCell.addNodes(new ArrayList<>(nodes));
        shuntCell.alignExternCells();
        return shuntCell;
    }

    public void calculateCoord(LayoutParameters layoutParam) {
        if (getRootBlock() instanceof BodyPrimaryBlock) {
            ((BodyPrimaryBlock) getRootBlock()).coordShuntCase();
        } else {
            throw new PowsyblException("ShuntCell can only be composed of a single BodyPrimaryBlock");
        }
    }

    public void alignExternCells() {
        if (cells.get(Side.LEFT).getOrder() > cells.get(Side.RIGHT).getOrder()) {
            reverse();
        }
    }

    public void reverse() {
        ExternCell cell = cells.get(Side.LEFT);
        cells.put(Side.LEFT, cells.get(Side.RIGHT));
        cells.put(Side.RIGHT, cell);
        Collections.reverse(nodes);
    }

    public ExternCell getSideCell(Side side) {
        return cells.get(side);
    }

    public FictitiousNode getSideShuntNode(Side side) {
        if (side == Side.UNDEFINED) {
            return null;
        }
        return (FictitiousNode) (side == Side.LEFT ? nodes.get(0) : nodes.get(nodes.size() - 1));
    }

    public List<ExternCell> getCells() {
        return new ArrayList<>(cells.values());
    }

    public List<BusNode> getParentBusNodes() {
        return getCells().stream().flatMap(c -> c.getBusNodes().stream()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ShuntCell(" + nodes + " )";
    }
}
