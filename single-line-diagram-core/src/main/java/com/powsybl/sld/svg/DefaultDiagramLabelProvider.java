/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BranchStatus;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Feeder2WTLegNode;
import com.powsybl.sld.model.nodes.Feeder3WTLegNode;
import com.powsybl.sld.model.nodes.FeederBranchNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.Middle3WTNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static com.powsybl.sld.model.nodes.Node.NodeType.FEEDER;
import static com.powsybl.sld.model.coordinate.Direction.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at franck.lecuyer@rte-france.com>
 */
public class DefaultDiagramLabelProvider implements DiagramLabelProvider {

    private static final double LABEL_OFFSET = 5d;
    private static final double DECORATOR_OFFSET = 5d;
    private static final String PLANNED_OUTAGE_BRANCH_NODE_DECORATOR = "LOCK";
    private static final String FORCED_OUTAGE_BRANCH_NODE_DECORATOR = "FLASH";

    private final Network network;
    private final ComponentLibrary componentLibrary;
    private final LayoutParameters layoutParameters;

    public DefaultDiagramLabelProvider(Network net, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        this.network = Objects.requireNonNull(net);
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
    }

    @Override
    public List<FeederInfo> getFeederInfos(FeederNode node) {
        Objects.requireNonNull(node);

        List<FeederInfo> feederInfos = new ArrayList<>();

        switch (node.getFeederType()) {
            case INJECTION:
                feederInfos = getInjectionFeederInfos(node);
                break;
            case BRANCH:
                feederInfos = getBranchFeederInfos((FeederBranchNode) node);
                break;
            case TWO_WINDINGS_TRANSFORMER_LEG:
                feederInfos = get2WTFeederInfos((Feeder2WTLegNode) node);
                break;
            case THREE_WINDINGS_TRANSFORMER_LEG:
                feederInfos = get3WTFeederInfos((Feeder3WTLegNode) node);
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

    private List<FeederInfo> getBranchFeederInfos(FeederBranchNode node) {
        List<FeederInfo> measures = new ArrayList<>();
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            measures = buildFeederInfos(branch, side);
        }
        return measures;
    }

    private List<FeederInfo> get3WTFeederInfos(Feeder3WTLegNode node) {
        List<FeederInfo> feederInfos = new ArrayList<>();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(node.getSide().name());
            feederInfos = buildFeederInfos(transformer, side);
        }
        return feederInfos;
    }

    private List<FeederInfo> get2WTFeederInfos(Feeder2WTLegNode node) {
        List<FeederInfo> measures = new ArrayList<>();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            measures = buildFeederInfos(transformer, side);
        }
        return measures;
    }

    @Override
    public List<NodeLabel> getNodeLabels(Node node, Direction direction) {
        Objects.requireNonNull(node);

        List<NodeLabel> nodeLabels = new ArrayList<>();
        if (node instanceof FeederNode) {
            getLabelOrNameOrId(node).ifPresent(label -> nodeLabels.add(new NodeLabel(label, getFeederLabelPosition(node, direction), null)));
        } else if (node instanceof BusNode) {
            getLabelOrNameOrId(node).ifPresent(label -> nodeLabels.add(new NodeLabel(label, getBusLabelPosition(), null)));
        }

        return nodeLabels;
    }

    private Optional<String> getLabelOrNameOrId(Node node) {
        return Optional.ofNullable(node.getLabel().orElse(layoutParameters.isUseName() ? node.getName() : node.getId()));
    }

    @Override
    public List<NodeDecorator> getNodeDecorators(Node node, Direction direction) {
        Objects.requireNonNull(node);

        List<NodeDecorator> nodeDecorators = new ArrayList<>();
        if (node.getType() == FEEDER) {
            FeederType feederType = ((FeederNode) node).getFeederType();
            switch (feederType) {
                case BRANCH:
                case TWO_WINDINGS_TRANSFORMER_LEG:
                    addBranchStatusDecorator(nodeDecorators, node, direction, network.getBranch(node.getEquipmentId()));
                    break;
                case THREE_WINDINGS_TRANSFORMER_LEG:
                    if (node.getAdjacentNodes().stream().noneMatch(Middle3WTNode.class::isInstance)) {
                        addBranchStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(node.getEquipmentId()));
                    }
                    break;
                default:
                    break;
            }
        } else if (node instanceof Middle3WTNode && ((Middle3WTNode) node).isEmbeddedInVlGraph()) {
            addBranchStatusDecorator(nodeDecorators, node, direction, network.getThreeWindingsTransformer(node.getEquipmentId()));
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
                new NodeDecorator(decoratorType, getFeederDecoratorPosition(node, direction, decoratorType));
    }

    private LabelPosition getFeederDecoratorPosition(Node node, Direction direction, String componentType) {
        double yShift = -DECORATOR_OFFSET;
        String positionName = "";
        if (direction != UNDEFINED) {
            yShift = -direction.toOrientation().progressionSign() * layoutParameters.getFeederSpan();
            positionName = direction == TOP ? "N" : "S";
        }

        return new LabelPosition(positionName + "_DECORATOR",
                (int) (componentLibrary.getSize(componentType).getWidth() / 2 + DECORATOR_OFFSET), yShift, true, 0);
    }

    private LabelPosition getMiddle3WTDecoratorPosition(Middle3WTNode node, Direction direction) {
        double yShift = -DECORATOR_OFFSET;
        String positionName = "";
        if (direction != UNDEFINED) {
            int excessHeight3wt = 10;
            yShift = direction.toOrientation().progressionSign() * (componentLibrary.getSize(node.getComponentType()).getHeight() - 1.5 * excessHeight3wt + DECORATOR_OFFSET);
            positionName = direction == TOP ? "N" : "S";
        }

        return new LabelPosition(positionName + "_DECORATOR",
                0, yShift, true, 0);
    }

    private LabelPosition getFeederLabelPosition(Node node, Direction direction) {
        double yShift = -LABEL_OFFSET;
        String positionName = "";
        double angle = 0;
        if (direction != UNDEFINED) {
            yShift = direction == TOP
                    ? -LABEL_OFFSET
                    : ((int) (componentLibrary.getSize(node.getComponentType()).getHeight()) + LABEL_OFFSET);
            positionName = direction == TOP ? "N" : "S";
            if (layoutParameters.isLabelDiagonal()) {
                angle = direction == TOP ? -layoutParameters.getAngleLabelShift() : layoutParameters.getAngleLabelShift();
            }
        }

        return new LabelPosition(positionName + "_LABEL",
                layoutParameters.isLabelCentered() ? 0 : -LABEL_OFFSET, yShift, layoutParameters.isLabelCentered(), (int) angle);
    }

    private LabelPosition getBusLabelPosition() {
        return new LabelPosition("NW_LABEL", -LABEL_OFFSET, -LABEL_OFFSET, false, 0);
    }

    private List<FeederInfo> buildFeederInfos(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        return Arrays.asList(
                new FeederInfo(ARROW_ACTIVE, transformer.getTerminal(side).getP()),
                new FeederInfo(ARROW_REACTIVE, transformer.getTerminal(side).getQ()));
    }

    private List<FeederInfo> buildFeederInfos(Injection injection) {
        return Arrays.asList(
                new FeederInfo(ARROW_ACTIVE, injection.getTerminal().getP()),
                new FeederInfo(ARROW_REACTIVE, injection.getTerminal().getQ()));
    }

    private List<FeederInfo> buildFeederInfos(Branch branch, Branch.Side side) {
        return Arrays.asList(
                new FeederInfo(ARROW_ACTIVE, branch.getTerminal(side).getP()),
                new FeederInfo(ARROW_REACTIVE, branch.getTerminal(side).getQ()));
    }
}
