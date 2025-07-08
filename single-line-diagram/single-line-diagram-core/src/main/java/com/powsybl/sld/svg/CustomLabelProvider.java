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
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import java.util.*;

import static com.powsybl.sld.model.coordinate.Direction.*;

/**
 * Enables the configuration of content displayed in the SLD, for labels and feeder infos.
 * <p>
 * Customizations are defined in the constructor's map parameters.
 *
 * <p>
 * The labels map defines what will be displayed for the equipments, and it is indexed by the equipment ID.
 * The custom content is declared through an SldCustomLabels record: the label will be displayed in the diagram at its standard position;
 * The additional label2will be displayed on the equipment's right.
 *
 * <p>
 * The feederInfosData map defines what will be displayed along the feeder, and it is indexed by the equipment ID.
 * The custom content is declared via as a list of SldCustomFeederInfos records: the componentType is the info component type name;
 * side determines at which side the feeder info is placed (for feeders with multiple sides e.g., lines transformers);
 * labelDirection determines the direction (e.g., IN or OUT for the arrows); label is the string displayed next to the info component.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomLabelProvider extends AbstractLabelProvider {

    private static final double LABEL2_OFFSET = 6d;

    public record SldCustomLabels(String label, String label2) {
        public SldCustomLabels(String label) {
            this(label, null);
        }

        public boolean hasLabel() {
            return label != null && !label.isEmpty();
        }

        public boolean hasLabel2() {
            return label2 != null && !label2.isEmpty();
        }
    }

    public record SldCustomFeederInfos(String componentType, NodeSide side, LabelDirection labelDirection, String label) {
    }

    private final Map<String, SldCustomLabels> labels;
    private final Map<String, List<SldCustomFeederInfos>> feederInfosData;

    public CustomLabelProvider(Map<String, SldCustomLabels> labels, Map<String, List<SldCustomFeederInfos>> feederInfosData,
                               SldComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        super(componentLibrary, layoutParameters, svgParameters);
        this.labels = Objects.requireNonNull(labels);
        this.feederInfosData = Objects.requireNonNull(feederInfosData);
    }

    private Optional<SldCustomLabels> getEquipmentLabel(Node node) {
        if (node instanceof EquipmentNode eqNode) {
            return Optional.ofNullable(labels.get(eqNode.getEquipmentId()));
        } else {
            return Optional.empty();
        }
    }

    private List<FeederInfo> getInjectionFeederInfos(FeederNode node) {
        List<SldCustomFeederInfos> eqFeederList = feederInfosData.getOrDefault(node.getEquipmentId(), List.of());
        List<FeederInfo> feederInfos = new ArrayList<>();
        eqFeederList.stream().forEach(eqFeeder ->
            feederInfos.add(new DirectionalFeederInfo(eqFeeder.componentType(), eqFeeder.labelDirection(), null, eqFeeder.label()))
        );
        return feederInfos;
    }

    private List<FeederInfo> getFeederWithSidesInfos(FeederNode node, FeederWithSides feeder) {
        List<SldCustomFeederInfos> eqFeederList = feederInfosData.getOrDefault(node.getEquipmentId(), List.of());
        List<FeederInfo> feederInfos = new ArrayList<>();
        eqFeederList.stream().forEach(eqFeeder -> {
            if (eqFeeder.side() == feeder.getSide()) {
                feederInfos.add(new DirectionalFeederInfo(eqFeeder.componentType(), eqFeeder.labelDirection(), null, eqFeeder.label()));
            }
        });
        return feederInfos;
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
            case BRANCH, HVDC:
                feederInfos = getFeederWithSidesInfos(node, (FeederWithSides) feeder);
                break;
            case TWO_WINDINGS_TRANSFORMER_LEG, THREE_WINDINGS_TRANSFORMER_LEG:
                feederInfos = getFeederWithSidesInfos(node, (FeederTwLeg) feeder);
                break;
            default:
                break;
        }
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
                    : ((int) (componentLibrary.getSize(node.getComponentType()).getHeight()) - 2 * LABEL2_OFFSET);
            positionName = direction == TOP ? "N" : "S";
            if (svgParameters.isLabelDiagonal()) {
                angle = direction == TOP ? -svgParameters.getAngleLabelShift() : svgParameters.getAngleLabelShift();
            }
        }
        double dx = (int) componentLibrary.getSize(node.getComponentType()).getWidth() + LABEL2_OFFSET;
        return new LabelPosition(positionName + "_LABEL2", dx, yShift, false, (int) angle);
    }

    private void addNodeLabels(SldCustomLabels labels, List<NodeLabel> nodeLabels, LabelPosition labelPosition, LabelPosition labelPosition2) {
        if (labels.hasLabel()) {
            nodeLabels.add(new NodeLabel(labels.label(), labelPosition, null));
        }
        if (labels.hasLabel2()) {
            nodeLabels.add(new NodeLabel(labels.label2(), labelPosition2, null));
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

