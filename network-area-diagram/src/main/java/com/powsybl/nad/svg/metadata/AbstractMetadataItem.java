/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractMetadataItem {
    private final String svgId;
    private final String equipmentId;

    protected AbstractMetadataItem(@JsonProperty("svgId") String svgId,
                                   @JsonProperty("equipmentId") String equipmentId) {
        this.svgId = svgId;
        this.equipmentId = equipmentId;
    }

    @JsonProperty("svgId")
    public String getSvgId() {
        return svgId;
    }

    @JsonProperty("equipmentId")
    public String getEquipmentId() {
        return equipmentId;
    }
}
