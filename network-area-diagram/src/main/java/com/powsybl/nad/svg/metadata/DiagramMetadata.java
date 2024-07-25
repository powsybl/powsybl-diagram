/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class DiagramMetadata {

    static final String METADATA_NAMESPACE_URI = "http://www.powsybl.org/schema/nad-metadata/1_0";
    static final String METADATA_PREFIX = "nad";

    private static final String METADATA_DIAGRAM_ELEMENT_NAME = "nad";
    private static final String METADATA_BUS_NODES_ELEMENT_NAME = "busNodes";
    private static final String METADATA_NODES_ELEMENT_NAME = "nodes";
    private static final String METADATA_EDGES_ELEMENT_NAME = "edges";
    private static final String METADATA_SVG_PARAMETERS_ELEMENT_NAME = "svgParameters";

    private final List<BusNodeMetadata> busNodesMetadata = new ArrayList<>();
    private final List<NodeMetadata> nodesMetadata = new ArrayList<>();
    private final List<EdgeMetadata> edgesMetadata = new ArrayList<>();
    private SvgParametersMetadata svgParametersMetadata;

    public static DiagramMetadata readFromSvg(InputStream inputStream) throws XMLStreamException {
        return readFromSvg(XMLInputFactory.newDefaultFactory().createXMLStreamReader(inputStream));
    }

    public static DiagramMetadata readFromSvg(XMLStreamReader reader) throws XMLStreamException {
        DiagramMetadata metadata = new DiagramMetadata();

        XmlUtil.readUntilStartElement("/svg/metadata/nad", reader, metadataToken ->
            XmlUtil.readSubElements(reader, token -> {
                switch (token) {
                    case METADATA_BUS_NODES_ELEMENT_NAME -> readCollection(metadata.busNodesMetadata, new BusNodeMetadata.Reader(), reader);
                    case METADATA_NODES_ELEMENT_NAME -> readCollection(metadata.nodesMetadata, new NodeMetadata.Reader(), reader);
                    case METADATA_EDGES_ELEMENT_NAME -> readCollection(metadata.edgesMetadata, new EdgeMetadata.Reader(), reader);
                    case METADATA_SVG_PARAMETERS_ELEMENT_NAME -> metadata.svgParametersMetadata = new SvgParametersMetadata.Reader().read(reader);
                    default -> throw new PowsyblException("Unexpected element '" + token + "' in metadata");
                }
            })
        );
        return metadata;
    }

    private static <M extends AbstractMetadataItem, R extends AbstractMetadataItem.MetadataItemReader<M>> void readCollection(
            Collection<M> items,
            R itemReader,
            XMLStreamReader reader) {
        XmlUtil.readSubElements(reader, token -> {
            if (token.equals(itemReader.getElementName())) {
                items.add(itemReader.read(reader));
            }
        });
    }

    public void writeXml(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(METADATA_PREFIX, METADATA_DIAGRAM_ELEMENT_NAME, METADATA_NAMESPACE_URI);
        writer.writeNamespace(METADATA_PREFIX, METADATA_NAMESPACE_URI);

        writeCollection(busNodesMetadata, METADATA_BUS_NODES_ELEMENT_NAME, writer);
        writeCollection(nodesMetadata, METADATA_NODES_ELEMENT_NAME, writer);
        writeCollection(edgesMetadata, METADATA_EDGES_ELEMENT_NAME, writer);
        if (svgParametersMetadata != null) {
            svgParametersMetadata.write(writer);
        } else {
            SvgParametersMetadata.writeEmpty(writer);
        }
        writer.writeEndElement();
    }

    private static <M extends AbstractMetadataItem> void writeCollection(
            Collection<M> items,
            String collectionElementName,
            XMLStreamWriter writer) throws XMLStreamException {
        if (items.isEmpty()) {
            writer.writeEmptyElement(METADATA_PREFIX, collectionElementName, METADATA_NAMESPACE_URI);
        } else {
            writer.writeStartElement(METADATA_PREFIX, collectionElementName, METADATA_NAMESPACE_URI);
            for (M item : items) {
                item.write(writer);
            }
            writer.writeEndElement();
        }
    }

    public void addBusNode(String svgId, String equipmentId, String nbNeighbours, String index, String vlNodeId) {
        busNodesMetadata.add(new BusNodeMetadata(svgId, equipmentId, nbNeighbours, index, vlNodeId));
    }

    public void addNode(String svgId, String equipmentId, String positionX, String positionY) {
        nodesMetadata.add(new NodeMetadata(svgId, equipmentId, positionX, positionY));
    }

    public void addEdge(String svgId, String equipmentId, String node1SvgId, String node2SvgId, String busNode1SvgId, String busNode2SvgId, String edgeType) {
        edgesMetadata.add(new EdgeMetadata(svgId, equipmentId, node1SvgId, node2SvgId, busNode1SvgId, busNode2SvgId, edgeType));
    }

    public void addSvgParameters(String insertNameDesc, String svgWidthAndHeightAdded, String cssLocation, String sizeConstraint, String fixedWidth,
                                 String fixedHeight, String fixedScale, String arrowShift, String arrowLabelShift, String converterStationWidth,
                                 String voltageLevelCircleRadius, String fictitiousVoltageLevelCircleRadius, String transformerCircleRadius,
                                 String nodeHollowWidth, String edgesForkLength, String edgesForkAperture, String edgeStartShift, String unknownBusNodeExtraRadius,
                                 String loopDistance, String loopEdgesAperture, String loopControlDistance, String edgeInfoAlongEdge, String edgeNameDisplayed,
                                 String interAnnulusSpace, String svgPrefix, String idDisplayed, String substationDescriptionDisplayed, String arrowHeight,
                                 String busLegend, String voltageLevelDetails, String languageTag, String voltageValuePrecision, String powerValuePrecision,
                                 String angleValuePrecision, String currentValuePrecision, String edgeInfoDisplayed, String pstArrowHeadSize, String undefinedValueSymbol) {
        svgParametersMetadata = new SvgParametersMetadata(insertNameDesc, svgWidthAndHeightAdded, cssLocation, sizeConstraint, fixedWidth, fixedHeight, fixedScale,
                                                          arrowShift, arrowLabelShift, converterStationWidth, voltageLevelCircleRadius, fictitiousVoltageLevelCircleRadius,
                                                          transformerCircleRadius, nodeHollowWidth, edgesForkLength, edgesForkAperture, edgeStartShift,
                                                          unknownBusNodeExtraRadius, loopDistance, loopEdgesAperture, loopControlDistance, edgeInfoAlongEdge,
                                                          edgeNameDisplayed, interAnnulusSpace, svgPrefix, idDisplayed, substationDescriptionDisplayed, arrowHeight,
                                                          busLegend, voltageLevelDetails, languageTag, voltageValuePrecision, powerValuePrecision, angleValuePrecision,
                                                          currentValuePrecision, edgeInfoDisplayed, pstArrowHeadSize, undefinedValueSymbol);
    }
}
