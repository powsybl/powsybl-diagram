/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractBranchEdge extends Edge implements BranchEdge {

    protected List<Point> snakeLine = new ArrayList<>();

    protected AbstractBranchEdge(Node node1, Node node2) {
        super(node1, node2);
    }

    @Override
    public List<Point> getSnakeLine() {
        return snakeLine;
    }

    @Override
    public void setSnakeLine(List<Point> snakeLine) {
        this.snakeLine = Objects.requireNonNull(snakeLine);
    }

    @Override
    void writeJson(JsonGenerator generator) throws IOException {
        writeJson(generator, false);
    }

    void writeJson(JsonGenerator generator, boolean generateCoordsInJson) throws IOException {
        generator.writeStartObject();
        writeIdJson(generator);
        generator.writeArrayFieldStart("nodes");
        super.writeJson(generator);
        generator.writeEndArray();
        if (generateCoordsInJson) {
            generator.writeArrayFieldStart("snakeLine");
            for (Point point : getSnakeLine()) {
                generator.writeNumber(point.getX());
                generator.writeNumber(point.getY());
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }

    protected abstract void writeIdJson(JsonGenerator generator) throws IOException;

}
