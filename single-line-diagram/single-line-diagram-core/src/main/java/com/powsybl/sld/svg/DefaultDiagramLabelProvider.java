/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

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
import static com.powsybl.sld.library.ComponentTypeName.ARROW_CURRENT;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at franck.lecuyer@rte-france.com>
 */
public class DefaultDiagramLabelProvider extends AbstractDiagramLabelProvider {

    private static final String PLANNED_OUTAGE_BRANCH_NODE_DECORATOR = "LOCK";
    private static final String FORCED_OUTAGE_BRANCH_NODE_DECORATOR = "FLASH";

    protected final Network network;

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
        Injection<?> injection = (Injection<?>) network.getIdentifiable(node.getEquipmentId());
        if (injection != null) {
            measures = buildFeederInfos(injection);
        }
        return measures;
    }

    private List<FeederInfo> getBranchFeederInfos(FeederNode node, FeederWithSides feeder) {
        List<FeederInfo> measures = new ArrayList<>();
        Branch<?> branch = network.getBranch(node.getEquipmentId());
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

        // BranchStatus extension is on connectables, so we're looking for them
        if (node instanceof EquipmentNode && !(node instanceof SwitchNode)) {
            if (node instanceof FeederNode) {
                FeederNode feederNode = (FeederNode) node;
                switch (feederNode.getFeeder().getFeederType()) {
                    case BRANCH:
                    case TWO_WINDINGS_TRANSFORMER_LEG:
                        addBranchStatusDecorator(nodeDecorators, node, direction, network.getBranch(feederNode.getEquipmentId()));
                        break;
                    case THREE_WINDINGS_TRANSFORMER_LEG:
                        // if this is an outer leg (leg corresponding to another voltage level), we display the decorator on the inner 3wt
                        if (node.getAdjacentNodes().stream().noneMatch(Middle3WTNode.class::isInstance)) {
                            addBranchStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(feederNode.getEquipmentId()));
                        }
                        break;
                    case HVDC:
                        HvdcLine hvdcLine = network.getHvdcLine(feederNode.getEquipmentId());
                        Connectable<?> converterStation = ((FeederWithSides) feederNode.getFeeder()).getSide() == NodeSide.ONE ? hvdcLine.getConverterStation1() : hvdcLine.getConverterStation2();
                        addBranchStatusDecorator(nodeDecorators, node, direction, converterStation);
                        break;
                    default:
                        break;
                }
            } else if (node instanceof MiddleTwtNode) {
                if (node instanceof Middle3WTNode && ((Middle3WTNode) node).isEmbeddedInVlGraph()) {
                    addBranchStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(((Middle3WTNode) node).getEquipmentId()));
                }
            } else {
                Identifiable<?> identifiable = network.getIdentifiable(((EquipmentNode) node).getEquipmentId());
                if (identifiable instanceof Connectable<?>) {
                    addBranchStatusDecorator(nodeDecorators, node, direction, (Connectable<?>) identifiable);
                }
            }
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

    private void addBranchStatusDecorator(List<NodeDecorator> nodeDecorators, Node node, Direction direction, Connectable<?> c) {
        BranchStatus<?> branchStatus = (BranchStatus<?>) c.getExtension(BranchStatus.class);
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

    private void addBranchStatusDecorator(List<NodeDecorator> nodeDecorators, Node node, Direction direction, Branch branch) {
        BranchStatus<?> branchStatus = (BranchStatus<?>) branch.getExtension(BranchStatus.class);
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
        return this.buildFeederInfos(transformer.getTerminal(side));
    }

    private List<FeederInfo> buildFeederInfos(Injection<?> injection) {
        return this.buildFeederInfos(injection.getTerminal());
    }

    private List<FeederInfo> buildFeederInfos(Branch<?> branch, Branch.Side side) {
        return this.buildFeederInfos(branch.getTerminal(side));
    }

    private List<FeederInfo> buildFeederInfos(HvdcLine hvdcLine, NodeSide side) {
        HvdcConverterStation<?> hvdcConverterStation = side == NodeSide.ONE ? hvdcLine.getConverterStation1()
                                                                                        : hvdcLine.getConverterStation2();
        return this.buildFeederInfos(hvdcConverterStation.getTerminal());
    }

    private List<FeederInfo> buildFeederInfos(Terminal terminal) {
        List<FeederInfo> feederInfoList = new ArrayList<>();
        feederInfoList.add(new DirectionalFeederInfo(ARROW_ACTIVE, terminal.getP(), valueFormatter::formatPower));
        feederInfoList.add(new DirectionalFeederInfo(ARROW_REACTIVE, terminal.getQ(), valueFormatter::formatPower));
        if (this.layoutParameters.isDisplayCurrentFeederInfo()) {
            feederInfoList.add(new DirectionalFeederInfo(ARROW_CURRENT, terminal.getI(), valueFormatter::formatCurrent));
        }
        return feederInfoList;
    }
}
