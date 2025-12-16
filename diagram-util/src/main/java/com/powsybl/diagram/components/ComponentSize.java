/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public record ComponentSize(double width, double height) {

    @JsonCreator
    public ComponentSize(@JsonProperty("width") double width,
                         @JsonProperty("height") double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    @NonNull
    public String toString() {
        return "ComponentSize(width=" + width + ", height=" + height + ")";
    }
}
