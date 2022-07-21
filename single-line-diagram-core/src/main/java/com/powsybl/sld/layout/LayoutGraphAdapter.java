package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;

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

    void run(VoltageLevelGraph graph) {
        graph.substituteFictitiousNodesMirroringBusNodes();
        if (removeUnnecessaryFictitiousNodes) {
            graph.removeUnnecessaryFictitiousNodes();
        }
        if (substituteSingularFictitiousByFeederNode) {
            graph.substituteSingularFictitiousByFeederNode();
        }

        graph.extendBusesConnectedToBuses();

        Predicate<Node> nodesOnBus = node -> node instanceof SwitchNode && ((SwitchNode) node).getKind() == SwitchNode.SwitchKind.DISCONNECTOR;
        Set<Node> nodesOnBusBetweenBuses = getNodesOnBusBetweenBuses(graph, nodesOnBus);
        nodesOnBus = nodesOnBus.and(node -> !nodesOnBusBetweenBuses.contains(node));

        graph.insertBusConnections(nodesOnBus);
        graph.insertHookNodesAtBuses();
        graph.insertHookNodesAtFeeders();
    }

    private Set<Node> getNodesOnBusBetweenBuses(VoltageLevelGraph graph, Predicate<Node> nodesOnBus) {
        return graph.getNodeBuses().stream()
                .flatMap(nodeBus -> nodeBus.getAdjacentNodes().stream())
                .filter(nodesOnBus)
                .filter(n -> n.getAdjacentNodes().stream().allMatch(BusNode.class::isInstance))
                .collect(Collectors.toSet());
    }
}
