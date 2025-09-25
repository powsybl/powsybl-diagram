/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Point;

import java.io.IOException;

public class TeePointNode extends AbstractNode {

    private final String name;

    private final String equipmentId;

    public TeePointNode(String id, String name, String equipmentId, String componentTypeName) {
        super(NodeType.INTERNAL, id, componentTypeName, false);
        this.name = name;
        this.equipmentId = equipmentId;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        if (name != null) {
            generator.writeStringField("name", name);
        }
        if (equipmentId != null) {
            generator.writeStringField("equipmentId", equipmentId);
        }
    }

    public String getName() {
        return name;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    @Override
    public String toString() {
        return super.toString() + " " + name;
    }

    @Override
    public void setX(double x) {
        // TODO Auto-generated method stub
        super.setX(x);
    }

    @Override
    public void setY(double x) {
        // TODO Auto-generated method stub
        super.setY(x);
    }

    @Override
    public void setCoordinates(Point coord) {
        // TODO Auto-generated method stub
        super.setCoordinates(coord);
    }

    @Override
    public void setCoordinates(double x, double y) {
        // TODO Auto-generated method stub
        super.setCoordinates(x, y);
    }


}
