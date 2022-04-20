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
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.*;
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
        Node nodeForStyle = edge.getNode1() instanceof MiddleTwtNode ? edge.getNode2() : edge.getNode1();
        VoltageLevelInfos vLevelInfos =  nodeForStyle instanceof FeederTwtLegNode
                                            ? ((FeederTwtLegNode) nodeForStyle).getVoltageLevelInfos()
                                            : graph.getVoltageLevelInfos(nodeForStyle);
        return getVoltageLevelNodeStyle(vLevelInfos, nodeForStyle);
    }

    @Override
    public List<String> getSvgNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        List<String> styles = super.getSvgNodeStyles(graph, node, componentLibrary, showInternalNodes);

        if (graph != null && !(node instanceof Middle3WTNode) && !(node instanceof Feeder2WTNode)) {
            // node inside a voltageLevel graph
            // Middle3WTNode and Feeder2WTNode have style depending on their subcomponents -> see getSvgNodeSubcomponentStyles
            getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(), node).ifPresent(styles::add);
        }
        // Nothing is done for nodes outside any voltageLevel graph (multi-terminal node),
        // indeed they have subcomponents with specific styles -> see getSvgNodeSubcomponentStyles

        return styles;
    }

    @Override
    protected Optional<String> getHighlightLineStateStyle(Graph graph, Edge edge) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        if (n1 instanceof FeederWithSideNode || n2 instanceof FeederWithSideNode) {
            FeederWithSideNode n = n1 instanceof FeederWithSideNode ? (FeederWithSideNode) n1 : (FeederWithSideNode) n2;
            Map<FeederWithSideNode.Side, Boolean> connectionStatus = connectionStatus(n);
            FeederWithSideNode.Side side = null;
            FeederWithSideNode.Side otherSide = null;

            if (n.getFeederType() == FeederType.BRANCH || n.getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
                side = n.getSide();
                otherSide = side == FeederWithSideNode.Side.ONE ? FeederWithSideNode.Side.TWO : FeederWithSideNode.Side.ONE;
                if (n instanceof FeederLineNode) {
                    side = Boolean.TRUE.equals(connectionStatus.get(side)) ? side : otherSide;
                    otherSide = side == FeederWithSideNode.Side.ONE ? FeederWithSideNode.Side.TWO : FeederWithSideNode.Side.ONE;
                }
            } else if (n.getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
                String idVl = graph.getVoltageLevelInfos(n).getId();
                ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(n.getEquipmentId());
                if (transformer != null) {
                    if (transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getId().equals(idVl)) {
                        side = FeederWithSideNode.Side.ONE;
                    } else if (transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getId().equals(idVl)) {
                        side = FeederWithSideNode.Side.TWO;
                    } else {
                        side = FeederWithSideNode.Side.THREE;
                    }
                }
                otherSide = n.getSide();
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

    protected Map<FeederWithSideNode.Side, Boolean> connectionStatus(FeederWithSideNode node) {
        Map<FeederWithSideNode.Side, Boolean> res = new EnumMap<>(FeederWithSideNode.Side.class);
        if (node.getFeederType() == FeederType.BRANCH || node.getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
            Branch branch = network.getBranch(node.getEquipmentId());
            if (branch != null) {
                res.put(FeederWithSideNode.Side.ONE, branch.getTerminal(Branch.Side.ONE).isConnected());
                res.put(FeederWithSideNode.Side.TWO, branch.getTerminal(Branch.Side.TWO).isConnected());
            }
        } else if (node.getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
            ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
            if (transformer != null) {
                res.put(FeederWithSideNode.Side.ONE, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).isConnected());
                res.put(FeederWithSideNode.Side.TWO, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).isConnected());
                res.put(FeederWithSideNode.Side.THREE, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).isConnected());
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
            if (node instanceof Feeder2WTNode) {
                vlInfo = getWindingVoltageLevelInfos(graph, (Feeder2WTNode) node, subComponentName);
            } else if (node instanceof Middle3WTNode) {
                vlInfo = getWindingVoltageLevelInfos((Middle3WTNode) node, subComponentName);
            }
            if (vlInfo != null) {
                getVoltageLevelNodeStyle(vlInfo, node).ifPresent(styles::add);
            }

        } else {
            // node outside any voltageLevel graph (multi-terminal node)
            Node windingNode = null;
            if (node instanceof Middle2WTNode) {
                windingNode = getFeederNode((Middle2WTNode) node, subComponentName);
            } else if (node instanceof Middle3WTNode) {
                windingNode = getFeederNode((Middle3WTNode) node, subComponentName);
            }
            if (windingNode != null) {
                getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(windingNode), windingNode).ifPresent(styles::add);
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

    private VoltageLevelInfos getWindingVoltageLevelInfos(Graph graph, Feeder2WTNode node, String subComponentName) {
        if (subComponentName.equals(WINDING1)) {
            return graph.getVoltageLevelInfos(node);
        } else if (subComponentName.equals(WINDING2)) {
            return node.getOtherSideVoltageLevelInfos();
        } else {
            return null;
        }
    }

    private VoltageLevelInfos getWindingVoltageLevelInfos(Middle3WTNode node, String subComponentName) {
        switch (subComponentName) {
            case WINDING1: return node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_LEFT);
            case WINDING2: return node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_RIGHT);
            case WINDING3: return node.getVoltageLevelInfos(Middle3WTNode.Winding.DOWN);
            default: throw new IllegalStateException("Unexpected subComponent name: " + subComponentName);
        }
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        Bus bus = network.getBusView().getBus(busId);
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
