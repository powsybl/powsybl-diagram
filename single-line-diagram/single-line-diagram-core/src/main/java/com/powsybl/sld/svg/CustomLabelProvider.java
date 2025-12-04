/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.EquipmentNode;
import com.powsybl.sld.model.nodes.Feeder;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.NodeSide;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static com.powsybl.sld.model.coordinate.Direction.UNDEFINED;

/**
 * Enables the configuration of content displayed in the SLD, for labels and feeder infos.
 * <p>
 * Customizations are defined in the constructor's map parameters.
 *
 * <p>
 * The labels map defines what will be displayed for the equipments, and it is indexed by the equipment ID.
 * The custom content is declared through an CustomLabels record: the label will be displayed in the diagram at its standard position;
 * The additionalLabel will be displayed on the equipment's right side.
 *
 * <p>
 * The feederInfosData map defines what will be displayed along the feeder, and it is indexed by the equipment ID (and a not-null side,
 * for feeders with sides such as lines and transformers), through the FeederContext record;
 * The custom content is declared via as a list of CustomFeederInfos records: the componentType is the info component type name;
 * labelDirection determines the direction (e.g., IN or OUT for the arrows); label is the string displayed next to the info component.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomLabelProvider extends AbstractLabelProvider {

    private static final double LABEL2_OFFSET = 6d;

    public record CustomLabels(String label, String additionalLabel) {
        public CustomLabels(String label) {
            this(label, null);
        }

        public boolean hasLabel() {
            return label != null && !label.isEmpty();
        }

        public boolean hasAdditionalLabel() {
            return additionalLabel != null && !additionalLabel.isEmpty();
        }
    }

    public record CustomFeederInfos(String componentType, LabelDirection labelDirection, String label) {
    }

    public record FeederContext(String feederId, NodeSide side) {
        public FeederContext(String feederId) {
            this(feederId, null);
        }
    }

    private final Map<String, CustomLabels> labels;

    private final Map<FeederContext, List<CustomFeederInfos>> feederInfosData;

    public CustomLabelProvider(Map<String, CustomLabels> labels, Map<FeederContext, List<CustomFeederInfos>> feederInfosData,
                               SldComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        super(componentLibrary, layoutParameters, svgParameters);
        this.labels = Objects.requireNonNull(labels);
        this.feederInfosData = Objects.requireNonNull(feederInfosData);
    }

    private Optional<CustomLabels> getEquipmentLabel(Node node) {
        if (node instanceof EquipmentNode eqNode) {
            return Optional.ofNullable(labels.get(eqNode.getEquipmentId()));
        } else {
            return Optional.empty();
        }
    }

    private List<FeederInfo> getCustomFeederInfos(FeederNode node, NodeSide side) {
        return feederInfosData.getOrDefault(new FeederContext(node.getEquipmentId(), side), List.of())
                .stream()
                .map(info -> new DirectionalFeederInfo(info.componentType(),
                        info.labelDirection(),
                        null,
                        info.label()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FeederInfo> getFeederInfos(FeederNode node) {
        Objects.requireNonNull(node);

        Feeder feeder = node.getFeeder();
        List<FeederInfo> feederInfos = switch (feeder.getFeederType()) {
            case INJECTION -> getCustomFeederInfos(node, null);
            case BRANCH, HVDC, TWO_WINDINGS_TRANSFORMER_LEG, THREE_WINDINGS_TRANSFORMER_LEG -> getCustomFeederInfos(node, ((FeederWithSides) feeder).getSide());
            default -> new ArrayList<>();
        };

        if (node.getDirection() == BOTTOM && !svgParameters.isFeederInfoSymmetry()) {
            Collections.reverse(feederInfos);
        }
        return feederInfos;
    }

    protected LabelPosition getAdditionalBusLabelPosition() {
        return new LabelPosition("NW_LABEL2", -LABEL2_OFFSET, 2 * LABEL2_OFFSET, false, 0);
    }

    protected LabelPosition getAdditionalLabelPosition(Node node, Direction direction) {
        double yShift = -LABEL2_OFFSET;
        String positionName = "";
        double angle = 0;
        if (direction != UNDEFINED) {
            yShift = direction == TOP
                    ? 2 * LABEL2_OFFSET
                    : ((int) (componentLibrary.getSize(node.getComponentType()).height()) - 2 * LABEL2_OFFSET);
            positionName = direction == TOP ? "N" : "S";
            if (svgParameters.isLabelDiagonal()) {
                angle = direction == TOP ? -svgParameters.getAngleLabelShift() : svgParameters.getAngleLabelShift();
            }
        }
        double dx = (int) componentLibrary.getSize(node.getComponentType()).width() + LABEL2_OFFSET;
        return new LabelPosition(positionName + "_LABEL2", dx, yShift, false, (int) angle);
    }

    private void addNodeLabels(CustomLabels labels, List<NodeLabel> nodeLabels, LabelPosition labelPosition, LabelPosition additionalLabelPosition) {
        if (labels.hasLabel()) {
            nodeLabels.add(new NodeLabel(labels.label(), labelPosition, null));
        }
        if (labels.hasAdditionalLabel()) {
            nodeLabels.add(new NodeLabel(labels.additionalLabel(), additionalLabelPosition, null));
        }
    }

    @Override
    public List<NodeLabel> getNodeLabels(Node node, Direction direction) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(direction);

        List<NodeLabel> nodeLabels = new ArrayList<>();
        if (node instanceof BusNode) {
            getEquipmentLabel(node).ifPresent(l ->
                    addNodeLabels(l, nodeLabels, getBusLabelPosition(), getAdditionalBusLabelPosition()));
        } else if (node instanceof FeederNode || svgParameters.isDisplayEquipmentNodesLabel() && node instanceof EquipmentNode) {
            getEquipmentLabel(node).ifPresent(l ->
                    addNodeLabels(l, nodeLabels, getLabelPosition(node, direction), getAdditionalLabelPosition(node, direction)));
        } else if (svgParameters.isDisplayConnectivityNodesId() && node instanceof ConnectivityNode) {
            nodeLabels.add(new NodeLabel(node.getId(), getLabelPosition(node, direction), null));
        }
        return nodeLabels;
    }

    @Override
    public List<NodeDecorator> getNodeDecorators(Node node, Direction direction) {
        return List.of();
    }
}

