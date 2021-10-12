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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public List<FeederMeasure> getFeederMeasures(FeederNode node) {
        Objects.requireNonNull(node);

        List<FeederMeasure> measures = new ArrayList<>();

        switch (node.getFeederType()) {
            case INJECTION:
                measures = getInjectionFeederMeasures((FeederInjectionNode) node);
                break;
            case BRANCH:
                measures = getBranchFeederMeasures((FeederBranchNode) node);
                break;
            case TWO_WINDINGS_TRANSFORMER_LEG:
                measures = get2WTFeederMeasures((Feeder2WTLegNode) node);
                break;
            case THREE_WINDINGS_TRANSFORMER_LEG:
                measures = get3WTFeederMeasures((Feeder3WTLegNode) node);
                break;
            default:
                break;
        }
        boolean symmetry = node.getDirection() == BusCell.Direction.TOP || layoutParameters.isFeederArrowSymmetry();
        if (!symmetry) {
            Collections.reverse(measures);
        }
        return measures;
    }

    private List<FeederMeasure> getInjectionFeederMeasures(FeederInjectionNode node) {
        List<FeederMeasure> measures = new ArrayList<>();
        Injection injection = (Injection) network.getIdentifiable(node.getEquipmentId());
        if (injection != null) {
            measures = buildFeederMeasures(injection);
        }
        return measures;
    }

    private List<FeederMeasure> getBranchFeederMeasures(FeederBranchNode node) {
        List<FeederMeasure> measures = new ArrayList<>();
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            measures = buildFeederMeasures(branch, side);
        }
        return measures;
    }

    private List<FeederMeasure> get3WTFeederMeasures(Feeder3WTLegNode node) {
        List<FeederMeasure> measures = new ArrayList<>();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(node.getSide().name());
            measures = buildFeederMeasures(transformer, side);
        }
        return measures;
    }

    private List<FeederMeasure> get2WTFeederMeasures(Feeder2WTLegNode node) {
        List<FeederMeasure> measures = new ArrayList<>();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            measures = buildFeederMeasures(transformer, side);
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

    private List<FeederMeasure> buildFeederMeasures(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        List<FeederMeasure> measures = new ArrayList<>();
        measures.add(new FeederMeasure(ARROW_ACTIVE, transformer.getTerminal(side).getP()));
        measures.add(new FeederMeasure(ARROW_REACTIVE, transformer.getTerminal(side).getQ()));
        return measures;
    }

    private List<FeederMeasure> buildFeederMeasures(Injection injection) {
        Objects.requireNonNull(injection);
        List<FeederMeasure> measures = new ArrayList<>();
        measures.add(new FeederMeasure(ARROW_ACTIVE, injection.getTerminal().getP()));
        measures.add(new FeederMeasure(ARROW_REACTIVE, injection.getTerminal().getQ()));
        return measures;
    }

    private List<FeederMeasure> buildFeederMeasures(Branch branch, Branch.Side side) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        List<FeederMeasure> measures = new ArrayList<>();
        measures.add(new FeederMeasure(ARROW_ACTIVE, branch.getTerminal(side).getP()));
        measures.add(new FeederMeasure(ARROW_REACTIVE, branch.getTerminal(side).getQ()));
        return measures;
    }
}
