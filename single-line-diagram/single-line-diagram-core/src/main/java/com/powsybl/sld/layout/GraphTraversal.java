/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.nodes.Node;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class GraphTraversal {

    private GraphTraversal() {
    }

    /**
     * @param node                 the entry point for the exploration
     * @param extremityCriteria    criteria applied to node returning if we reach an extremity node (the node is included in the result)
     * @param unsuccessfulCriteria criteria applied to node returning if the traversal is to be invalidated
     * @param nodesResult          the resulting set of nodes
     * @param outsideNodes         nodes already visited
     * @return true if no unsuccessfulCriteria reached or node outside
     **/

    static boolean run(Node node,
                       Predicate<Node> extremityCriteria,
                       Predicate<Node> unsuccessfulCriteria,
                       Set<Node> nodesResult,
                       Set<Node> outsideNodes) {

        if (outsideNodes.contains(node)) {
            return false;
        }
        nodesResult.add(node);
        List<Node> nodesToVisit = node.getAdjacentNodes().stream()
                .filter(n -> !outsideNodes.contains(n) && !nodesResult.contains(n))
                .collect(Collectors.toList());
        if (nodesToVisit.isEmpty()) {
            return true;
        }
        for (Node n : nodesToVisit) {
            if (unsuccessfulCriteria.test(n)) {
                return false;
            } else if (extremityCriteria.test(n)) {
                nodesResult.add(n);
            } else if (!run(n, extremityCriteria, unsuccessfulCriteria, nodesResult, outsideNodes)) {
                return false;
            }
        }
        return true;
    }

    static Set<Node> run(Node node,
                          Predicate<Node> extremityCriteria,
                          Set<Node> outsideNodes) {
        Set<Node> nodesResult = new LinkedHashSet<>();
        run(node, extremityCriteria, n -> false, nodesResult, outsideNodes);
        return nodesResult;
    }

}
