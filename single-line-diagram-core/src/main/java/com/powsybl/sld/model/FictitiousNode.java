/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 */
public class FictitiousNode extends Node {

    protected FictitiousNode(VoltageLevelGraph graph, String id, String componentType) {
        super(NodeType.FICTITIOUS, id, id, id, componentType, true, graph);
    }

    public int getCardinality() {
        return this.getAdjacentNodes().size() - (getType() == NodeType.SHUNT ? 1 : 0);
    }
}
