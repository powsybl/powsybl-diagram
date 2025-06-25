/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgParameters.EdgeInfoEnum;
import com.powsybl.nad.utils.iidm.IidmUtils;
import com.powsybl.nad.utils.svg.SvgUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultLabelProvider implements LabelProvider {
    private final Network network;
    private final SvgParameters svgParameters;
    private final ValueFormatter valueFormatter;

    public DefaultLabelProvider(Network network, SvgParameters svgParameters) {
        this.network = network;
        this.svgParameters = svgParameters;
        this.valueFormatter = svgParameters.createValueFormatter();
    }

    @Override
    public List<EdgeInfo> getEdgeInfo(Graph graph, BranchEdge edge, BranchEdge.Side side) {
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
        return getEdgeInfo(terminal);
    }

    @Override
    public List<EdgeInfo> getEdgeInfo(Graph graph, ThreeWtEdge edge) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(edge.getEquipmentId());
        if (transformer == null) {
            throw new PowsyblException("Unknown three windings transformer '" + edge.getEquipmentId() + "'");
        }
        Terminal terminal = transformer.getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(edge.getSide()));
        return getEdgeInfo(terminal);
    }

    @Override
    public String getLabel(Edge edge) {
        return svgParameters.isEdgeNameDisplayed() ? edge.getEquipmentId() : null;
    }

    private List<EdgeInfo> getEdgeInfo(Terminal terminal) {
        if (terminal == null) {
            return List.of();
        }
        List<EdgeInfo> edgeInfos = new ArrayList<>();
        EdgeInfoEnum[] infos = svgParameters.getEdgeInfoDisplayed();
        for (EdgeInfoEnum info : infos) {
            switch (info) {
                case ACTIVE_POWER:
                    edgeInfos.add(new EdgeInfo(EdgeInfo.ACTIVE_POWER, terminal.getP(), valueFormatter::formatPower));
                    break;
                case REACTIVE_POWER:
                    edgeInfos.add(new EdgeInfo(EdgeInfo.REACTIVE_POWER, terminal.getQ(), valueFormatter::formatPower));
                    break;
                case CURRENT:
                    edgeInfos.add(new EdgeInfo(EdgeInfo.CURRENT, terminal.getI(), valueFormatter::formatCurrent));
                    break;
                default:
                    break;
            }
        }
        return List.copyOf(edgeInfos);
    }

    @Override
    public List<String> getVoltageLevelDescription(VoltageLevelNode voltageLevelNode) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelNode.getEquipmentId());
        List<String> description = new ArrayList<>();
        description.add(svgParameters.isIdDisplayed() ? vl.getId() : vl.getNameOrId());
        if (svgParameters.isSubstationDescriptionDisplayed()) {
            vl.getSubstation()
                    .map(s -> svgParameters.isIdDisplayed() ? s.getId() : s.getNameOrId())
                    .ifPresent(description::add);
        }
        return description;
    }

    @Override
    public String getBusDescription(BusNode busNode) {
        if (svgParameters.isBusLegend()) {
            Bus b = network.getBusView().getBus(busNode.getEquipmentId());
            String voltage = valueFormatter.formatVoltage(b.getV(), "kV");
            String angle = valueFormatter.formatAngleInDegrees(b.getAngle());
            return voltage + " / " + angle;
        }
        return null;
    }

    @Override
    public List<String> getVoltageLevelDetails(VoltageLevelNode vlNode) {
        List<String> voltageLevelDetails = new ArrayList<>();

        if (svgParameters.isVoltageLevelDetails()) {
            VoltageLevel voltageLevel = network.getVoltageLevel(vlNode.getEquipmentId());

            double activeProductionValue = voltageLevel.getGeneratorStream().mapToDouble(generator -> -generator.getTerminal().getP()).filter(p -> !Double.isNaN(p)).sum();
            String activeProduction = activeProductionValue == 0 ? "" : valueFormatter.formatPower(activeProductionValue, "MW");

            double reactiveProductionValue = voltageLevel.getGeneratorStream().mapToDouble(generator -> -generator.getTerminal().getQ()).filter(q -> !Double.isNaN(q)).sum();
            String reactiveProduction = reactiveProductionValue == 0 ? "" : valueFormatter.formatPower(reactiveProductionValue, "MVAR");

            double activeConsumptionValue = voltageLevel.getLoadStream().mapToDouble(load -> load.getTerminal().getP()).filter(p -> !Double.isNaN(p)).sum();
            String activeConsumption = activeConsumptionValue == 0 ? "" : valueFormatter.formatPower(activeConsumptionValue, "MW");

            double reactiveConsumptionValue = voltageLevel.getLoadStream().mapToDouble(load -> load.getTerminal().getQ()).filter(q -> !Double.isNaN(q)).sum();
            String reactiveConsumption = reactiveConsumptionValue == 0 ? "" : valueFormatter.formatPower(reactiveConsumptionValue, "MVAR");

            if (!activeProduction.isEmpty() || !reactiveProduction.isEmpty()) {
                voltageLevelDetails.add(String.format("~ %s / %s", activeProduction, reactiveProduction));
            }

            if (!activeConsumption.isEmpty() || !reactiveConsumption.isEmpty()) {
                voltageLevelDetails.add(String.format("⌂ %s / %s", activeConsumption, reactiveConsumption));
            }
        }

        return voltageLevelDetails;
    }

    @Override
    public String getArrowPathDIn() {
        return SvgUtils.ARROW_PATH_DIN;
    }

    @Override
    public String getArrowPathDOut() {
        return SvgUtils.ARROW_PATH_DOUT;
    }
}
