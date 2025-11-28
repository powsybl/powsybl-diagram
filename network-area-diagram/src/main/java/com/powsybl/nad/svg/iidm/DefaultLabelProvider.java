/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.util.PermanentLimitPercentageMax;
import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.svg.LabelProviderParameters;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.VoltageLevelLegend;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultLabelProvider implements LabelProvider {

    private final EdgeInfoParameters edgeInfoParameters;
    private final Network network;
    private final LabelProviderParameters parameters;
    private final ValueFormatter valueFormatter;

    public DefaultLabelProvider(Network network, SvgParameters svgParameters) {
        this.network = network;
        this.edgeInfoParameters = new EdgeInfoParameters(EdgeInfoEnum.ACTIVE_POWER, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY);
        this.valueFormatter = svgParameters.createValueFormatter();
        this.parameters = new LabelProviderParameters();
    }

    public DefaultLabelProvider(Network network, ValueFormatter valueFormatter) {
        this.network = network;
        this.edgeInfoParameters = new EdgeInfoParameters(EdgeInfoEnum.ACTIVE_POWER, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY);
        this.valueFormatter = valueFormatter;
        this.parameters = new LabelProviderParameters();
    }

    public DefaultLabelProvider(Network network, ValueFormatter valueFormatter, LabelProviderParameters parameters) {
        this.network = network;
        this.edgeInfoParameters = new EdgeInfoParameters(EdgeInfoEnum.ACTIVE_POWER, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY);
        this.valueFormatter = valueFormatter;
        this.parameters = parameters;
    }

    public DefaultLabelProvider(Network network, EdgeInfoParameters edgeInfoParameters, ValueFormatter valueFormatter, LabelProviderParameters parameters) {
        this.network = network;
        this.edgeInfoParameters = edgeInfoParameters;
        this.valueFormatter = valueFormatter;
        this.parameters = parameters;
    }

    public static Optional<Double> toOptional(double value) {
        return Double.isNaN(value) ? Optional.empty() : Optional.of(value);
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
        if (!(connectable instanceof Injection<?> iidmInjection)) {
            throw new PowsyblException("Unknown injection '" + injectionId + "'");
        }
        return getEdgeInfo(iidmInjection.getTerminal());
    }

    @Override
    public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, String branchType) {
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, branchId, BranchEdge.Side.ONE, branchType);
        return getEdgeInfo(branchId, terminal);
    }

    @Override
    public VoltageLevelLegend getVoltageLevelLegend(String voltageLevelId) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        Map<String, String> busLegend = new HashMap<>();
        if (parameters.isBusLegend()) {
            for (Bus bus : vl.getBusView().getBuses()) {
                busLegend.put(bus.getId(), getBusLegend(bus.getId()));
            }
        }
        return new VoltageLevelLegend(getLegendHeader(vl), getLegendFooter(vl), busLegend);
    }

    protected Network getNetwork() {
        return this.network;
    }

    protected ValueFormatter getValueFormatter() {
        return this.valueFormatter;
    }

    private Optional<EdgeInfo> getEdgeInfo(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        return Optional.of(new EdgeInfo(
            getDisplayedType(edgeInfoParameters.infoSideInternal),
            getDisplayedType(edgeInfoParameters.infoSideExternal),
            getReferenceValue(terminal, edgeInfoParameters.infoSideExternal).orElse(Double.NaN),
            getDisplayedValue(terminal, edgeInfoParameters.infoSideInternal).orElse(null),
            getDisplayedValue(terminal, edgeInfoParameters.infoSideExternal).orElse(null)
        ));
    }

    private Optional<EdgeInfo> getEdgeInfo(String branchId, Terminal terminal) {
        return Optional.of(new EdgeInfo(
            getDisplayedType(edgeInfoParameters.infoMiddleSide1),
            getDisplayedType(edgeInfoParameters.infoMiddleSide2),
            getReferenceValue(terminal, edgeInfoParameters.infoMiddleSide2).orElse(Double.NaN),
            getDisplayedValue(terminal, edgeInfoParameters.infoMiddleSide1, branchId).orElse(null),
            getDisplayedValue(terminal, edgeInfoParameters.infoMiddleSide2, branchId).orElse(null)
        ));
    }

    private Optional<String> getDisplayedValue(Terminal terminal, EdgeInfoEnum infoEnum, String connectableNameOrId) {
        return switch (infoEnum) {
            case ACTIVE_POWER -> toOptional(terminal.getP()).map(valueFormatter::formatPower);
            case REACTIVE_POWER -> toOptional(terminal.getQ()).map(valueFormatter::formatPower);
            case CURRENT -> toOptional(terminal.getI()).map(valueFormatter::formatCurrent);
            case NAME -> Optional.of(connectableNameOrId);
            case VALUE_PERMANENT_LIMIT_PERCENTAGE -> toOptional(getPermanentLimitPercentage(terminal)).map(valueFormatter::formatPercentage);
            case EMPTY -> Optional.empty();
        };
    }

    private double getPermanentLimitPercentage(Terminal terminal) {
        Connectable<?> connectable = terminal.getConnectable();
        return switch (connectable) {
            case Branch<?> branch -> PermanentLimitPercentageMax.getPermanentLimitPercentageMax(branch);
            case ThreeWindingsTransformer twt -> PermanentLimitPercentageMax.getPermanentLimitPercentageMax(twt);
            default -> Double.NaN;
        };
    }

    private Optional<String> getDisplayedValue(Terminal terminal, EdgeInfoEnum infoEnum) {
        return getDisplayedValue(terminal, infoEnum, terminal.getConnectable().getNameOrId());
    }

    private Optional<Double> getReferenceValue(Terminal terminal, EdgeInfoEnum infoEnum) {
        return switch (infoEnum) {
            case ACTIVE_POWER -> toOptional(terminal.getP());
            case REACTIVE_POWER -> toOptional(terminal.getQ());
            case CURRENT -> toOptional(terminal.getI());
            case NAME, VALUE_PERMANENT_LIMIT_PERCENTAGE, EMPTY -> Optional.empty();
        };
    }

    private String getDisplayedType(EdgeInfoEnum infoEnum) {
        return switch (infoEnum) {
            case ACTIVE_POWER -> EdgeInfo.ACTIVE_POWER;
            case REACTIVE_POWER -> EdgeInfo.REACTIVE_POWER;
            case CURRENT -> EdgeInfo.CURRENT;
            case NAME -> EdgeInfo.NAME;
            case VALUE_PERMANENT_LIMIT_PERCENTAGE -> EdgeInfo.LOAD_PERCENTAGE;
            case EMPTY -> EdgeInfo.EMPTY;
        };
    }

    private List<String> getLegendHeader(VoltageLevel vl) {
        List<String> description = new ArrayList<>();
        description.add(parameters.isIdDisplayed() ? vl.getId() : vl.getNameOrId());
        if (parameters.isSubstationDescriptionDisplayed()) {
            vl.getSubstation()
                .map(s -> parameters.isIdDisplayed() ? s.getId() : s.getNameOrId())
                .ifPresent(description::add);
        }
        return description;
    }

    private String getBusLegend(String busId) {
        if (parameters.isBusLegend()) {
            Bus b = network.getBusView().getBus(busId);
            String voltage = valueFormatter.formatVoltage(b.getV(), "kV");
            String angle = valueFormatter.formatAngleInDegrees(b.getAngle());
            return voltage + " / " + angle;
        }
        return null;
    }

    private List<String> getLegendFooter(VoltageLevel voltageLevel) {
        List<String> voltageLevelDetails = new ArrayList<>();

        if (parameters.isVoltageLevelDetails()) {
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

    public enum EdgeInfoEnum {
        ACTIVE_POWER,
        REACTIVE_POWER,
        CURRENT,
        NAME,
        VALUE_PERMANENT_LIMIT_PERCENTAGE,
        EMPTY
    }

    public static class Builder {
        private EdgeInfoEnum infoSideExternal = EdgeInfoEnum.ACTIVE_POWER;
        private EdgeInfoEnum infoMiddleSide1 = EdgeInfoEnum.NAME;
        private EdgeInfoEnum infoMiddleSide2 = EdgeInfoEnum.EMPTY;
        private EdgeInfoEnum infoSideInternal = EdgeInfoEnum.EMPTY;
        private final LabelProviderParameters parameters = new LabelProviderParameters();

        public Builder setInfoSideExternal(EdgeInfoEnum infoSideExternal) {
            this.infoSideExternal = infoSideExternal;
            return this;
        }

        public Builder setInfoMiddleSide1(EdgeInfoEnum infoMiddleSide1) {
            this.infoMiddleSide1 = infoMiddleSide1;
            return this;
        }

        public Builder setInfoMiddleSide2(EdgeInfoEnum infoMiddleSide2) {
            this.infoMiddleSide2 = infoMiddleSide2;
            return this;
        }

        public Builder setInfoSideInternal(EdgeInfoEnum infoSideInternal) {
            this.infoSideInternal = infoSideInternal;
            return this;
        }

        public Builder setBusLegend(boolean busLegend) {
            this.parameters.setBusLegend(busLegend);
            return this;
        }

        public Builder setSubstationDescriptionDisplayed(boolean substationDescriptionDisplayed) {
            this.parameters.setSubstationDescriptionDisplayed(substationDescriptionDisplayed);
            return this;
        }

        public Builder setIdDisplayed(boolean idDisplayed) {
            this.parameters.setIdDisplayed(idDisplayed);
            return this;
        }

        public Builder setVoltageLevelDetails(boolean voltageLevelDetails) {
            this.parameters.setVoltageLevelDetails(voltageLevelDetails);
            return this;
        }

        public DefaultLabelProvider build(Network network, SvgParameters svgParameters) {
            return new DefaultLabelProvider(network,
                new EdgeInfoParameters(infoSideExternal, infoMiddleSide1, infoMiddleSide2, infoSideInternal),
                svgParameters.createValueFormatter(), parameters);
        }

        public DefaultLabelProvider build(Network network, ValueFormatter valueFormatter) {
            return new DefaultLabelProvider(network,
                new EdgeInfoParameters(infoSideExternal, infoMiddleSide1, infoMiddleSide2, infoSideInternal),
                valueFormatter, parameters);
        }
    }

    public record EdgeInfoParameters(EdgeInfoEnum infoSideExternal,
                                     EdgeInfoEnum infoMiddleSide1,
                                     EdgeInfoEnum infoMiddleSide2,
                                     EdgeInfoEnum infoSideInternal) {
    }
}
