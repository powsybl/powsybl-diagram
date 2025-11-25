/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.ThreeWtEdge;
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
    private final boolean isBusLegend;
    private final EdgeInfoParameters edgeInfoParameters;
    private final Network network;
    private final boolean substationDescriptionDisplayed;
    private final SvgParameters svgParameters;
    private final ValueFormatter valueFormatter;

    public DefaultLabelProvider(Network network, SvgParameters svgParameters) {
        this.network = network;
        this.svgParameters = svgParameters;
        this.edgeInfoParameters = new EdgeInfoParameters(EdgeInfoEnum.ACTIVE_POWER, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY, EdgeInfoEnum.EMPTY);
        this.valueFormatter = svgParameters.createValueFormatter();
        this.isBusLegend = svgParameters.isBusLegend();
        this.substationDescriptionDisplayed = svgParameters.isSubstationDescriptionDisplayed();
    }

    public DefaultLabelProvider(Network network, EdgeInfoParameters edgeInfoParameters, ValueFormatter valueFormatter, boolean substationDescriptionDisplayed) {
        this.network = network;
        this.svgParameters = new SvgParameters();
        this.edgeInfoParameters = edgeInfoParameters;
        this.valueFormatter = valueFormatter;
        this.substationDescriptionDisplayed = substationDescriptionDisplayed;
        this.isBusLegend = true;
    }

    public DefaultLabelProvider(Network network, EdgeInfoParameters edgeInfoParameters, ValueFormatter valueFormatter, boolean substationDescriptionDisplayed, boolean isBusLegend) {
        this.network = network;
        this.svgParameters = new SvgParameters();
        this.edgeInfoParameters = edgeInfoParameters;
        this.valueFormatter = valueFormatter;
        this.substationDescriptionDisplayed = substationDescriptionDisplayed;
        this.isBusLegend = isBusLegend;
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
    public String getBranchLabel(String branchId) {
        return edgeInfoParameters.infoMiddleSide1 == EdgeInfoEnum.NAME || edgeInfoParameters.infoMiddleSide2 == EdgeInfoEnum.NAME ? branchId : null;
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
        if (isBusLegend) {
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
            case LOAD_PERCENTAGE -> Optional.empty();
            case EMPTY -> Optional.empty();
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
            case NAME, LOAD_PERCENTAGE, EMPTY -> Optional.empty();
        };
    }

    private String getDisplayedType(EdgeInfoEnum infoEnum) {
        return switch (infoEnum) {
            case ACTIVE_POWER -> EdgeInfo.ACTIVE_POWER;
            case REACTIVE_POWER -> EdgeInfo.REACTIVE_POWER;
            case CURRENT -> EdgeInfo.CURRENT;
            case NAME -> EdgeInfo.NAME;
            case LOAD_PERCENTAGE -> EdgeInfo.LOAD_PERCENTAGE;
            case EMPTY -> EdgeInfo.EMPTY;
        };
    }

    private List<String> getLegendHeader(VoltageLevel vl) {
        List<String> description = new ArrayList<>();
        description.add(svgParameters.isIdDisplayed() ? vl.getId() : vl.getNameOrId());
        if (substationDescriptionDisplayed) {
            vl.getSubstation()
                .map(s -> svgParameters.isIdDisplayed() ? s.getId() : s.getNameOrId())
                .ifPresent(description::add);
        }
        return description;
    }

    private String getBusLegend(String busId) {
        if (isBusLegend) {
            Bus b = network.getBusView().getBus(busId);
            String voltage = valueFormatter.formatVoltage(b.getV(), "kV");
            String angle = valueFormatter.formatAngleInDegrees(b.getAngle());
            return voltage + " / " + angle;
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

    public enum EdgeInfoEnum {
        ACTIVE_POWER,
        REACTIVE_POWER,
        CURRENT,
        NAME,
        LOAD_PERCENTAGE,
        EMPTY
    }

    public static class Builder {
        private EdgeInfoEnum infoSideExternal = EdgeInfoEnum.ACTIVE_POWER;
        private EdgeInfoEnum infoMiddleSide1 = EdgeInfoEnum.NAME;
        private EdgeInfoEnum infoMiddleSide2 = EdgeInfoEnum.EMPTY;
        private EdgeInfoEnum infoSideInternal = EdgeInfoEnum.EMPTY;
        private boolean busLegend = true;

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
            this.busLegend = busLegend;
            return this;
        }

        public DefaultLabelProvider build(Network network, SvgParameters svgParameters) {
            return new DefaultLabelProvider(network,
                new EdgeInfoParameters(infoSideExternal, infoMiddleSide1, infoMiddleSide2, infoSideInternal),
                svgParameters.createValueFormatter(), svgParameters.isSubstationDescriptionDisplayed(), busLegend);
        }

        public DefaultLabelProvider build(Network network, ValueFormatter valueFormatter, boolean substationDescriptionDisplayed) {
            return new DefaultLabelProvider(network,
                new EdgeInfoParameters(infoSideExternal, infoMiddleSide1, infoMiddleSide2, infoSideInternal),
                valueFormatter, substationDescriptionDisplayed, busLegend);
        }
    }

    public record EdgeInfoParameters(EdgeInfoEnum infoSideExternal,
                                     EdgeInfoEnum infoMiddleSide1,
                                     EdgeInfoEnum infoMiddleSide2,
                                     EdgeInfoEnum infoSideInternal) {
    }
}
