/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

import java.awt.Color;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.*;

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
    protected static final String BLACK_COLOR = "black";
    protected static final Color DEFAULT_COLOR = new Color(200, 0, 0);
    protected static final String STROKE_DASHARRAY = "stroke-dasharray:3,3";

    protected final Network network;

    public DefaultDiagramStyleProvider() {
        network = null;
    }

    public DefaultDiagramStyleProvider(Network network) {
        this.network = network;
    }

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

    @Override
    public String getIdWireStyle(Edge edge) {
        return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevelInfos().getId());
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

    protected Optional<String> buildWireStyle(Edge edge, String id, int index, String color) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        if (n1 instanceof FeederWithSideNode || n2 instanceof FeederWithSideNode) {
            String wireId = DiagramStyles.escapeId(id + "_Wire" + index);

            FeederWithSideNode n = n1 instanceof FeederWithSideNode ? (FeederWithSideNode) n1 : (FeederWithSideNode) n2;
            Map<FeederWithSideNode.Side, Boolean> connectionStatus = connectionStatus(n);
            FeederWithSideNode.Side side = null;
            FeederWithSideNode.Side otherSide = null;

            if (n.getFeederType() == FeederType.BRANCH || n.getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
                side = n.getSide();
                otherSide = side == FeederWithSideNode.Side.ONE ? FeederWithSideNode.Side.TWO : FeederWithSideNode.Side.ONE;
            } else if (n.getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
                String idVl = n.getGraph().getVoltageLevelInfos().getId();
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
                if (!connectionStatus.get(side) && !connectionStatus.get(otherSide)) {  // disconnected on both ends
                    return Optional.of(" #" + wireId + " {stroke:" + BLACK_COLOR + ";stroke-width:1}");
                } else if (connectionStatus.get(side) && !connectionStatus.get(otherSide)) {  // connected on side and disconnected on other side
                    return Optional.of(" #" + wireId + " {stroke:" + color + ";stroke-width:1;" + STROKE_DASHARRAY + "}");
                } else if (!connectionStatus.get(side) && connectionStatus.get(otherSide)) {  // disconnected on side and connected on other side
                    return Optional.of(" #" + wireId + " {stroke:" + BLACK_COLOR + ";stroke-width:1;" + STROKE_DASHARRAY + "}");
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index, boolean isIndicateOpenLines) {
        if (isIndicateOpenLines && network != null) {
            return buildWireStyle(edge, id, index, DEFAULT_COLOR.toString());
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String> getNodeSVGStyle(Node node, ComponentSize size, String nameSubComponent, boolean isShowInternalNodes) {
        Map<String, String> attributes = new HashMap<>();
        Optional<String> color = Optional.empty();
        Graph g = node.getGraph();

        if (g != null) {  // node inside a voltageLevel graph
            String vlId = g.getVoltageLevelInfos().getId();

            if (node instanceof Middle3WTNode) {
                color = getColorFictitious3WTNode((Middle3WTNode) node, nameSubComponent, vlId);
            } else if (node instanceof Feeder2WTNode) {
                if (nameSubComponent.equals(WINDING1)) {
                    color = getColor(node.getGraph().getVoltageLevelInfos().getNominalVoltage(), null);
                } else if (nameSubComponent.equals(WINDING2)) {
                    color = getColor(((Feeder2WTNode) node).getOtherSideVoltageLevelInfos().getNominalVoltage(), null);
                } else {
                    // phase shifter case
                    color = getColor(node.getGraph().getVoltageLevelInfos().getNominalVoltage(), null);
                }
            } else if (!isShowInternalNodes && node.getComponentType().equals(NODE)) {
                attributes.put("stroke-opacity", "0");
                attributes.put("fill-opacity", "0");
            }
        } else {  // node outside any voltageLevel graph (multi-terminal node)
            List<Node> adjacentNodes = node.getAdjacentNodes();
            if (adjacentNodes.size() == 2) {  // 2 windings transformer
                // if node is not rotated, subComponent WINDING1 is linked to the adjacent node with min x value
                // if node is rotated, subComponent WINDING1 is linked to the adjacent node with min y value
                adjacentNodes.sort(Comparator.comparingDouble(!node.isRotated() ? Node::getX : Node::getY));
                Node n1 = adjacentNodes.get(0);
                Node n2 = adjacentNodes.get(1);

                double vNom1 = n1.getGraph().getVoltageLevelInfos().getNominalVoltage();
                double vNom2 = n2.getGraph().getVoltageLevelInfos().getNominalVoltage();

                color = getColor(nameSubComponent.equals(WINDING1) ? vNom1 : vNom2,
                                 nameSubComponent.equals(WINDING1) ? n1 : n2);
            } else if (adjacentNodes.size() == 3) {  // 3 windings transformer
                adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
                Node n1 = adjacentNodes.get(0);
                Node n2 = adjacentNodes.get(1);
                Node n3 = adjacentNodes.get(2);

                Node n = n1;
                switch (nameSubComponent) {
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
                color = getColor(n.getGraph().getVoltageLevelInfos().getNominalVoltage(), n);
            }
        }

        color.ifPresent(s -> attributes.put(STROKE, s));

        return attributes;
    }

    private Optional<String> getColorFictitious3WTNode(Middle3WTNode node, String nameSubComponent, String vlId) {
        VoltageLevelInfos voltageLevelInfosLeg1 = node.getVoltageLevelInfosLeg1();
        VoltageLevelInfos voltageLevelInfosLeg2 = node.getVoltageLevelInfosLeg2();
        VoltageLevelInfos voltageLevelInfosLeg3 = node.getVoltageLevelInfosLeg3();

        VoltageLevelInfos voltageLevelInfos = voltageLevelInfosLeg1;

        if (nameSubComponent.equals(WINDING1)) {
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg2;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg1;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg2 : voltageLevelInfosLeg1;
            }
        } else if (nameSubComponent.equals(WINDING2)) {
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

        return getColor(voltageLevelInfos.getNominalVoltage(), null);
    }

    protected Optional<String> getColor(double nominalV, Node node) {
        return Optional.empty();
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
