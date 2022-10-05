/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.BasicStyleProvider;
import com.powsybl.sld.svg.DiagramStyles;

import java.util.*;

import static com.powsybl.sld.svg.DiagramStyles.NODE_INFOS;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractBaseVoltageDiagramStyleProvider extends BasicStyleProvider {

    protected static final String BASE_VOLTAGE_PROFILE = "Default";

    private static final String WINDING1 = "WINDING1";
    private static final String WINDING2 = "WINDING2";
    private static final String WINDING3 = "WINDING3";

    protected final Network network;
    protected final BaseVoltagesConfig baseVoltagesConfig;

    protected AbstractBaseVoltageDiagramStyleProvider(BaseVoltagesConfig baseVoltagesConfig, Network network) {
        this.baseVoltagesConfig = Objects.requireNonNull(baseVoltagesConfig);
        this.network = network;
    }

    @Override
    protected Optional<String> getEdgeStyle(Graph graph, Edge edge) {
        VoltageLevelInfos vLevelInfos;
        Node nodeForStyle = edge.getNode1() instanceof MiddleTwtNode ? edge.getNode2() : edge.getNode1();
        if (nodeForStyle instanceof FeederNode && ((FeederNode) nodeForStyle).getFeeder() instanceof FeederTwLeg) {
            vLevelInfos = ((FeederTwLeg) ((FeederNode) nodeForStyle).getFeeder()).getVoltageLevelInfos();
        } else {
            vLevelInfos = graph.getVoltageLevelInfos(nodeForStyle);
        }
        return getVoltageLevelNodeStyle(vLevelInfos, nodeForStyle);
    }

    @Override
    public List<String> getSvgNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        List<String> styles = super.getSvgNodeStyles(graph, node, componentLibrary, showInternalNodes);

        if (graph != null && !isNodeConnectingVoltageLevels(node)) {
            // Nodes connecting voltage levels might have style depending on their subcomponents (-> see getSvgNodeSubcomponentStyles)
            // to display the fact it is the connection of two or more voltage levels: 2WT for instance
            // Note that nodes outside a voltageLevel graph (graph==null) are nodes connecting voltageLevels
            getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(), node).ifPresent(styles::add);
        }

        return styles;
    }

    protected boolean isNodeConnectingVoltageLevels(Node node) {
        if (node instanceof FeederNode) {
            // filtering out leg nodes as they are nodes with the same voltage level at each side
            Feeder feeder = ((FeederNode) node).getFeeder();
            return feeder instanceof FeederWithSides && !(feeder instanceof FeederTwLeg);
        } else {
            return node instanceof Middle3WTNode;
        }
    }

    @Override
    protected Optional<String> getHighlightLineStateStyle(Graph graph, Edge edge) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        FeederNode n;

        if (n1 instanceof FeederNode || n2 instanceof FeederNode) {
            n = (FeederNode) (n1 instanceof FeederNode ? n1 : n2);
        } else {
            return Optional.empty();
        }

        if (n.getFeeder() instanceof FeederWithSides) {
            FeederWithSides feederWs = (FeederWithSides) n.getFeeder();
            Map<NodeSide, Boolean> connectionStatus = connectionStatus(n);
            NodeSide side = null;
            NodeSide otherSide = null;

            if (feederWs.getFeederType() == FeederType.BRANCH || feederWs.getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
                side = feederWs.getSide();
                otherSide = side == NodeSide.ONE ? NodeSide.TWO : NodeSide.ONE;
                if (ComponentTypeName.LINE.equals(n.getComponentType())) {
                    side = Boolean.TRUE.equals(connectionStatus.get(side)) ? side : otherSide;
                    otherSide = side == NodeSide.ONE ? NodeSide.TWO : NodeSide.ONE;
                }
            } else if (feederWs.getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
                String idVl = graph.getVoltageLevelInfos(n).getId();
                ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(n.getEquipmentId());
                if (transformer != null) {
                    if (transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getId().equals(idVl)) {
                        side = NodeSide.ONE;
                    } else if (transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getId().equals(idVl)) {
                        side = NodeSide.TWO;
                    } else {
                        side = NodeSide.THREE;
                    }
                }
                otherSide = feederWs.getSide();
            }

            if (side != null && otherSide != null) {
                if (Boolean.FALSE.equals(connectionStatus.get(side)) && Boolean.FALSE.equals(connectionStatus.get(otherSide))) {  // disconnected on both ends
                    return Optional.of(DiagramStyles.FEEDER_DISCONNECTED);
                } else if (Boolean.TRUE.equals(connectionStatus.get(side)) && Boolean.FALSE.equals(connectionStatus.get(otherSide))) {  // connected on side and disconnected on other side
                    return Optional.of(DiagramStyles.FEEDER_CONNECTED_DISCONNECTED);
                } else if (Boolean.FALSE.equals(connectionStatus.get(side)) && Boolean.TRUE.equals(connectionStatus.get(otherSide))) {  // disconnected on side and connected on other side
                    return Optional.of(DiagramStyles.FEEDER_DISCONNECTED_CONNECTED);
                }
            }
        }
        return Optional.empty();
    }

    protected Map<NodeSide, Boolean> connectionStatus(FeederNode node) {
        Map<NodeSide, Boolean> res = new EnumMap<>(NodeSide.class);
        if (node.getFeeder().getFeederType() == FeederType.BRANCH || node.getFeeder().getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
            Branch branch = network.getBranch(node.getEquipmentId());
            if (branch != null) {
                res.put(NodeSide.ONE, branch.getTerminal(Branch.Side.ONE).isConnected());
                res.put(NodeSide.TWO, branch.getTerminal(Branch.Side.TWO).isConnected());
            }
        } else if (node.getFeeder().getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
            ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
            if (transformer != null) {
                res.put(NodeSide.ONE, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).isConnected());
                res.put(NodeSide.TWO, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).isConnected());
                res.put(NodeSide.THREE, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).isConnected());
            }
        }
        return res;
    }

    @Override
    public List<String> getSvgNodeSubcomponentStyles(Graph graph, Node node, String subComponentName) {

        List<String> styles = new ArrayList<>();

        VoltageLevelGraph g = graph.getVoltageLevelGraph(node);
        if (g != null) {
            // node inside a voltageLevel graph
            VoltageLevelInfos vlInfo = null;
            if (node instanceof FeederNode) {
                Feeder feeder = ((FeederNode) node).getFeeder();
                if (feeder instanceof FeederWithSides) {
                    vlInfo = getSubComponentVoltageLevelInfos((FeederWithSides) feeder, subComponentName);
                }
            } else if (node instanceof Middle3WTNode) {
                vlInfo = getSubComponentVoltageLevelInfos((Middle3WTNode) node, subComponentName);
            }
            if (vlInfo != null) {
                getVoltageLevelNodeStyle(vlInfo, node).ifPresent(styles::add);
            }

        } else {
            // node outside any voltageLevel graph (multi-terminal node)
            Node feederNode = null;
            if (node instanceof Middle2WTNode) {
                feederNode = getFeederNode((Middle2WTNode) node, subComponentName);
            } else if (node instanceof Middle3WTNode) {
                feederNode = getFeederNode((Middle3WTNode) node, subComponentName);
            }
            if (feederNode != null) {
                getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(feederNode), feederNode).ifPresent(styles::add);
            }
        }

        return styles;
    }

    /**
     * Returns the voltage level style if any to apply to the given node
     *
     * @param vlInfo the VoltageLevelInfos related to the given node
     * @param node   the node on which the style if any is applied to
     * @return the voltage level style if any
     */
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos vlInfo, Node node) {
        return baseVoltagesConfig.getBaseVoltageName(vlInfo.getNominalVoltage(), BASE_VOLTAGE_PROFILE)
                .map(bvName -> DiagramStyles.STYLE_PREFIX + bvName);
    }

    private Node getFeederNode(Middle3WTNode node, String subComponentName) {
        switch (subComponentName) {
            case WINDING1: return node.getAdjacentNode(Middle3WTNode.Winding.UPPER_LEFT);
            case WINDING2: return node.getAdjacentNode(Middle3WTNode.Winding.UPPER_RIGHT);
            case WINDING3: return node.getAdjacentNode(Middle3WTNode.Winding.DOWN);
            default: throw new IllegalStateException("Unexpected subComponent name: " + subComponentName);
        }
    }

    private Node getFeederNode(Middle2WTNode node, String subComponentName) {
        return node.getAdjacentNodes().get(subComponentName.equals(WINDING1) ?  0 : 1);
    }

    protected VoltageLevelInfos getSubComponentVoltageLevelInfos(FeederWithSides feederWs, String subComponentName) {
        if (subComponentName.equals(WINDING1)) {
            return feederWs.getVoltageLevelInfos();
        } else if (subComponentName.equals(WINDING2)) {
            return feederWs.getOtherSideVoltageLevelInfos();
        } else {
            return null;
        }
    }

    protected VoltageLevelInfos getSubComponentVoltageLevelInfos(Middle3WTNode node, String subComponentName) {
        switch (subComponentName) {
            case WINDING1: return node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_LEFT);
            case WINDING2: return node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_RIGHT);
            case WINDING3: return node.getVoltageLevelInfos(Middle3WTNode.Winding.DOWN);
            default: throw new IllegalStateException("Unexpected subComponent name: " + subComponentName);
        }
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        Bus bus = network.getVoltageLevel(graph.getVoltageLevelInfos().getId()).getBusView().getBus(busId);
        if (bus != null) {
            for (Terminal t : bus.getConnectedTerminals()) {
                for (FeederNode feederNode : graph.getFeederNodes()) {
                    if (feederNode.getEquipmentId().equals(t.getConnectable().getId())) {
                        Optional<String> voltageLevelStyle = getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(), feederNode);
                        if (voltageLevelStyle.isPresent()) {
                            return Arrays.asList(voltageLevelStyle.get(), NODE_INFOS);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css", "baseVoltages.css", "highlightLineStates.css");
    }

}
