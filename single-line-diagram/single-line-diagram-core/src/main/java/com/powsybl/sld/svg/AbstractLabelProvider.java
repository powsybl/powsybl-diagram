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
            getLabelOrNameOrId(node).ifPresent(label -> nodeLabels.add(new NodeLabel(label, getLabelPosition(node, direction), null)));
        } else if (svgParameters.isDisplayConnectivityNodesId() && node instanceof ConnectivityNode) {
            nodeLabels.add(new NodeLabel(node.getId(), getLabelPosition(node, direction), null));
        }

        return nodeLabels;
    }

    private Optional<String> getLabelOrNameOrId(Node node) {
        if (node instanceof EquipmentNode) {
            EquipmentNode eqNode = (EquipmentNode) node;
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
                (int) (componentLibrary.getSize(componentType).getWidth() / 2 + DECORATOR_OFFSET), yShift, true, 0);
    }

    protected LabelPosition getMiddle3WTDecoratorPosition(Middle3WTNode node, Direction direction) {
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

    protected LabelPosition getBusDecoratorPosition() {
        return new LabelPosition("BUS_DECORATOR", 35, -10, true, 0);
    }

    protected LabelPosition getLabelPosition(Node node, Direction direction) {
        double yShift = -LABEL_OFFSET;
        String positionName = "";
        double angle = 0;
        if (direction != UNDEFINED) {
            yShift = direction == TOP
                    ? -LABEL_OFFSET
                    : ((int) (componentLibrary.getSize(node.getComponentType()).getHeight()) + LABEL_OFFSET);
            positionName = direction == TOP ? "N" : "S";
            if (svgParameters.isLabelDiagonal()) {
                angle = direction == TOP ? -svgParameters.getAngleLabelShift() : svgParameters.getAngleLabelShift();
            }
        }

        return new LabelPosition(positionName + "_LABEL",
                svgParameters.isLabelCentered() ? 0 : -LABEL_OFFSET, yShift, svgParameters.isLabelCentered(), (int) angle);
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
        return new LabelPosition("NW_LABEL", -LABEL_OFFSET, -LABEL_OFFSET, false, 0);
    }

    @Override
    public List<BusLegendInfo> getBusLegendInfos(VoltageLevelGraph graph) {
        return Collections.emptyList();
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
