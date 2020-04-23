/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.sld.model.*;

import java.nio.file.Path;
import java.util.Optional;

import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    public NominalVoltageDiagramStyleProvider() {
        super(null);
    }

    public NominalVoltageDiagramStyleProvider(Path config) {
        super(config);
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

    private VoltageLevelInfos getVoltageLevelInfos(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        VoltageLevelInfos voltageLevelInfos;
        if (node1 instanceof Middle3wtNode && node2 instanceof Feeder3WTLegNode) {
            voltageLevelInfos = ((Feeder3WTLegNode) node2).getOtherSideVoltageLevelInfos();
        } else if (node1 instanceof Feeder3WTLegNode && node2 instanceof Middle3wtNode) {
            voltageLevelInfos = ((Feeder3WTLegNode) node1).getOtherSideVoltageLevelInfos();
        } else {
            voltageLevelInfos = node1.getGraph() != null ? node1.getGraph().getVoltageLevelInfos() : node2.getGraph().getVoltageLevelInfos();
        }
        return voltageLevelInfos;
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        VoltageLevelInfos voltageLevelInfos = getVoltageLevelInfos(edge);
        return WIRE_STYLE_CLASS + "_" + escapeClassName(voltageLevelInfos.getId());
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index) {
        VoltageLevelInfos voltageLevelInfos = getVoltageLevelInfos(edge);
        String color = getBaseColor(voltageLevelInfos.getNominalVoltage());
        String style = "." + WIRE_STYLE_CLASS + "_" + escapeClassName(voltageLevelInfos.getId()) +
                " {stroke:" + color + ";stroke-width:1;}";
        return Optional.of(style);
    }
}
