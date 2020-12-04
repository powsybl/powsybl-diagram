/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.InternalNode;
import com.powsybl.sld.model.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultDiagramStyleProvider implements DiagramStyleProvider {

    @Override
    public String getCssAdditionalInlineStyle() {
        return "";
    }

    @Override
    public List<String> getSvgWireStyles(Edge edge, boolean highlightLineState) {
        List<String> styles = new ArrayList<>();
        styles.add(WIRE_STYLE_CLASS);
        getEdgeStyle(edge).ifPresent(styles::add);
        if (highlightLineState) {
            getHighlightLineStateStyle(edge).ifPresent(styles::add);
        }
        return styles;
    }

    protected Optional<String> getEdgeStyle(Edge edge) {
        return Optional.empty();
    }

    protected Optional<String> getHighlightLineStateStyle(Edge edge) {
        return Optional.empty();
    }

    @Override
    public List<String> getSvgNodeStyles(Node node, boolean showInternalNodes) {

        List<String> styles = new ArrayList<>();
        styles.add(getNodeDiagramStyle(node));

        if (!showInternalNodes && node instanceof InternalNode) {
            styles.add(HIDDEN_INTERNAL_NODE_CLASS);
        }
        if (node.getType() == Node.NodeType.SWITCH) {
            styles.add(node.isOpen() ? DiagramStyles.OPEN_SWITCH_STYLE_CLASS : DiagramStyles.CLOSED_SWITCH_STYLE_CLASS);
        }

        return styles;
    }

    private String getNodeDiagramStyle(Node node) {
        String componentType = node.getComponentType();
        return componentType.toLowerCase().replace('_', '-'); //FIXME: Add style info to Component class / xml
    }

    @Override
    public List<String> getSvgNodeSubcomponentStyles(Node node, String subComponentName) {
        return new ArrayList<>();
    }

    @Override
    public void reset() {
        // Nothing to reset for this implementation
    }
}
