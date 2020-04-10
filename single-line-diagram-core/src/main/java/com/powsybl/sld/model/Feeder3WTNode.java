/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder3WTNode extends FeederWithSideNode {

    public Feeder3WTNode(String id, String name, String equipmentId, String componentType,
                         boolean fictitious, Graph graph, Side side) {
        super(id, name, equipmentId, componentType, fictitious, graph, side);
    }

    public Side getSide2() {
        switch (side) {
            case ONE: return Side.TWO;
            case TWO: case THREE: return Side.ONE;
        }
        return null;
    }

    public Side getSide3() {
        switch (side) {
            case ONE: case TWO: return Side.THREE;
            case THREE: return Side.TWO;
        }
        return null;
    }
}
