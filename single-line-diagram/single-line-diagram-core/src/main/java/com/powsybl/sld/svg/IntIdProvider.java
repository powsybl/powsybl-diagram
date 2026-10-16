/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Charly Bion {@literal <charly.bion at rte-france.com>}
 */
public class IntIdProvider extends AbstractIdProvider {

    private int count;

    public IntIdProvider(String prefixId) {
        super(prefixId);
        this.count = 0;
    }

    @Override
    protected String createEquipmentId(String equipmentId) {
        return prefixId + count++;
    }

    @Override
    protected String createEquipmentSubtypeId(String equipmentId, String subType) {
        return prefixId + count++;
    }

    @Override
    protected String createWireId(String containerId, String id1, String id2) {
        return prefixId + count++;
    }
}
