/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.Node.NodeType;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.svg.DiagramStyles;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologicalStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    private final Map<String, Map<String, String>> vlNodeIdStyleMap = new HashMap<>();
    private final Map<String, Map<String, String>> vlEquipmentIdStyleMap = new HashMap<>();

    public TopologicalStyleProvider(Network network) {
        this(BaseVoltagesConfig.fromPlatformConfig(), network);
    }

    public TopologicalStyleProvider(BaseVoltagesConfig baseVoltagesConfig, Network network) {
        super(baseVoltagesConfig, network);
    }

    @Override
    protected Optional<String> getEdgeStyle(Graph graph, Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if (edge instanceof BranchEdge && (ComponentTypeName.LINE.equals(node1.getComponentType()) || ComponentTypeName.LINE.equals(node2.getComponentType()))) {
            return getLineEdgeStyle(graph, (BranchEdge) edge);
        } else {
            if (node1.getType() == NodeType.SWITCH && ((SwitchNode) node1).isOpen()) {
                return graph.getVoltageLevelInfos(node2) != null ? getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(node2), node2) : Optional.empty();
            }
            if (node2.getType() == NodeType.SWITCH && ((SwitchNode) node2).isOpen()) {
                return graph.getVoltageLevelInfos(node1) != null ? getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(node1), node1) : Optional.empty();
            }
            return super.getEdgeStyle(graph, edge);
        }
    }

    private Optional<String> getLineEdgeStyle(Graph graph, BranchEdge edge) {
        Optional<String> edgeStyle = getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(edge.getNode1()), edge.getNode1());
        if (edgeStyle.isPresent() && edgeStyle.get().equals(DiagramStyles.DISCONNECTED_STYLE_CLASS)) {
            edgeStyle = getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(edge.getNode2()), edge.getNode2());
        }
        return edgeStyle;
    }

    @Override
    public void reset() {
        vlNodeIdStyleMap.clear();
        vlEquipmentIdStyleMap.clear();
    }

    private Map<String, String> createEquipmentIdStyleMap(String baseVoltageLevelStyle, VoltageLevelInfos voltageLevelInfos) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelInfos.getId());
        List<Bus> buses = vl.getBusView().getBusStream().collect(Collectors.toList());

        Map<String, String> equipmentIdStyleMap = new HashMap<>();
        AtomicInteger idxStyle = new AtomicInteger(0);
        buses.forEach(b -> {
            String style = baseVoltageLevelStyle + '-' + idxStyle.getAndIncrement();
            b.visitConnectedEquipments(new TopologyVisitorId(equipmentId -> equipmentIdStyleMap.put(equipmentId, style)));
        });

        return equipmentIdStyleMap;
    }

    private Optional<String> getNodeTopologicalStyle(String baseVoltageLevelStyle, VoltageLevelInfos
            voltageLevelInfos, Node node) {
        Map<String, String> equipmentIdStyleMap = getEquipmentIdStyleMap(baseVoltageLevelStyle, voltageLevelInfos);
        Map<String, String> nodeIdStyleMap = vlNodeIdStyleMap.computeIfAbsent(voltageLevelInfos.getId(), k -> new HashMap<>());
        String nodeTopologicalStyle = nodeIdStyleMap.getOrDefault(node.getId(), findConnectedStyle(equipmentIdStyleMap, nodeIdStyleMap, node));
        return Optional.ofNullable(nodeTopologicalStyle);
    }

    private String findConnectedStyle(Map<String, String> equipmentIdStyleMap, Map<String, String> nodeIdStyleMap, Node node) {
        Set<Node> connectedNodes = new HashSet<>();
        findConnectedNodes(node, connectedNodes);
        String connectedStyle = connectedNodes.stream()
                .filter(EquipmentNode.class::isInstance)
                .map(EquipmentNode.class::cast)
                .map(c -> equipmentIdStyleMap.get(c.getEquipmentId()))
                .filter(Objects::nonNull).findFirst().orElse(null);
        connectedNodes.forEach(n -> nodeIdStyleMap.put(n.getId(), connectedStyle));
        return connectedStyle;
    }

    private Map<String, String> getEquipmentIdStyleMap(String baseVoltageLevelStyle, VoltageLevelInfos
            voltageLevelInfos) {
        return vlEquipmentIdStyleMap.computeIfAbsent(
                voltageLevelInfos.getId(), k -> createEquipmentIdStyleMap(baseVoltageLevelStyle, voltageLevelInfos));
    }

    private void findConnectedNodes(Node node, Set<Node> visitedNodes) {
        if (visitedNodes.contains(node)) {
            return;
        }
        if (node.getType() == NodeType.SWITCH && ((SwitchNode) node).isOpen()) {
            return;
        }
        visitedNodes.add(node);
        for (Node adjNode : node.getAdjacentNodes()) {
            findConnectedNodes(adjNode, visitedNodes);
        }
    }

    @Override
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos voltageLevelInfos, Node node) {
        if (node.getType() == NodeType.SWITCH && ((SwitchNode) node).isOpen()) {
            return Optional.of(DiagramStyles.DISCONNECTED_STYLE_CLASS);
        }
        Optional<String> baseVoltageLevelStyle = super.getVoltageLevelNodeStyle(voltageLevelInfos, node);
        if (baseVoltageLevelStyle.isPresent()) {
            Optional<String> nodeTopologicalStyle = getNodeTopologicalStyle(baseVoltageLevelStyle.get(), voltageLevelInfos, node);
            if (nodeTopologicalStyle.isPresent()) {
                return nodeTopologicalStyle;
            }
        }
        return Optional.of(DiagramStyles.DISCONNECTED_STYLE_CLASS);
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css", "topologicalBaseVoltages.css", "highlightLineStates.css");
    }

}
