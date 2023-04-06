/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractStyleProvider implements DiagramStyleProvider {

    @Override
    public List<String> getSvgNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {

        List<String> styles = new ArrayList<>();
        componentLibrary.getComponentStyleClass(node.getComponentType()).ifPresent(styles::add);

        if (graph != null) {
            Direction direction = graph.getDirection(node);
            if (node instanceof FeederNode && direction != Direction.UNDEFINED) {
                styles.add(direction == Direction.BOTTOM ? DiagramStyles.BOTTOM_FEEDER : DiagramStyles.TOP_FEEDER);
            }
        }
        if (!showInternalNodes && isEquivalentToInternalNode(node)) {
            styles.add(HIDDEN_NODE_CLASS);
        }
        if (node.getType() == Node.NodeType.SWITCH) {
            styles.add(((SwitchNode) node).isOpen() ? DiagramStyles.OPEN_SWITCH_STYLE_CLASS : DiagramStyles.CLOSED_SWITCH_STYLE_CLASS);
        }
        if (node.isFictitious()) {
            styles.add(FICTITIOUS_NODE_STYLE_CLASS);
        }

        return styles;
    }

    private static boolean isEquivalentToInternalNode(Node node) {
        return node.getComponentType().equals(ComponentTypeName.NODE);
    }

    @Override
    public List<String> getSvgNodeDecoratorStyles(DiagramLabelProvider.NodeDecorator nodeDecorator, Node node, ComponentLibrary componentLibrary) {
        return componentLibrary.getComponentStyleClass(nodeDecorator.getType())
                .map(List::of)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<String> getZoneLineStyles(BranchEdge edge, ComponentLibrary componentLibrary) {
        List<String> styles = new ArrayList<>();
        styles.add(WIRE_STYLE_CLASS);
        return styles;
    }

    @Override
    public List<String> getCellStyles(Cell cell) {
        if (cell instanceof ExternCell) {
            return List.of(EXTERN_CELL, buildStyle(cell.getDirection()));
        }
        if (cell instanceof InternCell) {
            return List.of(INTERN_CELL, buildStyle(((InternCell) cell).getShape()));
        }
        if (cell instanceof ShuntCell) {
            return List.of(SHUNT_CELL);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getFeederInfoStyles(FeederInfo info) {
        List<String> styles = new ArrayList<>();
        styles.add(FEEDER_INFO);
        if (info instanceof DirectionalFeederInfo) {
            styles.add(((DirectionalFeederInfo) info).getDirection() == DiagramLabelProvider.LabelDirection.OUT ? OUT_CLASS : IN_CLASS);
        }
        return styles;
    }
}
