/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Middle3WTNode extends MiddleTwtNode {

    private final VoltageLevelInfos voltageLevelInfosLeg1;

    private final VoltageLevelInfos voltageLevelInfosLeg2;

    private final VoltageLevelInfos voltageLevelInfosLeg3;

    public Middle3WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, VoltageLevelInfos voltageLevelInfosLeg3, VoltageLevelGraph graph) {
        super(id, name, id, THREE_WINDINGS_TRANSFORMER, graph);
        this.voltageLevelInfosLeg1 = Objects.requireNonNull(voltageLevelInfosLeg1);
        this.voltageLevelInfosLeg2 = Objects.requireNonNull(voltageLevelInfosLeg2);
        this.voltageLevelInfosLeg3 = Objects.requireNonNull(voltageLevelInfosLeg3);
    }

    public static Middle3WTNode create(String id, String name, BaseGraph ssGraph,
                                       Feeder3WTLegNode legNode1, Feeder3WTLegNode legNode2, Feeder3WTLegNode legNode3,
                                       VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, VoltageLevelInfos vlInfos3) {
        Middle3WTNode middleNode = new Middle3WTNode(id, name, vlInfos1, vlInfos2, vlInfos3, null);
        ssGraph.addTwtEdge(legNode1, middleNode);
        ssGraph.addTwtEdge(legNode2, middleNode);
        ssGraph.addTwtEdge(legNode3, middleNode);
        return middleNode;
    }

    @Override
    public VoltageLevelInfos getVoltageLevelInfos() {
        return null; // there is not a unique voltage level infos for a middle point so we consider this is undefined
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg1() {
        return voltageLevelInfosLeg1;
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg2() {
        return voltageLevelInfosLeg2;
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg3() {
        return voltageLevelInfosLeg3;
    }

    public VoltageLevelInfos getVoltageLevelInfos(FeederWithSideNode.Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return voltageLevelInfosLeg1;
            case TWO:
                return voltageLevelInfosLeg2;
            case THREE:
                return voltageLevelInfosLeg3;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg1");
        voltageLevelInfosLeg1.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg2");
        voltageLevelInfosLeg2.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg3");
        voltageLevelInfosLeg3.writeJsonContent(generator);
    }
}
