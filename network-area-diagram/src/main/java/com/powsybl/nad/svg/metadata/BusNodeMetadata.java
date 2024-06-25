package com.powsybl.nad.svg.metadata;

/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */

public class BusNodeMetadata extends AbstractMetadataItem {
    private static final String ELEMENT_NAME = "busNode";
    private static final String NB_NEIGHBOURS_ATTRIBUTE = "nbNeighbours";
    private static final String INDEX_ATTRIBUTE = "index";
    private static final String VL_NODE_ATTRIBUTE = "vlNode";

    private final String nbNeighbours;
    private final String index;
    private final String vlNodeId;

    public BusNodeMetadata(String svgId, String equipmentId, String nbNeighbours, String index, String vlNodeId) {
        super(svgId, equipmentId);
        this.nbNeighbours = nbNeighbours;
        this.index = index;
        this.vlNodeId = vlNodeId;
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    void write(XMLStreamWriter writer) throws XMLStreamException {
        super.write(writer);
        writer.writeAttribute(NB_NEIGHBOURS_ATTRIBUTE, nbNeighbours);
        writer.writeAttribute(INDEX_ATTRIBUTE, index);
        writer.writeAttribute(VL_NODE_ATTRIBUTE, vlNodeId);
    }

    static class Reader implements AbstractMetadataItem.MetadataItemReader<BusNodeMetadata> {
        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        public BusNodeMetadata read(XMLStreamReader reader) {
            try {
                String diagramId = readDiagramId(reader);
                String equipmentId = readEquipmentId(reader);
                String nbNeighbours = reader.getAttributeValue(null, NB_NEIGHBOURS_ATTRIBUTE);
                String index = reader.getAttributeValue(null, INDEX_ATTRIBUTE);
                String vlNodeId = reader.getAttributeValue(null, VL_NODE_ATTRIBUTE);
                XmlUtil.readEndElementOrThrow(reader);
                return new BusNodeMetadata(diagramId, equipmentId, nbNeighbours, index, vlNodeId);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }
    }
}
