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
public class IntIdProvider implements IdProvider {

    private final String prefixId;
    private int count;

    public IntIdProvider(String prefixId) {
        this.prefixId = prefixId;
        this.count = 0;
    }

    @Override
    public String createSvgId(String equipmentId) {
        return nextId();
    }

    @Override
    public String createSvgId(String equipmentId, String subType) {
        return nextId();
    }

    @Override
    public String createSvgId(String containerId, String id1, String id2) {
        return nextId();
    }

    private String nextId() {
        return prefixId + count++;
    }
}
