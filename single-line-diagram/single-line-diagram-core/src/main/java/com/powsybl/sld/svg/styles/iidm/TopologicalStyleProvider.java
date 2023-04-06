/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg.styles.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.Node.NodeType;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.svg.styles.AbstractVoltageStyleProvider;
import com.powsybl.sld.svg.styles.StyleClassConstants;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.svg.styles.StyleClassConstants.NODE_INFOS;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologicalStyleProvider extends AbstractVoltageStyleProvider {

    private final Map<String, Map<String, String>> vlNodeIdStyleMap = new HashMap<>();
    private final Map<String, Map<String, String>> vlBusIdStyleMap = new HashMap<>();
    private final Map<String, Integer> stylesIndices = new HashMap<>();
    private final Network network;

    public TopologicalStyleProvider(Network network) {
        this(BaseVoltagesConfig.fromPlatformConfig(), network);
    }

    public TopologicalStyleProvider(BaseVoltagesConfig baseVoltagesConfig, Network network) {
        super(baseVoltagesConfig);
        this.network = network;
    }

    @Override
    protected Optional<String> getVoltageLevelEdgeStyle(Graph graph, Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if (node1.getType() == NodeType.SWITCH && ((SwitchNode) node1).isOpen()) {
            return getSwitchEdgeStyle(graph, node2);
        }
        if (node2.getType() == NodeType.SWITCH && ((SwitchNode) node2).isOpen()) {
            return getSwitchEdgeStyle(graph, node1);
        }
        return super.getVoltageLevelEdgeStyle(graph, edge);
    }

    private Optional<String> getSwitchEdgeStyle(Graph graph, Node node) {
        return graph.getVoltageLevelInfos(node) != null ? getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(node), node) : Optional.empty();
    }

    @Override
    protected boolean isNodeSeparatingStyles(Node node) {
        return isMultiTerminalNode(node)
                // filtering out leg nodes as they are nodes with the same voltage level at each side
                && !((node instanceof FeederNode) && (((FeederNode) node).getFeeder() instanceof FeederTwLeg));
    }

    private boolean isMultiTerminalNode(Node node) {
        if (node instanceof EquipmentNode) {
            Identifiable<?> identifiable = network.getIdentifiable(((EquipmentNode) node).getEquipmentId());
            if (identifiable instanceof Connectable<?>) {
                return ((Connectable<?>) identifiable).getTerminals().size() > 1;
            }
        }
        return false;
    }

    @Override
    public void reset() {
        vlNodeIdStyleMap.clear();
        vlBusIdStyleMap.clear();
    }

    private Map<String, String> createBusIdStyleMap(String baseVoltageLevelStyle, String vlId) {
        List<Bus> buses = network.getVoltageLevel(vlId)
                .getBusView().getBusStream().collect(Collectors.toList());

        Map<String, String> busIdStyleMap = new HashMap<>();
        for (Bus b : buses) {
            int newIndex = stylesIndices.compute(baseVoltageLevelStyle, (s, i) -> i == null ? 0 : i + 1);
            String style = baseVoltageLevelStyle + '-' + newIndex;
            busIdStyleMap.put(b.getId(), style);
        }

        return busIdStyleMap;
    }

    private Optional<String> getNodeTopologicalStyle(String baseVoltageLevelStyle, String vlId, Node node) {
        Map<String, String> busIdStyleMap = vlBusIdStyleMap.computeIfAbsent(vlId, k -> createBusIdStyleMap(baseVoltageLevelStyle, vlId));
        Map<String, String> nodeIdStyleMap = vlNodeIdStyleMap.computeIfAbsent(vlId, k -> new HashMap<>());
        String nodeTopologicalStyle = nodeIdStyleMap.get(node.getId());
        if (nodeTopologicalStyle == null) {
            nodeTopologicalStyle = findConnectedStyle(vlId, busIdStyleMap, nodeIdStyleMap, node);
        }
        return Optional.ofNullable(nodeTopologicalStyle);
    }

    private String findConnectedStyle(String vlId, Map<String, String> busIdStyleMap, Map<String, String> nodeIdStyleMap, Node node) {
        Set<Node> connectedNodes = new LinkedHashSet<>();
        findConnectedNodes(node, connectedNodes);
        String connectedStyle = getConnectedStyle(vlId, busIdStyleMap, connectedNodes);
        connectedNodes.forEach(n -> nodeIdStyleMap.put(n.getId(), connectedStyle));
        return connectedStyle;
    }

    private String getConnectedStyle(String vlId, Map<String, String> busIdStyleMap, Set<Node> connectedNodes) {
        return connectedNodes.stream()
                .filter(EquipmentNode.class::isInstance)
                .map(EquipmentNode.class::cast)
                .map(en -> network.getIdentifiable(en.getEquipmentId()))
                .filter(identifiable -> identifiable instanceof Connectable<?>)
                .map(i -> (Connectable<?>) i)
                .map(c -> {
                    List<Terminal> terminals = c.getTerminals().stream()
                            .filter(t -> t.getVoltageLevel().getId().equals(vlId))
                            .collect(Collectors.toList());
                    if (terminals.size() == 1) {
                        // if more than one (vl-internal transformer), we don't know which side to take
                        Bus bus = terminals.get(0).getBusView().getBus();
                        return bus != null ? bus.getId() : null;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(busIdStyleMap::get)
                .findFirst()
                .orElse(StyleClassConstants.DISCONNECTED_STYLE_CLASS);
    }

    private void findConnectedNodes(Node node, Set<Node> visitedNodes) {
        if (visitedNodes.contains(node)) {
            return;
        }
        if (node.getType() == NodeType.SWITCH && ((SwitchNode) node).isOpen()) {
            return;
        }
        if (isMultiTerminalInternalNode(node)) {
            visitedNodes.add(node);
            return;
        }
        visitedNodes.add(node);
        for (Node adjNode : node.getAdjacentNodes()) {
            findConnectedNodes(adjNode, visitedNodes);
        }
    }

    private boolean isMultiTerminalInternalNode(Node node) {
        return isMultiTerminalNode(node) && node.getAdjacentEdges().size() > 1;
    }

    @Override
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos voltageLevelInfos, Node node) {
        if (node.getType() == NodeType.SWITCH && ((SwitchNode) node).isOpen()) {
            return Optional.of(StyleClassConstants.DISCONNECTED_STYLE_CLASS);
        }
        String style = Optional.ofNullable(voltageLevelInfos)
                .flatMap(vli -> baseVoltagesConfig.getBaseVoltageName(vli.getNominalVoltage(), BASE_VOLTAGE_PROFILE))
                .flatMap(baseVoltageName -> getNodeTopologicalStyle(StyleClassConstants.STYLE_PREFIX + baseVoltageName, voltageLevelInfos.getId(), node))
                .orElse(StyleClassConstants.DISCONNECTED_STYLE_CLASS);
        return Optional.of(style);
    }

    @Override
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos vlInfo, Node node, NodeSide side) {
        return getVoltageLevelNodeStyle(vlInfo, node.getAdjacentNodes().get(side.getIntValue() - 1));
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        String busStyle = vlBusIdStyleMap.getOrDefault(graph.getVoltageLevelInfos().getId(), Collections.emptyMap())
                .getOrDefault(busId, null);
        return busStyle != null ? List.of(busStyle, NODE_INFOS) : List.of(NODE_INFOS);
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css", "topologicalBaseVoltages.css");
    }

}
