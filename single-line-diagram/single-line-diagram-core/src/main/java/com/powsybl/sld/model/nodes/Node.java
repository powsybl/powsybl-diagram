/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>

 */
public interface Node {

    enum NodeType {
        BUS,
        FEEDER,
        INTERNAL,
        SWITCH
    }

    String getComponentType();

    boolean isFictitious();

    void setType(NodeType type);

    String getId();

    Optional<String> getLabel();

    void setLabel(String label);

    List<Node> getAdjacentNodes();

    List<Edge> getAdjacentEdges();

    void addAdjacentEdge(Edge e);

    void removeAdjacentEdge(Edge e);

    Point getCoordinates();

    void setCoordinates(Point coord);

    void setCoordinates(double x, double y);

    double getX();

    double getY();

    void setX(double x);

    void setY(double y);

    NodeType getType();

    Optional<Integer> getOrder();

    void setOrder(int order);

    void removeOrder();

    Direction getDirection();

    void setDirection(Direction direction);

    Orientation getOrientation();

    void setOrientation(Orientation orientation);

    void setOrientationFromBlock(Orientation blockOrientation, List<Node> blockNodes);

    boolean checkNodeSimilarity(Node n);

    boolean similarToAFeederNode(Node n);

    int getCardinality(VoltageLevelGraph vlGraph);

    void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException;
}
