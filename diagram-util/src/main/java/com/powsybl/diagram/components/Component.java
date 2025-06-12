/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class Component {

    private final String type;

    private final ComponentSize size;

    private final String styleClass;

    private final List<SubComponent> subComponents;

    public Component(@JsonProperty("type") String type,
                     @JsonProperty("size") ComponentSize size,
                     @JsonProperty("style") String styleClass,
                     @JsonProperty("subComponents") List<SubComponent> subComponents) {
        this.type = Objects.requireNonNull(type);
        this.size = Objects.requireNonNullElse(size, new ComponentSize(0, 0));
        this.styleClass = styleClass;
        this.subComponents = Collections.unmodifiableList(Objects.requireNonNullElse(subComponents, Collections.emptyList()));
    }

    public String getType() {
        return type;
    }

    public ComponentSize getSize() {
        return size;
    }

    public List<SubComponent> getSubComponents() {
        return subComponents;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
