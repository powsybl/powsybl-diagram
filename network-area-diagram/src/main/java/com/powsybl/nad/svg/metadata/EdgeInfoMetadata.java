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
    private final String infoType1;
    private final String infoType2;
    private final String direction;
    private final String label1;
    private final String label2;

    public EdgeInfoMetadata(@JsonProperty("svgId") String svgId,
                            @JsonProperty("internalInfoType") String infoType1,
                            @JsonProperty("externalInfoType") String infoType2,
                            @JsonProperty("direction") String direction,
                            @JsonProperty("internalLabel") String label1,
                            @JsonProperty("externalLabel") String label2) {
        this.svgId = svgId;
        this.infoType1 = infoType1;
        this.infoType2 = infoType2;
        this.direction = direction;
        this.label1 = label1;
        this.label2 = label2;
    }

    @JsonProperty("svgId")
    public String getSvgId() {
        return svgId;
    }

    @JsonProperty("internalInfoType")
    public String getInfoType1() {
        return infoType1;
    }

    @JsonProperty("externalInfoType")
    public String getInfoType2() {
        return infoType2;
    }

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

    @JsonProperty("internalLabel")
    public String getLabel1() {
        return label1;
    }

    @JsonProperty("externalLabel")
    public String getLabel2() {
        return label2;
    }
}
