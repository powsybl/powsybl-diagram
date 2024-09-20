/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextNodeMetadata extends AbstractMetadataItem {

    private final String vlNodeId;
    private final double positionShiftX;
    private final double positionShiftY;
    private final double connectionShiftX;
    private final double connectionShiftY;

    public TextNodeMetadata(@JsonProperty("svgId") String svgId,
                            @JsonProperty("equipmentId") String equipmentId,
                            @JsonProperty("vlNode") String vlNodeId,
                            @JsonProperty("shiftX") double positionShiftX,
                            @JsonProperty("shiftY") double positionShiftY,
                            @JsonProperty("connectionShiftX") double connectionShiftX,
                            @JsonProperty("connectionShiftY") double connectionShiftY) {
        super(svgId, equipmentId);
        this.vlNodeId = vlNodeId;
        this.positionShiftX = positionShiftX;
        this.positionShiftY = positionShiftY;
        this.connectionShiftX = connectionShiftX;
        this.connectionShiftY = connectionShiftY;
    }

    @JsonProperty("vlNode")
    public String getVlNodeId() {
        return vlNodeId;
    }

    @JsonProperty("shiftX")
    public double getPositionShiftX() {
        return positionShiftX;
    }

    @JsonProperty("shiftY")
    public double getPositionShiftY() {
        return positionShiftY;
    }

    @JsonProperty("connectionShiftX")
    public double getConnectionShiftX() {
        return connectionShiftX;
    }

    @JsonProperty("connectionShiftY")
    public double getConnectionShiftY() {
        return connectionShiftY;
    }
}
