/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeMetadata extends AbstractMetadataItem {

    private final double x;
    private final double y;
    private final boolean fictitious;

    public NodeMetadata(@JsonProperty("svgId") String svgId,
                        @JsonProperty("equipmentId") String equipmentId,
                        @JsonProperty("x") double x,
                        @JsonProperty("y") double y,
                        @JsonProperty("fictitious") boolean fictitious) {
        super(svgId, equipmentId);
        this.x = x;
        this.y = y;
        this.fictitious = fictitious;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isFictitious() {
        return fictitious;
    }
}
