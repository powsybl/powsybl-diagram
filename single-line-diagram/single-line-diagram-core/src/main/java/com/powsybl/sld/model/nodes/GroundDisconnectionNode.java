/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.nodes;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class GroundDisconnectionNode extends EquipmentNode {
    private final boolean isDisconnectorOpen;

    public GroundDisconnectionNode(String id, String name, boolean isDisconnectorOpen, String componentTypeName) {
        super(NodeType.INTERNAL, id, name, id, componentTypeName, false);
        this.isDisconnectorOpen = isDisconnectorOpen;
    }

    public boolean isDisconnectorOpen() {
        return isDisconnectorOpen;
    }
}
