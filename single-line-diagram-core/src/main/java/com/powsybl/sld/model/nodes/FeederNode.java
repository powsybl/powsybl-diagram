/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Orientation;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederNode extends Node {

    private final FeederType feederType;

    public FeederNode(String id, String name, String equipmentId, String componentType, boolean fictitious,
                         FeederType feederType, Orientation orientation) {
        super(NodeType.FEEDER, id, name, equipmentId, componentType, fictitious);
        this.feederType = Objects.requireNonNull(feederType);
        setOrientation(orientation);
    }

    protected FeederNode(String id, String name, String equipmentId, String componentType,
                         FeederType feederType) {
        this(id, name, equipmentId, componentType, false, feederType, null);
    }

    static FeederNode createFictitious(String id, Orientation orientation) {
        return new FeederNode(id, id, id, NODE, true, FeederType.FICTITIOUS, orientation);
    }

    public FeederType getFeederType() {
        return feederType;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        generator.writeStringField("feederType", feederType.name());
        Optional<Integer> order = getOrder();
        if (order.isPresent()) {
            generator.writeNumberField("order", order.get());
        }
        if (includeCoordinates) {
            generator.writeStringField("direction", getDirection().name());
        }
    }
}
