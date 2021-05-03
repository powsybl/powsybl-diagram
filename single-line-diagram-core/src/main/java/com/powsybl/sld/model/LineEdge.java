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
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LineEdge extends AbstractBranchEdge {

    private final String lineId;

    public LineEdge(String lineId, Node node1, Node node2) {
        super(node1, node2);
        this.lineId = Objects.requireNonNull(lineId);
    }

    public String getLineId() {
        return lineId;
    }

    public void addPoint(double x, double y) {
        snakeLine.add(new Point(x, y));
    }

    @Override
    public void writeIdJson(JsonGenerator generator) throws IOException {
        generator.writeStringField("lineId", lineId);
    }
}
