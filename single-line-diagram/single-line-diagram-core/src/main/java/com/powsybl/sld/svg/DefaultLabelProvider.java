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
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTeePointLeg;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.sld.library.SldComponentTypeName.*;
import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at franck.lecuyer@rte-france.com>}
 */
public class DefaultLabelProvider extends AbstractLabelProvider {

    private static final String PLANNED_OUTAGE_BRANCH_NODE_DECORATOR = "LOCK";
    private static final String FORCED_OUTAGE_BRANCH_NODE_DECORATOR = "FLASH";

    private boolean displayCurrent = false;
    private boolean displayArrowForCurrent = true;
    private boolean displayPermanentLimitPercentage = false;

    protected final Network network;

    public DefaultLabelProvider(Network net, SldComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        super(componentLibrary, layoutParameters, svgParameters);
        this.network = Objects.requireNonNull(net);
    }

    @Override
    public List<FeederInfo> getFeederInfos(FeederNode node) {
        Objects.requireNonNull(node);

        Feeder feeder = node.getFeeder();

        List<FeederInfo> feederInfos = switch (feeder.getFeederType()) {
            case INJECTION -> getInjectionFeederInfos(node);
            case BRANCH -> getBranchFeederInfos(node, ((FeederWithSides) feeder).getSide());
            case TWO_WINDINGS_TRANSFORMER_LEG -> getBranchFeederInfos(node, ((FeederTwLeg) feeder).getSide());
            case THREE_WINDINGS_TRANSFORMER_LEG -> get3WTFeederInfos(node, (FeederTwLeg) feeder);
            case TEE_POINT_LEG -> getTeePointFeederInfos(node, (FeederTeePointLeg) feeder);
            case HVDC -> getHvdcFeederInfos(node, (FeederWithSides) feeder);
            default -> new ArrayList<>();
        };
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

    private List<FeederInfo> getBranchFeederInfos(FeederNode node, NodeSide nodeSide) {
        List<FeederInfo> measures = new ArrayList<>();
        Branch<?> branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            TwoSides side = TwoSides.valueOf(nodeSide.name());
            measures = getBranchFeederInfos(branch, side);
        }
        return measures;
    }

    private List<FeederInfo> get3WTFeederInfos(FeederNode node, FeederTwLeg feeder) {
        List<FeederInfo> feederInfos = new ArrayList<>();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeSides side = ThreeSides.valueOf(feeder.getSide().name());
            boolean insideVoltageLevel = feeder.getOwnVoltageLevelInfos().id().equals(feeder.getVoltageLevelInfos().id());
            feederInfos = get3WTFeederInfos(transformer, side, insideVoltageLevel);
        }
        return feederInfos;
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

    private List<FeederInfo> getTeePointFeederInfos(FeederNode node, FeederTeePointLeg feeder) {
        boolean insideVoltageLevel = feeder.getOwnVoltageLevelInfos().id().equals(feeder.getVoltageLevelInfos().id());
        return buildFeederInfos(network.getLine(node.getEquipmentId()).getTerminal1(), insideVoltageLevel);
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
        return switch (node) {
            case Middle3WTNode middle3WTNode -> new NodeDecorator(decoratorType, getMiddle3WTDecoratorPosition(middle3WTNode, direction));
            case BusNode ignored -> new NodeDecorator(decoratorType, getBusDecoratorPosition());
            case FeederNode ignored -> new NodeDecorator(decoratorType, getFeederDecoratorPosition(direction, decoratorType));
            case Internal2WTNode ignored -> new NodeDecorator(decoratorType, getInternal2WTDecoratorPosition(node.getOrientation()));
            case null, default -> new NodeDecorator(decoratorType, getGenericDecoratorPosition());
        };
    }

    private List<FeederInfo> buildFeederInfos(Injection<?> injection) {
        return this.buildFeederInfos(injection.getTerminal());
    }

    private List<FeederInfo> buildFeederInfos(HvdcLine hvdcLine, NodeSide side) {
        HvdcConverterStation<?> hvdcConverterStation = side == NodeSide.ONE ? hvdcLine.getConverterStation1()
                : hvdcLine.getConverterStation2();
        return this.buildFeederInfos(hvdcConverterStation.getTerminal());
    }

    private List<FeederInfo> buildFeederInfos(Terminal terminal) {
        return buildFeederInfos(terminal, true);
    }

    private List<FeederInfo> getBranchFeederInfos(Branch<?> branch, TwoSides side) {
        List<FeederInfo> feederInfoList = buildFeederInfos(branch.getTerminal(side), true);
        if (this.displayPermanentLimitPercentage) {
            feederInfoList.add(new ValueFeederInfo(VALUE_PERMANENT_LIMIT_PERCENTAGE, LabelDirection.NONE, getPermanentLimitPercentageMax(branch), valueFormatter::formatPercentage));
        }
        return feederInfoList;
    }

    private List<FeederInfo> get3WTFeederInfos(ThreeWindingsTransformer transformer, ThreeSides side, boolean insideVoltageLevel) {
        List<FeederInfo> feederInfoList = buildFeederInfos(transformer.getTerminal(side), insideVoltageLevel);
        if (this.displayPermanentLimitPercentage) {
            feederInfoList.add(new ValueFeederInfo(VALUE_PERMANENT_LIMIT_PERCENTAGE, LabelDirection.NONE, getPermanentLimitPercentageMax(transformer), valueFormatter::formatPercentage));
        }
        return feederInfoList;
    }

    private double getPermanentLimitPercentageMax(Branch<?> branch) {
        return Stream.of(TwoSides.ONE, TwoSides.TWO)
            .map(side -> getPermanentLimitPercentageMax(branch.getTerminal(side), branch.getCurrentLimits(side).orElse(null)))
            .mapToDouble(Double::doubleValue).max().getAsDouble();
    }

    private double getPermanentLimitPercentageMax(ThreeWindingsTransformer transformer) {
        return Stream.of(ThreeSides.ONE, ThreeSides.TWO, ThreeSides.THREE)
            .map(side -> getPermanentLimitPercentageMax(transformer.getTerminal(side), transformer.getLeg(side).getCurrentLimits().orElse(null)))
            .mapToDouble(Double::doubleValue).max().getAsDouble();
    }

    private double getPermanentLimitPercentageMax(Terminal terminal, CurrentLimits currentLimits) {
        return currentLimits != null ? (Math.abs(terminal.getI() * 100) / currentLimits.getPermanentLimit()) : 0;
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
        feederInfoList.add(new ValueFeederInfo(ARROW_ACTIVE, terminalP, svgParameters.getActivePowerUnit(), valueFormatter::formatPower));
        feederInfoList.add(new ValueFeederInfo(ARROW_REACTIVE, terminalQ, svgParameters.getReactivePowerUnit(), valueFormatter::formatPower));
        if (this.displayCurrent) {
            if (this.displayArrowForCurrent) {
                feederInfoList.add(new ValueFeederInfo(ARROW_CURRENT, terminalI, svgParameters.getCurrentUnit(), valueFormatter::formatCurrent));
            } else {
                feederInfoList.add(new ValueFeederInfo(VALUE_CURRENT, LabelDirection.NONE, terminalI, svgParameters.getCurrentUnit(), valueFormatter::formatCurrent));
            }
        }
        return feederInfoList;
    }

    public void setDisplayCurrent(boolean displayCurrent) {
        this.displayCurrent = displayCurrent;
    }

    public void setDisplayArrowForCurrent(boolean displayArrowForCurrent) {
        this.displayArrowForCurrent = displayArrowForCurrent;
    }

    public void setDisplayPermanentLimitPercentage(boolean displayPermanentLimitPercentage) {
        this.displayPermanentLimitPercentage = displayPermanentLimitPercentage;
    }
}
