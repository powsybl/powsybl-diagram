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

    private final double positionX;
    private final double positionY;

    public NodeMetadata(@JsonProperty("svgId") String svgId,
                        @JsonProperty("equipmentId") String equipmentId,
                        @JsonProperty("x") double positionX,
                        @JsonProperty("y") double positionY) {
        super(svgId, equipmentId);
        this.positionX = positionX;
        this.positionY = positionY;
    }

    @JsonProperty("x")
    public double getPositionX() {
        return positionX;
    }

    @JsonProperty("y")
    public double getPositionY() {
        return positionY;
    }
}
