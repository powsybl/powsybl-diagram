/*
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
public class EdgeInfoMetadata {
    private final String svgId;
    private final String infoTypeA;
    private final String infoTypeB;
    private final String direction;
    private final String labelA;
    private final String labelB;

    public EdgeInfoMetadata(@JsonProperty("svgId") String svgId,
                            @JsonProperty("infoTypeA") String infoTypeA,
                            @JsonProperty("infoTypeB") String infoTypeB,
                            @JsonProperty("direction") String direction,
                            @JsonProperty("labelA") String labelA,
                            @JsonProperty("labelB") String labelB) {
        this.svgId = svgId;
        this.infoTypeA = infoTypeA;
        this.infoTypeB = infoTypeB;
        this.direction = direction;
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

    @JsonProperty("labelA")
    public String getLabelA() {
        return labelA;
    }

    @JsonProperty("labelB")
    public String getLabelB() {
        return labelB;
    }
}
