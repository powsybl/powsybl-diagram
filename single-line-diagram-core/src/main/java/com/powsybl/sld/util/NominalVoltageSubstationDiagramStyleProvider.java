/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.DefaultSubstationDiagramStyleProvider;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.powsybl.sld.svg.SubstationDiagramStyles.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageSubstationDiagramStyleProvider extends DefaultSubstationDiagramStyleProvider {

    private static final String DEFAULT_COLOR = "rgb(171, 175, 40)";

    @Override
    public Optional<String> getColor(VoltageLevel vl) {
        String color;
        if (vl.getNominalV() >= 300) {
            color = "rgb(255, 0, 0)";
        } else if (vl.getNominalV() >= 170 && vl.getNominalV() < 300) {
            color = "rgb(34, 139, 34)";
        } else if (vl.getNominalV() >= 120 && vl.getNominalV() < 170) {
            color = "rgb(1, 175, 175)";
        } else if (vl.getNominalV() >= 70 && vl.getNominalV() < 120) {
            color = "rgb(204, 85, 0)";
        } else if (vl.getNominalV() >= 50 && vl.getNominalV() < 70) {
            color = "rgb(160, 32, 240)";
        } else if (vl.getNominalV() >= 30 && vl.getNominalV() < 50) {
            color = "rgb(255, 130, 144)";
        } else {
            color = DEFAULT_COLOR;
        }
        return Optional.of(color);
    }

    @Override
    public Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication) {
        Optional<String> defaultStyle = super.getNodeStyle(node, avoidSVGComponentsDuplication);

        String color = getColor(node.getGraph().getVoltageLevel()).orElse(DEFAULT_COLOR);
        try {
            if (node.getType() == Node.NodeType.SWITCH) {
                return defaultStyle;
            } else {
                return Optional.of(defaultStyle.orElse("") + " ." + escapeId(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())) + " {stroke:" + color + ";}");
            }
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if ((node1 instanceof Fictitious3WTNode && node2 instanceof Feeder2WTNode) ||
                (node1 instanceof Feeder2WTNode && node2 instanceof Fictitious3WTNode)) {
            VoltageLevel vl = node1 instanceof Feeder2WTNode ? ((Feeder2WTNode) node1).getVlOtherSide() : ((Feeder2WTNode) node2).getVlOtherSide();
            return WIRE_STYLE_CLASS + "_" + escapeClassName(vl.getId());
        } else {
            return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevel().getId());
        }
    }

    @Override
    public Optional<String> getWireStyle(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        VoltageLevel vl;
        if ((node1 instanceof Fictitious3WTNode && node2 instanceof Feeder2WTNode) ||
                (node1 instanceof Feeder2WTNode && node2 instanceof Fictitious3WTNode)) {
            vl = node1 instanceof Feeder2WTNode ? ((Feeder2WTNode) node1).getVlOtherSide() : ((Feeder2WTNode) node2).getVlOtherSide();
        } else {
            vl = edge.getNode1().getGraph().getVoltageLevel();
        }
        String idVL = escapeClassName(vl.getId());
        String color = getColor(vl).orElse(DEFAULT_COLOR);
        StringBuilder style = new StringBuilder();
        style.append(".").append(WIRE_STYLE_CLASS).append("_").append(idVL).append(" {stroke:").append(color).append(";stroke-width:1;}");
        return Optional.of(style.toString());
    }

    @Override
    public Optional<String> getNode3WTStyle(Fictitious3WTNode node, ThreeWindingsTransformer.Side side) {
        return getColor(node.getTransformer().getTerminal(side).getVoltageLevel());
    }

    @Override
    public Optional<String> getNode2WTStyle(Feeder2WTNode node, TwoWindingsTransformer.Side side) {
        return getColor(side == TwoWindingsTransformer.Side.ONE ? node.getGraph().getVoltageLevel() : node.getVlOtherSide());
    }
}
