/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class ConnectivityNode extends Node {

    private boolean isShunt = false;

    public ConnectivityNode(String id, String name, String equipmentId) {
        super(NodeType.INTERNAL, id, name, equipmentId, NODE, true);
    }

    public boolean isShunt() {
        return isShunt;
    }

    public void setShunt(boolean shunt) {
        if (getAdjacentNodes().size() < 3) {
            throw new PowsyblException("to be a shunt, a node must have 3+ adjacent nodes");
        }
        isShunt = shunt;
    }

    @Override
    public int getCardinality(VoltageLevelGraph vlGraph) {
        List<Node> adjacentNodes = getAdjacentNodes();
        int cardinality = adjacentNodes.size();
        if (isShunt) {
            long nbAdjacentShuntCells = adjacentNodes.stream().filter(n -> vlGraph.getCell(n).map(c -> c.getType() == Cell.CellType.SHUNT).orElse(true)).count();
            cardinality -= nbAdjacentShuntCells;
        }
        return cardinality;
    }

    public static boolean isIidmInternalNode(Node node) {
        return node instanceof ConnectivityNode && StringUtils.isNumeric(node.getEquipmentId());
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        if (isShunt) {
            generator.writeBooleanField("isShunt", true);
        }
    }

}
