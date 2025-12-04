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
import com.powsybl.nad.svg.VoltageLevelLegend;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultLabelProvider implements LabelProvider {
    private final Network network;
    private final SvgParameters svgParameters;
    private final ValueFormatter valueFormatter;
    private boolean displayAngle = true;

    public DefaultLabelProvider(Network network, SvgParameters svgParameters) {
        this.network = network;
        this.svgParameters = svgParameters;
        this.valueFormatter = svgParameters.createValueFormatter();
    }

    @Override
    public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, BranchEdge.Side side, String branchType) {
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, branchId, side, branchType);
        return getEdgeInfo(terminal);
    }

    @Override
    public Optional<EdgeInfo> getThreeWindingTransformerEdgeInfo(String threeWindingTransformerId, ThreeWtEdge.Side side) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(threeWindingTransformerId);
        if (transformer == null) {
            throw new PowsyblException("Unknown three windings transformer '" + threeWindingTransformerId + "'");
        }
        Terminal terminal = transformer.getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(side));
        return getEdgeInfo(terminal);
    }

    @Override
    public Optional<EdgeInfo> getInjectionEdgeInfo(String injectionId) {
        var connectable = network.getConnectable(injectionId);
        if (!(connectable instanceof com.powsybl.iidm.network.Injection<?> iidmInjection)) {
            throw new PowsyblException("Unknown injection '" + injectionId + "'");
        }
        return getEdgeInfo(iidmInjection.getTerminal());
    }

    @Override
    public String getBranchLabel(String branchId) {
        return svgParameters.isEdgeNameDisplayed() ? branchId : null;
    }

    private Optional<EdgeInfo> getEdgeInfo(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        return switch (svgParameters.getEdgeInfoDisplayed()) {
            case ACTIVE_POWER -> toOptional(terminal.getP()).map(p -> new EdgeInfo(EdgeInfo.ACTIVE_POWER, p, valueFormatter::formatPower));
            case REACTIVE_POWER -> toOptional(terminal.getQ()).map(q -> new EdgeInfo(EdgeInfo.REACTIVE_POWER, q, valueFormatter::formatPower));
            case CURRENT -> toOptional(terminal.getI()).map(i -> new EdgeInfo(EdgeInfo.CURRENT, i, valueFormatter::formatCurrent));
        };
    }

    public static Optional<Double> toOptional(double value) {
        return Double.isNaN(value) ? Optional.empty() : Optional.of(value);
    }

    @Override
    public VoltageLevelLegend getVoltageLevelLegend(String voltageLevelId) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        Map<String, String> busLegend = new HashMap<>();
        if (svgParameters.isBusLegend()) {
            for (Bus bus : vl.getBusView().getBuses()) {
                busLegend.put(bus.getId(), getBusLegend(bus.getId()));
            }
        }
        return new VoltageLevelLegend(getLegendHeader(vl), getLegendFooter(vl), busLegend);
    }

    private List<String> getLegendHeader(VoltageLevel vl) {
        List<String> description = new ArrayList<>();
        description.add(svgParameters.isIdDisplayed() ? vl.getId() : vl.getNameOrId());
        if (svgParameters.isSubstationDescriptionDisplayed()) {
            vl.getSubstation()
                    .map(s -> svgParameters.isIdDisplayed() ? s.getId() : s.getNameOrId())
                    .ifPresent(description::add);
        }
        return description;
    }

    private String getBusLegend(String busId) {
        if (svgParameters.isBusLegend()) {
            Bus b = network.getBusView().getBus(busId);
            String voltage = valueFormatter.formatVoltage(b.getV(), "kV");
            String angle = valueFormatter.formatAngleInDegrees(b.getAngle());
            return this.displayAngle ? voltage + " / " + angle : voltage;
        }
        return null;
    }

    private List<String> getLegendFooter(VoltageLevel voltageLevel) {
        List<String> voltageLevelDetails = new ArrayList<>();

        if (svgParameters.isVoltageLevelDetails()) {
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

    protected Network getNetwork() {
        return this.network;
    }

    protected SvgParameters getSvgParameters() {
        return this.svgParameters;
    }

    protected ValueFormatter getValueFormatter() {
        return this.valueFormatter;
    }

    public DefaultLabelProvider setDisplayAngle(boolean displayAngle) {
        this.displayAngle = displayAngle;
        return this;
    }
}
