/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.nad.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TopologicalStyleProvider extends AbstractVoltageStyleProvider {

    private final Map<String, String> violationStyles;

    public TopologicalStyleProvider(Network network) {
        this(network, Collections.emptyMap());
    }

    public TopologicalStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        this(network, baseVoltageStyle, Collections.emptyMap());
    }

    public TopologicalStyleProvider(Network network, Map<String, String> violationStyles) {
        super(network);
        this.violationStyles = violationStyles != null ? violationStyles : Collections.emptyMap();
    }

    public TopologicalStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle, Map<String, String> violationStyles) {
        super(network, baseVoltageStyle);
        this.violationStyles = violationStyles != null ? violationStyles : Collections.emptyMap();
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("topologicalStyle.css");
    }

    @Override
    public List<String> getBranchEdgeStyleClasses(BranchEdge branchEdge) {
        // Check custom violations first
        if (!violationStyles.isEmpty()) {
            String customStyle = violationStyles.get(branchEdge.getEquipmentId());
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
        if (busNode instanceof BoundaryBusNode) {
            String dlId = busNode.getEquipmentId();
            getBaseVoltageStyle(network.getDanglingLine(dlId).getTerminal().getVoltageLevel().getNominalV())
                    .ifPresent(baseVoltageStyle -> {
                        styles.add(baseVoltageStyle);
                        styles.add(CLASSES_PREFIX + "bus-" + busNode.getBusIndex());
                    });
        } else {
            Bus b = network.getBusView().getBus(busNode.getEquipmentId());
            if (b != null) {
                getBaseVoltageStyle(b.getVoltageLevel().getNominalV())
                        .ifPresent(baseVoltageStyle -> {
                            styles.add(baseVoltageStyle);
                            styles.add(CLASSES_PREFIX + "bus-" + busNode.getBusIndex());
                        });
            }
        }
        return styles;
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        String branchType = edge.getType();
        if (branchType.equals(BranchEdge.DANGLING_LINE_EDGE)) {
            return getBaseVoltageStyle(network.getDanglingLine(edge.getEquipmentId()).getTerminal().getVoltageLevel().getNominalV());
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
