/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import java.util.Objects;

import com.powsybl.sld.model.graphs.VoltageLevelInfos;

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
