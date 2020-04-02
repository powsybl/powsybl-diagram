/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    private Node getVoltageLevelSideNode(Edge edge) {
        Node node;
        if (edge.getNode1() instanceof StarNode) {
            node = edge.getNode2();
        } else {
            node = edge.getNode1();
        }
        return node;
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        return WIRE_STYLE_CLASS + "_" + escapeClassName(getVoltageLevelSideNode(edge).getVoltageLevelInfos().getId());
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index) {
        Node node = getVoltageLevelSideNode(edge);

        String color = getColor(node, node.getVoltageLevelInfos());

        String style = "." + WIRE_STYLE_CLASS + "_" + escapeClassName(node.getVoltageLevelInfos().getId()) +
                " {stroke:" + color + ";stroke-width:1;}";
        return Optional.of(style);
    }

    @Override
    public Map<String, String> getNodeSVGStyle(Node node, ComponentSize size, String subComponentName, boolean isShowInternalNodes) {
        Map<String, String> attributes = new HashMap<>();

        String color = null;

        if (node instanceof StarNode) {
            FeederNode.Side side = getSide(node, subComponentName);
            System.out.println(side);
            // find the adjacent leg node that match the side
            for (Edge edge : node.getAdjacentEdges()) {
                Node otherNode = edge.getNode1() == node ? edge.getNode2() : edge.getNode1();
                if (otherNode instanceof FeederLegNode) {
                    FeederLegNode legNode = ((FeederLegNode) otherNode);
                    if (legNode.getSide() == side) {
                        color = getColor(legNode, legNode.getVoltageLevelInfos());
                        break;
                    }
                }
            }

            // if no adjacent leg node has been found, it is the side connected to the voltage level
//            if (color == null ) {
//                color = getColor(node, node.getVoltageLevelInfos());
//            }
        } else if (node instanceof Feeder2wtNode) {
            if (subComponentName.equals("WINDING2")) {
                color = getColor(node, ((Feeder2wtNode) node).getOtherSideVoltageLevelInfos());
            } else {
                color = getColor(node, node.getVoltageLevelInfos());
            }
        } else {
            color = getColor(node, node.getVoltageLevelInfos());

            if (!isShowInternalNodes && node.getComponentType().equals(NODE)) {
                attributes.put("stroke-opacity", "0");
                attributes.put("fill-opacity", "0");
            }
        }

        if (color != null) {
            attributes.put(STROKE, color);
        }

        return attributes;
    }

    private FeederNode.Side getSide(Node node, String subComponentName) {
        FeederNode.Side side;
        switch (subComponentName) {
            case WINDING1:
                side = FeederNode.Side.ONE;
                break;

            case WINDING2:
                side = FeederNode.Side.TWO;
                break;

            case WINDING3:
                side = FeederNode.Side.THREE;
                break;

            default:
                return null;
        }
        return node.isRotated() ? FeederNode.Side.values()[(side.ordinal() + 1) % 3] : side;
    }

    protected String getColor(Node node, VoltageLevelInfos voltageLevelInfos) {
        return null;
    }

    @Override
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
