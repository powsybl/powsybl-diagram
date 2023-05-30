/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles;

import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.DirectionalFeederInfo;
import com.powsybl.sld.svg.FeederInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractStyleProvider implements StyleProvider {

    @Override
    public List<String> getNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {

        List<String> styles = new ArrayList<>();
        componentLibrary.getComponentStyleClass(node.getComponentType()).ifPresent(styles::add);

        if (graph != null) {
            Direction direction = graph.getDirection(node);
            if (node instanceof FeederNode && direction != Direction.UNDEFINED) {
                styles.add(direction == Direction.BOTTOM ? StyleClassConstants.BOTTOM_FEEDER : StyleClassConstants.TOP_FEEDER);
            }
        }
        if (!showInternalNodes && isEquivalentToInternalNode(node)) {
            styles.add(StyleClassConstants.HIDDEN_NODE_CLASS);
        }
        if (node.getType() == Node.NodeType.SWITCH) {
            styles.add(((SwitchNode) node).isOpen() ? StyleClassConstants.OPEN_SWITCH_STYLE_CLASS : StyleClassConstants.CLOSED_SWITCH_STYLE_CLASS);
        }
        if (node.isFictitious()) {
            styles.add(StyleClassConstants.FICTITIOUS_NODE_STYLE_CLASS);
        }

        if (node instanceof BusConnection) {
            if (node.isDisconnected()) {
                styles.add(StyleClassConstants.DISCONNECTED_BUS_CONNECTION_STYLE_CLASS);
            }
        }

        return styles;
    }

    private static boolean isEquivalentToInternalNode(Node node) {
        return node.getComponentType().equals(ComponentTypeName.NODE);
    }

    @Override
    public List<String> getNodeDecoratorStyles(DiagramLabelProvider.NodeDecorator nodeDecorator, Node node, ComponentLibrary componentLibrary) {
        return componentLibrary.getComponentStyleClass(nodeDecorator.getType())
                .map(List::of)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<String> getBranchEdgeStyles(BranchEdge edge, ComponentLibrary componentLibrary) {
        List<String> styles = new ArrayList<>();
        styles.add(StyleClassConstants.WIRE_STYLE_CLASS);
        return styles;
    }

    @Override
    public List<String> getCellStyles(Cell cell) {
        if (cell instanceof ExternCell) {
            return List.of(StyleClassConstants.EXTERN_CELL, StyleClassConstants.buildStyle(cell.getDirection()));
        }
        if (cell instanceof InternCell) {
            return List.of(StyleClassConstants.INTERN_CELL, StyleClassConstants.buildStyle(((InternCell) cell).getShape()));
        }
        if (cell instanceof ShuntCell) {
            return List.of(StyleClassConstants.SHUNT_CELL);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getFeederInfoStyles(FeederInfo info) {
        List<String> styles = new ArrayList<>();
        styles.add(StyleClassConstants.FEEDER_INFO);
        if (info instanceof DirectionalFeederInfo) {
            styles.add(((DirectionalFeederInfo) info).getDirection() == DiagramLabelProvider.LabelDirection.OUT ? StyleClassConstants.OUT_CLASS : StyleClassConstants.IN_CLASS);
        }
        return styles;
    }
}
