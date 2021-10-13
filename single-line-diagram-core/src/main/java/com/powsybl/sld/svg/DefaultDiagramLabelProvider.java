/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BranchStatus;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.*;

import java.util.*;

import static com.powsybl.sld.model.Node.NodeType.FEEDER;

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
    public List<FeederValue> getFeederValues(FeederNode node) {
        Objects.requireNonNull(node);

        List<FeederValue> feederValues = new ArrayList<>();

        switch (node.getFeederType()) {
            case INJECTION:
                feederValues = getInjectionFeederValues((FeederInjectionNode) node);
                break;
            case BRANCH:
                feederValues = getBranchFeederValues((FeederBranchNode) node);
                break;
            case TWO_WINDINGS_TRANSFORMER_LEG:
                feederValues = get2WTFeederValues((Feeder2WTLegNode) node);
                break;
            case THREE_WINDINGS_TRANSFORMER_LEG:
                feederValues = get3WTFeederValues((Feeder3WTLegNode) node);
                break;
            default:
                break;
        }
        if (node.getDirection() == BusCell.Direction.BOTTOM && !layoutParameters.isFeederArrowSymmetry()) {
            Collections.reverse(feederValues);
        }
        return feederValues;
    }

    private List<FeederValue> getInjectionFeederValues(FeederInjectionNode node) {
        List<FeederValue> measures = new ArrayList<>();
        Injection injection = (Injection) network.getIdentifiable(node.getEquipmentId());
        if (injection != null) {
            measures = buildFeederValues(injection);
        }
        return measures;
    }

    private List<FeederValue> getBranchFeederValues(FeederBranchNode node) {
        List<FeederValue> measures = new ArrayList<>();
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            measures = buildFeederValues(branch, side);
        }
        return measures;
    }

    private List<FeederValue> get3WTFeederValues(Feeder3WTLegNode node) {
        List<FeederValue> feederValues = new ArrayList<>();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(node.getSide().name());
            feederValues = buildFeederValues(transformer, side);
        }
        return feederValues;
    }

    private List<FeederValue> get2WTFeederValues(Feeder2WTLegNode node) {
        List<FeederValue> measures = new ArrayList<>();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            measures = buildFeederValues(transformer, side);
        }
        return measures;
    }

    @Override
    public List<NodeLabel> getNodeLabels(Node node) {
        Objects.requireNonNull(node);

        List<NodeLabel> nodeLabels = new ArrayList<>();
        if (node instanceof FeederNode) {
            nodeLabels.add(new NodeLabel(node.getLabel(), getFeederLabelPosition(node)));
        } else if (node instanceof BusNode) {
            nodeLabels.add(new NodeLabel(node.getLabel(), getBusLabelPosition(node)));
        }

        return nodeLabels;
    }

    @Override
    public List<NodeDecorator> getNodeDecorators(Node node) {
        Objects.requireNonNull(node);

        List<NodeDecorator> nodeDecorators = new ArrayList<>();
        if (node.getType() == FEEDER) {
            FeederType feederType = ((FeederNode) node).getFeederType();
            switch (feederType) {
                case BRANCH:
                case TWO_WINDINGS_TRANSFORMER_LEG:
                    addBranchStatusDecorator(nodeDecorators, node, network.getBranch(node.getEquipmentId()));
                    break;
                case THREE_WINDINGS_TRANSFORMER_LEG:
                    if (node.getAdjacentNodes().stream().noneMatch(Middle3WTNode.class::isInstance)) {
                        addBranchStatusDecorator(nodeDecorators, node, network.getThreeWindingsTransformer(node.getEquipmentId()));
                    }
                    break;
                default:
                    break;
            }
        } else if (node instanceof Middle3WTNode) {
            addBranchStatusDecorator(nodeDecorators, node, network.getThreeWindingsTransformer(node.getEquipmentId()));
        }

        return nodeDecorators;
    }

    private void addBranchStatusDecorator(List<NodeDecorator> nodeDecorators, Node node, Extendable e) {
        BranchStatus branchStatus = (BranchStatus) e.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            switch (branchStatus.getStatus()) {
                case PLANNED_OUTAGE:
                    nodeDecorators.add(getBranchStatusDecorator(node, PLANNED_OUTAGE_BRANCH_NODE_DECORATOR));
                    break;
                case FORCED_OUTAGE:
                    nodeDecorators.add(getBranchStatusDecorator(node, FORCED_OUTAGE_BRANCH_NODE_DECORATOR));
                    break;
                default:
                    break;
            }
        }
    }

    private NodeDecorator getBranchStatusDecorator(Node node, String decoratorType) {
        return (node instanceof Middle3WTNode) ?
                new NodeDecorator(decoratorType, getMiddle3WTDecoratorPosition((Middle3WTNode) node)) :
                new NodeDecorator(decoratorType, getFeederDecoratorPosition(node, decoratorType));
    }

    private LabelPosition getFeederDecoratorPosition(Node node, String componentType) {
        double yShift = -DECORATOR_OFFSET;
        String positionName = "";
        if (node.getCell() != null) {
            BusCell.Direction direction = ((BusCell) node.getCell()).getDirection();
            yShift = -direction.toOrientation().progressionSign() * PositionVoltageLevelLayout.getFeederSpan(layoutParameters);
            positionName = direction == BusCell.Direction.TOP ? "N" : "S";
        }

        return new LabelPosition(node.getId() + "_" + positionName + "_DECORATOR",
                (int) (componentLibrary.getSize(componentType).getWidth() / 2 + DECORATOR_OFFSET), yShift, true, 0);
    }

    private LabelPosition getMiddle3WTDecoratorPosition(Middle3WTNode node) {
        double yShift = -DECORATOR_OFFSET;
        String positionName = "";
        if (node.getCell() != null) {
            BusCell.Direction direction = ((BusCell) node.getCell()).getDirection();
            int excessHeight3wt = 10;
            yShift = direction.toOrientation().progressionSign() * (componentLibrary.getSize(node.getComponentType()).getHeight() - 1.5 * excessHeight3wt + DECORATOR_OFFSET);
            positionName = direction == BusCell.Direction.TOP ? "N" : "S";
        }

        return new LabelPosition(node.getId() + "_" + positionName + "_DECORATOR",
                0, yShift, true, 0);
    }

    private LabelPosition getFeederLabelPosition(Node node) {
        BusCell.Direction direction = node.getCell() != null
                ? ((BusCell) node.getCell()).getDirection()
                : BusCell.Direction.UNDEFINED;

        double yShift = -LABEL_OFFSET;
        String positionName = "";
        double angle = 0;
        if (node.getCell() != null) {
            yShift = direction == BusCell.Direction.TOP
                    ? -LABEL_OFFSET
                    : ((int) (componentLibrary.getSize(node.getComponentType()).getHeight()) + LABEL_OFFSET);
            positionName = direction == BusCell.Direction.TOP ? "N" : "S";
            if (layoutParameters.isLabelDiagonal()) {
                angle = direction == BusCell.Direction.TOP ? -layoutParameters.getAngleLabelShift() : layoutParameters.getAngleLabelShift();
            }
        }

        return new LabelPosition(node.getId() + "_" + positionName + "_LABEL",
                layoutParameters.isLabelCentered() ? 0 : -LABEL_OFFSET, yShift, layoutParameters.isLabelCentered(), (int) angle);
    }

    private LabelPosition getBusLabelPosition(Node node) {
        return new LabelPosition(node.getId() + "_NW_LABEL", -LABEL_OFFSET, -LABEL_OFFSET, false, 0);
    }

    private List<FeederValue> buildFeederValues(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        return Arrays.asList(
                new FeederValue(ARROW_ACTIVE, transformer.getTerminal(side).getP()),
                new FeederValue(ARROW_REACTIVE, transformer.getTerminal(side).getQ()));
    }

    private List<FeederValue> buildFeederValues(Injection injection) {
        return Arrays.asList(
                new FeederValue(ARROW_ACTIVE, injection.getTerminal().getP()),
                new FeederValue(ARROW_REACTIVE, injection.getTerminal().getQ()));
    }

    private List<FeederValue> buildFeederValues(Branch branch, Branch.Side side) {
        return Arrays.asList(
                new FeederValue(ARROW_ACTIVE, branch.getTerminal(side).getP()),
                new FeederValue(ARROW_REACTIVE, branch.getTerminal(side).getQ()));
    }
}
