/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.color.BaseVoltageColor;
import com.powsybl.sld.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.powsybl.sld.svg.DiagramStyles.escapeId;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    public NominalVoltageDiagramStyleProvider(Network network) {
        this(BaseVoltageColor.fromPlatformConfig(), network);
    }

    public NominalVoltageDiagramStyleProvider(BaseVoltageColor baseVoltageColor, Network network) {
        super(baseVoltageColor, network);
    }

    @Override
    protected Optional<String> getColor(double nominalV, Node node) {
        return Optional.of(getBaseColor(nominalV));
    }

    @Override
    public Optional<String> getCssNodeStyleAttributes(Node node, boolean isShowInternalNodes) {
        Optional<String> defaultStyle = super.getCssNodeStyleAttributes(node, isShowInternalNodes);

        String color = getBaseColor(node.getGraph().getVoltageLevelInfos().getNominalVoltage());
        if (node.getType() == Node.NodeType.SWITCH) {
            return defaultStyle;
        } else {
            return Optional.of(defaultStyle.orElse("") + " ." + escapeId(node.getId()) + " {stroke:" + color + ";}");
        }
    }

    private VoltageLevelInfos getVoltageLevelInfos(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        VoltageLevelInfos voltageLevelInfos;
        if (node1 instanceof Middle3WTNode && node2 instanceof Feeder3WTLegNode) {
            voltageLevelInfos = ((Feeder3WTLegNode) node2).getOtherSideVoltageLevelInfos();
        } else if (node1 instanceof Feeder3WTLegNode && node2 instanceof Middle3WTNode) {
            voltageLevelInfos = ((Feeder3WTLegNode) node1).getOtherSideVoltageLevelInfos();
        } else {
            voltageLevelInfos = node1.getGraph() != null ? node1.getGraph().getVoltageLevelInfos() : node2.getGraph().getVoltageLevelInfos();
        }
        return voltageLevelInfos;
    }

    @Override
    public Map<String, String> getCssWireStyleAttributes(Edge edge, boolean isHighLightLineState) {
        Map<String, String> style = new HashMap<>();

        VoltageLevelInfos voltageLevelInfos = getVoltageLevelInfos(edge);
        String color = getBaseColor(voltageLevelInfos.getNominalVoltage());

        style.put("stroke", color);
        style.put("stroke-width", "1");

        if (isHighLightLineState && network != null) {
            buildWireStyle(edge, style, color);
        }

        return style;
    }
}
