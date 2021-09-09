/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.Node.NodeType;
import com.powsybl.sld.styles.BaseVoltageStyle;
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
        this(BaseVoltageStyle.fromPlatformConfig(), network);
    }

    public TopologicalStyleProvider(BaseVoltageStyle baseVoltageStyle, Network network) {
        super(baseVoltageStyle, network);
    }

    @Override
    protected Optional<String> getEdgeStyle(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if (edge instanceof BranchEdge && (node1 instanceof FeederLineNode || node2 instanceof FeederLineNode)) {
            return getLineEdgeStyle((BranchEdge) edge);
        } else {
            if (node1.getType() == NodeType.SWITCH && node1.isOpen()) {
                return node2.getVoltageLevelInfos() != null ? getVoltageLevelNodeStyle(node2.getVoltageLevelInfos(), node2) : Optional.empty();
            }
            if (node2.getType() == NodeType.SWITCH && node2.isOpen()) {
                return node1.getVoltageLevelInfos() != null ? getVoltageLevelNodeStyle(node1.getVoltageLevelInfos(), node1) : Optional.empty();
            }
            return super.getEdgeStyle(edge);
        }
    }

    private Optional<String> getLineEdgeStyle(BranchEdge edge) {
        Optional<String> edgeStyle = getVoltageLevelNodeStyle(edge.getNode1().getVoltageLevelInfos(), edge.getNode1());
        if (edgeStyle.isPresent() && edgeStyle.get().equals(DiagramStyles.DISCONNECTED_STYLE_CLASS)) {
            edgeStyle = getVoltageLevelNodeStyle(edge.getNode2().getVoltageLevelInfos(), edge.getNode2());
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
        String connectedStyle = connectedNodes.stream().map(c -> equipmentIdStyleMap.get(c.getEquipmentId())).filter(Objects::nonNull).findFirst().orElse(null);
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
        if (node.getType() == NodeType.SWITCH && node.isOpen()) {
            return;
        }
        visitedNodes.add(node);
        for (Node adjNode : node.getAdjacentNodes()) {
            findConnectedNodes(adjNode, visitedNodes);
        }
    }

    @Override
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos voltageLevelInfos, Node node) {
        if (node.getType() == NodeType.SWITCH && node.isOpen()) {
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
        return Arrays.asList("tautologies.css", "topologicalBaseVoltages.css", "highlightLineStates.css", "baseVoltageConstantColors.css");
    }

}
