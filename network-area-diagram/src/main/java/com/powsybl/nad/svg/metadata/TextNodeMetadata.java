/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.nad.layout.TextPosition;
import com.powsybl.nad.model.Point;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextNodeMetadata extends AbstractMetadataItem {

    private final String vlNodeId;
    private final double shiftX;
    private final double shiftY;
    private final double connectionShiftX;
    private final double connectionShiftY;

    public TextNodeMetadata(@JsonProperty("svgId") String svgId,
                            @JsonProperty("equipmentId") String equipmentId,
                            @JsonProperty("vlNode") String vlNodeId,
                            @JsonProperty("shiftX") double shiftX,
                            @JsonProperty("shiftY") double shiftY,
                            @JsonProperty("connectionShiftX") double connectionShiftX,
                            @JsonProperty("connectionShiftY") double connectionShiftY) {
        super(svgId, equipmentId);
        this.vlNodeId = vlNodeId;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
        this.connectionShiftX = connectionShiftX;
        this.connectionShiftY = connectionShiftY;
    }

    @JsonProperty("vlNode")
    public String getVlNodeId() {
        return vlNodeId;
    }

    @JsonProperty("shiftX")
    public double getShiftX() {
        return shiftX;
    }

    @JsonProperty("shiftY")
    public double getShiftY() {
        return shiftY;
    }

    @JsonProperty("connectionShiftX")
    public double getConnectionShiftX() {
        return connectionShiftX;
    }

    @JsonProperty("connectionShiftY")
    public double getConnectionShiftY() {
        return connectionShiftY;
    }

    @JsonIgnore
    public TextPosition getTextPosition() {
        return new TextPosition(new Point(shiftX, shiftY), new Point(connectionShiftX, connectionShiftY));
    }
}
