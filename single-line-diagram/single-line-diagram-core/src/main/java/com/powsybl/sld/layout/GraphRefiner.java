/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Refines the graph so that it becomes consistent with the diagram layout.
 * In particular for cell detection: it inserts the bus connection nodes and the hook nodes needed for it.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class GraphRefiner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphRefiner.class);
    private final boolean removeUnnecessaryFictitiousNodes;
    private final boolean substituteSingularFictitiousByFeederNode;
    private final boolean substituteInternalMiddle2wtByEquipmentNodes;

    public GraphRefiner(boolean removeUnnecessaryFictitiousNodes, boolean substituteSingularFictitiousByFeederNode,
                        boolean substituteInternalMiddle2wtByEquipmentNodes) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
        this.substituteInternalMiddle2wtByEquipmentNodes = substituteInternalMiddle2wtByEquipmentNodes;
    }

    void run(VoltageLevelGraph graph, LayoutParameters layoutParameters) {
        if (substituteInternalMiddle2wtByEquipmentNodes) {
            graph.substituteInternalMiddle2wtByEquipmentNodes();
        }
        handleConnectedComponents(graph);
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

        graph.substituteNodesMirroringGroundDisconnectionComponent();
    }

    /**
     * Check if the graph is connected or not
     */
    private void handleConnectedComponents(VoltageLevelGraph graph) {
        List<Set<Node>> connectedSets = new ConnectivityInspector<>(graph.toJgrapht()).connectedSets();
        if (connectedSets.size() != 1) {
            LOGGER.warn("{} connected components found", connectedSets.size());
            connectedSets.stream()
                    .sorted(Comparator.comparingInt(Set::size))
                    .map(setNodes -> setNodes.stream().map(Node::getId).collect(Collectors.toSet()))
                    .forEach(strings -> LOGGER.warn("   - {}", strings));
        }
        // Add a fictitious bus for all connected components without any bus
        connectedSets.stream()
                .filter(s -> s.stream().noneMatch(node -> node.getType() == Node.NodeType.BUS))
                .forEach(s -> addFictitiousBusInConnectedComponent(graph, s));
    }

    private void addFictitiousBusInConnectedComponent(VoltageLevelGraph graph, Set<Node> nodes) {
        // Replace the most meshed fictitious node by a fictitious BusNode.
        // If no fictitious node, insert a fictitious BusNode at the first node of the set.
        nodes.stream().filter(node -> node.getType() == Node.NodeType.INTERNAL)
                .min(Comparator.<Node>comparingInt(node -> node.getAdjacentEdges().size()).reversed().thenComparing(Node::getId)) // for stable fictitious node selection, also sort on id
                .ifPresentOrElse(
                        mostMeshedFictitiousNode -> graph.substituteNode(mostMeshedFictitiousNode,
                                NodeFactory.createFictitiousBusNode(graph, mostMeshedFictitiousNode.getId() + "_FictitiousBus")),
                        () -> {
                            Node attachedNode = nodes.iterator().next();
                            BusNode busNode = NodeFactory.createFictitiousBusNode(graph, attachedNode.getId() + "_FictitiousBus");
                            graph.addEdge(busNode, attachedNode);
                        });
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
