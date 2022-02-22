/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BasicStyleProvider implements DiagramStyleProvider {

    @Override
    public List<String> getSvgWireStyles(Graph graph, Edge edge, boolean highlightLineState) {
        List<String> styles = new ArrayList<>();
        styles.add(WIRE_STYLE_CLASS);
        getEdgeStyle(graph, edge).ifPresent(styles::add);
        if (highlightLineState) {
            getHighlightLineStateStyle(graph, edge).ifPresent(styles::add);
        }
        return styles;
    }

    /**
     * Return the style if any applied to given edge
     * @param edge the edge on which the style if any is applied to
     * @return the style if any
     */
    protected Optional<String> getEdgeStyle(Graph graph, Edge edge) {
        return Optional.empty();
    }

    /**
     * Return the highlight style if any to apply to given edge (if {@link LayoutParameters#isHighlightLineState()})
     * @param edge the edge on which the style if any is applied to
     * @return the highlight style if any
     */
    protected Optional<String> getHighlightLineStateStyle(Graph graph, Edge edge) {
        return Optional.empty();
    }

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
            styles.add(node.isOpen() ? DiagramStyles.OPEN_SWITCH_STYLE_CLASS : DiagramStyles.CLOSED_SWITCH_STYLE_CLASS);
        }

        return styles;
    }

    private static boolean isEquivalentToInternalNode(Node node) {
        return node.getComponentType().equals(ComponentTypeName.NODE);
    }

    @Override
    public List<String> getSvgNodeDecoratorStyles(DiagramLabelProvider.NodeDecorator nodeDecorator, Node node, ComponentLibrary componentLibrary) {
        List<String> styles = new ArrayList<>();
        componentLibrary.getComponentStyleClass(nodeDecorator.getType()).ifPresent(styles::add);
        return styles;
    }

    @Override
    public List<String> getZoneLineStyles(BranchEdge edge, ComponentLibrary componentLibrary) {
        List<String> styles = new ArrayList<>();
        styles.add(WIRE_STYLE_CLASS);
        return styles;
    }

    @Override
    public List<String> getSvgNodeSubcomponentStyles(Graph graph, Node node, String subComponentName) {
        return new ArrayList<>();
    }

    @Override
    public void reset() {
        // Nothing to reset for this implementation
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css");
    }

    @Override
    public List<URL> getCssUrls() {
        return getCssFilenames().stream().map(n -> getClass().getResource("/" + n))
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        return Collections.singletonList(NODE_INFOS);
    }
}
