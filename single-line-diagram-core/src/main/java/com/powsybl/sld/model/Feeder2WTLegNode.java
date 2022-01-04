/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder2WTLegNode extends FeederTwtLegNode {

    protected Feeder2WTLegNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph, Side side,
                               VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, equipmentId, componentType, graph, side, otherSideVoltageLevelInfos,
                FeederType.TWO_WINDINGS_TRANSFORMER_LEG);
    }

    public static Feeder2WTLegNode createForVoltageLevelDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side,
                                                                VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new Feeder2WTLegNode(id, name, equipmentId, TWO_WINDINGS_TRANSFORMER, graph, side, otherSideVoltageLevelInfos);
    }

    public static Feeder2WTLegNode createForSubstationDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side) {
        return new Feeder2WTLegNode(id, name, equipmentId, TWO_WINDINGS_TRANSFORMER_LEG, graph, side, graph.getVoltageLevelInfos());
    }

    public static Feeder2WTLegNode createWithPhaseShifter(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side) {
        return new Feeder2WTLegNode(id, name, equipmentId, PHASE_SHIFT_TRANSFORMER_LEG, graph, side, graph.getVoltageLevelInfos());
    }
}
