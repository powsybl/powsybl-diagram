package com.powsybl.nad.svg.metadata;

import com.powsybl.nad.model.Identifiable;

import javax.xml.stream.XMLStreamReader;

public class BusNodeMetadata extends AbstractMetadataItem {
    private static final String ELEMENT_NAME = "busNode";

    public BusNodeMetadata(Identifiable identifiable) {
        super(identifiable);
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
            Identifiable deserializedIdentifiable = readIdentifiable(reader);
            // Read busNode-specific metadata
            // ...
            return new BusNodeMetadata(deserializedIdentifiable);
        }
    }
}
