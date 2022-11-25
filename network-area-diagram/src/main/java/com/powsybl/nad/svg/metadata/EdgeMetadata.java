/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.nad.model.Identifiable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EdgeMetadata extends AbstractMetadataItem {

    private static final String ELEMENT_NAME = "edge";
    private static final String NODE1_ATTRIBUTE = "node1";
    private static final String NODE2_ATTRIBUTE = "node2";

    private final String node1DiagramId;
    private final String node2DiagramId;

    public EdgeMetadata(Identifiable identifiable, String node1DiagramId, String node2DiagramId) {
        super(identifiable);
        this.node1DiagramId = node1DiagramId;
        this.node2DiagramId = node2DiagramId;
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    void write(DiagramMetadata.WritingContext ctx) throws XMLStreamException {
        super.write(ctx);
        ctx.writer.writeAttribute(NODE1_ATTRIBUTE, node1DiagramId);
        ctx.writer.writeAttribute(NODE2_ATTRIBUTE, node2DiagramId);
    }

    static class Reader implements MetadataItemReader<EdgeMetadata> {
        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        public EdgeMetadata read(XMLStreamReader reader) {
            Identifiable deserializedIdentifiable = readIdentifiable(reader);
            String id1 = reader.getAttributeValue(null, NODE1_ATTRIBUTE);
            String id2 = reader.getAttributeValue(null, NODE2_ATTRIBUTE);
            return new EdgeMetadata(deserializedIdentifiable, id1, id2);
        }
    }
}
