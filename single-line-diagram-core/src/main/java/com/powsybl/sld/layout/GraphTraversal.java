package com.powsybl.sld.layout;

import com.powsybl.sld.model.Node;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GraphTraversal {

    private GraphTraversal() {
    }

    /**
     * @param node                 the entry point for the exploration
     * @param extremityCriteria    criteria applied to node returning if we reach an extremity node (the node is included in the result)
     * @param unsuccessfulCriteria criteria applied to node returning if the traversal is to be invalidated
     * @param nodesResult          the resulting list of nodes
     * @param outsideNodes         nodes already visited
     * @return true if no unsuccessfulCriteria reached or node outside
     **/

    static boolean run(Node node,
                       Function<Node, Boolean> extremityCriteria,
                       Function<Node, Boolean> unsuccessfulCriteria,
                       List<Node> nodesResult,
                       List<Node> outsideNodes) {

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
            if (unsuccessfulCriteria.apply(n)) {
                return false;
            } else if (extremityCriteria.apply(n)) {
                nodesResult.add(n);
            } else if (!run(n, extremityCriteria, unsuccessfulCriteria, nodesResult, outsideNodes)) {
                return false;
            }
        }
        return true;
    }
}
