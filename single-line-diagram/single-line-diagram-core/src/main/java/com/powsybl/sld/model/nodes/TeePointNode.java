/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import static com.powsybl.sld.library.SldComponentTypeName.TEE_POINT;

public class TeePointNode extends EquipmentNode {

    public TeePointNode(String id, String name, String equipmentId) {
        super(NodeType.INTERNAL, id, name, equipmentId, TEE_POINT, true);
    }

}
