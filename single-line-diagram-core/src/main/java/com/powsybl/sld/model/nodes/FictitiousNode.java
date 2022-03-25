/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 */
public class FictitiousNode extends Node {

    public FictitiousNode(String id, String name, String equipmentId, String componentType) {
        super(NodeType.FICTITIOUS, id, name, equipmentId, componentType, true);
    }

    public long getCardinality(VoltageLevelGraph vlGraph) {
        List<Node> adjacentNodes = getAdjacentNodes();
        int cardinality = adjacentNodes.size();
        if (getType() == NodeType.SHUNT) {
            long nbAdjacentShuntCells = adjacentNodes.stream().filter(n -> vlGraph.getCell(n).map(c -> c.getType() == Cell.CellType.SHUNT).orElse(true)).count();
            cardinality -= nbAdjacentShuntCells;
        }
        return cardinality;
    }
}
