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
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TopologicalStyleProvider extends AbstractVoltageStyleProvider {

    public static final String LINE_STYLE_SUFFIX = "-line";

    public TopologicalStyleProvider(Network network) {
        super(network);
    }

    public TopologicalStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(network, baseVoltageStyle);
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("topologicalStyle.css");
    }

    @Override
    public List<String> getNodeStyleClasses(BusNode busNode) {
        List<String> styles = new ArrayList<>(super.getNodeStyleClasses(busNode));
        if (busNode instanceof BoundaryBusNode) {
            String dlId = busNode.getEquipmentId();
            getBaseVoltageStyle(network.getDanglingLine(dlId).getTerminal().getVoltageLevel().getNominalV())
                    .map(baseVoltageStyle -> baseVoltageStyle + "-" + busNode.getIndex())
                    .ifPresent(styles::add);
        } else {
            Bus b = network.getBusView().getBus(busNode.getEquipmentId());
            if (b != null) {
                getBaseVoltageStyle(b.getVoltageLevel().getNominalV())
                        .map(baseVoltageStyle -> baseVoltageStyle + "-" + busNode.getIndex())
                        .ifPresent(styles::add);
            }
        }
        return styles;
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        String branchType = edge.getType();
        if (branchType.equals(BranchEdge.DANGLING_LINE_EDGE)) {
            return getBaseVoltageStyle(network.getDanglingLine(edge.getEquipmentId()).getTerminal().getVoltageLevel().getNominalV())
                .map(baseVoltageStyle -> baseVoltageStyle + LINE_STYLE_SUFFIX);
        } else {
            return super.getBaseVoltageStyle(edge, side);
        }
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        return getBaseVoltageStyle(terminal.getVoltageLevel().getNominalV())
                .map(baseVoltageStyle -> baseVoltageStyle + LINE_STYLE_SUFFIX);
    }
}
