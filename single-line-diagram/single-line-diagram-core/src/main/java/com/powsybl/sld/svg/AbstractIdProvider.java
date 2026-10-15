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
 * Common implementation of the typed-key cache used by SVG identifier providers.
 *
 * <p>Concrete providers only define how each kind of identifier is formatted;
 * this class guarantees that equipment, equipment subtype and wire identifiers
 * use distinct key types and remain stable across repeated calls.</p>
 */
abstract class AbstractIdProvider implements IdProvider {

    private interface IdKey {
    }

    private record EquipmentKey(String equipmentId) implements IdKey {
    }

    private record EquipmentSubtypeKey(String equipmentId, String subtype) implements IdKey {
    }

    private record WireKey(String containerId, String id1, String id2) implements IdKey {
    }

    protected final String prefixId;
    private final Map<IdKey, String> idMap = new HashMap<>();

    AbstractIdProvider(String prefixId) {
        this.prefixId = prefixId;
    }

    @Override
    public final String getOrCreateSvgId(String equipmentId) {
        return idMap.computeIfAbsent(new EquipmentKey(equipmentId), ignored -> createEquipmentId(equipmentId));
    }

    @Override
    public final String getOrCreateSvgId(String equipmentId, String subType) {
        return idMap.computeIfAbsent(new EquipmentSubtypeKey(equipmentId, subType),
            ignored -> createEquipmentSubtypeId(equipmentId, subType));
    }

    @Override
    public final String getOrCreateSvgId(String containerId, String id1, String id2) {
        return idMap.computeIfAbsent(new WireKey(containerId, id1, id2),
            ignored -> createWireId(containerId, id1, id2));
    }

    protected abstract String createEquipmentId(String equipmentId);

    protected abstract String createEquipmentSubtypeId(String equipmentId, String subType);

    protected abstract String createWireId(String containerId, String id1, String id2);
}
