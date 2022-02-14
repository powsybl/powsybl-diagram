/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Middle3WTNode extends MiddleTwtNode {
    private boolean embeddedInVlGraph;

    public Middle3WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, VoltageLevelInfos voltageLevelInfosLeg3, boolean embeddedInVLGraph) {
        super(id, name,
            new VoltageLevelInfos[]{Objects.requireNonNull(voltageLevelInfosLeg1), Objects.requireNonNull(voltageLevelInfosLeg2), Objects.requireNonNull(voltageLevelInfosLeg3)}, THREE_WINDINGS_TRANSFORMER);
        this.embeddedInVlGraph = embeddedInVLGraph;
    }

    public static Middle3WTNode create(String id, String name, BaseGraph ssGraph,
                                       Feeder3WTLegNode legNode1, Feeder3WTLegNode legNode2, Feeder3WTLegNode legNode3,
                                       VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, VoltageLevelInfos vlInfos3, boolean embeddedInVLGraph) {
        Middle3WTNode middleNode = new Middle3WTNode(id, name, vlInfos1, vlInfos2, vlInfos3, embeddedInVLGraph);
        ssGraph.addTwtEdge(legNode1, middleNode);
        ssGraph.addTwtEdge(legNode2, middleNode);
        ssGraph.addTwtEdge(legNode3, middleNode);
        return middleNode;
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg1() {
        return getVoltageLevelInfos(FeederWithSideNode.Side.ONE);
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg2() {
        return getVoltageLevelInfos(FeederWithSideNode.Side.TWO);
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg3() {
        return getVoltageLevelInfos(FeederWithSideNode.Side.THREE);
    }

    public boolean isEmbeddedInVlGraph() {
        return embeddedInVlGraph;
    }
}
