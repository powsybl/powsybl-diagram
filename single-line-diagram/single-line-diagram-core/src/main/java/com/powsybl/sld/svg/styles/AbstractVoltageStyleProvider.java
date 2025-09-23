/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg.styles;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.library.SldComponentTypeName;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.Feeder;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.Middle2WTNode;
import com.powsybl.sld.model.nodes.Middle3WTNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.NodeSide;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.BusInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.sld.svg.styles.StyleClassConstants.WIRE_STYLE_CLASS;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractVoltageStyleProvider extends AbstractStyleProvider {

    protected static final String BASE_VOLTAGE_PROFILE = "Default";

    private static final String WINDING1 = "WINDING1";
    private static final String WINDING2 = "WINDING2";
    private static final String WINDING3 = "WINDING3";
    private static final String ARROW1 = "ARROW1";
    private static final String ARROW2 = "ARROW2";
    private static final String ARROW3 = "ARROW3";

    protected final BaseVoltagesConfig baseVoltagesConfig;

    protected AbstractVoltageStyleProvider(BaseVoltagesConfig baseVoltagesConfig) {
        this.baseVoltagesConfig = Objects.requireNonNull(baseVoltagesConfig);
    }

    @Override
    public List<String> getEdgeStyles(Graph graph, Edge edge) {
        List<String> edgesStyles = new ArrayList<>();
        edgesStyles.add(WIRE_STYLE_CLASS);
        edgesStyles.addAll(getVoltageLevelEdgeStyles(graph, edge));
        getDanglingLineStyle(edge).ifPresent(edgesStyles::add);
        return edgesStyles;
    }

    private Optional<String> getDanglingLineStyle(Edge edge) {
        if (edge.getNode1() instanceof FeederNode feederNode1) {
            return getDanglingLineStyle(feederNode1);
        }
        if (edge.getNode2() instanceof FeederNode feederNode2) {
            return getDanglingLineStyle(feederNode2);
        }
        return Optional.empty();
    }

    private Optional<String> getDanglingLineStyle(FeederNode n) {
        if (n.getFeeder().getFeederType() == FeederType.BRANCH) {
            return switch (n.getComponentType()) {
                case SldComponentTypeName.TIE_LINE -> Optional.of(StyleClassConstants.TIE_LINE);
                case SldComponentTypeName.DANGLING_LINE -> Optional.of(StyleClassConstants.DANGLING_LINE);
                default -> Optional.empty();
            };
        }
        return Optional.empty();
    }

    protected List<String> getVoltageLevelEdgeStyles(Graph graph, Edge edge) {
        VoltageLevelInfos vLevelInfos;
        Node nodeForStyle = isNodeSeparatingStyles(edge.getNode1()) ? edge.getNode2() : edge.getNode1();
        if (nodeForStyle instanceof FeederNode && ((FeederNode) nodeForStyle).getFeeder() instanceof FeederTwLeg) {
            vLevelInfos = ((FeederTwLeg) ((FeederNode) nodeForStyle).getFeeder()).getVoltageLevelInfos();
        } else {
            vLevelInfos = graph.getVoltageLevelInfos(nodeForStyle);
        }
        return getNodeStyles(vLevelInfos, nodeForStyle);
    }

    @Override
    public List<String> getNodeStyles(VoltageLevelGraph graph, Node node, SldComponentLibrary componentLibrary, boolean showInternalNodes) {
        List<String> styles = super.getNodeStyles(graph, node, componentLibrary, showInternalNodes);

        if (graph != null && !isNodeSeparatingStyles(node)) {
            // Some nodes have two styles as they separate voltage levels for instance
            // Then they  have a style depending on their subcomponents (-> see getSvgNodeSubcomponentStyles)
            // Note that nodes outside a voltageLevel graph (graph==null) are nodes with two styles
            styles.addAll(getNodeStyles(graph.getVoltageLevelInfos(), node));
        }

        return styles;
    }

    protected abstract boolean isNodeSeparatingStyles(Node node);

    @Override
    public List<String> getNodeSubcomponentStyles(Graph graph, Node node, String subComponentName) {

        List<String> styles = new ArrayList<>();

        VoltageLevelGraph g = graph.getVoltageLevelGraph(node);
        if (g != null) {
            // node inside a voltageLevel graph
            if (isNodeSeparatingStyles(node)) {
                if (node instanceof FeederNode) {
                    Feeder feeder = ((FeederNode) node).getFeeder();
                    if (feeder instanceof FeederWithSides) {
                        VoltageLevelInfos vlInfo = getSubComponentVoltageLevelInfos((FeederWithSides) feeder, subComponentName);
                        styles.addAll(getNodeStyles(vlInfo, node));
                    }
                } else if (node instanceof Middle3WTNode) {
                    VoltageLevelInfos vlInfo = getSubComponentVoltageLevelInfos((Middle3WTNode) node, subComponentName);
                    styles.addAll(getNodeStyles(vlInfo, node));
                } else {
                    VoltageLevelInfos vlInfo = graph.getVoltageLevelInfos(node);
                    styles.addAll(getNodeStyles(vlInfo, node, getSide(subComponentName)));
                }
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
                styles.addAll(getNodeStyles(graph.getVoltageLevelInfos(feederNode), feederNode));
            }
        }

        return styles;
    }

    /**
     * Returns the list of styles to apply to the given node
     *
     * @param vlInfo the VoltageLevelInfos related to the given node
     * @param node   the node on which the styles if any are applied to
     * @return the list of node styles
     */
    public abstract List<String> getNodeStyles(VoltageLevelInfos vlInfo, Node node);

    public abstract List<String> getNodeStyles(VoltageLevelInfos vlInfo, Node node, NodeSide side);

    private Node getFeederNode(Middle3WTNode node, String subComponentName) {
        return switch (subComponentName) {
            case WINDING1, ARROW1 -> node.getAdjacentNode(Middle3WTNode.Winding.UPPER_LEFT);
            case WINDING2, ARROW2 -> node.getAdjacentNode(Middle3WTNode.Winding.UPPER_RIGHT);
            case WINDING3, ARROW3 -> node.getAdjacentNode(Middle3WTNode.Winding.DOWN);
            default -> throw new IllegalStateException("Unexpected subComponent name: " + subComponentName);
        };
    }

    private Node getFeederNode(Middle2WTNode node, String subComponentName) {
        return node.getAdjacentNodes().get(subComponentName.equals(WINDING1) ? 0 : 1);
    }

    protected VoltageLevelInfos getSubComponentVoltageLevelInfos(FeederWithSides feederWs, String subComponentName) {
        if (subComponentName.equals(WINDING2)) {
            return feederWs.getOtherSideVoltageLevelInfos();
        } else {
            return feederWs.getVoltageLevelInfos();
        }
    }

    protected NodeSide getSide(String subComponentName) {
        return subComponentName.equals(WINDING2) ? NodeSide.TWO : NodeSide.ONE;
    }

    protected VoltageLevelInfos getSubComponentVoltageLevelInfos(Middle3WTNode node, String subComponentName) {
        return switch (subComponentName) {
            case WINDING1, ARROW1 -> node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_LEFT);
            case WINDING2, ARROW2 -> node.getVoltageLevelInfos(Middle3WTNode.Winding.UPPER_RIGHT);
            case WINDING3, ARROW3 -> node.getVoltageLevelInfos(Middle3WTNode.Winding.DOWN);
            default -> null; // for decorators
        };
    }

    @Override
    public List<String> getBusInfoStyle(BusInfo info) {
        return Collections.emptyList();
    }
}
