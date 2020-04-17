/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TwtEdge extends Edge {

    private String componentType;

    private List<Double> snakeLine = new ArrayList<>();

    public TwtEdge(String componentType, Node... nodes) {
        super(nodes);
        this.componentType = componentType;
    }

    public List<Double> getSnakeLine() {
        return snakeLine;
    }

    public void setSnakeLine(List<Double> snakeLine) {
        this.snakeLine = Objects.requireNonNull(snakeLine);
    }

    public String getComponentType() {
        return componentType;
    }

    @Override
    void writeJson(JsonGenerator generator) throws IOException {
        writeJson(generator, false);
    }

    void writeJson(JsonGenerator generator, boolean generateCoordsInJson) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("componentType", componentType);
        generator.writeArrayFieldStart("nodes");
        super.writeJson(generator);
        generator.writeEndArray();
        if (generateCoordsInJson) {
            generator.writeArrayFieldStart("snakeLine");
            for (Double point : snakeLine) {
                generator.writeNumber(point);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }
}
