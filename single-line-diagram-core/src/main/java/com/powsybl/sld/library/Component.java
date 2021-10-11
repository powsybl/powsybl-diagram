/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Thomas Adam <tadam at silicom>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Component {

    private final String type;

    private final String id;

    private final List<AnchorPoint> anchorPoints;

    private final ComponentSize size;

    private final String styleClass;

    private final boolean allowRotation;

    private final List<SubComponent> subComponents;

    @JsonCreator
    public Component(@JsonProperty("type") String type,
                     @JsonProperty("id") String id,
                     @JsonProperty("anchorPoints") List<AnchorPoint> anchorPoints,
                     @JsonProperty("size") ComponentSize size,
                     @JsonProperty("style") String styleClass,
                     @JsonProperty("allowRotation") boolean allowRotation,
                     @JsonProperty("subComponents") List<SubComponent> subComponents) {
        this.type = Objects.requireNonNull(type);
        this.id = id;
        this.anchorPoints = Collections.unmodifiableList(Objects.requireNonNullElse(anchorPoints, Collections.emptyList()));
        this.size = Objects.requireNonNull(size);
        this.styleClass = styleClass;
        this.allowRotation = allowRotation;
        if (subComponents != null) {
            this.subComponents = Collections.unmodifiableList(subComponents);
        } else {
            this.subComponents = null;
        }
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public ComponentSize getSize() {
        return size;
    }

    public List<AnchorPoint> getAnchorPoints() {
        return anchorPoints;
    }

    public boolean isAllowRotation() {
        return allowRotation;
    }

    public List<SubComponent> getSubComponents() {
        return subComponents;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
