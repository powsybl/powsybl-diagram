/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InternalNode extends Node {

    private static final String ID_PREFIX = "INTERNAL_";

    private boolean isShunt = false;

    protected InternalNode(String id, String equipmentId, String voltageLevelId) {
        super(NodeType.INTERNAL, prefixId(id, voltageLevelId), null, equipmentId, NODE, true);
    }

    public InternalNode(String id, String voltageLevelId) {
        this(id, null, voltageLevelId);
    }

    public InternalNode(int id, String voltageLevelId) {
        this(String.valueOf(id), String.valueOf(id), voltageLevelId);
    }

    private static String prefixId(String id, String voltageLevelId) {
        // for uniqueness purpose (in substation diagram), we prefix the id of the internal nodes with the voltageLevel id and "_"
        return ID_PREFIX + voltageLevelId + "_" + Objects.requireNonNull(id);
    }

    public boolean isShunt() {
        return isShunt;
    }

    public void setShunt(boolean shunt) {
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
        return node instanceof InternalNode && StringUtils.isNumeric(node.getEquipmentId());
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        if (isShunt) {
            generator.writeBooleanField("isShunt", true);
        }
    }


}
