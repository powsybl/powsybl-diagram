/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at franck.lecuyer@rte-france.com>
 */
public class DefaultDiagramLabelProvider implements DiagramLabelProvider {

    private static final double LABEL_OFFSET = 5d;

    private final Network network;
    private final ComponentLibrary componentLibrary;
    private final LayoutParameters layoutParameters;

    public DefaultDiagramLabelProvider(Network net, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        this.network = Objects.requireNonNull(net);
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
    }

    @Override
    public InitialValue getInitialValue(Node node) {
        Objects.requireNonNull(node);

        InitialValue initialValue = null;

        switch (node.getType()) {
            case BUS:
                initialValue = new InitialValue(null, null, node.getLabel(), null, null, null);
                break;
            case FEEDER:
                switch (((FeederNode) node).getFeederType()) {
                    case INJECTION:
                        initialValue = getInjectionInitialValue((FeederInjectionNode) node);
                        break;
                    case BRANCH:
                        initialValue = getBranchInitialValue((FeederBranchNode) node);
                        break;
                    case TWO_WINDINGS_TRANSFORMER_LEG:
                        initialValue = get2WTInitialValue((Feeder2WTLegNode) node);
                        break;
                    case THREE_WINDINGS_TRANSFORMER_LEG:
                        initialValue = get3WTInitialValue((Feeder3WTLegNode) node);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        return initialValue != null ? initialValue : new InitialValue(null, null, null, null, null, null);
    }

    private InitialValue getInjectionInitialValue(FeederInjectionNode node) {
        Injection injection = (Injection) network.getIdentifiable(node.getEquipmentId());
        if (injection != null) {
            return buildInitialValue(injection);
        }
        return null;
    }

    private InitialValue getBranchInitialValue(FeederBranchNode node) {
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            return buildInitialValue(branch, side);
        }
        return null;
    }

    private InitialValue get3WTInitialValue(Feeder3WTLegNode node) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(node.getSide().name());
            return buildInitialValue(transformer, side);
        }
        return null;
    }

    private InitialValue get2WTInitialValue(Feeder2WTLegNode node) {
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            return buildInitialValue(transformer, side);
        }
        return null;
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
        return Collections.emptyList();
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

    private InitialValue buildInitialValue(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        double p = transformer.getTerminal(side).getP();
        double q = transformer.getTerminal(side).getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }

    private InitialValue buildInitialValue(Injection injection) {
        Objects.requireNonNull(injection);
        double p = injection.getTerminal().getP();
        double q = injection.getTerminal().getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }

    private InitialValue buildInitialValue(Branch branch, Branch.Side side) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        double p = branch.getTerminal(side).getP();
        double q = branch.getTerminal(side).getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }
}
