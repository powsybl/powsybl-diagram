/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BusNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TopologicalStyleProvider extends AbstractVoltageStyleProvider {

    private Map<String, String> limitViolationStyles = Map.of();

    public TopologicalStyleProvider(Network network) {
        super(network);
    }

    public TopologicalStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(network, baseVoltageStyle);
    }

    public TopologicalStyleProvider(Network network, Map<String, String> limitViolationStyles) {
        super(network);
        this.limitViolationStyles = limitViolationStyles;
    }

    public TopologicalStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle, Map<String, String> limitViolationStyles) {
        super(network, baseVoltageStyle);
        this.limitViolationStyles = limitViolationStyles;
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("topologicalStyle.css");
    }

    @Override
    public List<String> getBranchEdgeStyleClasses(BranchEdge branchEdge) {
        // Check custom violations first
        if (!limitViolationStyles.isEmpty()) {
            String customStyle = limitViolationStyles.get(branchEdge.getEquipmentId());
            if (customStyle != null && !customStyle.isBlank()) {
                return List.of(customStyle);
            }
        }
        // Fallback to default overload detection
        return super.getBranchEdgeStyleClasses(branchEdge);
    }

    @Override
    public List<String> getBusNodeStyleClasses(BusNode busNode) {
        List<String> styles = new ArrayList<>(super.getBusNodeStyleClasses(busNode));
        styles.add(CLASSES_PREFIX + "bus-" + busNode.getBusIndex());
        return styles;
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        String branchType = edge.getType();
        if (branchType.equals(BranchEdge.BOUNDARY_LINE_EDGE)) {
            return getBaseVoltageStyle(network.getBoundaryLine(edge.getEquipmentId()).getTerminal().getVoltageLevel().getNominalV());
        } else {
            return super.getBaseVoltageStyle(edge, side);
        }
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        return getBaseVoltageStyle(terminal.getVoltageLevel().getNominalV());
    }
}
