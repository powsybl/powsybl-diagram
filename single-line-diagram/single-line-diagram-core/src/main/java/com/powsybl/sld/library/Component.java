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
import com.powsybl.sld.model.coordinate.Orientation;

import java.util.*;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Thomas Adam {@literal <tadam at silicom>}
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Component {

    public enum Transformation {
        ROTATION,
        FLIP,
        NONE
    }

    private final String type;

    private final List<AnchorPoint> anchorPoints;

    private final ComponentSize size;

    private final String styleClass;

    private final Map<Orientation, Transformation> transformations;

    private final List<SubComponent> subComponents;

    @JsonCreator
    public Component(@JsonProperty("type") String type,
                     @JsonProperty("anchorPoints") List<AnchorPoint> anchorPoints,
                     @JsonProperty("size") ComponentSize size,
                     @JsonProperty("style") String styleClass,
                     @JsonProperty("transformations") Map<Orientation, Transformation> transformations,
                     @JsonProperty("subComponents") List<SubComponent> subComponents) {
        this.type = Objects.requireNonNull(type);
        this.anchorPoints = Collections.unmodifiableList(Objects.requireNonNullElse(anchorPoints, Collections.emptyList()));
        this.size = Objects.requireNonNullElse(size, new ComponentSize(0, 0));
        this.styleClass = styleClass;
        this.transformations = Objects.requireNonNullElse(transformations, Collections.emptyMap());
        this.subComponents = Collections.unmodifiableList(Objects.requireNonNullElse(subComponents, Collections.emptyList()));
    }

    public String getType() {
        return type;
    }

    public ComponentSize getSize() {
        return size;
    }

    public List<AnchorPoint> getAnchorPoints() {
        return anchorPoints;
    }

    public Map<Orientation, Transformation> getTransformations() {
        return transformations;
    }

    public List<SubComponent> getSubComponents() {
        return subComponents;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
