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
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LineEdge extends AbstractBranchEdge {

    private final String lineId;
    private List<Point> points = new ArrayList<>();

    public LineEdge(String lineId, Node node1, Node node2) {
        super(node1, node2);
        this.lineId = Objects.requireNonNull(lineId);
    }

    public String getLineId() {
        return lineId;
    }

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    public void setPoints(List<Point> points) {
        this.points = Objects.requireNonNull(points);
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public List<Point> getSnakeLine() {
        return getPoints();
    }

    @Override
    public void setSnakeLine(List<Point> snakeLine) {
        points = Objects.requireNonNull(snakeLine);
    }

    @Override
    void writeJson(JsonGenerator generator) throws IOException {
        writeJson(generator, false);
    }

    void writeJson(JsonGenerator generator, boolean generateCoordsInJson) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("lineId", lineId);
        generator.writeArrayFieldStart("nodes");
        super.writeJson(generator);
        generator.writeEndArray();
        if (generateCoordsInJson) {
            generator.writeArrayFieldStart("points");
            for (Point point : points) {
                point.writeJson(generator);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }
}
