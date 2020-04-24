/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultDiagramStyleProvider implements DiagramStyleProvider {
    private static final String ARROW1 = ".ARROW1_";
    private static final String ARROW2 = ".ARROW2_";
    private static final String UP = "_UP";
    private static final String DOWN = "_DOWN";
    private static final String STROKE = "stroke";
    protected static final String WINDING1 = "WINDING1";
    protected static final String WINDING2 = "WINDING2";
    protected static final String WINDING3 = "WINDING3";

    @Override
    public Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication, boolean isShowInternalNodes) {
        Objects.requireNonNull(node);

        if (node.getComponentType().equals(NODE) && !isShowInternalNodes && !avoidSVGComponentsDuplication) {
            StringBuilder style = new StringBuilder();
            String className = escapeId(node.getId());
            style.append(".").append(className)
                    .append(" {stroke-opacity:0; fill-opacity:0; visibility: hidden;}");
            return Optional.of(style.toString());
        }
        if (node.getType() == Node.NodeType.SWITCH && !avoidSVGComponentsDuplication) {

            StringBuilder style = new StringBuilder();
            String className = escapeId(node.getId());
            style.append(".").append(className)
                    .append(" .open { visibility: ").append(node.isOpen() ? "visible;}" : "hidden;}");

            style.append(".").append(className)
                    .append(" .closed { visibility: ").append(node.isOpen() ? "hidden;}" : "visible;}");

            return Optional.of(style.toString());

        }
        if (node instanceof FeederNode && !avoidSVGComponentsDuplication) {
            StringBuilder style = new StringBuilder();
            style.append(ARROW1).append(escapeClassName(node.getId()))
                    .append(UP).append(" .arrow-up {stroke: black; fill: black; fill-opacity:1; visibility: visible;}");
            style.append(ARROW1).append(escapeClassName(node.getId()))
            .append(UP).append(" .arrow-down { stroke-opacity:0; fill-opacity:0; visibility: hidden;}");

            style.append(ARROW1).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-down {stroke: black; fill: black; fill-opacity:1;  visibility: visible;}");
            style.append(ARROW1).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-up { stroke-opacity:0; fill-opacity:0; visibility: hidden;}");

            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(UP).append(" .arrow-up {stroke: blue; fill: blue; fill-opacity:1; visibility: visible;}");
            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(UP).append(" .arrow-down { stroke-opacity:0; fill-opacity:0; visibility: hidden;}");

            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-down {stroke: blue; fill: blue; fill-opacity:1;  visibility: visible;}");
            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-up { stroke-opacity:0; fill-opacity:0; visibility: hidden;}");

            return Optional.of(style.toString());
        }
        return Optional.empty();
    }

    protected String getNodeColor(VoltageLevelInfos voltageLevelInfos, Node node) {
        return null;
    }

    private Pair<Node, VoltageLevelInfos> getVoltageLevelInfos(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if (node1 instanceof Middle3WTNode && node2 instanceof Feeder3WTLegNode) {
            return Pair.of(node2, ((Feeder3WTLegNode) node2).getOtherSideVoltageLevelInfos());
        } else if (node1 instanceof Feeder3WTLegNode && node2 instanceof Middle3WTNode) {
            return Pair.of(node1, ((Feeder3WTLegNode) node1).getOtherSideVoltageLevelInfos());
        } else {
            if (node1.getGraph() != null) {
                return Pair.of(node1, node1.getGraph().getVoltageLevelInfos());
            } else {
                return Pair.of(node2, node2.getGraph().getVoltageLevelInfos());
            }
        }
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        VoltageLevelInfos voltageLevelInfos = getVoltageLevelInfos(edge).getRight();
        return WIRE_STYLE_CLASS + "_" + escapeClassName(voltageLevelInfos.getId());
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index) {
        Pair<Node, VoltageLevelInfos> p = getVoltageLevelInfos(edge);
        Node node = p.getLeft();
        VoltageLevelInfos voltageLevelInfos = p.getRight();
        String color = getNodeColor(voltageLevelInfos, node);
        String style = "." + WIRE_STYLE_CLASS + "_" + escapeClassName(voltageLevelInfos.getId()) +
                " {stroke:" + color + ";stroke-width:1;}";
        return Optional.of(style);
    }

    @Override
    public Map<String, String> getNodeSVGStyle(Node node, ComponentSize size, String subComponentName, boolean isShowInternalNodes) {
        Map<String, String> attributes = new HashMap<>();
        String color = null;

        Graph g = node.getGraph();
        if (g != null) {  // node inside a voltageLevel graph
            if (node.getType() != Node.NodeType.SWITCH) { // we don't color switches
                if (node instanceof Middle3WTNode) {
                    String vlId = g.getVoltageLevelInfos().getId();
                    color = get3WTColor((Middle3WTNode) node, subComponentName, vlId);
                } else if (node instanceof Feeder2WTNode) {
                    if (subComponentName.equals(WINDING2)) {
                        color = getNodeColor(((Feeder2WTNode) node).getOtherSideVoltageLevelInfos(), node);
                    }
                } else if (!isShowInternalNodes && node.getComponentType().equals(NODE)) {
                    attributes.put("stroke-opacity", "0");
                    attributes.put("fill-opacity", "0");
                }

                if (color == null) {
                    color = getNodeColor(node.getGraph().getVoltageLevelInfos(), node);
                }
            }
        } else {  // node outside any voltageLevel graph (multi-terminal node)
            List<Node> adjacentNodes = node.getAdjacentNodes();
            if (adjacentNodes.size() == 2) {  // 2 windings transformer
                // if node is not rotated, subComponent WINDING1 is linked to the adjacent node with min x value
                // if node is rotated, subComponent WINDING1 is linked to the adjacent node with min y value
                adjacentNodes.sort(Comparator.comparingDouble(!node.isRotated() ? Node::getX : Node::getY));
                Node n1 = adjacentNodes.get(0);
                Node n2 = adjacentNodes.get(1);

                color = getNodeColor(subComponentName.equals(WINDING1) ? n1.getGraph().getVoltageLevelInfos() : n2.getGraph().getVoltageLevelInfos(),
                                 subComponentName.equals(WINDING1) ? n1 : n2);
            } else if (adjacentNodes.size() == 3) {  // 3 windings transformer
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
                color = getNodeColor(n.getGraph().getVoltageLevelInfos(), n);
            }
        }

        if (color != null) {
            attributes.put("fill", color);
            attributes.put(STROKE, color);
        }

        return attributes;
    }

    private String get3WTColor(Middle3WTNode node, String subComponentName, String vlId) {
        VoltageLevelInfos voltageLevelInfosLeg1 = node.getVoltageLevelInfosLeg1();
        VoltageLevelInfos voltageLevelInfosLeg2 = node.getVoltageLevelInfosLeg2();
        VoltageLevelInfos voltageLevelInfosLeg3 = node.getVoltageLevelInfosLeg3();

        VoltageLevelInfos voltageLevelInfos = voltageLevelInfosLeg1;

        if (subComponentName.equals(WINDING1)) {
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg2;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg1;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg2 : voltageLevelInfosLeg1;
            }
        } else if (subComponentName.equals(WINDING2)) {
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg2 : voltageLevelInfosLeg3;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg1 : voltageLevelInfosLeg3;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg1 : voltageLevelInfosLeg2;
            }
        } else {
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                voltageLevelInfos = voltageLevelInfosLeg1;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                voltageLevelInfos = voltageLevelInfosLeg2;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                voltageLevelInfos = voltageLevelInfosLeg3;
            }
        }

        return getNodeColor(voltageLevelInfos, node);
    }

    public Map<String, String> getAttributesArrow(int num) {
        Map<String, String> ret = new HashMap<>();
        ret.put(STROKE, num == 1 ? "black" : "blue");
        ret.put("fill", num == 1 ? "black" : "blue");
        ret.put("fill-opacity", "1");
        return ret;
    }

    @Override
    public void reset() {
        // Nothing to reset for this implementation
    }
}
