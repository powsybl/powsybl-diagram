/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class EquipmentNode extends AbstractNode {

    private final String name;

    private final String equipmentId;

    public EquipmentNode(NodeType type, String id, String name, String equipmentId, String componentTypeName, boolean fictitious) {
        super(type, id, componentTypeName, fictitious);
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
}
