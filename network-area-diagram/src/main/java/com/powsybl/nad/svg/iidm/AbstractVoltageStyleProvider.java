/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.AbstractStyleProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractVoltageStyleProvider extends AbstractStyleProvider {

    protected final Network network;

    protected AbstractVoltageStyleProvider(Network network) {
        this.network = network;
    }

    protected AbstractVoltageStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(baseVoltageStyle);
        this.network = network;
    }

    @Override
    public List<String> getNodeStyleClasses(BusNode busNode) {
        if (busNode == BusNode.UNKNOWN) {
            return List.of(UNKNOWN_BUSNODE_CLASS);
        }
        List<String> styles = new ArrayList<>();
        Bus b = network.getBusView().getBus(busNode.getEquipmentId());
        if (b != null) {
            if (b.getV() > b.getVoltageLevel().getHighVoltageLimit()) {
                styles.add(StyleProvider.VL_OVERVOLTAGE_CLASS);
            } else if (b.getV() < b.getVoltageLevel().getLowVoltageLimit()) {
                styles.add(StyleProvider.VL_UNDERVOLTAGE_CLASS);
            }
        }
        return styles;
    }

    @Override
    public List<String> getEdgeStyleClasses(Edge edge) {
        List<String> styleClasses = new ArrayList<>(super.getEdgeStyleClasses(edge));
        if (IidmUtils.isIidmBranch(edge)) {
            Branch<?> branch = network.getBranch(edge.getEquipmentId());
            if (branch.isOverloaded()) {
                styleClasses.add(StyleProvider.LINE_OVERLOADED_CLASS);
            }
        }
        return styleClasses;
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(ThreeWtEdge threeWtEdge) {
        Terminal terminal = network.getThreeWindingsTransformer(threeWtEdge.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(threeWtEdge.getSide()));
        return getBaseVoltageStyle(terminal);
    }

    @Override
    protected boolean isDisconnected(Edge edge) {
        if (edge instanceof ThreeWtEdge) {
            ThreeWtEdge twtEdge = (ThreeWtEdge) edge;
            Terminal terminal = network.getThreeWindingsTransformer(twtEdge.getEquipmentId())
                    .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(twtEdge.getSide()));
            return terminal == null || !terminal.isConnected();
        }
        if (edge instanceof BranchEdge) {
            return isDisconnected((BranchEdge) edge, BranchEdge.Side.ONE) && isDisconnected((BranchEdge) edge, BranchEdge.Side.TWO);
        }
        return false;
    }

    @Override
    protected boolean isDisconnected(BranchEdge edge, BranchEdge.Side side) {
        return IidmUtils.isDisconnected(network, edge, side);
    }

    @Override
    protected boolean isDisconnected(ThreeWtNode threeWtNode, ThreeWtEdge.Side side) {
        Terminal terminal = network.getThreeWindingsTransformer(threeWtNode.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(side));
        return terminal == null || !terminal.isConnected();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Edge edge) {
        if (edge instanceof ThreeWtEdge threeWtEdge) {
            Terminal terminal = network.getThreeWindingsTransformer(edge.getEquipmentId())
                    .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(threeWtEdge.getSide()));
            return getBaseVoltageStyle(terminal);
        }
        return Optional.empty();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        String branchType = edge.getType();
        if (branchType.equals(BranchEdge.DANGLING_LINE_EDGE)) {
            return getBaseVoltageStyle(network.getDanglingLine(edge.getEquipmentId()).getTerminal().getVoltageLevel().getNominalV());
        }
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
        return getBaseVoltageStyle(terminal);
    }

    protected abstract Optional<String> getBaseVoltageStyle(Terminal terminal);
}
