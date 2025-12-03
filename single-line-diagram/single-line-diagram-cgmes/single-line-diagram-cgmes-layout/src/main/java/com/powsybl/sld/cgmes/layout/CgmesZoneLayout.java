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
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.network = Objects.requireNonNull(network);
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
        String diagramName = layoutParam.getCgmesDiagramName();
        if (!checkDiagram(diagramName, "")) {
            return;
        }
        // assign coordinates
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId());
            setNodeCoordinates(vl, vlGraph, diagramName, layoutParam.isCgmesUseNames());
        }
        for (BranchEdge edge : graph.getLineEdges()) {
            VoltageLevel vl = network.getVoltageLevel(graph.getVoltageLevelGraph(edge.getNode1()).getVoltageLevelInfos().getId());
            setLineCoordinates(vl, edge, diagramName);
        }
        // shift coordinates
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            vlGraph.getNodes().forEach(node -> shiftNodeCoordinates(node));
        }
        for (BranchEdge edge : graph.getLineEdges()) {
            shiftLineCoordinates(edge);
        }
        // scale coordinates
        if (layoutParam.getCgmesScaleFactor() != 1) {
            for (VoltageLevelGraph vlGraph : vlGraphs) {
                vlGraph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getCgmesScaleFactor()));
            }
            for (BranchEdge edge : graph.getLineEdges()) {
                scaleLineCoordinates(edge, layoutParam.getCgmesScaleFactor());
            }
        }
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
        List<Point> snakeLine = edge.getSnakeLine();
        lineDiagramData.getPoints(diagramName).forEach(point -> {
            snakeLine.add(new Point(point.getX(), point.getY()));
            setMinMax(point);
        });

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

    private void shiftLineCoordinates(BranchEdge edge) {
        Point shift = new Point(-minX, -minY);
        edge.getSnakeLine().forEach(p -> p.shift(shift));
    }

    private void scaleLineCoordinates(BranchEdge edge, double scaleFactor) {
        edge.getSnakeLine().forEach(p -> p.scale(scaleFactor));
    }

}
