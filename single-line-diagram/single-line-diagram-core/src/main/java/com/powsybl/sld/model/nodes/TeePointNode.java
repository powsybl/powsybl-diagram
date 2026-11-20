/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.svg.IdProvider;

import static com.powsybl.sld.library.SldComponentTypeName.TEE_POINT;

/**
 * @author Giovanni Ferrari {@literal <giovani.ferrari at soft.it>}
 */
public class TeePointNode extends EquipmentNode {

    public TeePointNode(String id, String name, String equipmentId) {
        this(id, name, equipmentId, new com.powsybl.sld.svg.LegacyIdProvider(""));
    }

    public TeePointNode(String id, String name, String equipmentId, IdProvider idProvider) {
        super(NodeType.INTERNAL, id, name, equipmentId, TEE_POINT, true, idProvider);
    }

}
