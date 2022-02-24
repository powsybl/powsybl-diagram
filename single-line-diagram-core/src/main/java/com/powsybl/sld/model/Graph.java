/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.coordinate.Direction;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public interface Graph {
    String getId();

    VoltageLevelGraph getVoltageLevel(String voltageLevelId);

    List<VoltageLevelGraph> getVoltageLevels();

    Stream<VoltageLevelGraph> getVoltageLevelStream();

    VoltageLevelGraph getVoltageLevelGraph(Node node);

    default VoltageLevelInfos getVoltageLevelInfos(Node node) {
        VoltageLevelGraph vlGraph = getVoltageLevelGraph(node);
        return vlGraph == null ? null : vlGraph.getVoltageLevelInfos();
    }

    default Point getShiftedPoint(Node node) {
        VoltageLevelGraph vlGraph = getVoltageLevelGraph(node);
        return vlGraph == null ? node.getCoordinates() : node.getCoordinates().getShiftedPoint(vlGraph.getCoord());
    }

    Map<Node, VoltageLevelGraph> getNodeToVlGraph();

    Optional<Cell> getCell(Node node);

    Direction getDirection(Node node);

    void addNode(VoltageLevelGraph vlGraph, Node node);

    void removeNode(Node node);

    Stream<Node> getAllNodesStream();

    BranchEdge addLineEdge(String lineId, Node n1, Node n2);

    List<BranchEdge> getLineEdges();

    void setCoordinatesSerialized(boolean coordinatesSerialized);

    double getWidth();

    double getHeight();

    void writeJson(Path file);

    void writeJson(Writer writer);
}
