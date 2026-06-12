/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultIdProvider implements IdProvider {

    private final String prefixId;
    private final Map<String, String> idMap = new HashMap<>();
    private int count;

    public DefaultIdProvider(String prefixId) {
        this.prefixId = prefixId;
        this.count = 0;
    }

    @Override
    public String getOrCreateSvgId(String equipmentId) {
        return getOrCreateId("equipmentId_" + equipmentId);
    }

    @Override
    public String getOrCreateSvgId(String equipmentId, String subType) {
        return getOrCreateId(String.format("equipmentId_%s_subType_%s", equipmentId, subType));
    }

    @Override
    public String getOrCreateSvgId(String containerId, String id1, String id2) {
        return getOrCreateId(String.format("containerId_%s_id1_%s_id2_%s", containerId, id1, id2));
    }

    private String getOrCreateId(String key) {
        return idMap.computeIfAbsent(key, ignored -> prefixId + count++);
    }
}
