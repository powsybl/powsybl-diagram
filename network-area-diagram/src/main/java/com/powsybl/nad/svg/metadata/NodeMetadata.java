/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.nad.model.Point;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeMetadata extends AbstractMetadataItem {

    private final double x;
    private final double y;

    public NodeMetadata(@JsonProperty("svgId") String svgId,
                        @JsonProperty("equipmentId") String equipmentId,
                        @JsonProperty("x") double x,
                        @JsonProperty("y") double y) {
        super(svgId, equipmentId);
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @JsonIgnore
    public Point getPosition() {
        return new Point(x, y);
    }
}
