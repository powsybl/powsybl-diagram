/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeInfoMetadata {
    private final String svgId;
    private final String infoTypeA;
    private final String infoTypeB;
    private final String direction;
    private final String directionA;
    private final String directionB;
    private final String labelA;
    private final String labelB;

    @JsonCreator
    public EdgeInfoMetadata(@JsonProperty("svgId") String svgId,
                            @JsonProperty("infoTypeA") String infoTypeA,
                            @JsonProperty("infoTypeB") String infoTypeB,
                            @JsonProperty("direction") String direction,
                            @JsonProperty("directionA") String directionA,
                            @JsonProperty("directionB") String directionB,
                            @JsonProperty("labelA") String labelA,
                            @JsonProperty("labelB") String labelB) {
        this.svgId = svgId;
        this.infoTypeA = infoTypeA;
        this.infoTypeB = infoTypeB;
        if (directionA != null && directionB != null) {
            // Double arrows case
            this.direction = null;
            this.directionA = directionA;
            this.directionB = directionB;
        } else {
            // Single arrow case
            this.direction = direction;
            this.directionA = null;
            this.directionB = null;
        }
        this.labelA = labelA;
        this.labelB = labelB;
    }

    @JsonProperty("svgId")
    public String getSvgId() {
        return svgId;
    }

    @JsonProperty("infoTypeA")
    public String getInfoTypeA() {
        return infoTypeA;
    }

    @JsonProperty("infoTypeB")
    public String getInfoTypeB() {
        return infoTypeB;
    }

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

    @JsonProperty("directionA")
    public String getDirectionA() {
        return directionA;
    }

    @JsonProperty("directionB")
    public String getDirectionB() {
        return directionB;
    }

    @JsonProperty("labelA")
    public String getLabelA() {
        return labelA;
    }

    @JsonProperty("labelB")
    public String getLabelB() {
        return labelB;
    }
}
