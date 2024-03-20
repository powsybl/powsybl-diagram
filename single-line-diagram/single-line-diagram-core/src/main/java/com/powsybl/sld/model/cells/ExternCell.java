/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.powsybl.sld.model.cells.Cell.CellType.EXTERN;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;
import static com.powsybl.sld.model.coordinate.Side.LEFT;
import static com.powsybl.sld.model.coordinate.Side.RIGHT;
import static com.powsybl.sld.model.nodes.Node.NodeType.FEEDER;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class ExternCell extends AbstractBusCell {

    private final List<ShuntCell> shuntCells = new ArrayList<>();

    private ExternCell(int cellNumber, Collection<Node> nodes, List<ShuntCell> shuntCells) {
        super(cellNumber, EXTERN, nodes);
        shuntCells.forEach(this::addShuntCell);
    }

    public static ExternCell create(VoltageLevelGraph graph, Collection<Node> nodes, List<ShuntCell> shuntCells) {
        var externCell = new ExternCell(graph.getNextCellNumber(), nodes, shuntCells);
        graph.addCell(externCell);
        return externCell;
    }

    public void organizeBlockDirections() {
        getRootBlock().setOrientation(getDirection().toOrientation());
    }

    @Override
    public void accept(CellVisitor cellVisitor) {
        cellVisitor.visit(this);
    }

    @Override
    public int newHPosition(int hPosition) {
        int minHv = shuntCells.stream().filter(shuntCell -> shuntCell.getSideCell(RIGHT) == this)
                .mapToInt(shuntCell -> Math.max(hPosition, shuntCell.getSidePosition(LEFT).get(H) + shuntCell.getSidePosition(LEFT).getSpan(H) + shuntCell.getHSpan()))
                .max()
                .orElse(hPosition);
        Position pos = getRootBlock().getPosition();
        pos.set(H, minHv);
        pos.set(V, 0);
        return minHv + pos.getSpan(H);
    }

    public boolean isShunted() {
        return !shuntCells.isEmpty();
    }

    public List<ShuntCell> getShuntCells() {
        return shuntCells;
    }

    public void addShuntCell(ShuntCell shuntCell) {
        List<Node> shuntNodes = shuntCell.getNodes();
        if (getNodes().contains(shuntNodes.get(0))) {
            shuntCell.putSideCell(Side.LEFT, this);
        } else if (getNodes().contains(shuntNodes.get(shuntNodes.size() - 1))) {
            shuntCell.putSideCell(Side.RIGHT, this);
        } else {
            throw new PowsyblException("ShuntCell list of nodes incoherent with the connected externCells");
        }
        this.shuntCells.add(shuntCell);
    }

    @Override
    public String toString() {
        return getType() + " " + getOrder() + " " + getDirection() + " " + nodes;
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
        getNodes().stream().filter(f -> f.getType() == FEEDER)
                .map(FeederNode.class::cast)
                .forEach(fn -> {
                    if (fn.getOrientation() == null || !fn.getOrientation().isHorizontal()) {
                        fn.setDirection(direction);
                    }
                });
    }

}
