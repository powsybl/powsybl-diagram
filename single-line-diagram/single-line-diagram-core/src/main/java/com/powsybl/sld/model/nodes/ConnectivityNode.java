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

import java.io.IOException;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
public class ConnectivityNode extends AbstractNode {

    private boolean isShunt = false;

    public ConnectivityNode(String id, String componentType) {
        super(NodeType.INTERNAL, id, componentType, true);
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
        if (!isShunt) {
            return super.getCardinality(vlGraph);
        } else {
            List<Node> adjacentNodes = getAdjacentNodes();
            int nbAdjacentShuntCells = (int) adjacentNodes.stream().filter(n -> vlGraph.getCell(n).map(c -> c.getType() == Cell.CellType.SHUNT).orElse(true)).count();
            return adjacentNodes.size() - nbAdjacentShuntCells;
        }
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        if (isShunt) {
            generator.writeBooleanField("isShunt", true);
        }
    }

}
