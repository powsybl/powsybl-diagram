/**
 * Copyright (c) 2022-2025, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.model.*;
import com.powsybl.nad.model.BranchEdge.Side;
import com.powsybl.nad.svg.AbstractStyleProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractVoltageStyleProvider extends AbstractStyleProvider {

    protected final Network network;
    private final HashMap<String, String> subnetworksHighlightMap = new HashMap<>();
    private final HashMap<String, String> subnetworkEquipmentMap = new HashMap<>();

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
        Terminal terminal = network.getThreeWindingsTransformer(threeWtEdge.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(threeWtEdge.getSide()));
        return getBaseVoltageStyle(terminal);
    }

    @Override
    protected boolean isDisconnected(BranchEdge branchEdge) {
        return isDisconnected(branchEdge, BranchEdge.Side.ONE) && isDisconnected(branchEdge, BranchEdge.Side.TWO);
    }

    @Override
    protected boolean isDisconnected(ThreeWtEdge threeWtEdge) {
        Terminal terminal = network.getThreeWindingsTransformer(threeWtEdge.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(threeWtEdge.getSide()));
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
        String subnetworkId = subnetworkEquipmentMap.get(node.getEquipmentId());
        return List.of(subnetworksHighlightMap.get(subnetworkId));
    }

    @Override
    public List<String> getHighlightSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side) {
        String subnetworkId = getSubnetworkId(edge, side);
        return List.of(subnetworksHighlightMap.get(subnetworkId));
    }

    @Override
    public List<String> getHighlightThreeWtEdgStyleClasses(ThreeWtEdge edge) {
        String subnetworkId = getSubnetworkId(edge.getEquipmentId(), edge.getSide());
        return List.of(subnetworksHighlightMap.get(subnetworkId));
    }

    private String getSubnetworkId(BranchEdge edge, Side side) {
        String subnetworId = null;
        switch (edge.getType()) {
            case BranchEdge.LINE_EDGE:
                TwoSides lineSide = side.equals(Side.ONE) ? TwoSides.ONE : TwoSides.TWO;
                subnetworId = network.getLine(edge.getEquipmentId()).getTerminal(lineSide).getVoltageLevel().getParentNetwork().getId();
                break;
            case BranchEdge.TWO_WT_EDGE, BranchEdge.PST_EDGE:
                TwoSides twSide = side.equals(Side.ONE) ? TwoSides.ONE : TwoSides.TWO;
                subnetworId = network.getTwoWindingsTransformer(edge.getEquipmentId()).getTerminal(twSide).getVoltageLevel().getParentNetwork().getId();
                break;
            case BranchEdge.DANGLING_LINE_EDGE:
                subnetworId = network.getDanglingLine(edge.getEquipmentId()).getTerminal().getVoltageLevel().getParentNetwork().getId();
                break;
            case BranchEdge.TIE_LINE_EDGE:
                subnetworId = network.getTieLine(edge.getEquipmentId()).getTerminal(side.equals(Side.ONE) ? TwoSides.ONE : TwoSides.TWO).getVoltageLevel().getParentNetwork().getId();
                break;
            case BranchEdge.HVDC_LINE_EDGE:
                subnetworId = network.getHvdcLine(edge.getEquipmentId()).getConverterStation(side.equals(Side.ONE) ? TwoSides.ONE : TwoSides.TWO).getTerminal().getVoltageLevel().getParentNetwork().getId();
                break;
            default:
                break;
        }
        return subnetworId;
    }

    private String getSubnetworkId(String id, ThreeWtEdge.Side side) {
        return network.getThreeWindingsTransformer(id).getLeg(ThreeSides.valueOf(side.name())).getTerminal().getVoltageLevel().getParentNetwork().getId();
    }

    private void buildSubnetworkMaps() {
        List<VoltageLevel> voltageLevels = getVoltageLevels();
        voltageLevels.forEach(vl -> addNode(vl.getId(), vl.getParentNetwork().getId()));
        network.getDanglingLineStream().forEach(dl -> addNode(dl.getId(), dl.getTerminal().getVoltageLevel().getParentNetwork().getId()));
    }

    private void addNode(String id, String subnetworkId) {
        subnetworkEquipmentMap.put(id, subnetworkId);
        subnetworksHighlightMap.computeIfAbsent(subnetworkId, k -> getHighlightClass(subnetworksHighlightMap.size()));
    }

    private String getHighlightClass(int index) {
        return StyleProvider.HIGHLIGHT_CLASS + "-" + index % 5;
    }

    private List<VoltageLevel> getVoltageLevels() {
        return network.getVoltageLevelStream()
                .sorted(Comparator.comparing(VoltageLevel::getId))
                .toList();
    }
}
