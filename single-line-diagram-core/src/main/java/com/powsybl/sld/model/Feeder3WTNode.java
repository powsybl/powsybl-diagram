/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder3WTNode extends FeederNode {

    public enum Side {
        ONE,
        TWO,
        THREE;
    }

    private String transformerId;
    private Side side;

    public Feeder3WTNode(String id, String name, String componentType,
                         boolean fictitious, Graph graph,
                         String transformerId,
                         Side side) {
        super(id, name, componentType, fictitious, graph);
        this.transformerId = transformerId;
        this.side = side;
    }

    public String getId2() {
        String ret = null;
        switch (side) {
            case ONE: ret = Side.TWO.name(); break;
            case TWO: case THREE: ret = Side.ONE.name(); break;
        }
        return ret;
    }

    public String getId3() {
        String ret = null;
        switch (side) {
            case ONE: case TWO: ret = Side.THREE.name(); break;
            case THREE: ret = Side.TWO.name(); break;
        }
        return ret;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public Side getSide() {
        return side;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        generator.writeStringField("transformerId", transformerId);
        generator.writeStringField("transformerSide", side.name());
    }
}
