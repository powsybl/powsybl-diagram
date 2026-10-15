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
 */
public interface IdProvider {

    /**
     * Returns the unescaped identifier corresponding to an equipment subtype.
     * This is used when metadata must preserve a human-readable identifier
     * while the SVG document uses an escaped identifier.
     *
     * @param equipmentId equipment identifier
     * @param subType subtype or label position
     * @return the unescaped identifier
     */
    default String getUnescapedSvgId(String equipmentId, String subType) {
        return getOrCreateSvgId(equipmentId, subType);
    }

    String getOrCreateSvgId(String equipmentId);

    String getOrCreateSvgId(String equipmentId, String subType);

    String getOrCreateSvgId(String containerId, String id1, String id2);
}
