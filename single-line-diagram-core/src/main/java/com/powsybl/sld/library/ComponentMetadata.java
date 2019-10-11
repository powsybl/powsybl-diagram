/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@XmlJavaTypeAdapter(ComponentMetadataAdapter.class)
public class ComponentMetadata {

    private final String type;

    private final String id;

    private final List<AnchorPoint> anchorPoints;

    private final ComponentSize size;

    @JsonCreator
    public ComponentMetadata(@JsonProperty("type") String type,
                             @JsonProperty("id") String id,
                             @JsonProperty("anchorPoints") List<AnchorPoint> anchorPoints,
                             @JsonProperty("size") ComponentSize size) {
        this.type = Objects.requireNonNull(type);
        this.id = id;
        this.anchorPoints = Collections.unmodifiableList(Objects.requireNonNull(anchorPoints));
        this.size = Objects.requireNonNull(size);
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
}
