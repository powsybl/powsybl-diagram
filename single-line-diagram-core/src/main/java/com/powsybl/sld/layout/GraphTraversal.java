package com.powsybl.sld.layout;

import com.powsybl.sld.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GraphTraversal {

    private GraphTraversal() {
    }

    /**
     * @param node                 the entry point for the exploration
     * @param extremityCriteria    criteria applied to node returning if we reach an extremity node (the node is included in the result)
     * @param unsuccessfulCriteria criteria applied to node returning if the traversal is to be invalidated
     * @param nodesResult          the resulting list of nodes
     * @param exploredNodes        nodes already visited
     * @return true if no unsuccessfulCriteria reached or node outside
     **/

    static boolean rDelimitedExploration(Node node,
                                         Function<Node, Boolean> extremityCriteria,
                                         Function<Node, Boolean> unsuccessfulCriteria,
                                         List<Node> nodesResult,
                                         List<Node> exploredNodes) {

        if (exploredNodes.contains(node)) {
            return false;
        }
        exploredNodes.add(node);
        List<Node> nodesToVisit = new ArrayList<>(node.getAdjacentNodes());
        nodesToVisit.removeAll(exploredNodes);
        if (nodesToVisit.isEmpty()) {
            return true;
        }
        for (Node n : nodesToVisit) {
            if (unsuccessfulCriteria.apply(n)) {
                return false;
            } else if (extremityCriteria.apply(n)) {
                nodesResult.add(n);
                exploredNodes.add(n);
            } else if (rDelimitedExploration(n, extremityCriteria, unsuccessfulCriteria, nodesResult, exploredNodes)) {
                nodesResult.add(n);
            } else {
                return false;
            }
        }
        return true;
    }
}
