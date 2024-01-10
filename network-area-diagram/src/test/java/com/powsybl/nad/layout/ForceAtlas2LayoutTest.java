/**
 Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.VoltageLevelNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class ForceAtlas2LayoutTest {

    @Test
    void tenBusesTest() throws IOException {
        Graph graph = createTenBusesGraph();

        Layout forceAtlas2Layout = new BasicForceAtlas2LayoutFactory().create();
        LayoutParameters layoutParameters = new LayoutParameters();
        forceAtlas2Layout.run(graph, layoutParameters);

        assertFalse(graph.getVoltageLevelNodesStream().anyMatch(vlNode -> !validPosition(vlNode)));
    }

    private boolean validPosition(VoltageLevelNode vlNode) {
        String id = vlNode.getEquipmentId();
        double x = vlNode.getPosition().getX();
        double y = vlNode.getPosition().getY();

        if (id.equals("VL0")) {
            return equalPosition(-3666.6, 2450.4, x, y);
        } else if (id.equals("VL1")) {
            return equalPosition(-3540.9, 2599.9, x, y);
        } else if (id.equals("VL2")) {
            return equalPosition(-2811.5, 2019.1, x, y);
        } else if (id.equals("VL3")) {
            return equalPosition(-870.8, 81.8, x, y);
        } else if (id.equals("VL4")) {
            return equalPosition(841.0, -129.5, x, y);
        } else if (id.equals("VL5")) {
            return equalPosition(1538.4, 680.0, x, y);
        } else if (id.equals("VL6")) {
            return equalPosition(1551.8, 501.4, x, y);
        } else if (id.equals("VL7")) {
            return equalPosition(1389.2, -2993.5, x, y);
        } else if (id.equals("VL8")) {
            return equalPosition(1008.7, -5363.8, x, y);
        } else if (id.equals("VL9")) {
            return equalPosition(2984.5, -4696.3, x, y);
        }
        return false;
    }

    private boolean equalPosition(double expectedX, double expectedY, double x, double y) {
        double tol = 0.1;
        return Math.abs(expectedX - x) < tol && Math.abs(expectedY - y) < tol;
    }

    private static Graph createTenBusesGraph() {
        Graph graph = new Graph();

        VoltageLevelNode node0 = createBusNode("VL0");
        VoltageLevelNode node1 = createBusNode("VL1");
        VoltageLevelNode node2 = createBusNode("VL2");
        VoltageLevelNode node3 = createBusNode("VL3");
        VoltageLevelNode node4 = createBusNode("VL4");
        VoltageLevelNode node5 = createBusNode("VL5");
        VoltageLevelNode node6 = createBusNode("VL6");
        VoltageLevelNode node7 = createBusNode("VL7");
        VoltageLevelNode node8 = createBusNode("VL8");
        VoltageLevelNode node9 = createBusNode("VL9");

        graph.addNode(node0);
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);
        graph.addNode(node4);
        graph.addNode(node5);
        graph.addNode(node6);
        graph.addNode(node7);
        graph.addNode(node8);
        graph.addNode(node9);

        graph.addEdge(node0, getBusNode(node0), node1, getBusNode(node1), createBranchEdge("L0-1"));
        graph.addEdge(node0, getBusNode(node0), node2, getBusNode(node2), createBranchEdge("L0-2"));
        graph.addEdge(node1, getBusNode(node1), node2, getBusNode(node2), createBranchEdge("L1-2"));
        graph.addEdge(node2, getBusNode(node2), node3, getBusNode(node3), createBranchEdge("L2-3"));
        graph.addEdge(node2, getBusNode(node2), node4, getBusNode(node4), createBranchEdge("L2-4"));
        graph.addEdge(node3, getBusNode(node3), node4, getBusNode(node4), createBranchEdge("L3-4"));
        graph.addEdge(node4, getBusNode(node4), node5, getBusNode(node5), createBranchEdge("L4-5"));
        graph.addEdge(node4, getBusNode(node4), node6, getBusNode(node6), createBranchEdge("L4-6"));
        graph.addEdge(node5, getBusNode(node5), node6, getBusNode(node6), createBranchEdge("L5-6"));
        graph.addEdge(node4, getBusNode(node4), node7, getBusNode(node7), createBranchEdge("L4-7"));
        graph.addEdge(node7, getBusNode(node7), node8, getBusNode(node8), createBranchEdge("L7-8"));
        graph.addEdge(node7, getBusNode(node7), node9, getBusNode(node9), createBranchEdge("L7-9"));
        graph.addEdge(node8, getBusNode(node8), node9, getBusNode(node9), createBranchEdge("L8-9"));

        return graph;
    }

    private static BusNode getBusNode(VoltageLevelNode vlNode) {
        return vlNode.getBusNodes().stream().findFirst().orElseThrow();
    }

    private static VoltageLevelNode createBusNode(String id) {
        VoltageLevelNode vlNode = new VoltageLevelNode(id, id, id, false, true);
        vlNode.addBusNode(new BusNode(id.replace("VL", "Bus"), id.replace("VL", "Bus")));
        return vlNode;
    }

    private static BranchEdge createBranchEdge(String id) {
        return new BranchEdge(id, id, id, "Line");
    }
}
