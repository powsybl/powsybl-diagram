/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;

import java.util.*;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static com.powsybl.sld.model.coordinate.Direction.UNDEFINED;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractLabelProvider implements LabelProvider {

    private static final double LABEL_OFFSET = 5d;
    private static final double DECORATOR_OFFSET = 5d;

    protected final SldComponentLibrary componentLibrary;
    protected final LayoutParameters layoutParameters;
    protected final ValueFormatter valueFormatter;
    protected final SvgParameters svgParameters;

    protected AbstractLabelProvider(SldComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        this.svgParameters = svgParameters;
        this.valueFormatter = svgParameters.createValueFormatter();
    }

    @Override
    public String getTooltip(Node node) {
        return node instanceof EquipmentNode ? ((EquipmentNode) node).getName() : null;
    }

    @Override
    public List<NodeLabel> getNodeLabels(Node node, Direction direction) {
        Objects.requireNonNull(node);

        List<NodeLabel> nodeLabels = new ArrayList<>();
        if (node instanceof BusNode) {
            getLabelOrNameOrId(node).ifPresent(label -> nodeLabels.add(new NodeLabel(label, getBusLabelPosition(), null)));
        } else if (node instanceof FeederNode || svgParameters.isDisplayEquipmentNodesLabel() && node instanceof EquipmentNode) {
            getLabelOrNameOrId(node).ifPresent(label -> nodeLabels.add(new NodeLabel(label, getFeederLabelPosition(node, direction), null)));
        } else if (svgParameters.isDisplayConnectivityNodesId() && node instanceof ConnectivityNode) {
            nodeLabels.add(new NodeLabel(node.getId(), getLabelPosition(node, direction), null));
        }

        return nodeLabels;
    }

    private Optional<String> getLabelOrNameOrId(Node node) {
        if (node instanceof EquipmentNode eqNode) {
            return Optional.ofNullable(node.getLabel().orElse(svgParameters.isUseName() ? eqNode.getName() : eqNode.getEquipmentId()));
        } else {
            return node.getLabel();
        }
    }

    protected LabelPosition getFeederDecoratorPosition(Direction direction, String componentType) {
        double yShift = -DECORATOR_OFFSET;
        String positionName = "";
        if (direction != UNDEFINED) {
            yShift = -direction.toOrientation().progressionSign() * layoutParameters.getFeederSpan();
            positionName = direction == TOP ? "N" : "S";
        }

        return new LabelPosition(positionName + "_DECORATOR",
                (int) (componentLibrary.getSize(componentType).width() / 2 + DECORATOR_OFFSET), yShift, true, 0);
    }

    protected LabelPosition getMiddle3WTDecoratorPosition(Middle3WTNode node, Direction direction) {
        double yShift = -DECORATOR_OFFSET;
        String positionName = "";
        if (direction != UNDEFINED) {
            int excessHeight3wt = 10;
            yShift = direction.toOrientation().progressionSign() * (componentLibrary.getSize(node.getComponentType()).height() - 1.5 * excessHeight3wt + DECORATOR_OFFSET);
            positionName = direction == TOP ? "N" : "S";
        }

        return new LabelPosition(positionName + "_DECORATOR",
                0, yShift, true, 0);
    }

    protected LabelPosition getBusDecoratorPosition() {
        return new LabelPosition("BUS_DECORATOR", 35, -10, true, 0);
    }

    protected LabelPosition getFeederLabelPosition(Node node, Direction direction) {
        return new LabelPosition(getLabelPositionName(direction), getLabelXShift(),
                getFeederLabelYShift(node, direction), svgParameters.isLabelCentered(), getLabelAngle(direction));
    }

    protected LabelPosition getLabelPosition(Node node, Direction direction) {
        return new LabelPosition(getLabelPositionName(direction), getLabelXShift(),
                getLabelYShift(node, direction), svgParameters.isLabelCentered(), getLabelAngle(direction));
    }

    private double getLabelXShift() {
        return svgParameters.isLabelCentered() ? 0 : -LABEL_OFFSET;
    }

    private double getLabelYShift(Node node, Direction direction) {
        return direction != TOP
                ? componentLibrary.getSize(node.getComponentType()).height() + LABEL_OFFSET
                : -LABEL_OFFSET;
    }

    private double getFeederLabelYShift(Node node, Direction direction) {
        if (direction == UNDEFINED) {
            return -LABEL_OFFSET;
        }

        // The FeederNode position is at the top-left position of the corresponding component.
        // We first shift to half the component height to be back at the center of the component, knowing that
        // all the FeederNode centers are on the same horizontal line.
        double shiftToFeederComponentCenter = componentLibrary.getSize(node.getComponentType()).height() / 2;

        // Then we add a shift of half the max component height to be just above/just below all feeder components.
        double shiftToFeederOut = (direction == TOP ? -1 : 1) * layoutParameters.getMaxComponentHeight() / 2;

        // And finally we add an offset to be slightly above a component whose height is equal to the max height
        double margin = (direction == TOP ? -1 : 1) * LABEL_OFFSET;

        return shiftToFeederComponentCenter + shiftToFeederOut + margin;
    }

    private int getLabelAngle(Direction direction) {
        if (!svgParameters.isLabelDiagonal()) {
            return 0;
        }
        return (int) Math.round((direction == TOP ? -1 : 1) * svgParameters.getAngleLabelShift());
    }

    private String getLabelPositionName(Direction direction) {
        return switch (direction) {
            case TOP -> "N_LABEL";
            case BOTTOM -> "S_LABEL";
            default -> "LABEL";
        };
    }

    protected LabelPosition getInternal2WTDecoratorPosition(Orientation orientation) {
        if (orientation.isHorizontal()) {
            return new LabelPosition("INTERNAL_2WT_DECORATOR", 0, -15, true, 0);
        } else {
            return new LabelPosition("INTERNAL_2WT_DECORATOR", 15, 0, true, 0);
        }
    }

    protected LabelPosition getGenericDecoratorPosition() {
        return new LabelPosition("GENERIC_DECORATOR", 0, 0, true, 0);
    }

    protected LabelPosition getBusLabelPosition() {
        int angle = svgParameters.isLabelDiagonal() ? (int) -svgParameters.getAngleLabelShift() : 0;
        return new LabelPosition("NW_LABEL", -LABEL_OFFSET, -LABEL_OFFSET, false, angle);
    }

    @Override
    public Optional<BusInfo> getBusInfo(BusNode node) {
        return Optional.empty();
    }

    @Override
    public Map<String, Side> getBusInfoSides(VoltageLevelGraph graph) {
        Map<String, Side> result = new HashMap<>();
        graph.getNodeBuses().forEach(busNode -> getBusInfo(busNode).ifPresent(busInfo -> result.put(busNode.getId(), busInfo.getAnchor())));
        return result;
    }
}
