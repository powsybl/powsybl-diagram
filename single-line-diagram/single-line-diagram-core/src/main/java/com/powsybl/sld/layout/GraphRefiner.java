/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Refines the graph so that it becomes consistent with the diagram layout.
 * In particular for cell detection: it inserts the {@link BusConnection} nodes and {@link InternalNode} hook nodes needed for it.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class GraphRefiner {
    private final boolean removeUnnecessaryFictitiousNodes;
    private final boolean substituteSingularFictitiousByFeederNode;

    public GraphRefiner(boolean removeUnnecessaryFictitiousNodes, boolean substituteSingularFictitiousByFeederNode) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
    }

    void run(VoltageLevelGraph graph, LayoutParameters layoutParameters) {
        graph.substituteFictitiousNodesMirroringBusNodes();
        if (removeUnnecessaryFictitiousNodes) {
            graph.removeUnnecessaryConnectivityNodes();
        }
        if (substituteSingularFictitiousByFeederNode) {
            graph.substituteSingularFictitiousByFeederNode();
        }
        if (layoutParameters.isRemoveFictitiousSwitchNodes()) {
            graph.removeFictitiousSwitchNode();
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
