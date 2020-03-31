/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder3WTNode extends FeederNode {

    private final Side side;

    public Feeder3WTNode(String id, String name, String equipmentId, String componentType,
                         boolean fictitious, Graph graph, Side side) {
        super(id, name, equipmentId, componentType, fictitious, graph);
        this.side = Objects.requireNonNull(side);
    }

    public Side getSide() {
        return side;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        generator.writeStringField("transformerSide", side.name());
    }
}
