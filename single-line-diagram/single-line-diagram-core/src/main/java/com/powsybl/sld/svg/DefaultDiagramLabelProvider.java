/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.nodes.Node.NodeType.FEEDER;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at franck.lecuyer@rte-france.com>
 */
public class DefaultDiagramLabelProvider extends AbstractDiagramLabelProvider {

    private static final String PLANNED_OUTAGE_BRANCH_NODE_DECORATOR = "LOCK";
    private static final String FORCED_OUTAGE_BRANCH_NODE_DECORATOR = "FLASH";

    private final Network network;

    public DefaultDiagramLabelProvider(Network net, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        super(componentLibrary, layoutParameters);
        this.network = Objects.requireNonNull(net);
    }

    @Override
    public List<FeederInfo> getFeederInfos(FeederNode node) {
        Objects.requireNonNull(node);

        List<FeederInfo> feederInfos = new ArrayList<>();
        Feeder feeder = node.getFeeder();

        switch (feeder.getFeederType()) {
            case INJECTION:
                feederInfos = getInjectionFeederInfos(node);
                break;
            case BRANCH:
                feederInfos = getBranchFeederInfos(node, (FeederWithSides) feeder);
                break;
            case TWO_WINDINGS_TRANSFORMER_LEG:
                feederInfos = get2WTFeederInfos(node, (FeederTwLeg) feeder);
                break;
            case THREE_WINDINGS_TRANSFORMER_LEG:
                feederInfos = get3WTFeederInfos(node, (FeederTwLeg) feeder);
                break;
            case HVDC:
                feederInfos = getHvdcFeederInfos(node, (FeederWithSides) feeder);
                break;
            default:
                break;
        }
        if (node.getDirection() == BOTTOM && !layoutParameters.isFeederInfoSymmetry()) {
            Collections.reverse(feederInfos);
        }
        return feederInfos;
    }

    private List<FeederInfo> getInjectionFeederInfos(FeederNode node) {
        List<FeederInfo> measures = new ArrayList<>();
        Injection injection = (Injection) network.getIdentifiable(node.getEquipmentId());
        if (injection != null) {
            measures = buildFeederInfos(injection);
        }
        return measures;
    }

    private List<FeederInfo> getBranchFeederInfos(FeederNode node, FeederWithSides feeder) {
        List<FeederInfo> measures = new ArrayList<>();
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(feeder.getSide().name());
            measures = buildFeederInfos(branch, side);
        }
        return measures;
    }

    private List<FeederInfo> get3WTFeederInfos(FeederNode node, FeederTwLeg feeder) {
        List<FeederInfo> feederInfos = new ArrayList<>();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(feeder.getSide().name());
            feederInfos = buildFeederInfos(transformer, side);
        }
        return feederInfos;
    }

    private List<FeederInfo> get2WTFeederInfos(FeederNode node, FeederTwLeg feeder) {
        List<FeederInfo> measures = new ArrayList<>();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            Branch.Side side = Branch.Side.valueOf(feeder.getSide().name());
            measures = buildFeederInfos(transformer, side);
        }
        return measures;
    }

    private List<FeederInfo> getHvdcFeederInfos(FeederNode node, FeederWithSides feeder) {
        List<FeederInfo> measures = new ArrayList<>();
        HvdcLine hvdcLine = network.getHvdcLine(node.getEquipmentId());
        if (hvdcLine != null) {
            NodeSide side = feeder.getSide();
            measures = buildFeederInfos(hvdcLine, side);
        }
        return measures;
    }

    @Override
    public List<NodeDecorator> getNodeDecorators(Node node, Direction direction) {
        Objects.requireNonNull(node);

        List<NodeDecorator> nodeDecorators = new ArrayList<>();
        if (node.getType() == FEEDER) {
            FeederNode feederNode = (FeederNode) node;
            FeederType feederType = feederNode.getFeeder().getFeederType();
            switch (feederType) {
                case BRANCH:
                case TWO_WINDINGS_TRANSFORMER_LEG:
                    addBranchStatusDecorator(nodeDecorators, node, direction, network.getBranch(feederNode.getEquipmentId()));
                    break;
                case THREE_WINDINGS_TRANSFORMER_LEG:
                    if (node.getAdjacentNodes().stream().noneMatch(Middle3WTNode.class::isInstance)) {
                        addBranchStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(feederNode.getEquipmentId()));
                    }
                    break;
                case HVDC:
                    addBranchStatusDecorator(nodeDecorators, node, direction, network.getHvdcLine(feederNode.getEquipmentId()));
                    break;
                default:
                    break;
            }
        } else if (node instanceof Middle3WTNode && ((Middle3WTNode) node).isEmbeddedInVlGraph()) {
            addBranchStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(((Middle3WTNode) node).getEquipmentId()));
        }

        return nodeDecorators;
    }

    @Override
    public List<ElectricalNodeInfo> getElectricalNodesInfos(VoltageLevelGraph graph) {
        VoltageLevel vl = network.getVoltageLevel(graph.getVoltageLevelInfos().getId());
        return vl.getBusView().getBusStream()
                .map(b -> new ElectricalNodeInfo(b.getId(), b.getV(), b.getAngle()))
                .collect(Collectors.toList());
    }

    private void addBranchStatusDecorator(List<NodeDecorator> nodeDecorators, Node node, Direction direction, Extendable e) {
        BranchStatus branchStatus = (BranchStatus) e.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            switch (branchStatus.getStatus()) {
                case PLANNED_OUTAGE:
                    nodeDecorators.add(getBranchStatusDecorator(node, direction, PLANNED_OUTAGE_BRANCH_NODE_DECORATOR));
                    break;
                case FORCED_OUTAGE:
                    nodeDecorators.add(getBranchStatusDecorator(node, direction, FORCED_OUTAGE_BRANCH_NODE_DECORATOR));
                    break;
                default:
                    break;
            }
        }
    }

    private NodeDecorator getBranchStatusDecorator(Node node, Direction direction, String decoratorType) {
        return (node instanceof Middle3WTNode) ?
                new NodeDecorator(decoratorType, getMiddle3WTDecoratorPosition((Middle3WTNode) node, direction)) :
                new NodeDecorator(decoratorType, getFeederDecoratorPosition(direction, decoratorType));
    }

    private List<FeederInfo> buildFeederInfos(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        return Arrays.asList(
                new DirectionalFeederInfo(ARROW_ACTIVE, transformer.getTerminal(side).getP(), valueFormatter::formatPower),
                new DirectionalFeederInfo(ARROW_REACTIVE, transformer.getTerminal(side).getQ(), valueFormatter::formatPower));
    }

    private List<FeederInfo> buildFeederInfos(Injection<?> injection) {
        return Arrays.asList(
                new DirectionalFeederInfo(ARROW_ACTIVE, injection.getTerminal().getP(), valueFormatter::formatPower),
                new DirectionalFeederInfo(ARROW_REACTIVE, injection.getTerminal().getQ(), valueFormatter::formatPower));
    }

    private List<FeederInfo> buildFeederInfos(Branch<?> branch, Branch.Side side) {
        return Arrays.asList(
                new DirectionalFeederInfo(ARROW_ACTIVE, branch.getTerminal(side).getP(), valueFormatter::formatPower),
                new DirectionalFeederInfo(ARROW_REACTIVE, branch.getTerminal(side).getQ(), valueFormatter::formatPower));
    }

    private List<FeederInfo> buildFeederInfos(HvdcLine hvdcLine, NodeSide side) {
        HvdcConverterStation<?> hvdcConverterStation = side == NodeSide.ONE ? hvdcLine.getConverterStation1()
                                                                                        : hvdcLine.getConverterStation2();
        return Arrays.asList(
            new DirectionalFeederInfo(ARROW_ACTIVE, hvdcConverterStation.getTerminal().getP(), valueFormatter::formatPower),
            new DirectionalFeederInfo(ARROW_REACTIVE, hvdcConverterStation.getTerminal().getQ(), valueFormatter::formatPower));
    }
}
