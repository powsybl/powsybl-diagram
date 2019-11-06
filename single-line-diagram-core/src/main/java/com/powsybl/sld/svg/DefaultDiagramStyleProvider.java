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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Fictitious3WTNode;
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
            .append(UP).append(" .arrow-down { fill-opacity:0; visibility: hidden;}");

            style.append(ARROW1).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-down {stroke: black; fill: black; fill-opacity:1;  visibility: visible;}");
            style.append(ARROW1).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-up { fill-opacity:0; visibility: hidden;}");

            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(UP).append(" .arrow-up {stroke: blue; fill: blue; fill-opacity:1; visibility: visible;}");
            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(UP).append(" .arrow-down { fill-opacity:0; visibility: hidden;}");

            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-down {stroke: blue; fill: blue; fill-opacity:1;  visibility: visible;}");
            style.append(ARROW2).append(escapeClassName(node.getId()))
            .append(DOWN).append(" .arrow-up { fill-opacity:0; visibility: hidden;}");

            return Optional.of(style.toString());
        }
        return Optional.empty();
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevelId());
    }

    @Override
    public Optional<String> getWireStyle(Edge edge) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getNode3WTStyle(Fictitious3WTNode node, boolean rotate, String vId, String idWinding) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getNode2WTStyle(Feeder2WTNode node, String idWinding) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getColor(double nominalV) {
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

    @Override
    public void reset() {
        // Nothing to reset for this implementation

    }
}
