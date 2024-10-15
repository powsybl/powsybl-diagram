/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.ComponentTypeName.*;
import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at franck.lecuyer@rte-france.com>}
 */
public class DefaultLabelProvider extends AbstractLabelProvider {

    private static final String PLANNED_OUTAGE_BRANCH_NODE_DECORATOR = "LOCK";
    private static final String FORCED_OUTAGE_BRANCH_NODE_DECORATOR = "FLASH";

    protected final Network network;

    public DefaultLabelProvider(Network net, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        super(componentLibrary, layoutParameters, svgParameters);
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
        if (node.getDirection() == BOTTOM && !svgParameters.isFeederInfoSymmetry()) {
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
            TwoSides side = TwoSides.valueOf(feeder.getSide().name());
            measures = buildFeederInfos(branch, side);
        }
        return measures;
    }

    private List<FeederInfo> get3WTFeederInfos(FeederNode node, FeederTwLeg feeder) {
        List<FeederInfo> feederInfos = new ArrayList<>();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeSides side = ThreeSides.valueOf(feeder.getSide().name());
            boolean insideVoltageLevel = feeder.getOwnVoltageLevelInfos().getId().equals(feeder.getVoltageLevelInfos().getId());
            feederInfos = buildFeederInfos(transformer, side, insideVoltageLevel);
        }
        return feederInfos;
    }

    private List<FeederInfo> get2WTFeederInfos(FeederNode node, FeederTwLeg feeder) {
        List<FeederInfo> measures = new ArrayList<>();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            TwoSides side = TwoSides.valueOf(feeder.getSide().name());
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

        if (node instanceof EquipmentNode equipmentNode && !(node instanceof SwitchNode)) {
            if (node instanceof FeederNode feederNode) {
                switch (feederNode.getFeeder().getFeederType()) {
                    case BRANCH, TWO_WINDINGS_TRANSFORMER_LEG -> addOperatingStatusDecorator(nodeDecorators, node, direction, network.getBranch(feederNode.getEquipmentId()));
                    case THREE_WINDINGS_TRANSFORMER_LEG -> {
                        // if this is an outer leg (leg corresponding to another voltage level), we display the decorator on the inner 3wt
                        if (node.getAdjacentNodes().stream().noneMatch(Middle3WTNode.class::isInstance)) {
                            addOperatingStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(feederNode.getEquipmentId()));
                        }
                    }
                    case HVDC -> addOperatingStatusDecorator(nodeDecorators, node, direction, network.getHvdcLine(feederNode.getEquipmentId()));
                    default -> { /* No decorator for other feeder types */ }
                }
            } else if (node instanceof MiddleTwtNode) {
                if (node instanceof Middle3WTNode middle3WTNode && middle3WTNode.isEmbeddedInVlGraph()) {
                    addOperatingStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(middle3WTNode.getEquipmentId()));
                }
            } else {
                addOperatingStatusDecorator(nodeDecorators, node, direction, network.getIdentifiable(equipmentNode.getEquipmentId()));
            }
        }

        return nodeDecorators;
    }

    @Override
    public List<BusLegendInfo> getBusLegendInfos(VoltageLevelGraph graph) {
        VoltageLevel vl = network.getVoltageLevel(graph.getVoltageLevelInfos().getId());
        return vl.getBusView().getBusStream()
                .map(b -> new BusLegendInfo(b.getId(), List.of(
                    new BusLegendInfo.Caption(valueFormatter.formatVoltage(b.getV(), "kV"), "v"),
                    new BusLegendInfo.Caption(valueFormatter.formatAngleInDegrees(b.getAngle()), "angle")
                )))
                .collect(Collectors.toList());
    }

    private <T extends Identifiable<T>> void addOperatingStatusDecorator(List<NodeDecorator> nodeDecorators, Node node, Direction direction, Identifiable<T> identifiable) {
        if (identifiable != null) {
            OperatingStatus<T> operatingStatus = identifiable.getExtension(OperatingStatus.class);
            if (operatingStatus != null) {
                switch (operatingStatus.getStatus()) {
                    case PLANNED_OUTAGE -> nodeDecorators.add(getOperatingStatusDecorator(node, direction, PLANNED_OUTAGE_BRANCH_NODE_DECORATOR));
                    case FORCED_OUTAGE -> nodeDecorators.add(getOperatingStatusDecorator(node, direction, FORCED_OUTAGE_BRANCH_NODE_DECORATOR));
                    case IN_OPERATION -> { /* No decorator for IN_OPERATION equipment */ }
                }
            }
        }
    }

    private NodeDecorator getOperatingStatusDecorator(Node node, Direction direction, String decoratorType) {
        if (node instanceof Middle3WTNode middle3WTNode) {
            return new NodeDecorator(decoratorType, getMiddle3WTDecoratorPosition(middle3WTNode, direction));
        } else if (node instanceof BusNode) {
            return new NodeDecorator(decoratorType, getBusDecoratorPosition());
        } else if (node instanceof FeederNode) {
            return new NodeDecorator(decoratorType, getFeederDecoratorPosition(direction, decoratorType));
        } else if (node instanceof Internal2WTNode) {
            return new NodeDecorator(decoratorType, getInternal2WTDecoratorPosition(node.getOrientation()));
        } else {
            return new NodeDecorator(decoratorType, getGenericDecoratorPosition());
        }
    }

    private List<FeederInfo> buildFeederInfos(ThreeWindingsTransformer transformer, ThreeSides side, boolean insideVoltageLevel) {
        return this.buildFeederInfos(transformer.getTerminal(side), insideVoltageLevel);
    }

    private List<FeederInfo> buildFeederInfos(Injection<?> injection) {
        return this.buildFeederInfos(injection.getTerminal());
    }

    private List<FeederInfo> buildFeederInfos(Branch<?> branch, TwoSides side) {
        return this.buildFeederInfos(branch.getTerminal(side));
    }

    private List<FeederInfo> buildFeederInfos(HvdcLine hvdcLine, NodeSide side) {
        HvdcConverterStation<?> hvdcConverterStation = side == NodeSide.ONE ? hvdcLine.getConverterStation1()
                : hvdcLine.getConverterStation2();
        return this.buildFeederInfos(hvdcConverterStation.getTerminal());
    }

    private List<FeederInfo> buildFeederInfos(Terminal terminal) {
        return buildFeederInfos(terminal, true);
    }

    private List<FeederInfo> buildFeederInfos(Terminal terminal, boolean insideVoltageLevel) {
        List<FeederInfo> feederInfoList = new ArrayList<>();
        double terminalP = terminal.getP();
        double terminalQ = terminal.getQ();
        double terminalI = terminal.getI();
        if (!insideVoltageLevel) {
            terminalP = -terminalP;
            terminalQ = -terminalQ;
            terminalI = -terminalI;
        }
        feederInfoList.add(new DirectionalFeederInfo(ARROW_ACTIVE, terminalP, svgParameters.getActivePowerUnit(), valueFormatter::formatPower));
        feederInfoList.add(new DirectionalFeederInfo(ARROW_REACTIVE, terminalQ, svgParameters.getReactivePowerUnit(), valueFormatter::formatPower));
        if (this.svgParameters.isDisplayCurrentFeederInfo()) {
            feederInfoList.add(new DirectionalFeederInfo(ARROW_CURRENT, terminalI, svgParameters.getCurrentUnit(), valueFormatter::formatPower));
        }
        return feederInfoList;
    }
}
