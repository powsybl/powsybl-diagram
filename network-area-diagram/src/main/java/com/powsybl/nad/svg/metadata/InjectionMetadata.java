/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InjectionMetadata extends AbstractMetadataItem {

    private final String componentType;
    private final String busNodeId;
    private final String vlNodeId;

    public InjectionMetadata(@JsonProperty("svgId") String svgId,
                             @JsonProperty("equipmentId") String equipmentId,
                             @JsonProperty("componentType") String componentType,
                             @JsonProperty("busNodeId") String busNodeId,
                             @JsonProperty("vlNodeId") String vlNodeId) {
        super(svgId, equipmentId);
        this.componentType = componentType;
        this.busNodeId = busNodeId;
        this.vlNodeId = vlNodeId;
    }

    @JsonProperty("componentType")
    public String getComponentType() {
        return componentType;
    }

    @JsonProperty("busNodeId")
    public String getBusNodeId() {
        return busNodeId;
    }

    @JsonProperty("vlNodeId")
    public String getVlNodeId() {
        return vlNodeId;
    }
}
