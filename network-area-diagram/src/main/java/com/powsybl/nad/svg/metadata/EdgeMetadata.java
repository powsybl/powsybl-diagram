/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.nad.model.Identifiable;

import javax.xml.stream.XMLStreamReader;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EdgeMetadata extends AbstractMetadataItem {

    private static final String ELEMENT_NAME = "edge";

    public EdgeMetadata(String svgId, String equipmentId) {
        super(svgId, equipmentId);
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
    }

    static class Reader implements MetadataItemReader<EdgeMetadata> {
        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        public EdgeMetadata read(XMLStreamReader reader) {
            // Read edge-specific metadata
            // ...
            return new EdgeMetadata(readDiagramId(reader), readEquipmentId(reader));
        }
    }
}
