/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.util.IdUtil;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class LegacyIdProvider extends AbstractIdProvider {

    public LegacyIdProvider(String prefixId) {
        super(prefixId);
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
    protected String createEquipmentId(String equipmentId) {
        return IdUtil.escapeId(prefixId + equipmentId);
    }

    @Override
    protected String createEquipmentSubtypeId(String equipmentId, String subType) {
        return IdUtil.escapeId(prefixId + equipmentId + "_" + subType);
    }

    @Override
    protected String createWireId(String containerId, String id1, String id2) {
        // For global unicity in all type of container (voltage level, substation, zone), we prefix with the container Id and
        // we rely on the fact that node ids are unique inside a voltage level. We also prepend with a custom prefix id to
        // allow multiple diagrams unicity.
        return IdUtil.escapeClassName(prefixId + "_" + containerId + "_" + id1 + "_" + id2);
    }
}
