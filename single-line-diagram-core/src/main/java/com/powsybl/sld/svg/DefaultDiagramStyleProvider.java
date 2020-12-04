/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.svg.DiagramStyles.HIDDEN_INTERNAL_NODE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;

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
        styles.add(getEdgeStyle(edge));
        if (highlightLineState) {
            String highlightLineStateStyle = getHighlightLineStateStyle(edge);
            if (!highlightLineStateStyle.isEmpty()) {
                styles.add(highlightLineStateStyle);
            }
        }
        return styles;
    }

    protected String getEdgeStyle(Edge edge) {
        return "";
    }

    protected String getHighlightLineStateStyle(Edge edge) {
        return "";
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
        return componentType.toLowerCase().replace('_', '-'); //TODO: Add style info to Component class / xml
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
