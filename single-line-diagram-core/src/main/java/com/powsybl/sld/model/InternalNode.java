/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InternalNode extends FictitiousNode {

    private static final String ID_PREFIX = "INTERNAL_";

    InternalNode(String id, String equipmentId, VoltageLevelGraph graph) {
        super(prefixId(id, graph), null, equipmentId, NODE, graph);
    }

    InternalNode(String id, VoltageLevelGraph graph) {
        this(id, null, graph);
    }

    private static String prefixId(String id, VoltageLevelGraph graph) {
        // for uniqueness purpose (in substation diagram), we prefix the id of the internal nodes with the voltageLevel id and "_"
        return ID_PREFIX + graph.getVoltageLevelInfos().getId() + "_" + Objects.requireNonNull(id);
    }

    public static boolean isIidmInternalNode(Node node) {
        return node instanceof InternalNode && StringUtils.isNumeric(node.getEquipmentId());
    }
}
