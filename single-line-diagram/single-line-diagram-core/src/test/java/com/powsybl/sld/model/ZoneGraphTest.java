/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Node;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class ZoneGraphTest {

    private static final String SUBSTATION_ID_1 = "Substation1";
    private static final String SUBSTATION_ID_2 = "Substation2";
    private static final String VOLTAGELEVEL_ID_1 = "VoltageLevel1";
    private static final String VOLTAGELEVEL_ID_2 = "VoltageLevel2";
    private static final String LINE_ID = "Line";

    @Test
    void test() {
        List<String> zone = Arrays.asList(SUBSTATION_ID_1, SUBSTATION_ID_2);
        ZoneGraph graph = new NetworkGraphBuilder(Networks.createNetworkWithLine()).buildZoneGraph(zone);
        assertEquals(2, graph.getSubstations().size());
        assertEquals(SUBSTATION_ID_1, graph.getSubstations().get(0).getSubstationId());
        assertEquals(SUBSTATION_ID_2, graph.getSubstations().get(1).getSubstationId());
        assertEquals(1, graph.getLineEdges().size());
        BranchEdge edge = graph.getLineEdges().get(0);
        assertEquals(LINE_ID, edge.getId());
        String lineNodeId1 = getLineNodeId(graph, SUBSTATION_ID_1, VOLTAGELEVEL_ID_1, TwoSides.ONE);
        String lineNodeId2 = getLineNodeId(graph, SUBSTATION_ID_2, VOLTAGELEVEL_ID_2, TwoSides.TWO);
        assertEquals(lineNodeId1, edge.getNode1().getId());
        assertEquals(lineNodeId2, edge.getNode2().getId());
    }

    private String getLineNodeId(ZoneGraph graph, String substationId, String voltageLevelId, TwoSides side) {
        SubstationGraph substationGraph1 = graph.getSubstationGraph(substationId);
        assertNotNull(substationGraph1);
        VoltageLevelGraph voltageLevelGraph1 = substationGraph1.getVoltageLevel(voltageLevelId);
        assertNotNull(voltageLevelGraph1);
        Node lineNode = voltageLevelGraph1.getNode(LINE_ID + "_" + side);
        assertNotNull(lineNode);
        return lineNode.getId();
    }

}
