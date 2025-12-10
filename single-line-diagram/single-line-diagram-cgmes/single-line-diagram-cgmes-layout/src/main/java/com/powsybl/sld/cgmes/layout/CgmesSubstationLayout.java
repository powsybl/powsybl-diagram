/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class CgmesSubstationLayout extends AbstractCgmesLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesSubstationLayout.class);

    private final SubstationGraph graph;

    public CgmesSubstationLayout(SubstationGraph graph, Network network) {
        this(graph, network, null);
    }

    public CgmesSubstationLayout(SubstationGraph graph, Network network, String cgmesDiagramName) {
        super(cgmesDiagramName);
        this.network = Objects.requireNonNull(network);
        Objects.requireNonNull(graph);
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId()));
        }
        fixTransformersLabel = true;
        this.graph = graph;
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        String diagramName = getCgmesDiagramName();
        if (!checkDiagram(diagramName, "substation " + graph.getSubstationId())) {
            return;
        }
        LOG.info("Applying CGMES-DL layout to network {}, substation {}, diagram name {}", network.getId(), graph.getSubstationId(), diagramName);
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId());
            setNodeCoordinates(vl, vlGraph, diagramName);
        }
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            vlGraph.getNodes().forEach(this::shiftNodeCoordinates);
            if (layoutParam.getCgmesScaleFactor() != 1) {
                vlGraph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getCgmesScaleFactor()));
            }
            vlGraph.setCoord(layoutParam);
        }

        setMultiNodesCoord();

        setGraphSize(graph, layoutParam);
    }

    private void setMultiNodesCoord() {
        for (MiddleTwtNode multiNode : graph.getMultiTermNodes()) {
            var node = multiNode.getAdjacentNodes().getFirst();
            var multiNodeCoord = getCoordinatesInDiagram(node);
            multiNode.setCoordinates(multiNodeCoord);
            if (multiNode instanceof Middle3WTNode middle3WTNode) {
                List<List<Point>> snakeLines = multiNode.getAdjacentEdges().stream()
                        .map(e -> getSnakeLine(e, multiNode))
                        .toList();
                middle3WTNode.handle3wtNodeOrientation(snakeLines);
            }
        }
    }

    private List<Point> getSnakeLine(Edge e, Node node) {
        var oppositeNode = e.getOppositeNode(node);
        if (!getCoordinatesInDiagram(oppositeNode).equals(node.getCoordinates())) {
            return List.of(getCoordinatesInDiagram(oppositeNode), node.getCoordinates());
        }
        var adjacentNodes = oppositeNode.getAdjacentNodes();
        var nextNode = adjacentNodes.getFirst() == node ? adjacentNodes.get(1) : adjacentNodes.getFirst();
        Point nextNodeCoord = getCoordinatesInDiagram(nextNode);
        if (nextNode instanceof BusNode) {
            if (nextNode.getOrientation() == Orientation.UP) {
                nextNodeCoord.setY(node.getCoordinates().getY());
            } else {
                nextNodeCoord.setX(node.getCoordinates().getX());
            }
        }
        return List.of(nextNodeCoord, node.getCoordinates());
    }

    private Point getCoordinatesInDiagram(Node node) {
        var vlInfos = graph.getVoltageLevelInfos(node);
        var vlCoord = graph.getVoltageLevel(vlInfos.getId()).getCoord();
        return vlCoord.getShiftedPoint(node.getCoordinates());
    }
}
