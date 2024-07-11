/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */

public class TextNodeMetadata extends AbstractMetadataItem {
    private static final String ELEMENT_NAME = "textNode";
    private static final String VL_NODE_ATTRIBUTE = "vlNode";
    private static final String POSITION_SHIFT_X_ATTRIBUTE = "shiftX";
    private static final String POSITION_SHIFT_Y_ATTRIBUTE = "shiftY";
    private static final String CONNECTION_SHIFT_X_ATTRIBUTE = "connectionShiftX";
    private static final String CONNECTION_SHIFT_Y_ATTRIBUTE = "connectionShiftY";

    private final String vlNodeId;
    private final String positionShiftX;
    private final String positionShiftY;
    private final String connectionShiftX;
    private final String connectionShiftY;

    public TextNodeMetadata(String svgId, String equipmentId, String vlNodeId, String positionShiftX, String positionShiftY,
                            String connectionShiftX, String connectionShiftY) {
        super(svgId, equipmentId);
        this.vlNodeId = vlNodeId;
        this.positionShiftX = positionShiftX;
        this.positionShiftY = positionShiftY;
        this.connectionShiftX = connectionShiftX;
        this.connectionShiftY = connectionShiftY;
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    void write(XMLStreamWriter writer) throws XMLStreamException {
        super.write(writer);
        writer.writeAttribute(VL_NODE_ATTRIBUTE, vlNodeId);
        writer.writeAttribute(POSITION_SHIFT_X_ATTRIBUTE, positionShiftX);
        writer.writeAttribute(POSITION_SHIFT_Y_ATTRIBUTE, positionShiftY);
        writer.writeAttribute(CONNECTION_SHIFT_X_ATTRIBUTE, connectionShiftX);
        writer.writeAttribute(CONNECTION_SHIFT_Y_ATTRIBUTE, connectionShiftY);
    }

    static class Reader implements AbstractMetadataItem.MetadataItemReader<TextNodeMetadata> {
        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        public TextNodeMetadata read(XMLStreamReader reader) {
            try {
                String diagramId = readDiagramId(reader);
                String equipmentId = readEquipmentId(reader);
                String vlNodeId = reader.getAttributeValue(null, VL_NODE_ATTRIBUTE);
                String positionShiftX = reader.getAttributeValue(null, POSITION_SHIFT_X_ATTRIBUTE);
                String positionShiftY = reader.getAttributeValue(null, POSITION_SHIFT_Y_ATTRIBUTE);
                String connectionShiftX = reader.getAttributeValue(null, CONNECTION_SHIFT_X_ATTRIBUTE);
                String connectionShiftY = reader.getAttributeValue(null, CONNECTION_SHIFT_Y_ATTRIBUTE);
                XmlUtil.readEndElementOrThrow(reader);
                return new TextNodeMetadata(diagramId, equipmentId, vlNodeId, positionShiftX, positionShiftY, connectionShiftX, connectionShiftY);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }
    }
}
