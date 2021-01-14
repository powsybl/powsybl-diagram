/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractBranchEdge extends Edge {

    protected AbstractBranchEdge(Node node1, Node node2) {
        super(node1, node2);
    }

    public abstract List<Double> getSnakeLine();

    public abstract void setSnakeLine(List<Double> snakeLine);
}
