/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.color.BaseVoltageStyle;
import com.powsybl.sld.model.*;

import java.util.*;

import static com.powsybl.sld.svg.DiagramStyles.HIDDEN_INTERNAL_NODE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultDiagramStyleProvider implements DiagramStyleProvider {
    protected static final String WINDING1 = "WINDING1";
    protected static final String WINDING2 = "WINDING2";
    protected static final String WINDING3 = "WINDING3";

    private static final String PROFILE = "Default";

    protected final BaseVoltageStyle baseVoltageStyle;

    public DefaultDiagramStyleProvider() {
        this(BaseVoltageStyle.fromPlatformConfig());
    }

    public DefaultDiagramStyleProvider(BaseVoltageStyle baseVoltageStyle) {
        this.baseVoltageStyle = Objects.requireNonNull(baseVoltageStyle);
    }

    @Override
    public String getCssAdditionalInlineStyle() {
        return "";
    }

    @Override
    public List<String> getSvgWireStyles(Edge edge, boolean highlightLineState) {
        List<String> styles = new ArrayList<>();
        styles.add(WIRE_STYLE_CLASS);
        styles.add(getEdgeStyle(edge));
        if (highlightLineState) {
            String highlightLineStateStyle = getHighlightLineStateStyle(edge);
            if (!highlightLineStateStyle.isEmpty()) {
                styles.add(highlightLineStateStyle);
            }
        }
        return styles;
    }

    protected String getEdgeStyle(Edge edge) {
        Node nodeForStyle = edge.getNode1().getVoltageLevelInfos() != null ? edge.getNode1() : edge.getNode2();
        return getVoltageLevelNodeStyle(nodeForStyle.getVoltageLevelInfos(), nodeForStyle);
    }

    protected String getHighlightLineStateStyle(Edge edge) {
        return "";
    }

    @Override
    public List<String> getSvgNodeStyles(Node node, boolean showInternalNodes) {

        List<String> styles = new ArrayList<>();
        styles.add(getNodeDiagramStyle(node));

        if (!showInternalNodes && node instanceof InternalNode) {
            styles.add(HIDDEN_INTERNAL_NODE_CLASS);
        }
        if (node.getType() == Node.NodeType.SWITCH) {
            styles.add(node.isOpen() ? DiagramStyles.OPEN_SWITCH_STYLE_CLASS : DiagramStyles.CLOSED_SWITCH_STYLE_CLASS);
        }

        Graph g = node.getGraph();
        if (g != null) {  // node inside a voltageLevel graph
            // Middle3WTNode and Feeder2WTNode have style depending on their subcomponents -> see getSvgNodeSubcomponentStyles
            if (!(node instanceof Middle3WTNode) && !(node instanceof Feeder2WTNode)) {
                styles.add(getVoltageLevelNodeStyle(g.getVoltageLevelInfos(), node));
            }
        }
        // Nothing is done for nodes outside any voltageLevel graph (multi-terminal node),
        // indeed they have subcomponents with specific styles -> see getSvgNodeSubcomponentStyles

        return styles;
    }

    private String getNodeDiagramStyle(Node node) {
        String componentType = node.getComponentType();
        return componentType.toLowerCase().replace('_', '-'); //TODO: Add style info to Component class / xml
    }

    @Override
    public List<String> getSvgNodeSubcomponentStyles(Node node, String subComponentName) {

        List<String> styles = new ArrayList<>();

        boolean node3WT = node instanceof Middle3WTNode;
        boolean node2WT = node instanceof Feeder2WTNode;
        if (node2WT || node3WT) {
            VoltageLevelInfos vlInfo;
            Graph g = node.getGraph();
            if (g != null) {  // node inside a voltageLevel graph
                VoltageLevelInfos currentVoltageLevel = g.getVoltageLevelInfos();
                if (node2WT) {
                    vlInfo = subComponentName.equals(WINDING1) ? currentVoltageLevel : ((Feeder2WTNode) node).getOtherSideVoltageLevelInfos();
                } else {
                    vlInfo = get3WTNodeVoltageLevelInfos((Middle3WTNode) node, subComponentName, currentVoltageLevel.getId());
                }
            } else {  // node outside any voltageLevel graph (multi-terminal node)
                List<Node> adjacentNodes = node.getAdjacentNodes();
                if (node2WT) {
                    vlInfo = getMultiTerminal2WTVoltageLevelInfos(node, subComponentName, adjacentNodes);
                } else {
                    vlInfo = getMultiTerminal3WTVoltageLevelInfos(node, subComponentName, adjacentNodes);
                }
            }
            styles.add(getVoltageLevelNodeStyle(vlInfo, node));
        }

        return styles;
    }

    public String getVoltageLevelNodeStyle(VoltageLevelInfos vlInfo, Node node) {
        return baseVoltageStyle.getBaseVoltageName(vlInfo.getNominalVoltage(), PROFILE).orElse(vlInfo.getName());
    }

    private VoltageLevelInfos getMultiTerminal3WTVoltageLevelInfos(Node node, String subComponentName, List<Node> adjacentNodes) {
        adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
        Node n1 = adjacentNodes.get(0);
        Node n2 = adjacentNodes.get(1);
        Node n3 = adjacentNodes.get(2);

        Node n = n1;
        switch (subComponentName) {
            case WINDING1:
                n = !node.isRotated() ? n1 : n3;
                break;
            case WINDING2:
                n = !node.isRotated() ? n3 : n1;
                break;
            case WINDING3:
                n = n2;
                break;
            default:
        }

        return n.getGraph().getVoltageLevelInfos();
    }

    private VoltageLevelInfos getMultiTerminal2WTVoltageLevelInfos(Node node, String subComponentName, List<Node> adjacentNodes) {
        adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
        FeederWithSideNode node1 = (FeederWithSideNode) adjacentNodes.get(0);
        FeederWithSideNode node2 = (FeederWithSideNode) adjacentNodes.get(1);
        FeederWithSideNode nodeWinding1 = node1.getSide() == FeederWithSideNode.Side.ONE ? node1 : node2;
        FeederWithSideNode nodeWinding2 = node1.getSide() == FeederWithSideNode.Side.TWO ? node1 : node2;
        FeederWithSideNode nodeWinding = nodeWinding1;

        if (subComponentName.equals(WINDING1)) {
            if (!node.isRotated()) {
                nodeWinding = nodeWinding1.getY() > nodeWinding2.getY() ? nodeWinding1 : nodeWinding2;
            } else if (node.getRotationAngle() == 90.) {
                nodeWinding = nodeWinding1.getX() > nodeWinding2.getX() ? nodeWinding2 : nodeWinding1;
            } else if (node.getRotationAngle() == 180.) {
                nodeWinding = nodeWinding1.getY() > nodeWinding2.getY() ? nodeWinding2 : nodeWinding1;
            } else if (node.getRotationAngle() == 270.) {
                nodeWinding = nodeWinding1.getX() > nodeWinding2.getX() ? nodeWinding1 : nodeWinding2;
            }
        } else if (subComponentName.equals(WINDING2)) {
            if (!node.isRotated()) {
                nodeWinding = nodeWinding1.getY() > nodeWinding2.getY() ? nodeWinding2 : nodeWinding1;
            } else if (node.getRotationAngle() == 90.) {
                nodeWinding = nodeWinding1.getX() > nodeWinding2.getX() ? nodeWinding1 : nodeWinding2;
            } else if (node.getRotationAngle() == 180.) {
                nodeWinding = nodeWinding1.getY() > nodeWinding2.getY() ? nodeWinding1 : nodeWinding2;
            } else if (node.getRotationAngle() == 270.) {
                nodeWinding = nodeWinding1.getX() > nodeWinding2.getX() ? nodeWinding2 : nodeWinding1;
            }
        }

        return nodeWinding.getGraph().getVoltageLevelInfos();
    }

    private VoltageLevelInfos get3WTNodeVoltageLevelInfos(Middle3WTNode node, String subComponentName, String vlId) {
        VoltageLevelInfos voltageLevelInfosLeg1 = node.getVoltageLevelInfosLeg1();
        VoltageLevelInfos voltageLevelInfosLeg2 = node.getVoltageLevelInfosLeg2();
        VoltageLevelInfos voltageLevelInfosLeg3 = node.getVoltageLevelInfosLeg3();

        VoltageLevelInfos voltageLevelInfos = voltageLevelInfosLeg1;

        // A three windings transformer is represented by 3 circles identified by winding1, winding2 and winding3
        // winding1 and winding2 are horizontally aligned and winding3 is displayed below the others
        // But, if node is rotated (by 180 degrees), winding3 is displayed above the others and winding1 and winding2 are permuted

        if (subComponentName.equals(WINDING1)) {
            // colorizing winding1
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg1, winding1 corresponds to transformer leg2 or leg3, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg2 : voltageLevelInfosLeg3;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg2, winding1 corresponds to transformer leg1 or leg3, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg1 : voltageLevelInfosLeg3;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg3, winding1 corresponds to transformer leg1 or leg2, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg1 : voltageLevelInfosLeg2;
            }
        } else if (subComponentName.equals(WINDING2)) {
            // colorizing winding2
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg1, winding2 corresponds to transformer leg3 or leg2, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg2;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg2, winding2 corresponds to transformer leg3 or leg1, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg1;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg3, winding2 corresponds to transformer leg2 or leg1, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg2 : voltageLevelInfosLeg1;
            }
        } else {
            // colorizing winding3 : this winding always correspond to the leg of the voltage level displayed
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg1, winding3 corresponds to this transformer leg
                voltageLevelInfos = voltageLevelInfosLeg1;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg2, winding3 corresponds to this transformer leg
                voltageLevelInfos = voltageLevelInfosLeg2;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg3, winding3 corresponds to this transformer leg
                voltageLevelInfos = voltageLevelInfosLeg3;
            }
        }

        return voltageLevelInfos;
    }

    @Override
    public void reset() {
        // Nothing to reset for this implementation
    }
}
