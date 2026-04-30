/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.sld.model.coordinate.Orientation;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class FeederNode extends EquipmentNode {

    private final Feeder feeder;
    private final Terminal terminal;

    public FeederNode(String id, String name, String equipmentId, String componentTypeName, boolean fictitious,
                         Feeder feeder, Orientation orientation) {
        this(id, name, equipmentId, componentTypeName, fictitious, feeder, orientation, null);
    }

    public FeederNode(String id, String name, String equipmentId, String componentTypeName, boolean fictitious, Feeder feeder, Orientation orientation, Terminal terminal) {
        super(NodeType.FEEDER, id, name, equipmentId, componentTypeName, fictitious);
        this.feeder = Objects.requireNonNull(feeder);
        this.terminal = terminal;
        setOrientation(orientation);
    }

    protected FeederNode(String id, String name, String equipmentId, String componentTypeName, Feeder feeder) {
        this(id, name, equipmentId, componentTypeName, false, feeder, null);
    }

    public Feeder getFeeder() {
        return feeder;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        generator.writeStringField("feederType", feeder.getFeederType().name());
        Optional<Integer> order = getOrder();
        if (order.isPresent()) {
            generator.writeNumberField("order", order.get());
        }
        if (includeCoordinates) {
            generator.writeStringField("direction", getDirection().name());
        }
        getFeeder().writeJsonContent(generator);
    }
}
