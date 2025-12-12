/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class CgmesZoneLayout extends AbstractCgmesLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesZoneLayout.class);

    private final ZoneGraph graph;
    private final List<VoltageLevelGraph> vlGraphs;

    public CgmesZoneLayout(ZoneGraph graph, Network network) {
        this(graph, network, null, DEFAULT_CGMES_SCALE_FACTOR);
    }

    public CgmesZoneLayout(ZoneGraph graph, Network network, String cgmesDiagramName, double cgmesScaleFactor) {
        super(network, cgmesDiagramName, cgmesScaleFactor);
        this.graph = Objects.requireNonNull(graph);
        vlGraphs = graph.getVoltageLevels();
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId()));
        }
        fixTransformersLabel = true;
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        if (graph.getZone().isEmpty()) {
            LOG.warn("No substations in the zone: skipping coordinates assignment");
            return;
        }
        if (!checkDiagram(cgmesDiagramName, "")) {
            return;
        }
        // assign coordinates
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId());
            setNodeCoordinates(vl, vlGraph, cgmesDiagramName);
        }
        for (BranchEdge edge : graph.getLineEdges()) {
            VoltageLevel vl = network.getVoltageLevel(graph.getVoltageLevelGraph(edge.getNode1()).getVoltageLevelInfos().getId());
            setLineCoordinates(vl, edge, cgmesDiagramName);
        }
        // shift and scale coordinates
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            vlGraph.getNodes().forEach(n -> shiftAndScaleNodeCoordinates(n, cgmesScaleFactor));
            vlGraph.addPaddingToCoord(layoutParam);
        }
        for (BranchEdge edge : graph.getLineEdges()) {
            shiftAndScaleLineCoordinates(edge, layoutParam);
        }

        for (SubstationGraph substationGraph : graph.getSubstations()) {
            setMultiNodesCoord(substationGraph);
        }

        setGraphSize(graph, layoutParam);
    }

    private void setLineCoordinates(VoltageLevel vl, BranchEdge edge, String diagramName) {
        Line line = vl.getConnectable(edge.getId(), Line.class);
        if (line == null) {
            LOG.warn("No line {} in voltage level {}, skipping line edge", edge.getId(), edge.getId());
            return;
        }
        LineDiagramData<Line> lineDiagramData = line.getExtension(LineDiagramData.class);
        if (lineDiagramData == null) {
            LOG.warn("No CGMES-DL data for line {} name {}, skipping line edge {}", line.getId(), line.getNameOrId(), edge.getId());
            return;
        }
        if (!lineDiagramData.getDiagramsNames().contains(diagramName)) {
            LOG.warn("No CGMES-DL data for line {} name {}, diagramName {}, skipping line edge {}", line.getId(), line.getNameOrId(), diagramName, edge.getId());
            return;
        }
        var diagramDataPoints = lineDiagramData.getPoints(diagramName);
        diagramDataPoints.forEach(this::setMinMax);
        var snakeLine = diagramDataPoints.stream()
                .map(point -> new Point(point.getX(), point.getY()))
                .toList();
        edge.setSnakeLine(snakeLine);

        if (TopologyKind.BUS_BREAKER.equals(line.getTerminal1().getVoltageLevel().getTopologyKind())) {
            // if bus breaker topology first and last point of lines are shifted
            DiagramPoint firstPoint = lineDiagramData.getFirstPoint(diagramName, LINE_OFFSET);
            edge.getSnakeLine().getFirst().setX(firstPoint.getX());
            edge.getSnakeLine().getFirst().setY(firstPoint.getY());
            DiagramPoint lastPoint = lineDiagramData.getLastPoint(diagramName, LINE_OFFSET);
            edge.getSnakeLine().getLast().setX(lastPoint.getX());
            edge.getSnakeLine().getLast().setY(lastPoint.getY());
        }
    }

    private void shiftAndScaleLineCoordinates(BranchEdge edge, LayoutParameters layoutParam) {
        var dPadding = layoutParam.getDiagramPadding();
        var vlPadding = layoutParam.getVoltageLevelPadding();
        var snakeLine = new ArrayList<>(edge.getSnakeLine());
        snakeLine.forEach(point -> {
            point.shiftX(-minX);
            point.shiftY(-minY);
            point.scale(cgmesScaleFactor);
            point.shiftX(dPadding.getLeft() + vlPadding.getLeft());
            point.shiftY(dPadding.getTop() + vlPadding.getTop());
        });
    }
}
