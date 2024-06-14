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
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public Optional<EdgeInfo> getEdgeInfo(Graph graph, BranchEdge edge, BranchEdge.Side side) {
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
        return getEdgeInfo(terminal);
    }

    @Override
    public Optional<EdgeInfo> getEdgeInfo(Graph graph, ThreeWtEdge edge) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(edge.getEquipmentId());
        if (transformer == null) {
            throw new PowsyblException("Unknown three windings transformer '" + edge.getEquipmentId() + "'");
        }
        Terminal terminal = transformer.getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(edge.getSide()));
        return getEdgeInfo(terminal);
    }

    @Override
    public String getLabel(Edge edge) {
        return edge.getEquipmentId();
    }

    private Optional<EdgeInfo> getEdgeInfo(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        switch (svgParameters.getEdgeInfoDisplayed()) {
            case ACTIVE_POWER:
                return Optional.of(new EdgeInfo(EdgeInfo.ACTIVE_POWER, terminal.getP(), valueFormatter::formatPower));
            case REACTIVE_POWER:
                return Optional.of(new EdgeInfo(EdgeInfo.REACTIVE_POWER, terminal.getQ(), valueFormatter::formatPower));
            case CURRENT:
                return Optional.of(new EdgeInfo(EdgeInfo.CURRENT, terminal.getI(), valueFormatter::formatCurrent));
            default:
                return Optional.empty();
        }
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
        Bus b = network.getBusView().getBus(busNode.getEquipmentId());
        String voltage = valueFormatter.formatVoltage(b.getV(), "kV");
        String angle = valueFormatter.formatAngleInDegrees(b.getAngle());
        return voltage + " / " + angle;
    }

    @Override
    public List<String> getVoltageLevelDetails(VoltageLevelNode vlNode) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getProductionDemand(VoltageLevelNode voltageLevelNode) {
        return new ArrayList<>();
    }

    @Override
    public String getArrowPathDIn() {
        return "M-1 -1 H1 L0 1z";
    }

    @Override
    public String getArrowPathDOut() {
        return "M-1 1 H1 L0 -1z";
    }
}
