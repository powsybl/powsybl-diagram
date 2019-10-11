/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.model.*;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultDiagramStyleProvider implements DiagramStyleProvider {

    private static final String ARROW1 = ".ARROW1_";
    private static final String ARROW2 = ".ARROW2_";
    private static final String UP = "_UP";
    private static final String DOWN = "_DOWN";

    @Override
    public Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication) {
        Objects.requireNonNull(node);
        if (node.getType() == Node.NodeType.SWITCH && !avoidSVGComponentsDuplication) {
            try {
                StringBuilder style = new StringBuilder();
                String className = escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name()));
                style.append(".").append(className)
                        .append(" .open { visibility: ").append(node.isOpen() ? "visible;}" : "hidden;}");

                style.append(".").append(className)
                        .append(" .closed { visibility: ").append(node.isOpen() ? "hidden;}" : "visible;}");

                return Optional.of(style.toString());
            } catch (UnsupportedEncodingException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (node instanceof FeederNode && !avoidSVGComponentsDuplication) {
            try {
                StringBuilder style = new StringBuilder();
                style.append(ARROW1).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                        .append(UP).append(" .arrow-up {stroke: black; fill: black; fill-opacity:1; visibility: visible;}");
                style.append(ARROW1).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                .append(UP).append(" .arrow-down { fill-opacity:0; visibility: hidden;}");

                style.append(ARROW1).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                .append(DOWN).append(" .arrow-down {stroke: black; fill: black; fill-opacity:1;  visibility: visible;}");
                style.append(ARROW1).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                .append(DOWN).append(" .arrow-up { fill-opacity:0; visibility: hidden;}");

                style.append(ARROW2).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                .append(UP).append(" .arrow-up {stroke: blue; fill: blue; fill-opacity:1; visibility: visible;}");
                style.append(ARROW2).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                .append(UP).append(" .arrow-down { fill-opacity:0; visibility: hidden;}");

                style.append(ARROW2).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                .append(DOWN).append(" .arrow-down {stroke: blue; fill: blue; fill-opacity:1;  visibility: visible;}");
                style.append(ARROW2).append(escapeClassName(URLEncoder.encode(node.getId(), StandardCharsets.UTF_8.name())))
                .append(DOWN).append(" .arrow-up { fill-opacity:0; visibility: hidden;}");

                return Optional.of(style.toString());
            } catch (UnsupportedEncodingException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevel().getId());
    }

    @Override
    public Optional<String> getWireStyle(Edge edge) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getNode3WTStyle(Fictitious3WTNode node, ThreeWindingsTransformer.Side side) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getNode2WTStyle(Feeder2WTNode node, TwoWindingsTransformer.Side side) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getColor(VoltageLevel vl) {
        return Optional.empty();
    }

    @Override
    public Map<String, String> getAttributesArrow(int num) {
        Map<String, String> ret = new HashMap<>();
        ret.put("stroke", num == 1 ? "black" : "blue");
        ret.put("fill", num == 1 ? "black" : "blue");
        ret.put("fill-opacity", "1");
        return ret;
    }
}
