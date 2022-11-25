/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Node;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DiagramMetadata {

    static final String METADATA_NAMESPACE_URI = "http://www.powsybl.org/schema/nad-metadata/1_0";
    static final String METADATA_PREFIX = "nad";

    private static final String METADATA_DIAGRAM_ELEMENT_NAME = "nad";
    private static final String METADATA_BUS_NODES_ELEMENT_NAME = "busNodes";
    private static final String METADATA_NODES_ELEMENT_NAME = "nodes";
    private static final String METADATA_EDGES_ELEMENT_NAME = "edges";

    private final List<BusNodeMetadata> busNodesMetadata = new ArrayList<>();
    private final List<NodeMetadata> nodesMetadata = new ArrayList<>();
    private final List<EdgeMetadata> edgesMetadata = new ArrayList<>();

    public static DiagramMetadata readXml(InputStream inputStream) throws XMLStreamException {
        return readXml(XMLInputFactory.newDefaultFactory().createXMLStreamReader(inputStream));
    }

    public static DiagramMetadata readXml(XMLStreamReader reader) throws XMLStreamException {
        DiagramMetadata metadata = new DiagramMetadata();

        XmlUtil.readUntilEndElement(METADATA_DIAGRAM_ELEMENT_NAME, reader, () -> {
            String token = reader.getLocalName();
            switch (token) {
                case METADATA_BUS_NODES_ELEMENT_NAME:
                    readCollection(metadata.busNodesMetadata, METADATA_BUS_NODES_ELEMENT_NAME, new BusNodeMetadata.Reader(), reader);
                    break;
                case METADATA_NODES_ELEMENT_NAME:
                    readCollection(metadata.nodesMetadata, METADATA_NODES_ELEMENT_NAME, new NodeMetadata.Reader(), reader);
                    break;
                case METADATA_EDGES_ELEMENT_NAME:
                    readCollection(metadata.edgesMetadata, METADATA_EDGES_ELEMENT_NAME, new EdgeMetadata.Reader(), reader);
                    break;
                default:
                    // Not managed
            }
        });
        return metadata;
    }

    private static <M extends AbstractMetadataItem, R extends AbstractMetadataItem.MetadataItemReader<M>> void readCollection(
            Collection<M> items,
            String collectionElementName,
            R itemReader,
            XMLStreamReader reader) throws XMLStreamException {
        XmlUtil.readUntilEndElement(collectionElementName, reader, () -> {
            if (reader.getLocalName().equals(itemReader.getElementName())) {
                items.add(itemReader.read(reader));
            }
        });
    }

    static class WritingContext {
        final XMLStreamWriter writer;
        final UnaryOperator<String> diagramIdToSvgId;

        WritingContext(UnaryOperator<String> diagramIdToSvgId, XMLStreamWriter writer) {
            this.writer = writer;
            this.diagramIdToSvgId = diagramIdToSvgId;
        }
    }

    public void writeXml(XMLStreamWriter writer) throws XMLStreamException {
        writeXml(new WritingContext(UnaryOperator.identity(), writer));
    }

    public void writeXml(UnaryOperator<String> diagramIdToSvgId, XMLStreamWriter writer) throws XMLStreamException {
        writeXml(new WritingContext(diagramIdToSvgId, writer));
    }

    public void writeXml(WritingContext ctx) throws XMLStreamException {
        ctx.writer.writeStartElement(METADATA_PREFIX, METADATA_DIAGRAM_ELEMENT_NAME, METADATA_NAMESPACE_URI);
        ctx.writer.writeNamespace(METADATA_PREFIX, METADATA_NAMESPACE_URI);

        writeCollection(busNodesMetadata, METADATA_BUS_NODES_ELEMENT_NAME, ctx);
        writeCollection(nodesMetadata, METADATA_NODES_ELEMENT_NAME, ctx);
        writeCollection(edgesMetadata, METADATA_EDGES_ELEMENT_NAME, ctx);
        ctx.writer.writeEndElement();
    }

    private static <M extends AbstractMetadataItem> void writeCollection(
            Collection<M> items,
            String collectionElementName,
            WritingContext ctx) throws XMLStreamException {
        if (items.isEmpty()) {
            ctx.writer.writeEmptyElement(METADATA_PREFIX, collectionElementName, METADATA_NAMESPACE_URI);
        } else {
            ctx.writer.writeStartElement(METADATA_PREFIX, collectionElementName, METADATA_NAMESPACE_URI);
            for (M item : items) {
                item.write(ctx);
            }
            ctx.writer.writeEndElement();
        }
    }

    public void addBusNode(BusNode node) {
        busNodesMetadata.add(new BusNodeMetadata(node));
    }

    public void addNode(Node node) {
        nodesMetadata.add(new NodeMetadata(node, node.getPosition()));
    }

    public void addEdge(Edge edge) {
        edgesMetadata.add(new EdgeMetadata(edge));
    }
}
