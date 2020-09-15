/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import sun.invoke.util.VerifyType;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.model.Side.RIGHT;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederNode extends Node {

    private final FeederType feederType;

    private int order = -1;

    private BusCell.Direction direction = BusCell.Direction.UNDEFINED;

    private Orientation orientation;

    private Side side; // in case of HORIZONTAL feeder

    protected FeederNode(String id, String name, String equipmentId, String componentType, boolean fictitious, Graph graph,
                         FeederType feederType, Orientation orientation) {
        super(NodeType.FEEDER, id, name, equipmentId, componentType, fictitious, graph);
        this.feederType = Objects.requireNonNull(feederType);
        this.orientation = orientation;
        this.side = RIGHT;
    }

    protected FeederNode(String id, String name, String equipmentId, String componentType, Graph graph,
                         FeederType feederType) {
        this(id, name, equipmentId, componentType, false, graph, feederType, Orientation.VERTICAL);
    }

    static FeederNode createFictitious(Graph graph, String id, Orientation orientation) {
        return new FeederNode(id, id, id, NODE, true, graph, FeederType.FICTITIOUS, orientation);
    }

    static FeederNode createFictitious(Graph graph, String id) {
        return createFictitious(graph, id, Orientation.VERTICAL);
    }

    public FeederType getFeederType() {
        return feederType;
    }

    @Override
    public void setCell(Cell cell) {
        if (!(cell instanceof ExternCell)) {
            throw new PowsyblException("The Cell of a feeder node shall be an ExternCell");
        }
        super.setCell(cell);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public BusCell.Direction getDirection() {
        return direction;
    }

    public void setDirection(BusCell.Direction direction) {
        this.direction = direction;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        generator.writeStringField("feederType", feederType.name());
        generator.writeNumberField("order", order);
        if (graph.isGenerateCoordsInJson()) {
            generator.writeStringField("direction", direction.name());
        }
    }
}
