/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InternalNode extends Node {

    private static final String ID_PREFIX = "INTERNAL_";

    private InternalNode(String id, String equipmentId, String voltageLevelId) {
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

    public static boolean isIidmInternalNode(Node node) {
        return node instanceof InternalNode && StringUtils.isNumeric(node.getEquipmentId());
    }
}
