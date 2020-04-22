/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.library.ComponentTypeName;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder3wtLegNode extends FeederWithSideNode {

    private Feeder3wtLegNode(String id, String name, String equipmentId, Graph graph, Side side) {
        super(id, name, equipmentId, ComponentTypeName.LINE, false, graph, side, FeederType.THREE_WINDINGS_TRANSFORMER_LEG);
    }

    public static Feeder3wtLegNode create(String id, String name, String equipmentId, Graph graph, Side side) {
        return new Feeder3wtLegNode(id, name, equipmentId, graph, side);
    }
}
