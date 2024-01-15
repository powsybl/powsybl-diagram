/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EdgeMetadata extends AbstractMetadataItem {

    private static final String ELEMENT_NAME = "edge";
    private static final String NODE1_ATTRIBUTE = "node1";
    private static final String NODE2_ATTRIBUTE = "node2";

    private final String node1SvgId;
    private final String node2SvgId;

    public EdgeMetadata(String svgId, String equipmentId, String node1SvgId, String node2SvgId) {
        super(svgId, equipmentId);
        this.node1SvgId = node1SvgId;
        this.node2SvgId = node2SvgId;
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    void write(XMLStreamWriter writer) throws XMLStreamException {
        super.write(writer);
        writer.writeAttribute(NODE1_ATTRIBUTE, node1SvgId);
        writer.writeAttribute(NODE2_ATTRIBUTE, node2SvgId);
    }

    static class Reader implements MetadataItemReader<EdgeMetadata> {
        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        public EdgeMetadata read(XMLStreamReader reader) {
            try {
                String svgId = readDiagramId(reader);
                String equipmentId = readEquipmentId(reader);
                String node1 = reader.getAttributeValue(null, NODE1_ATTRIBUTE);
                String node2 = reader.getAttributeValue(null, NODE2_ATTRIBUTE);
                XmlUtil.readEndElementOrThrow(reader);
                return new EdgeMetadata(svgId, equipmentId, node1, node2);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }
    }
}
