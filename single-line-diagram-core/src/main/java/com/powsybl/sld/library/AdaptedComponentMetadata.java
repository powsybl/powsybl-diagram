/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class AdaptedComponentMetadata {

    private String type;

    private String id;

    private List<AnchorPoint> anchorPoints;

    private ComponentSize size;

    private String styleClass;

    private boolean allowRotation;

    private List<SubComponent> subComponents;

    /**
     * Constructor
     */
    @JsonCreator
    public AdaptedComponentMetadata(@JsonProperty("type") String type,
                                    @JsonProperty("id") String id,
                                    @JsonProperty("anchorPoint") List<AnchorPoint> anchorPoints,
                                    @JsonProperty("size") ComponentSize size,
                                    @JsonProperty("styleClass") String styleClass,
                                    @JsonProperty("allowRotation") boolean allowRotation,
                                    @JsonProperty("subComponent") List<SubComponent> subComponents) {
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(id);
        this.anchorPoints = Objects.requireNonNull(anchorPoints);
        this.size = Objects.requireNonNull(size);
        this.styleClass = Objects.requireNonNull(styleClass);
        this.allowRotation = allowRotation;
        this.subComponents = Objects.requireNonNull(subComponents);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ComponentSize getSize() {
        return size;
    }

    public void setSize(ComponentSize size) {
        this.size = size;
    }

    @JsonProperty("anchorPoint")
    public List<AnchorPoint> getAnchorPoints() {
        return anchorPoints;
    }

    public void setAnchorPoints(List<AnchorPoint> anchorPoints) {
        this.anchorPoints = anchorPoints;
    }

    public boolean isAllowRotation() {
        return allowRotation;
    }

    public void setAllowRotation(boolean allowRotation) {
        this.allowRotation = allowRotation;
    }

    @JsonProperty("subComponent")
    public List<SubComponent> getSubComponents() {
        return subComponents;
    }

    public void setSubComponents(List<SubComponent> subComponents) {
        this.subComponents = subComponents;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}
