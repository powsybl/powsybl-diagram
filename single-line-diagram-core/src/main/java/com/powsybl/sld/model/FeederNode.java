/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederNode extends Node {

    private int order = -1;

    private BusCell.Direction direction = BusCell.Direction.UNDEFINED;

    public FeederNode(String id, String name, String componentType, boolean fictitious, Graph graph) {
        super(NodeType.FEEDER, id, name, componentType, fictitious, graph);
    }

    public static FeederNode createFictitious(Graph graph, String id) {
        return new FeederNode(id, id, NODE, true, graph);
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
}
