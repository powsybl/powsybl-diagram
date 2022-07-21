package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LayoutGraphAdapter {
    private final boolean removeUnnecessaryFictitiousNodes;
    private final boolean substituteSingularFictitiousByFeederNode;

    public LayoutGraphAdapter(boolean removeUnnecessaryFictitiousNodes, boolean substituteSingularFictitiousByFeederNode) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
    }

    void run(VoltageLevelGraph graph, LayoutParameters layoutParameters) {
        graph.substituteFictitiousNodesMirroringBusNodes();
        if (removeUnnecessaryFictitiousNodes) {
            graph.removeUnnecessaryFictitiousNodes();
        }
        if (substituteSingularFictitiousByFeederNode) {
            graph.substituteSingularFictitiousByFeederNode();
        }

        graph.extendBusesConnectedToBuses();

        Predicate<Node> nodesOnBus = getNodesOnBusPredicate(graph, layoutParameters.getComponentsOnBusbars());
        graph.insertBusConnections(nodesOnBus);
        graph.insertHookNodesAtBuses();
        graph.insertHookNodesAtFeeders();
    }

    private Predicate<Node> getNodesOnBusPredicate(VoltageLevelGraph graph, List<String> componentsOnBusbars) {
        Set<Node> nodesOnBusBetweenBuses = getNodesOnBusBetweenBuses(graph, componentsOnBusbars);
        return node -> componentsOnBusbars.contains(node.getComponentType()) && !nodesOnBusBetweenBuses.contains(node);
    }

    private Set<Node> getNodesOnBusBetweenBuses(VoltageLevelGraph graph, List<String> componentsOnBusbars) {
        return graph.getNodeBuses().stream()
                .flatMap(nodeBus -> nodeBus.getAdjacentNodes().stream())
                .filter(nodeConnectedToBus -> componentsOnBusbars.contains(nodeConnectedToBus.getComponentType()))
                .filter(n -> n.getAdjacentNodes().stream().allMatch(BusNode.class::isInstance))
                .collect(Collectors.toSet());
    }
}
