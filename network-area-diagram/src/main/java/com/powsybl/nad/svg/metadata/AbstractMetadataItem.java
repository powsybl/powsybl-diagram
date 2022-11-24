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
import java.util.Optional;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractMetadataItem {
    private static final String DIAGRAM_ID_ATTRIBUTE = "diagramId";
    private static final String EQUIPMENT_ID_ATTRIBUTE = "equipmentId";

    private final Identifiable identifiable;

    protected AbstractMetadataItem(Identifiable identifiable) {
        this.identifiable = identifiable;
    }

    public Identifiable getIdentifiable() {
        return identifiable;
    }

    abstract String getElementName();

    void write(DiagramMetadata.WritingContext ctx) throws XMLStreamException {
        String elementName = ctx.overrideElementName ? ctx.elementName : getElementName();
        ctx.writer.writeEmptyElement(DiagramMetadata.METADATA_PREFIX, elementName, DiagramMetadata.METADATA_NAMESPACE_URI);
        ctx.writer.writeAttribute(DIAGRAM_ID_ATTRIBUTE, ctx.diagramIdToSvgId.apply(identifiable.getDiagramId()));
        ctx.writer.writeAttribute(EQUIPMENT_ID_ATTRIBUTE, identifiable.getEquipmentId());
    }

    interface MetadataItemReader<I extends AbstractMetadataItem> {
        String getElementName();

        I read(XMLStreamReader reader);
    }

    static Identifiable readIdentifiable(XMLStreamReader reader) {
        String diagramId = reader.getAttributeValue(null, DIAGRAM_ID_ATTRIBUTE);
        String equipmentId = reader.getAttributeValue(null, EQUIPMENT_ID_ATTRIBUTE);
        return new DeserializedIdentifiable(diagramId, equipmentId);
    }

    static class DeserializedIdentifiable implements Identifiable {

        private final String diagramId;
        private final String equipmentId;

        DeserializedIdentifiable(String diagramId, String equipmentId) {
            this.diagramId = diagramId;
            this.equipmentId = equipmentId;
        }

        @Override
        public String getDiagramId() {
            return diagramId;
        }

        @Override
        public String getEquipmentId() {
            return equipmentId;
        }

        @Override
        public Optional<String> getName() {
            return Optional.empty();
        }
    }
}
