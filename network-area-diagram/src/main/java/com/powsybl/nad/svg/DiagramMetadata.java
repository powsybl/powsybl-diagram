/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.nad.model.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DiagramMetadata {

    private static final String METADATA_DIAGRAM_ELEMENT_NAME = "nad";
    private static final String METADATA_NAMESPACE_URI = "http://www.powsybl.org/schema/nad-metadata/1_0";
    private static final String METADATA_PREFIX = "nad";
    private static final String METADATA_BUS_NODES_ELEMENT_NAME = "busNodes";
    private static final String METADATA_NODES_ELEMENT_NAME = "nodes";
    private static final String METADATA_EDGES_ELEMENT_NAME = "edges";
    private static final String METADATA_BUS_NODE_ELEMENT_NAME = "busNode";
    private static final String METADATA_NODE_ELEMENT_NAME = "node";
    private static final String METADATA_EDGE_ELEMENT_NAME = "edge";
    private static final String DIAGRAM_ID_ATTRIBUTE = "diagramId";
    private static final String EQUIPMENT_ID_ATTRIBUTE = "equipmentId";
    private static final String POSITION_X_ATTRIBUTE = "x";
    private static final String POSITION_Y_ATTRIBUTE = "y";
    private static final String POSITION_COORD_FORMAT = "%.4f";

    private final List<NodeMetadata> busNodesMetadata = new ArrayList<>();
    private final List<NodeMetadata> nodesMetadata = new ArrayList<>();
    private final List<EdgeMetadata> edgesMetadata = new ArrayList<>();

    public static DiagramMetadata parseXml(InputStream inputStream) throws XMLStreamException {
        return parseXml(XMLInputFactory.newDefaultFactory().createXMLStreamReader(inputStream));
    }

    public static DiagramMetadata parseXml(XMLStreamReader reader) throws XMLStreamException {
        DiagramMetadata metadata = new DiagramMetadata();

        XmlUtil.readUntilEndElement(METADATA_DIAGRAM_ELEMENT_NAME, reader, () -> {
            String token = reader.getLocalName();
            switch (token) {
                case METADATA_BUS_NODES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_BUS_NODE_ELEMENT_NAME)) {
                            parseNodeMetadata(metadata.busNodesMetadata, reader);
                        }
                    });
                    break;
                case METADATA_NODES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_NODE_ELEMENT_NAME)) {
                            parseNodeMetadata(metadata.nodesMetadata, reader);
                        }
                    });
                    break;
                case METADATA_EDGES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_EDGE_ELEMENT_NAME)) {
                            parseEdgeMetadata(metadata.edgesMetadata, reader);
                        }
                    });
                    break;
                default:
                    // Not managed
            }
        });
        return metadata;
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

    private static void parseNodeMetadata(List<NodeMetadata> nodesMetadata, XMLStreamReader reader) {
        Identifiable identifiable = parseIdentifiable(reader);
        Point position = parsePoint(reader);
        nodesMetadata.add(new NodeMetadata(identifiable, position));
    }

    private static void parseEdgeMetadata(List<EdgeMetadata> edgesMetadata, XMLStreamReader reader) {
        Identifiable identifiable = parseIdentifiable(reader);
        // Parse edge-specific metadata
        // ...
        edgesMetadata.add(new EdgeMetadata(identifiable));
    }

    private static Identifiable parseIdentifiable(XMLStreamReader reader) {
        String diagramId = reader.getAttributeValue(null, DIAGRAM_ID_ATTRIBUTE);
        String equipmentId = reader.getAttributeValue(null, EQUIPMENT_ID_ATTRIBUTE);
        return new DeserializedIdentifiable(diagramId, equipmentId);
    }

    private static Point parsePoint(XMLStreamReader reader) {
        double x = parseDouble(reader.getAttributeValue(null, POSITION_X_ATTRIBUTE));
        double y = parseDouble(reader.getAttributeValue(null, POSITION_Y_ATTRIBUTE));
        return new Point(x, y);
    }

    private static double parseDouble(String s) {
        if (s == null || s.isEmpty()) {
            return Double.NaN;
        }
        return Double.parseDouble(s);
    }

    interface MetadataItemWriter<I extends IdentifiableMetadata> {
        // To avoid checked exceptions while writing specific data for metadata elements
        void write(I item, XMLStreamWriter writer) throws XMLStreamException;
    }

    public void writeXml(XMLStreamWriter writer) throws XMLStreamException {
        writeXml(UnaryOperator.identity(), writer);
    }

    public void writeXml(UnaryOperator<String> diagramIdToSvgId, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(METADATA_PREFIX, METADATA_DIAGRAM_ELEMENT_NAME, METADATA_NAMESPACE_URI);
        writer.writeNamespace(METADATA_PREFIX, METADATA_NAMESPACE_URI);
        writeCollection(METADATA_BUS_NODES_ELEMENT_NAME, METADATA_BUS_NODE_ELEMENT_NAME, busNodesMetadata, DiagramMetadata::writeNodeMetadata, diagramIdToSvgId, writer);
        writeCollection(METADATA_NODES_ELEMENT_NAME, METADATA_NODE_ELEMENT_NAME, nodesMetadata, DiagramMetadata::writeNodeMetadata, diagramIdToSvgId, writer);
        writeCollection(METADATA_EDGES_ELEMENT_NAME, METADATA_EDGE_ELEMENT_NAME, edgesMetadata, DiagramMetadata::writeEdgeMetadata, diagramIdToSvgId, writer);
        writer.writeEndElement();
    }

    private static <I extends IdentifiableMetadata> void writeCollection(
            String collectionElementName,
            String itemElementName,
            Collection<I> items,
            MetadataItemWriter<I> elemWriter,
            UnaryOperator<String> diagramIdToSvgId,
            XMLStreamWriter writer) throws XMLStreamException {
        if (items.isEmpty()) {
            writer.writeEmptyElement(METADATA_PREFIX, collectionElementName, METADATA_NAMESPACE_URI);
        } else {
            writer.writeStartElement(METADATA_PREFIX, collectionElementName, METADATA_NAMESPACE_URI);
            for (I item : items) {
                writer.writeEmptyElement(METADATA_PREFIX, itemElementName, METADATA_NAMESPACE_URI);
                writer.writeAttribute(DIAGRAM_ID_ATTRIBUTE, diagramIdToSvgId.apply(item.getIdentifiable().getDiagramId()));
                writer.writeAttribute(EQUIPMENT_ID_ATTRIBUTE, item.getIdentifiable().getEquipmentId());
                elemWriter.write(item, writer);
            }
            writer.writeEndElement();
        }
    }

    private static void writeNodeMetadata(NodeMetadata nodeMetadata, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(POSITION_X_ATTRIBUTE, formatted(nodeMetadata.getPosition().getX()));
        writer.writeAttribute(POSITION_Y_ATTRIBUTE, formatted(nodeMetadata.getPosition().getY()));
    }

    private static void writeEdgeMetadata(EdgeMetadata edgeMetadata, XMLStreamWriter writer) {
        // Write edge-specific metadata
    }

    private static String formatted(double value) {
        return String.format(POSITION_COORD_FORMAT, value);
    }

    public void addBusNode(BusNode node) {
        busNodesMetadata.add(new NodeMetadata(node, node.getPosition()));
    }

    public void addNode(Node node) {
        nodesMetadata.add(new NodeMetadata(node, node.getPosition()));
    }

    public void addEdge(Edge edge) {
        edgesMetadata.add(new EdgeMetadata(edge));
    }
}
