/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Map;

import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Fictitious3WTNode extends FictitiousNode {

    private final Map<Feeder3WTNode.Side, String> idsLegs;
    private final Map<Feeder3WTNode.Side, Double> vNomsLegs;

    public Fictitious3WTNode(Graph graph, String id,
                             Map<Feeder3WTNode.Side, String> idsLegs,
                             Map<Feeder3WTNode.Side, Double> vNomsLegs) {
        super(graph, id, THREE_WINDINGS_TRANSFORMER);
        this.idsLegs = idsLegs;
        this.vNomsLegs = vNomsLegs;
    }

    public Map<Feeder3WTNode.Side, String> getIdsLegs() {
        return idsLegs;
    }

    public Map<Feeder3WTNode.Side, Double> getvNomsLegs() {
        return vNomsLegs;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);

        generator.writeArrayFieldStart("idsLegs");
        for (Map.Entry<Feeder3WTNode.Side, String> idL : idsLegs.entrySet()) {
            generator.writeStartObject();
            generator.writeObjectField(idL.getKey().name(), idL.getValue());
            generator.writeEndObject();
        }
        generator.writeEndArray();

        generator.writeArrayFieldStart("vNomsLegs");
        for (Map.Entry<Feeder3WTNode.Side, Double> vNomL : vNomsLegs.entrySet()) {
            generator.writeStartObject();
            generator.writeObjectField(vNomL.getKey().name(), vNomL.getValue());
            generator.writeEndObject();
        }
        generator.writeEndArray();
    }
}
