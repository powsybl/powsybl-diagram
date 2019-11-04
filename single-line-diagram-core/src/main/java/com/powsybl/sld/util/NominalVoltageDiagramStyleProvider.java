/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

import java.util.Optional;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends DefaultDiagramStyleProvider {

    private static final String DEFAULT_COLOR = "rgb(171, 175, 40)";
    protected static final String WINDING1 = "WINDING1";
    protected static final String WINDING2 = "WINDING2";
    protected static final String WINDING3 = "WINDING3";

    private final Network network;

    public NominalVoltageDiagramStyleProvider(Network network) {
        this.network = network;
    }

    @Override
    public Optional<String> getColor(double nominalV) {
        String color;
        if (nominalV >= 300) {
            color = "rgb(255, 0, 0)";
        } else if (nominalV >= 170 && nominalV < 300) {
            color = "rgb(34, 139, 34)";
        } else if (nominalV >= 120 && nominalV < 170) {
            color = "rgb(1, 175, 175)";
        } else if (nominalV >= 70 && nominalV < 120) {
            color = "rgb(204, 85, 0)";
        } else if (nominalV >= 50 && nominalV < 70) {
            color = "rgb(160, 32, 240)";
        } else if (nominalV >= 30 && nominalV < 50) {
            color = "rgb(255, 130, 144)";
        } else {
            color = DEFAULT_COLOR;
        }
        return Optional.of(color);
    }

    @Override
    public Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication, boolean isShowInternalNodes) {
        Optional<String> defaultStyle = super.getNodeStyle(node, avoidSVGComponentsDuplication, isShowInternalNodes);

        String color = getColor(node.getGraph().getVoltageLevelNominalV()).orElse(DEFAULT_COLOR);
        if (node.getType() == Node.NodeType.SWITCH) {
            return defaultStyle;
        } else {
            return Optional.of(defaultStyle.orElse("") + " ." + escapeId(node.getId()) + " {stroke:" + color + ";}");
        }
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if ((node1 instanceof Fictitious3WTNode && node2 instanceof Feeder2WTNode) ||
                (node1 instanceof Feeder2WTNode && node2 instanceof Fictitious3WTNode)) {
            String vlId = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getVIdOtherSide()
                    : ((Feeder2WTNode) node2).getVIdOtherSide();
            return WIRE_STYLE_CLASS + "_" + escapeClassName(vlId);
        } else {
            return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevelId());
        }
    }

    @Override
    public Optional<String> getWireStyle(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if ((node1 instanceof Fictitious3WTNode && node2 instanceof Feeder2WTNode) ||
                (node1 instanceof Feeder2WTNode && node2 instanceof Fictitious3WTNode)) {
            String vlId = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getVIdOtherSide()
                    : ((Feeder2WTNode) node2).getVIdOtherSide();
            double nominalV = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getNominalVOtherSide()
                    : ((Feeder2WTNode) node2).getNominalVOtherSide();
            String color = getColor(nominalV).orElse(DEFAULT_COLOR);
            StringBuilder style = new StringBuilder();
            style.append(".").append(WIRE_STYLE_CLASS).append("_").append(escapeClassName(vlId)).append(" {stroke:").append(color).append(";stroke-width:1;}");
            return Optional.of(style.toString());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getNode3WTStyle(Fictitious3WTNode node, boolean rotateSVG, String vlId, String idWinding) {
        Optional<String> color = Optional.empty();

        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getTransformerId());
        ThreeWindingsTransformer.Side otherSide = ThreeWindingsTransformer.Side.ONE;

        if (idWinding.endsWith(WINDING1)) {
            if (transformer.getLeg1().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                otherSide = !rotateSVG ? ThreeWindingsTransformer.Side.THREE : ThreeWindingsTransformer.Side.TWO;
            } else if (transformer.getLeg2().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                otherSide = !rotateSVG ? ThreeWindingsTransformer.Side.THREE : ThreeWindingsTransformer.Side.ONE;
            } else if (transformer.getLeg3().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                otherSide = !rotateSVG ? ThreeWindingsTransformer.Side.TWO : ThreeWindingsTransformer.Side.ONE;
            }
            color = getColor(transformer.getTerminal(otherSide).getVoltageLevel().getNominalV());
        } else if (idWinding.endsWith(WINDING2)) {
            if (transformer.getLeg1().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                otherSide = !rotateSVG ? ThreeWindingsTransformer.Side.TWO : ThreeWindingsTransformer.Side.THREE;
            } else if (transformer.getLeg2().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                otherSide = !rotateSVG ? ThreeWindingsTransformer.Side.ONE : ThreeWindingsTransformer.Side.THREE;
            } else if (transformer.getLeg3().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                otherSide = !rotateSVG ? ThreeWindingsTransformer.Side.ONE : ThreeWindingsTransformer.Side.TWO;
            }
            color = getColor(transformer.getTerminal(otherSide).getVoltageLevel().getNominalV());
        } else {
            if (transformer.getLeg1().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                color = getColor(transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getNominalV());
            } else if (transformer.getLeg2().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                color = getColor(transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getNominalV());
            } else if (transformer.getLeg3().getTerminal().getVoltageLevel().getId().equals(vlId)) {
                color = getColor(transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel().getNominalV());
            }
        }

        return color;
    }

    @Override
    public Optional<String> getNode2WTStyle(Feeder2WTNode node, String idWinding) {
        return getColor(idWinding.endsWith(WINDING1)
                ? node.getGraph().getVoltageLevelNominalV()
                : node.getNominalVOtherSide());
    }
}
