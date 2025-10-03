/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.library.SldComponentTypeName;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.SvgParameters;

import java.util.*;

import static com.powsybl.sld.library.SldComponentTypeName.BUS_CONNECTION;

/**
 * Enables the customization of the style of SLD elements connected to a bus.
 *
 * <p>
 * Custom SLD style data is defined in the constructor's map parameter.
 * The customStyles map is indexed by the bus ID and defines the style for the elements connected to the bus.
 * In the map, the custom style is declared in a CustomStyle record: color, width for the bus, width for the connected edges and elements, and a dash pattern.
 *
 * <p>
 * Note that busWidth and width are strings, and can be specified in pixel (e.g, 4px).
 * A dash pattern is a string with a sequence of comma and/or white space separated lengths and percentages, that specify the lengths of alternating dashes and gaps in the edge.
 * Through the optional componentTypesToSkip parameter it is possible to override the list of elements that are not affected by the custom style;
 * By default, switches are not affected by the custom style.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomTopologicalStyleProvider extends TopologicalStyleProvider {
    static final List<String> DEFAULT_COMPONENT_TYPES_TO_SKIP = List.of(
            SldComponentTypeName.BREAKER,
            SldComponentTypeName.DISCONNECTOR,
            SldComponentTypeName.LOAD_BREAK_SWITCH);

    public record CustomStyle(String color, String busWidth, String width, String dash) {
    }

    final Map<String, CustomStyle> customStyles;

    Set<String> componentTypesToSkip;

    public CustomTopologicalStyleProvider(Network network, SvgParameters svgParameters, Map<String, CustomStyle> customStyles) {
        this(BaseVoltagesConfig.fromPlatformConfig(), network, svgParameters, customStyles, DEFAULT_COMPONENT_TYPES_TO_SKIP);
    }

    public CustomTopologicalStyleProvider(BaseVoltagesConfig baseVoltagesConfig, Network network, SvgParameters svgParameters,
                                          Map<String, CustomStyle> customStyles, List<String> componentTypesToSkip) {
        super(baseVoltagesConfig, network, svgParameters);
        this.customStyles = Objects.requireNonNull(customStyles);
        this.componentTypesToSkip = new HashSet<>(componentTypesToSkip);
    }

    private CustomStyle getColorsForNodes(Map<String, CustomStyle> nodeColors, Set<Node> connectedNodes) {
        return connectedNodes.stream()
                .filter(node -> !componentTypesToSkip.contains(node.getComponentType()))
                .map(Node::getId)
                .filter(nodeColors::containsKey)
                .findFirst()
                .map(nodeColors::get)
                .orElse(null);
    }

    private String formatStyle(String color, String width, String dash, boolean enableFill) {
        List<String> parts = new ArrayList<>();
        if (color != null) {
            parts.add(String.format("stroke: %s;", color));
            if (enableFill) {
                parts.add(String.format("fill: %s;", color));
            }
        }
        if (width != null) {
            parts.add(String.format("stroke-width: %s;", width));
        }

        if (dash != null) {
            parts.add(String.format("stroke-dasharray:%s;", dash));
        }

        return parts.isEmpty() ? null : String.join(" ", parts);
    }

    private String formatStyleForBus(CustomStyle customStyle) {
        if (customStyle == null) {
            return formatStyle(null, null, null, false);
        } else {
            return formatStyle(customStyle.color(), customStyle.busWidth(), customStyle.dash(), false);
        }
    }

    private String formatStyle(CustomStyle customStyle, boolean enableFill) {
        if (customStyle == null) {
            return formatStyle(null, null, null, enableFill);
        } else {
            return formatStyle(customStyle.color(), customStyle.width(), customStyle.dash(), enableFill);
        }
    }

    private String formatStyle(CustomStyle customStyle) {
        return formatStyle(customStyle, false);
    }

    @Override
    public String getBusNodeStyle(BusNode busNode) {
        Set<Node> connectedNodes = getConnectedNodesSet(busNode);
        CustomStyle cStyle = getColorsForNodes(customStyles, connectedNodes);
        return formatStyleForBus(cStyle);
    }

    @Override
    public String getNodeStyle(VoltageLevelGraph graph, Node node, SldComponentLibrary componentLibrary, boolean showInternalNodes) {
        if (componentTypesToSkip.contains(node.getComponentType())) {
            return null;
        }
        Set<Node> connectedNodes = getConnectedNodesSet(node);
        CustomStyle cStyle = getColorsForNodes(customStyles, connectedNodes);
        return formatStyle(cStyle, node.getComponentType().equals(BUS_CONNECTION));
    }

    @Override
    public String getEdgeStyle(Graph graph, Edge edge) {
        Set<Node> connectedNodes = getConnectedNodesSet(edge.getNodes());
        CustomStyle cStyle = getColorsForNodes(customStyles, connectedNodes);
        return formatStyle(cStyle);
    }

    private Set<Node> getConnectedNodesSet(Node node) {
        Set<Node> connectedNodes = new LinkedHashSet<>();
        findConnectedNodes(node, connectedNodes);
        return connectedNodes;
    }

    private Set<Node> getConnectedNodesSet(List<Node> nodeList) {
        Set<Node> connectedNodes = new LinkedHashSet<>();
        nodeList.forEach(n -> findConnectedNodes(n, connectedNodes));
        return connectedNodes;
    }

    private Set<Node> getConnectedNodeSet(Node node, String subComponentName) {
        Set<Node> connectedNodes = Set.of();
        if (node instanceof FeederNode feederNode) {
            Feeder feeder = feederNode.getFeeder();

            if (feeder instanceof FeederWithSides) {
                NodeSide side = getSide(subComponentName);
                List<Node> adjNodes = node.getAdjacentNodes();
                int intPos = side.getIntValue();
                if (intPos <= adjNodes.size()) {
                    connectedNodes = getConnectedNodesSet(adjNodes);
                }
            }
        } else if (node instanceof Middle3WTNode) {
            if (WINDING3.equals(subComponentName) || ARROW3.equals(subComponentName)) {
                connectedNodes = getConnectedNodesSet(node.getAdjacentNodes());
            }
        } else {
            Node sideNode = node.getAdjacentNodes().get(getSide(subComponentName).getIntValue() - 1);
            connectedNodes = getConnectedNodesSet(sideNode);
        }
        return connectedNodes;
    }

    @Override
    public String getNodeSubcomponentStyle(Graph graph, Node node, String subComponentName) {
        if (componentTypesToSkip.contains(node.getComponentType())) {
            return null;
        }

        Set<Node> connectedNodes = Set.of();

        VoltageLevelGraph g = graph.getVoltageLevelGraph(node);

        if (g != null) {
            if (isNodeSeparatingStyles(node)) {
                connectedNodes = getConnectedNodeSet(node, subComponentName);
            }
        } else {
            // node outside voltageLevel graph
            Node feederNode = null;
            if (node instanceof Middle2WTNode m2Node) {
                feederNode = getFeederNode(m2Node, subComponentName);
            } else if (node instanceof Middle3WTNode m3Node) {
                feederNode = getFeederNode(m3Node, subComponentName);
            }
            if (feederNode != null) {
                connectedNodes = getConnectedNodesSet(feederNode);
            }
        }

        CustomStyle cStyle = getColorsForNodes(customStyles, connectedNodes);
        return formatStyle(cStyle);
    }
}
