/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class GraphTraversal {

    private GraphTraversal() {
    }

    /**
     * @param node                  the entry point for the exploration
     * @param extremityCriterion    predicate to know whether node is an extremity node (the node is included in the result)
     * @param unsuccessfulCriterion predicate to know whether node should invalidate the whole traversal (the node is not included in the result)
     * @param visitedNodes          the set of nodes visited
     * @param outsideNodes          nodes which should be considered outside the traversal
     * @return true if no unsuccessfulCriteria reached or node outside
     **/

    static boolean run(Node node,
                       Predicate<Node> extremityCriterion,
                       Predicate<Node> unsuccessfulCriterion,
                       List<Node> extremityNodes,
                       Set<Node> visitedNodes,
                       Set<Node> outsideNodes) {

        if (outsideNodes.contains(node)) {
            return false;
        }
        if (visitedNodes.contains(node)) {
            return true;
        }
        if (unsuccessfulCriterion.test(node)) {
            // Unsuccessful criterion prevails over the extremity criteria
            // Unsuccessful nodes are not in the visited nodes
            return false;
        }

        visitedNodes.add(node);
        if (extremityCriterion.test(node)) {
            extremityNodes.add(node);
            return true;
        }

        // continue on all adjacent nodes
        return node.getAdjacentNodes().stream()
                .allMatch(n -> run(n, extremityCriterion, unsuccessfulCriterion, extremityNodes, visitedNodes, outsideNodes));
    }

    public static Map<Node, Set<Node.NodeType>> getAdjacentBranchesTypes(Node node, Predicate<Node> branchEnd, Set<Node> externalNodes) {
        Map<Node, Set<Node.NodeType>> adjacentExtremityTypes = new HashMap<>();
        for (Node adj : node.getAdjacentNodes()) {
            List<Node> extremities = new ArrayList<>();
            Set<Node> visitedNodes = new LinkedHashSet<>();
            visitedNodes.add(node); // removal of the node to explore branches from it
            GraphTraversal.run(adj, branchEnd, n -> false, extremities, visitedNodes, externalNodes);

            // what are the types of terminal node of the branch
            adjacentExtremityTypes.put(adj, extremities.stream().map(Node::getType).collect(Collectors.toSet()));

            // Remove the bus nodes from visited nodes
            visitedNodes.removeIf(BusNode.class::isInstance);
        }
        return adjacentExtremityTypes;
    }
}
