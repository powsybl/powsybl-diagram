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
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GraphMetadata {

    private static final String METADATA_NAMESPACE_URI = "http://www.powsybl.org/schema/nad-metadata/1_0";
    private static final String METADATA_PREFIX = "nad";
    private static final String METADATA_ELEMENT_NAME = "metadata";
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

    private final Map<String, String> busNodeIdByDiagramId = new LinkedHashMap<>();

    private final Map<String, String> nodeIdByDiagramId = new LinkedHashMap<>();

    private final Map<String, String> edgeIdByDiagramId = new LinkedHashMap<>();

    private final Map<String, Point> positionByEquipmentId = new HashMap<>();

    public static GraphMetadata parseXml(InputStream inputStream) throws XMLStreamException {
        return parseXml(XMLInputFactory.newDefaultFactory().createXMLStreamReader(inputStream));
    }

    public static GraphMetadata parseXml(XMLStreamReader reader) throws XMLStreamException {
        GraphMetadata metadata = new GraphMetadata();

        XmlUtil.readUntilEndElement(METADATA_ELEMENT_NAME, reader, () -> {
            String token = reader.getLocalName();
            switch (token) {
                case METADATA_BUS_NODES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_BUS_NODE_ELEMENT_NAME)) {
                            parseId(metadata.busNodeIdByDiagramId, reader);
                        }
                    });
                    break;
                case METADATA_NODES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_NODE_ELEMENT_NAME)) {
                            parseId(metadata.nodeIdByDiagramId, reader);
                        }
                    });
                    break;
                case METADATA_EDGES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_EDGE_ELEMENT_NAME)) {
                            parseId(metadata.edgeIdByDiagramId, reader);
                        }
                    });
                    break;
                default:
                    // Not managed
            }
        });
        return metadata;
    }

    private static void parseId(Map<String, String> ids, XMLStreamReader reader) {
        String diagramId = reader.getAttributeValue(null, DIAGRAM_ID_ATTRIBUTE);
        String equipmentId = reader.getAttributeValue(null, EQUIPMENT_ID_ATTRIBUTE);
        ids.put(diagramId, equipmentId);
    }

    public void writeXml(XMLStreamWriter writer) throws XMLStreamException {
        // Root element
        writer.writeStartElement(METADATA_ELEMENT_NAME);
        writer.writeNamespace(METADATA_PREFIX, METADATA_NAMESPACE_URI);
        // BusNodes
        writeIdMapping(METADATA_BUS_NODES_ELEMENT_NAME, METADATA_BUS_NODE_ELEMENT_NAME, busNodeIdByDiagramId, Collections.emptyMap(), writer);
        // Nodes
        writeIdMapping(METADATA_NODES_ELEMENT_NAME, METADATA_NODE_ELEMENT_NAME, nodeIdByDiagramId, positionByEquipmentId, writer);
        // Edges
        writeIdMapping(METADATA_EDGES_ELEMENT_NAME, METADATA_EDGE_ELEMENT_NAME, edgeIdByDiagramId, Collections.emptyMap(), writer);
        // End root element
        writer.writeEndElement();
    }

    private void writeIdMapping(String rootElementName, String tagElementName, Map<String, String> ids, Map<String, Point> positions, XMLStreamWriter writer) throws XMLStreamException {
        if (ids.entrySet().isEmpty()) {
            writer.writeEmptyElement(METADATA_PREFIX, rootElementName, METADATA_NAMESPACE_URI);
        } else {
            writer.writeStartElement(METADATA_PREFIX, rootElementName, METADATA_NAMESPACE_URI);
            for (Map.Entry<String, String> entry : ids.entrySet()) {
                writer.writeEmptyElement(METADATA_PREFIX, tagElementName, METADATA_NAMESPACE_URI);
                writer.writeAttribute(DIAGRAM_ID_ATTRIBUTE, entry.getKey());
                String equipmentId = entry.getValue();
                writer.writeAttribute(EQUIPMENT_ID_ATTRIBUTE, equipmentId);
                if (positions.containsKey(equipmentId)) {
                    Point p = positions.get(equipmentId);
                    writer.writeAttribute(POSITION_X_ATTRIBUTE, formatted(p.getX()));
                    writer.writeAttribute(POSITION_Y_ATTRIBUTE, formatted(p.getY()));
                }
            }
            writer.writeEndElement();
        }
    }

    private static String formatted(double value) {
        return String.format(POSITION_COORD_FORMAT, value);
    }

    public void addBusNode(BusNode node, UnaryOperator<String> diagramIdToSvgId) {
        addIdentifiable(busNodeIdByDiagramId, node, diagramIdToSvgId);
    }

    public void addNode(Node node, UnaryOperator<String> diagramIdToSvgId) {
        addIdentifiable(nodeIdByDiagramId, node, diagramIdToSvgId);
        positionByEquipmentId.put(node.getEquipmentId(), node.getPosition());
    }

    public void addEdge(Edge edge, UnaryOperator<String> diagramIdToSvgId) {
        addIdentifiable(edgeIdByDiagramId, edge, diagramIdToSvgId);
    }

    private void addIdentifiable(Map<String, String> map, Identifiable identifiable, UnaryOperator<String> diagramIdToSvgId) {
        Objects.requireNonNull(identifiable);
        Objects.requireNonNull(diagramIdToSvgId);
        map.put(diagramIdToSvgId.apply(identifiable.getDiagramId()), identifiable.getEquipmentId());
    }
}
