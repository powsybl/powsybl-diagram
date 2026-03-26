/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.graphs.VoltageLevelInfos;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class Middle3WTNode extends MiddleTwtNode {
    private final Map<Winding, NodeSide> windingMap = new EnumMap<>(Winding.class);
    private final boolean embeddedInVlGraph;

    public Middle3WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, VoltageLevelInfos voltageLevelInfosLeg3, String componentType, boolean embeddedInVLGraph) {
        super(id, name,
            new VoltageLevelInfos[]{Objects.requireNonNull(voltageLevelInfosLeg1), Objects.requireNonNull(voltageLevelInfosLeg2), Objects.requireNonNull(voltageLevelInfosLeg3)},
            componentType);
        this.embeddedInVlGraph = embeddedInVLGraph;
    }

    public boolean isEmbeddedInVlGraph() {
        return embeddedInVlGraph;
    }

    public void setWindingOrder(Winding first, Winding second, Winding third) {
        setWindingOrder(first, NodeSide.ONE);
        setWindingOrder(second, NodeSide.TWO);
        setWindingOrder(third, NodeSide.THREE);
    }

    public void setWindingOrder(Winding winding, NodeSide legSide) {
        windingMap.put(winding, legSide);
    }

    public Node getAdjacentNode(Winding winding) {
        Edge edge = getAdjacentEdges().get(windingToLegIndex(winding));
        return edge.getOppositeNode(this);
    }

    public VoltageLevelInfos getVoltageLevelInfos(Winding winding) {
        return voltageLevelInfosLeg[windingToLegIndex(winding)];
    }

    private int windingToLegIndex(Winding winding) {
        return windingMap.getOrDefault(winding, NodeSide.ONE).getIntValue() - 1;
    }

    public enum Winding {
        UPPER_LEFT, UPPER_RIGHT, DOWN
    }
}
