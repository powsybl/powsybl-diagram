/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Visit the graph to identifies the connected sets of nodes
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class TopologyCalculation {

    private final Predicate<Node> extremityCriteria;

    public TopologyCalculation(Predicate<Node> extremityCriteria) {
        this.extremityCriteria = extremityCriteria;
    }

    public TopologyCalculation() {
        this(TopologyCalculation::isOpenSwitchNode);
    }

    /**
     * Analyses the graph to partition it into TopologicallyConnectedNodeSets.
     * @param graph the graph to be analysed
     * @return a list of TopologicallyConnectedNodeSets
     */
    public List<TopologicallyConnectedNodesSet> findConnectedNodeSets(VoltageLevelGraph graph) {
        List<TopologicallyConnectedNodesSet> topologicallyConnectedNodesSets = new ArrayList<>();
        List<Node> nodesToVisit = graph.getNodes();
        Set<Node> visitedNodes = new HashSet<>();
        Node node = identifyNonOpenNode(nodesToVisit);
        while (node != null) {
            List<Node> borderNodes = new ArrayList<>();
            Set<Node> connectedNodes = getConnectedNodesWithExtremityNodes(node, borderNodes, visitedNodes);
            Set<Node> borderSwitchNodes = getBorderNodes(borderNodes, connectedNodes);
            topologicallyConnectedNodesSets.add(new TopologicallyConnectedNodesSet(new HashSet<>(connectedNodes), borderSwitchNodes));
            connectedNodes.removeAll(borderSwitchNodes);
            visitedNodes.addAll(connectedNodes); //a border switch is part of 2 connectedNodesSets
            nodesToVisit.removeAll(connectedNodes);
            node = identifyNonOpenNode(nodesToVisit);
        }
        return topologicallyConnectedNodesSets;
    }

    /**
     * Analyses the graph to find the TopologicallyConnectedNodesSet corresponding to given predicate.
     * @param graph the graph to be analysed
     * @param filter the filter applied to the all TopologicallyConnectedNodesSet of the given graph
     * @return a list of TopologicallyConnectedNodeSets
     */
    public List<TopologicallyConnectedNodesSet> findConnectedNodeSets(VoltageLevelGraph graph, Predicate<TopologicallyConnectedNodesSet> filter) {
        return findConnectedNodeSets(graph).stream().filter(filter).collect(Collectors.toList());
    }

    private Set<Node> getBorderNodes(List<Node> borderNodes, Set<Node> connectedNodes) {
        return borderNodes.stream()
                .filter(n -> isBorderNode(n, connectedNodes))
                .collect(Collectors.toSet());
    }

    private Set<Node> getConnectedNodesWithExtremityNodes(Node node, List<Node> borderNodes, Set<Node> visitedNodes) {
        return GraphTraversal
                .run(node, n -> {
                    boolean isExtremity = extremityCriteria.test(n);
                    if (isExtremity) {
                        borderNodes.add(n);
                    }
                    return isExtremity;
                }, visitedNodes);
    }

    private static Node identifyNonOpenNode(List<Node> remainingNodes) {
        return remainingNodes.stream().filter(n -> !isOpenSwitchNode(n)).findFirst().orElse(null);
    }

    private static boolean isBorderNode(Node borderNode, Set<Node> connectedNodes) {
        return !connectedNodes.containsAll(borderNode.getAdjacentNodes());
    }

    private static boolean isOpenSwitchNode(Node node) {
        return node.getType() == Node.NodeType.SWITCH && node.isOpen();
    }
}
