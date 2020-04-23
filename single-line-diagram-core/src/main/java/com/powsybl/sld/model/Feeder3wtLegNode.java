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

    private Feeder3wtLegNode(String id, String name, String equipmentId, Graph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, equipmentId, ComponentTypeName.LINE, false, graph, side, otherSideVoltageLevelInfos,
                FeederType.THREE_WINDINGS_TRANSFORMER_LEG);
    }

    public static Feeder3wtLegNode createForVoltageLevelDiagram(Graph graph, String id, String name, String equipmentId, Side side,
                                          VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new Feeder3wtLegNode(id, name, equipmentId, graph, side, otherSideVoltageLevelInfos);
    }

    public static Feeder3wtLegNode createForSubstationDiagram(Graph graph, String id, String name, String equipmentId, Side side) {
        return new Feeder3wtLegNode(id, name, equipmentId, graph, side, null);
    }
}
