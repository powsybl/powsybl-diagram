/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.sld.color.BaseVoltageColor;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Node;

import java.util.Optional;

import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    public NominalVoltageDiagramStyleProvider() {
        super(BaseVoltageColor.fromPlatformConfig());
    }

    public NominalVoltageDiagramStyleProvider(BaseVoltageColor baseVoltageColor) {
        super(baseVoltageColor);
    }

    @Override
    protected Optional<String> getColor(double nominalV, Node node) {
        return Optional.of(getBaseColor(nominalV));
    }

    @Override
    public Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication, boolean isShowInternalNodes) {
        Optional<String> defaultStyle = super.getNodeStyle(node, avoidSVGComponentsDuplication, isShowInternalNodes);

        String color = getBaseColor(node.getGraph().getVoltageLevelInfos().getNominalVoltage());
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
                    ? ((Feeder2WTNode) node1).getOtherSideVoltageLevelInfos().getId()
                    : ((Feeder2WTNode) node2).getOtherSideVoltageLevelInfos().getId();
            return WIRE_STYLE_CLASS + "_" + escapeClassName(vlId);
        } else {
            return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevelInfos().getId());
        }
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        String vlId;
        double nominalV;

        if ((node1 instanceof Fictitious3WTNode && node2 instanceof Feeder2WTNode) ||
                (node1 instanceof Feeder2WTNode && node2 instanceof Fictitious3WTNode)) {
            vlId = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getOtherSideVoltageLevelInfos().getId()
                    : ((Feeder2WTNode) node2).getOtherSideVoltageLevelInfos().getId();
            nominalV = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getOtherSideVoltageLevelInfos().getNominalVoltage()
                    : ((Feeder2WTNode) node2).getOtherSideVoltageLevelInfos().getNominalVoltage();
        } else {
            vlId = node1.getGraph() != null ? node1.getGraph().getVoltageLevelInfos().getId() : node2.getGraph().getVoltageLevelInfos().getId();
            nominalV = node1.getGraph() != null ? node1.getGraph().getVoltageLevelInfos().getNominalVoltage() : node2.getGraph().getVoltageLevelInfos().getNominalVoltage();
        }

        String color = getBaseColor(nominalV);
        StringBuilder style = new StringBuilder();
        style.append(".").append(WIRE_STYLE_CLASS).append("_").append(escapeClassName(vlId)).append(" {stroke:").append(color).append(";stroke-width:1;}");
        return Optional.of(style.toString());
    }
}
