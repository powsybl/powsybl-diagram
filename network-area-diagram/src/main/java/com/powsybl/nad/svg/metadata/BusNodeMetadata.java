package com.powsybl.nad.svg.metadata;

import javax.xml.stream.XMLStreamReader;

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
            return new BusNodeMetadata(readDiagramId(reader), readEquipmentId(reader));
        }
    }
}
