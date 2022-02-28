/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.coordinate.Side;

import java.util.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface DiagramLabelProvider {

    class NodeLabel {
        private final String label;
        private final LabelPosition position;
        private final String userDefinedId;

        public NodeLabel(String label, LabelPosition labelPosition) {
            this(label, labelPosition, null);
        }

        public NodeLabel(String label, LabelPosition labelPosition, String userDefinedId) {
            this.label = label;
            this.position = labelPosition;
            this.userDefinedId = userDefinedId;
        }

        public String getLabel() {
            return label;
        }

        public LabelPosition getPosition() {
            return position;
        }

        public String getUserDefinedId() {
            return userDefinedId;
        }
    }

    class NodeDecorator {
        private final String type;
        private final LabelPosition position;

        /**
         * Creates a node decorator, with given type and position
         * @param type decorator type; corresponds to the type defined in components.json file
         * @param labelPosition position of the decorator relatively to the center of the decorated node
         */
        public NodeDecorator(String type, LabelPosition labelPosition) {
            this.type = type;
            this.position = labelPosition;
        }

        public String getType() {
            return type;
        }

        public LabelPosition getPosition() {
            return position;
        }

    }

    enum Direction {
        OUT, IN;
    }

    List<FeederInfo> getFeederInfos(FeederNode node);

    List<NodeLabel> getNodeLabels(Node node);

    default String getTooltip(Node node) {
        return "";
    }

    default String getTooltip(NodeDecorator decorator) {
        return "";
    }

    default String getTooltip(FeederInfo feederInfo) {
        return "";
    }

    default String getTooltip(BusInfo busInfo) {
        return "";
    }

    List<NodeDecorator> getNodeDecorators(Node node);

    default List<ElectricalNodeInfo> getElectricalNodesInfos(VoltageLevelGraph graph) {
        return Collections.emptyList();
    }

    default Optional<BusInfo> getBusInfo(BusNode node) {
        return Optional.empty();
    }

    default Map<String, Side> getBusInfoSides(VoltageLevelGraph graph) {
        Map<String, Side> result = new HashMap<>();
        graph.getNodeBuses().forEach(busNode -> getBusInfo(busNode).ifPresent(busInfo -> result.put(busNode.getId(), busInfo.getAnchor())));
        return result;
    }
}
