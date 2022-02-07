/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
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

    protected FeederNode(String id, String name, String equipmentId, String componentType, boolean fictitious, VoltageLevelGraph graph,
                         FeederType feederType, Orientation orientation) {
        super(NodeType.FEEDER, id, name, equipmentId, componentType, fictitious, graph);
        this.feederType = Objects.requireNonNull(feederType);
        setOrientation(orientation);
    }

    protected FeederNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph,
                         FeederType feederType) {
        this(id, name, equipmentId, componentType, false, graph, feederType, null);
    }

    static FeederNode createFictitious(VoltageLevelGraph graph, String id, Orientation orientation) {
        return new FeederNode(id, id, id, NODE, true, graph, FeederType.FICTITIOUS, orientation);
    }

    public FeederType getFeederType() {
        return feederType;
    }

    @Override
    public void setCell(Cell cell) {
        if (!(cell instanceof ExternCell)) {
            throw new PowsyblException("The Cell of a feeder node shall be an ExternCell");
        }
        super.setCell(cell);
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
