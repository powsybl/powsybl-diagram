/**
 * Copyright (c) 2022-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.nad.model.BoundaryNode;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BranchEdge.Side;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Injection;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.nad.svg.AbstractStyleProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractVoltageStyleProvider extends AbstractStyleProvider {

    protected final Network network;
    private final Map<String, String> subnetworksHighlightMap = new HashMap<>();

    protected AbstractVoltageStyleProvider(Network network) {
        this.network = network;
        buildSubnetworkMaps();
    }

    protected AbstractVoltageStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(baseVoltageStyle);
        this.network = network;
        buildSubnetworkMaps();
    }

    @Override
    public List<String> getNodeStyleClasses(Node node) {
        List<String> styles = new ArrayList<>(super.getNodeStyleClasses(node));
        Optional<Double> nominalV = Optional.empty();
        if (node instanceof BoundaryNode) {
            nominalV = Optional.of(network.getDanglingLine(node.getEquipmentId()).getTerminal().getVoltageLevel().getNominalV());
        } else if (node instanceof VoltageLevelNode) {
            nominalV = Optional.of(network.getVoltageLevel(node.getEquipmentId()).getNominalV());
        }
        nominalV.flatMap(this::getBaseVoltageStyle)
                .ifPresent(styles::add);
        return styles;
    }

    @Override
    public List<String> getBusNodeStyleClasses(BusNode busNode) {
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
    public List<String> getBranchEdgeStyleClasses(BranchEdge branchEdge) {
        List<String> styleClasses = new ArrayList<>(super.getBranchEdgeStyleClasses(branchEdge));
        if (IidmUtils.isIidmBranch(branchEdge)) {
            Branch<?> branch = network.getBranch(branchEdge.getEquipmentId());
            if (branch.isOverloaded()) {
                styleClasses.add(StyleProvider.LINE_OVERLOADED_CLASS);
            }
        }
        return styleClasses;
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(ThreeWtEdge threeWtEdge) {
        return getBaseVoltageStyle(getThreeWtTerminal(threeWtEdge));
    }

    @Override
    protected boolean isDisconnected(BranchEdge branchEdge) {
        return isDisconnected(branchEdge, BranchEdge.Side.ONE) && isDisconnected(branchEdge, BranchEdge.Side.TWO);
    }

    @Override
    protected boolean isDisconnected(ThreeWtEdge threeWtEdge) {
        return isDisconnected(getThreeWtTerminal(threeWtEdge));
    }

    private Terminal getThreeWtTerminal(ThreeWtEdge threeWtEdge) {
        return network.getThreeWindingsTransformer(threeWtEdge.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(threeWtEdge.getSide()));
    }

    @Override
    protected boolean isDisconnected(Injection injection) {
        return isDisconnected(getInjectionTerminal(injection));
    }

    private Terminal getInjectionTerminal(Injection injection) {
        var connectable = network.getConnectable(injection.getEquipmentId());
        if (!(connectable instanceof com.powsybl.iidm.network.Injection<?> iidmInj)) {
            throw new PowsyblException("Unknown injection :" + injection.getEquipmentId());
        }
        return iidmInj.getTerminal();
    }

    private static boolean isDisconnected(Terminal terminal) {
        return terminal == null || !terminal.isConnected();
    }

    @Override
    protected boolean isDisconnected(BranchEdge edge, BranchEdge.Side side) {
        return IidmUtils.isDisconnected(network, edge, side);
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

    @Override
    public List<String> getHighlightNodeStyleClasses(Node node) {
        String subnetworkId = network.getIdentifiable(node.getEquipmentId()).getParentNetwork().getId();
        return List.of(subnetworksHighlightMap.get(subnetworkId));
    }

    @Override
    public List<String> getHighlightSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side) {
        return getSubnetworkId(edge, side).map(id -> List.of(subnetworksHighlightMap.get(id))).orElse(Collections.emptyList());
    }

    @Override
    public List<String> getHighlightThreeWtEdgStyleClasses(ThreeWtEdge edge) {
        String subnetworkId = getSubnetworkId(edge.getEquipmentId(), edge.getSide());
        return List.of(subnetworksHighlightMap.get(subnetworkId));
    }

    private Optional<String> getSubnetworkId(BranchEdge edge, Side side) {
        final TwoSides iidmSide = IidmUtils.getIidmSideFromBranchEdgeSide(side);
        Terminal terminal = switch (edge.getType()) {
            case BranchEdge.LINE_EDGE -> network.getLine(edge.getEquipmentId()).getTerminal(iidmSide);
            case BranchEdge.TWO_WT_EDGE, BranchEdge.PST_EDGE -> network.getTwoWindingsTransformer(edge.getEquipmentId()).getTerminal(iidmSide);
            case BranchEdge.DANGLING_LINE_EDGE -> network.getDanglingLine(edge.getEquipmentId()).getTerminal();
            case BranchEdge.TIE_LINE_EDGE -> network.getTieLine(edge.getEquipmentId()).getTerminal(iidmSide);
            case BranchEdge.HVDC_LINE_LCC_EDGE, BranchEdge.HVDC_LINE_VSC_EDGE -> network.getHvdcLine(edge.getEquipmentId()).getConverterStation(iidmSide).getTerminal();
            default -> null;
        };
        return Optional.ofNullable(terminal).map(t -> t.getVoltageLevel().getParentNetwork().getId());
    }

    private String getSubnetworkId(String id, ThreeWtEdge.Side side) {
        return network.getThreeWindingsTransformer(id).getLeg(ThreeSides.valueOf(side.name())).getTerminal().getVoltageLevel().getParentNetwork().getId();
    }

    private void buildSubnetworkMaps() {
        network.getSubnetworks().forEach(n -> subnetworksHighlightMap.put(n.getId(), getHighlightClass(subnetworksHighlightMap.size())));
    }

    private String getHighlightClass(int index) {
        return StyleProvider.HIGHLIGHT_CLASS + "-" + index % 5;
    }

}
