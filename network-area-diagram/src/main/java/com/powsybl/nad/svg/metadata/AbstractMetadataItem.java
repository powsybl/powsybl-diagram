/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractMetadataItem {
    private static final String DIAGRAM_ID_ATTRIBUTE = "svgId";
    private static final String EQUIPMENT_ID_ATTRIBUTE = "equipmentId";

    private final String svgId;
    private final String equipmentId;

    protected AbstractMetadataItem(String svgId, String equipmentId) {
        this.svgId = svgId;
        this.equipmentId = equipmentId;
    }

    abstract String getElementName();

    void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(DiagramMetadata.METADATA_PREFIX, getElementName(), DiagramMetadata.METADATA_NAMESPACE_URI);
        writer.writeAttribute(DIAGRAM_ID_ATTRIBUTE, svgId);
        writer.writeAttribute(EQUIPMENT_ID_ATTRIBUTE, equipmentId);
    }

    interface MetadataItemReader<I extends AbstractMetadataItem> {
        String getElementName();

        I read(XMLStreamReader reader);
    }

    static String readDiagramId(XMLStreamReader reader) {
        return reader.getAttributeValue(null, DIAGRAM_ID_ATTRIBUTE);
    }

    static String readEquipmentId(XMLStreamReader reader) {
        return reader.getAttributeValue(null, EQUIPMENT_ID_ATTRIBUTE);
    }
}
