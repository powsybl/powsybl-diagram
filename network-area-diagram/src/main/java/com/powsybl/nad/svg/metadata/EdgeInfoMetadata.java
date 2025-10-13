/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.nad.svg.EdgeInfo;

import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class EdgeInfoMetadata {
    private final String svgId;
    private final String infoType;
    private final String direction;
    private final String internalLabel;
    private final String externalLabel;

    public EdgeInfoMetadata(@JsonProperty("svgId") String svgId,
                            @JsonProperty("infoType") String infoType,
                            @JsonProperty("direction") String direction,
                            @JsonProperty("internalLabel") String internalLabel,
                            @JsonProperty("externalLabel") String externalLabel) {
        this.svgId = svgId;
        this.infoType = infoType;
        this.direction = direction;
        this.internalLabel = internalLabel;
        this.externalLabel = externalLabel;
    }

    @JsonProperty("svgId")
    public String getSvgId() {
        return svgId;
    }

    @JsonProperty("infoType")
    public String getInfoType() {
        return infoType;
    }

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

    @JsonProperty("internalLabel")
    public String getInternalLabel() {
        return internalLabel;
    }

    @JsonProperty("externalLabel")
    public String getExternalLabel() {
        return externalLabel;
    }
}
