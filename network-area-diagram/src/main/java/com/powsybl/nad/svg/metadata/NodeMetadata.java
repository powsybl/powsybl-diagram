/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.nad.model.Identifiable;
import com.powsybl.nad.model.Point;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.Locale;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class NodeMetadata extends AbstractMetadataItem {
    private static final String ELEMENT_NAME = "node";
    private static final String POSITION_X_ATTRIBUTE = "x";
    private static final String POSITION_Y_ATTRIBUTE = "y";
    private static final String POSITION_COORD_FORMAT = "%.2f";

    private final String positionX;
    private final String positionY;

    public NodeMetadata(String svgId, String equipmentId, String positionX, String positionY) {
        super(svgId, equipmentId);
        this.positionX = positionX;
        this.positionY = positionY;
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    void write(XMLStreamWriter writer) throws XMLStreamException {
        super.write(writer);
        writer.writeAttribute(POSITION_X_ATTRIBUTE, positionX);
        writer.writeAttribute(POSITION_Y_ATTRIBUTE, positionY);
    }

    static class Reader implements MetadataItemReader<NodeMetadata> {

        private final String elementName;

        Reader() {
            this.elementName = ELEMENT_NAME;
        }

        @Override
        public String getElementName() {
            return elementName;
        }

        public NodeMetadata read(XMLStreamReader reader) {
            return new NodeMetadata(readDiagramId(reader), readEquipmentId(reader),
                    reader.getAttributeValue(null, POSITION_X_ATTRIBUTE),
                    reader.getAttributeValue(null, POSITION_Y_ATTRIBUTE));
        }
    }
}
