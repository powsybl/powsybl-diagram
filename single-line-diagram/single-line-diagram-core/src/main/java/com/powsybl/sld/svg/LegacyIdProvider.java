/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.util.IdUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class LegacyIdProvider implements IdProvider {

    private interface IdKey {
    }

    private record EquipmentKey(String equipmentId) implements IdKey {
    }

    private record EquipmentSubtypeKey(String equipmentId, String subtype) implements IdKey {
    }

    private record WireKey(String containerId, String id1, String id2) implements IdKey {
    }

    private final String prefixId;
    private final Map<IdKey, String> idMap = new HashMap<>();

    public LegacyIdProvider(String prefixId) {
        this.prefixId = prefixId;
    }

    /**
     * Returns the historical unescaped form used in SLD metadata.
     *
     * @param equipmentId equipment identifier
     * @param subType subtype or label position
     * @return the unescaped legacy identifier
     */
    @Override
    public String getUnescapedSvgId(String equipmentId, String subType) {
        return prefixId + equipmentId + "_" + subType;
    }

    @Override
    public String getOrCreateSvgId(String equipmentId) {
        return idMap.computeIfAbsent(new EquipmentKey(equipmentId), ignored -> IdUtil.escapeId(prefixId + equipmentId));
    }

    @Override
    public String getOrCreateSvgId(String equipmentId, String subType) {
        return idMap.computeIfAbsent(new EquipmentSubtypeKey(equipmentId, subType),
            ignored -> IdUtil.escapeId(prefixId + equipmentId + "_" + subType));
    }

    @Override
    public String getOrCreateSvgId(String containerId, String id1, String id2) {
        // For global unicity in all type of container (voltage level, substation, zone), we prefix with the container Id and
        // we rely on the fact that node ids are unique inside a voltage level. We also prepend with a custom prefix id to
        // allow multiple diagrams unicity.
        return idMap.computeIfAbsent(new WireKey(containerId, id1, id2),
            ignored -> IdUtil.escapeClassName(prefixId + "_" + containerId + "_" + id1 + "_" + id2));
    }
}
