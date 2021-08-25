/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InternalNode extends FictitiousNode {

    public static final String ID_PREFIX = "INTERNAL_";

    public InternalNode(String id, VoltageLevelGraph graph) {
        super(prefixId(id, graph), null, null, NODE, graph);
    }

    private static String prefixId(String id, VoltageLevelGraph graph) {
        // for uniqueness purpose (in substation diagram), we prefix the id of the internal nodes with the voltageLevel id and "_"
        String tmpId = Objects.requireNonNull(id);
        if (tmpId.startsWith(ID_PREFIX)) {
            // the ids prefixed with INTERNAL should already be unique
            return tmpId;
        } else {
            return ID_PREFIX + graph.getVoltageLevelInfos().getId() + "_" + tmpId;
        }
    }
}
