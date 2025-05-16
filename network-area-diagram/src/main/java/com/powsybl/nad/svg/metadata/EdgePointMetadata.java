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
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgePointMetadata {

    private final double x;
    private final double y;

    EdgePointMetadata(@JsonProperty("x") double x,
                      @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    @JsonProperty("x")
    public double getX() {
        return x;
    }

    @JsonProperty("y")
    public double getY() {
        return y;
    }
}
