/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;

import java.util.*;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomLabelProvider extends AbstractLabelProvider {

    public record SldCustomFeederInfos(String componentType, NodeSide side, LabelDirection labelDirection, String label) {
    }

    private final Map<String, String> labels;
    private final Map<String, List<SldCustomFeederInfos>> feederInfosData;

    public CustomLabelProvider(Map<String, String> labels, Map<String, List<SldCustomFeederInfos>> feederInfosData,
                               ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        super(componentLibrary, layoutParameters, svgParameters);
        this.labels = Objects.requireNonNull(labels);
        this.feederInfosData = Objects.requireNonNull(feederInfosData);
    }

    private Optional<String> getEquipmentLabel(Node node) {
        if (node instanceof EquipmentNode eqNode) {
            String eqLabel = labels.getOrDefault(eqNode.getEquipmentId(), "");
            return Optional.ofNullable(eqLabel);
        } else {
            return Optional.of("");
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

    @Override
    public List<NodeLabel> getNodeLabels(Node node, Direction direction) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(direction);

        List<NodeLabel> nodeLabels = new ArrayList<>();
        if (node instanceof BusNode) {
            getEquipmentLabel(node).ifPresent(label -> nodeLabels.add(new NodeLabel(label, getBusLabelPosition(), null)));
        } else if (node instanceof FeederNode || svgParameters.isDisplayEquipmentNodesLabel() && node instanceof EquipmentNode) {
            getEquipmentLabel(node).ifPresent(label -> nodeLabels.add(new NodeLabel(label, getLabelPosition(node, direction), null)));
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

