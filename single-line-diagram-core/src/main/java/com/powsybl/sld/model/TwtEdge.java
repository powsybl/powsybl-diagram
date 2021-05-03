/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TwtEdge extends AbstractBranchEdge {

    public TwtEdge(Node node1, Node node2) {
        super(node1, node2);
    }

    @Override
    protected void writeIdJson(JsonGenerator generator) {
    }

}
