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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class ForceAtlas2LayoutTest {

    @Test
    void tenBusesTest() {
        Graph graph = createTenBusesGraph();

        Layout forceAtlas2Layout = new BasicForceAtlas2LayoutFactory().create();
        LayoutParameters layoutParameters = new LayoutParameters();
        forceAtlas2Layout.run(graph, layoutParameters);

        assertTrue(graph.getVoltageLevelNodesStream().allMatch(this::validPosition));
    }

    private boolean validPosition(VoltageLevelNode vlNode) {
        String id = vlNode.getEquipmentId();
        double x = vlNode.getPosition().getX();
        double y = vlNode.getPosition().getY();

        return switch (id) {
            case "VL0" -> equalPosition(-4779.2, 973.6, x, y);
            case "VL1" -> equalPosition(-4519.7, 1807.8, x, y);
            case "VL2" -> equalPosition(-2992.6, 859.7, x, y);
            case "VL3" -> equalPosition(-1616.8, 307.3, x, y);
            case "VL4" -> equalPosition(3.4, -2.4, x, y);
            case "VL5" -> equalPosition(1032.4, 1508.9, x, y);
            case "VL6" -> equalPosition(1532.8, 843.2, x, y);
            case "VL7" -> equalPosition(2200.5, -2675.0, x, y);
            case "VL8" -> equalPosition(2891.8, -4164.0, x, y);
            case "VL9" -> equalPosition(3557.4, -3620.8, x, y);
            default -> false;
        };
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
