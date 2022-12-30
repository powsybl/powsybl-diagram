/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;

import java.util.*;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static com.powsybl.sld.model.coordinate.Direction.UNDEFINED;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractDiagramLabelProvider implements DiagramLabelProvider {

    private static final double LABEL_OFFSET = 5d;
    private static final double DECORATOR_OFFSET = 5d;

    protected final ComponentLibrary componentLibrary;
    protected final LayoutParameters layoutParameters;
    protected final ValueFormatter valueFormatter;

    protected AbstractDiagramLabelProvider(ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        this.valueFormatter = layoutParameters.createValueFormatter();
    }

    @Override
    public String getTooltip(Node node) {
        return node instanceof EquipmentNode ? ((EquipmentNode) node).getName() : null;
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
        if (node instanceof EquipmentNode) {
            EquipmentNode eqNode = (EquipmentNode) node;
            return Optional.ofNullable(node.getLabel().orElse(layoutParameters.isUseName() ? eqNode.getName() : eqNode.getEquipmentId()));
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

    protected LabelPosition getFeederLabelPosition(Node node, Direction direction) {
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

    protected LabelPosition getBusLabelPosition() {
        return new LabelPosition("NW_LABEL", -LABEL_OFFSET, -LABEL_OFFSET, false, 0);
    }

    @Override
    public List<ElectricalNodeInfo> getElectricalNodesInfos(VoltageLevelGraph graph) {
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
