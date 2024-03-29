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

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */

public class BusNodeMetadata extends AbstractMetadataItem {
    private static final String ELEMENT_NAME = "busNode";

    public BusNodeMetadata(String svgId, String equipmentId) {
        super(svgId, equipmentId);
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
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
                XmlUtil.readEndElementOrThrow(reader);
                return new BusNodeMetadata(diagramId, equipmentId);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }
    }
}
