/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.PHASE_SHIFT_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Middle2WTNode extends MiddleTwtNode {

    public Middle2WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, VoltageLevelGraph graph, String componentType) {
        super(id, name,
            new VoltageLevelInfos[]{Objects.requireNonNull(voltageLevelInfosLeg1), Objects.requireNonNull(voltageLevelInfosLeg2)},
            componentType, graph);
    }

    public static Middle2WTNode create(String id, String name, BaseGraph graph, Feeder2WTLegNode legNode1, Feeder2WTLegNode legNode2,
                                       VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, boolean hasPhaseTapChanger) {
        String componentType = hasPhaseTapChanger ? PHASE_SHIFT_TRANSFORMER : TWO_WINDINGS_TRANSFORMER;
        Middle2WTNode middleNode = new Middle2WTNode(id, name, vlInfos1, vlInfos2, null, componentType);
        graph.addTwtEdge(legNode1, middleNode);
        graph.addTwtEdge(legNode2, middleNode);
        return middleNode;
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg1() {
        return getVoltageLevelInfos(FeederWithSideNode.Side.ONE);
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg2() {
        return getVoltageLevelInfos(FeederWithSideNode.Side.TWO);
    }
}
