/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class MiddleTwtNode extends FictitiousNode {
    protected MiddleTwtNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph) {
        super(id, name, equipmentId, componentType, graph);
    }
}
