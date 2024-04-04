/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.cells;

import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.Collection;

import static com.powsybl.sld.model.cells.Cell.CellType.ARCH;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class ArchCell extends AbstractBusCell {

    private final Block pillar;

    private ArchCell(int cellNumber, Collection<Node> nodes, Block pillar) {
        super(cellNumber, ARCH, nodes);
        this.pillar = pillar;
    }

    public static ArchCell create(VoltageLevelGraph graph, Collection<Node> nodes, Block pillar) {
        var archCell = new ArchCell(graph.getNextCellNumber(), nodes, pillar);
        graph.addCell(archCell);
        return archCell;
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
        Position pos = getRootBlock().getPosition();
        pos.set(H, hPosition);
        pos.set(V, 0);
        return hPosition + pos.getSpan(H);
    }

    public Block getPillarBlock() {
        return pillar;
    }
}
