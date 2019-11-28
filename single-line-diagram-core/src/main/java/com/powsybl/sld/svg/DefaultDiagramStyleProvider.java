/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static com.powsybl.sld.library.ComponentTypeName.INDUCTOR;
import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;

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

    protected final Network network;

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
        return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevelId());
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index) {
        return Optional.empty();
    }

    @Override
    public Map<String, String> getNodeSVGStyle(Node node, ComponentSize size, String nameSubComponent, boolean isShowInternalNodes) {
        Map<String, String> attributes = new HashMap<>();
        Optional<String> color = Optional.empty();
        Graph g = node.getGraph();

        if (g != null) {  // node inside a voltageLevel graph
            String vlId = g.getVoltageLevelId();

            if (node instanceof Fictitious3WTNode ||
                    (node instanceof Feeder2WTNode && node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER))) {
                // We will rotate the 3WT SVG, if cell orientation is BOTTOM
                boolean rotateSVG = node instanceof Fictitious3WTNode
                        && node.getCell() != null
                        && ((ExternCell) node.getCell()).getDirection() == BusCell.Direction.BOTTOM;

                if (node instanceof Fictitious3WTNode) {
                    color = network != null ? getColor3WT(((Fictitious3WTNode) node).getTransformerId(), nameSubComponent, rotateSVG, vlId) : Optional.empty();
                } else {
                    color = getColor(nameSubComponent.equals(WINDING1) ? node.getGraph().getVoltageLevelNominalV() : ((Feeder2WTNode) node).getNominalVOtherSide(), null);
                }

                color.ifPresent(s -> attributes.put(STROKE, s));

                if (rotateSVG) {  // SVG element rotation
                    attributes.put("transform", "rotate(" + 180. + "," + size.getWidth() / 2 + "," + size.getHeight() / 2 + ")");
                    // We store the rotation angle in order to transform correctly the anchor points when further drawing the edges
                    node.setRotationAngle(180.);
                }
            } else if (node instanceof Feeder2WTNode && node.getComponentType().equals(INDUCTOR)) {
                color = getColor(((Feeder2WTNode) node).getNominalVOtherSide(), null);
                color.ifPresent(s -> attributes.put(STROKE, s));
            } else if (!isShowInternalNodes && node.getComponentType().equals(NODE)) {
                attributes.put("stroke-opacity", "0");
                attributes.put("fill-opacity", "0");
            }
        } else {  // node outside any voltageLevel graph (multi-terminal node)
            List<Node> adjacentNodes = node.getAdjacentNodes();
            boolean rotateSVG = false;

            if (adjacentNodes.size() == 2) {
                Node n1 = adjacentNodes.get(0);
                Node n2 = adjacentNodes.get(1);

                double vNom1 = n1.getGraph().getVoltageLevelNominalV();
                double vNom2 = n2.getGraph().getVoltageLevelNominalV();

                // We will rotate the SVG, if node 1 cell orientation is different from node 2 cell orientation
                BusCell.Direction dir1 = n1.getCell() != null ? ((ExternCell) n1.getCell()).getDirection() : BusCell.Direction.UNDEFINED;
                BusCell.Direction dir2 = n2.getCell() != null ? ((ExternCell) n2.getCell()).getDirection() : BusCell.Direction.UNDEFINED;
                rotateSVG = dir1 != dir2;

                double vNom = 0;
                Node n = null;
                switch (nameSubComponent) {
                    case WINDING1:
                        vNom = vNom1;
                        n = rotateSVG ? n1 : n2;
                        break;
                    case WINDING2:
                        vNom = vNom2;
                        n = rotateSVG ? n2 : n1;
                        break;
                    default:
                }
                color = getColor(vNom, n);
            } else if (adjacentNodes.size() == 3) {
                Node n1 = adjacentNodes.get(0);
                Node n2 = adjacentNodes.get(1);
                Node n3 = adjacentNodes.get(2);

                // We will rotate the SVG, if cell orientation for node 2 is BOTTOM
                rotateSVG = n2.getCell() != null && ((ExternCell) n2.getCell()).getDirection() == BusCell.Direction.BOTTOM;

                double nominalV = 0;
                Node n = null;
                switch (nameSubComponent) {
                    case WINDING1:
                        nominalV = !rotateSVG ? n1.getGraph().getVoltageLevelNominalV() : n3.getGraph().getVoltageLevelNominalV();
                        n = n1;
                        break;
                    case WINDING2:
                        nominalV = !rotateSVG ? n3.getGraph().getVoltageLevelNominalV() : n1.getGraph().getVoltageLevelNominalV();
                        n = n2;
                        break;
                    case WINDING3:
                        nominalV = n2.getGraph().getVoltageLevelNominalV();
                        n = n3;
                        break;
                    default:
                }
                color = getColor(nominalV, n);
            }

            color.ifPresent(s -> attributes.put(STROKE, s));

            if (rotateSVG) {  // SVG element rotation
                double rotationValue = adjacentNodes.size() == 3 ? 180 : 90;

                attributes.put("transform", "rotate(" + rotationValue + "," + size.getWidth() / 2 + "," + size.getHeight() / 2 + ")");
                // We store the rotation angle in order to transform correctly the anchor points when further drawing the edges
                node.setRotationAngle(rotationValue);
            }
        }

        return attributes;
    }

    private Optional<String> getColor3WT(String transformerId, String nameSubComponent, boolean rotateSVG, String vlId) {
        Optional<String> color = Optional.empty();

        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(transformerId);
        ThreeWindingsTransformer.Side otherSide = ThreeWindingsTransformer.Side.ONE;

        VoltageLevel leg1Vl = transformer.getLeg1().getTerminal().getVoltageLevel();
        VoltageLevel leg2Vl = transformer.getLeg2().getTerminal().getVoltageLevel();
        VoltageLevel leg3Vl = transformer.getLeg3().getTerminal().getVoltageLevel();

        ThreeWindingsTransformer.Side side1 = ThreeWindingsTransformer.Side.ONE;
        ThreeWindingsTransformer.Side side2 = ThreeWindingsTransformer.Side.TWO;
        ThreeWindingsTransformer.Side side3 = ThreeWindingsTransformer.Side.THREE;

        if (nameSubComponent.equals(WINDING1)) {
            if (leg1Vl.getId().equals(vlId)) {
                otherSide = !rotateSVG ? side3 : side2;
            } else if (leg2Vl.getId().equals(vlId)) {
                otherSide = !rotateSVG ? side3 : side1;
            } else if (leg3Vl.getId().equals(vlId)) {
                otherSide = !rotateSVG ? side2 : side1;
            }
            color = getColor(transformer.getTerminal(otherSide).getVoltageLevel().getNominalV(), null);
        } else if (nameSubComponent.equals(WINDING2)) {
            if (leg1Vl.getId().equals(vlId)) {
                otherSide = !rotateSVG ? side2 : side3;
            } else if (leg2Vl.getId().equals(vlId)) {
                otherSide = !rotateSVG ? side1 : side3;
            } else if (leg3Vl.getId().equals(vlId)) {
                otherSide = !rotateSVG ? side1 : side2;
            }
            color = getColor(transformer.getTerminal(otherSide).getVoltageLevel().getNominalV(), null);
        } else {
            if (leg1Vl.getId().equals(vlId)) {
                color = getColor(leg1Vl.getNominalV(), null);
            } else if (leg2Vl.getId().equals(vlId)) {
                color = getColor(leg2Vl.getNominalV(), null);
            } else if (leg3Vl.getId().equals(vlId)) {
                color = getColor(leg3Vl.getNominalV(), null);
            }
        }

        return color;
    }

    @Override
    public Optional<String> getColor(double nominalV, Node node) {
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
