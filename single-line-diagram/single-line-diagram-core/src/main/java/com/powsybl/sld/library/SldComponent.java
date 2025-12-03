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
import com.powsybl.diagram.components.Component;
import com.powsybl.diagram.components.ComponentSize;
import com.powsybl.diagram.components.SubComponent;
import com.powsybl.sld.model.coordinate.Orientation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Thomas Adam {@literal <tadam at silicom>}
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SldComponent extends Component {

    public enum Transformation {
        ROTATION,
        FLIP,
        FLIP_AND_ROTATION,
        NONE
    }

    private final List<AnchorPoint> anchorPoints;

    private final Map<Orientation, Transformation> transformations;

    @JsonCreator
    public SldComponent(@JsonProperty("type") String type,
                        @JsonProperty("anchorPoints") List<AnchorPoint> anchorPoints,
                        @JsonProperty("size") ComponentSize size,
                        @JsonProperty("style") String styleClass,
                        @JsonProperty("transformations") Map<Orientation, Transformation> transformations,
                        @JsonProperty("subComponents") List<SubComponent> subComponents) {
        super(type, size, styleClass, subComponents);
        this.anchorPoints = Collections.unmodifiableList(Objects.requireNonNullElse(anchorPoints, Collections.emptyList()));
        this.transformations = Objects.requireNonNullElse(transformations, Collections.emptyMap());
    }

    public List<AnchorPoint> getAnchorPoints() {
        return anchorPoints;
    }

    public Map<Orientation, Transformation> getTransformations() {
        return transformations;
    }
}
