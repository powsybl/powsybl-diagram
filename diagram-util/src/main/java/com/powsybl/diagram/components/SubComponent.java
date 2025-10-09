/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public record SubComponent(String name, String fileName, String styleClass) {

    @JsonCreator
    public SubComponent(@JsonProperty("name") String name,
                        @JsonProperty("fileName") String fileName,
                        @JsonProperty("styleClass") String styleClass) {
        this.name = Objects.requireNonNull(name);
        this.fileName = Objects.requireNonNull(fileName);
        this.styleClass = styleClass;
    }
}
