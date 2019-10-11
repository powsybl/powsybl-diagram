/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.Cell;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;

import java.util.List;
import java.util.stream.Collectors;

/**
 * a PositionFinder determines:
 * <ul>
 *     <li>the positions of nodeBuses</li>
 *     <li>cell order and direction of each cell connected to Bus (ie all cells except Shunt ones)</li>
 * </ul>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface PositionFinder {

    void buildLayout(Graph graph);

    default void forceSameOrientationForShuntedCell(Graph graph) {
        for (Cell cell : graph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.SHUNT).collect(Collectors.toList())) {
            List<Node> shNodes = cell.getNodes().stream()
                    .filter(node -> node.getType() == Node.NodeType.SHUNT).collect(Collectors.toList());
            ((ExternCell) shNodes.get(1).getCell()).setDirection(
                    ((ExternCell) shNodes.get(0).getCell()).getDirection());
        }
    }
}
