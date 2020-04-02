/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class WindingEdge extends Edge {

    private List<Double> snakeLine = new ArrayList<>();

    public WindingEdge(Node node1, Node node2) {
        super(node1, node2);
    }

    public List<Double> getSnakeLine() {
        return snakeLine;
    }

    public void setSnakeLine(List<Double> snakeLine) {
        this.snakeLine = Objects.requireNonNull(snakeLine);
    }
}
