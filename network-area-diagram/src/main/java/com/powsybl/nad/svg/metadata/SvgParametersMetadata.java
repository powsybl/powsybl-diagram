/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public class SvgParametersMetadata {

    private static final String ELEMENT_NAME = "svgParameters";
    private static final String INSERT_NAME_DESC = "insertNameDesc";
    private static final String SVG_WIDTH_AND_HEIGHT_ADDED = "svgWidthAndHeightAdded";
    private static final String CSS_LOCATION = "cssLocation";
    private static final String SIZE_CONSTRAINT = "sizeConstraint";
    private static final String FIXED_WIDTH = "fixedWidth";
    private static final String FIXED_HEIGHT = "fixedHeight";
    private static final String FIXED_SCALE = "fixedScale";
    private static final String ARROW_SHIFT = "arrowShift";
    private static final String ARROW_LABEL_SHIFT = "arrowLabelShift";
    private static final String CONVERTER_STATION_WIDTH = "converterStationWidth";
    private static final String VOLTAGE_LEVEL_CIRCLE_RADIUS = "voltageLevelCircleRadius";
    private static final String FICTITIOUS_VOLTAGE_LEVEL_CIRCLE_RADIUS = "fictitiousVoltageLevelCircleRadius";
    private static final String TRANSFORMER_CIRCLE_RADIUS = "transformerCircleRadius";
    private static final String NODE_HOLLOW_WIDTH = "nodeHollowWidth";
    private static final String EDGES_FORK_LENGTH = "edgesForkLength";
    private static final String EDGES_FORK_APERTURE = "edgesForkAperture";
    private static final String EDGE_START_SHIFT = "edgeStartShift";
    private static final String UNKNOWN_BUS_NODE_EXTRA_RADIUS = "unknownBusNodeExtraRadius";
    private static final String LOOP_DISTANCE = "loopDistance";
    private static final String LOOP_EDGES_APERTURE = "loopEdgesAperture";
    private static final String LOOP_CONTROL_DISTANCE = "loopControlDistance";
    private static final String EDGE_INFO_ALONG_EDGE = "edgeInfoAlongEdge";
    private static final String EDGE_NAME_DISPLAYED = "edgeNameDisplayed";
    private static final String INTER_ANNULUS_SPACE = "interAnnulusSpace";
    private static final String SVG_PREFIX = "svgPrefix";
    private static final String ID_DISPLAYED = "idDisplayed";
    private static final String SUBSTATION_DESCRIPTION_DISPLAYED = "substationDescriptionDisplayed";
    private static final String ARROW_HEIGHT = "arrowHeight";
    private static final String BUS_LEGEND = "busLegend";
    private static final String VOLTAGE_LEVEL_DETAILS = "voltageLevelDetails";
    private static final String LANGUAGE_TAG = "languageTag";
    private static final String VOLTAGE_VALUE_PRECISION = "voltageValuePrecision";
    private static final String POWER_VALUE_PRECISION = "powerValuePrecision";
    private static final String ANGLE_VALUE_PRECISION = "angleValuePrecision";
    private static final String CURRENT_VALUE_PRECISION = "currentValuePrecision";
    private static final String EDGE_INFO_DISPLAYED = "edgeInfoDisplayed";
    private static final String PST_ARROW_HEAD_SIZE = "pstArrowHeadSize";
    private static final String UNDEFINED_VALUE_SYMBOL = "undefinedValueSymbol";

    private final String insertNameDesc;
    private final String svgWidthAndHeightAdded;
    private final String cssLocation;
    private final String sizeConstraint;
    private final String fixedWidth;
    private final String fixedHeight;
    private final String fixedScale;
    private final String arrowShift;
    private final String arrowLabelShift;
    private final String converterStationWidth;
    private final String voltageLevelCircleRadius;
    private final String fictitiousVoltageLevelCircleRadius;
    private final String transformerCircleRadius;
    private final String nodeHollowWidth;
    private final String edgesForkLength;
    private final String edgesForkAperture;
    private final String edgeStartShift;
    private final String unknownBusNodeExtraRadius;
    private final String loopDistance;
    private final String loopEdgesAperture;
    private final String loopControlDistance;
    private final String edgeInfoAlongEdge;
    private final String edgeNameDisplayed;
    private final String interAnnulusSpace;
    private final String svgPrefix;
    private final String idDisplayed;
    private final String substationDescriptionDisplayed;
    private final String arrowHeight;
    private final String busLegend;
    private final String voltageLevelDetails;
    private final String languageTag;
    private final String voltageValuePrecision;
    private final String powerValuePrecision;
    private final String angleValuePrecision;
    private final String currentValuePrecision;
    private final String edgeInfoDisplayed;
    private final String pstArrowHeadSize;
    private final String undefinedValueSymbol;

    public SvgParametersMetadata(String insertNameDesc, String svgWidthAndHeightAdded, String cssLocation,
                                 String sizeConstraint, String fixedWidth, String fixedHeight, String fixedScale,
                                 String arrowShift, String arrowLabelShift, String converterStationWidth,
                                 String voltageLevelCircleRadius, String fictitiousVoltageLevelCircleRadius,
                                 String transformerCircleRadius, String nodeHollowWidth, String edgesForkLength,
                                 String edgesForkAperture, String edgeStartShift, String unknownBusNodeExtraRadius,
                                 String loopDistance, String loopEdgesAperture, String loopControlDistance,
                                 String edgeInfoAlongEdge, String edgeNameDisplayed, String interAnnulusSpace,
                                 String svgPrefix, String idDisplayed, String substationDescriptionDisplayed,
                                 String arrowHeight, String busLegend, String voltageLevelDetails,
                                 String languageTag, String voltageValuePrecision, String powerValuePrecision,
                                 String angleValuePrecision, String currentValuePrecision, String edgeInfoDisplayed,
                                 String pstArrowHeadSize, String undefinedValueSymbol) {
        this.insertNameDesc = insertNameDesc;
        this.svgWidthAndHeightAdded = svgWidthAndHeightAdded;
        this.cssLocation = cssLocation;
        this.sizeConstraint = sizeConstraint;
        this.fixedWidth = fixedWidth;
        this.fixedHeight = fixedHeight;
        this.fixedScale = fixedScale;
        this.arrowShift = arrowShift;
        this.arrowLabelShift = arrowLabelShift;
        this.converterStationWidth = converterStationWidth;
        this.voltageLevelCircleRadius = voltageLevelCircleRadius;
        this.fictitiousVoltageLevelCircleRadius = fictitiousVoltageLevelCircleRadius;
        this.transformerCircleRadius = transformerCircleRadius;
        this.nodeHollowWidth = nodeHollowWidth;
        this.edgesForkLength = edgesForkLength;
        this.edgesForkAperture = edgesForkAperture;
        this.edgeStartShift = edgeStartShift;
        this.unknownBusNodeExtraRadius = unknownBusNodeExtraRadius;
        this.loopDistance = loopDistance;
        this.loopEdgesAperture = loopEdgesAperture;
        this.loopControlDistance = loopControlDistance;
        this.edgeInfoAlongEdge = edgeInfoAlongEdge;
        this.edgeNameDisplayed = edgeNameDisplayed;
        this.interAnnulusSpace = interAnnulusSpace;
        this.svgPrefix = svgPrefix;
        this.idDisplayed = idDisplayed;
        this.substationDescriptionDisplayed = substationDescriptionDisplayed;
        this.arrowHeight = arrowHeight;
        this.busLegend = busLegend;
        this.voltageLevelDetails = voltageLevelDetails;
        this.languageTag = languageTag;
        this.voltageValuePrecision = voltageValuePrecision;
        this.powerValuePrecision = powerValuePrecision;
        this.angleValuePrecision = angleValuePrecision;
        this.currentValuePrecision = currentValuePrecision;
        this.edgeInfoDisplayed = edgeInfoDisplayed;
        this.pstArrowHeadSize = pstArrowHeadSize;
        this.undefinedValueSymbol = undefinedValueSymbol;
    }

    String getElementName() {
        return ELEMENT_NAME;
    }

    void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(DiagramMetadata.METADATA_PREFIX, getElementName(), DiagramMetadata.METADATA_NAMESPACE_URI);
        writer.writeAttribute(INSERT_NAME_DESC, insertNameDesc);
        writer.writeAttribute(SVG_WIDTH_AND_HEIGHT_ADDED, svgWidthAndHeightAdded);
        writer.writeAttribute(CSS_LOCATION, cssLocation);
        writer.writeAttribute(SIZE_CONSTRAINT, sizeConstraint);
        writer.writeAttribute(FIXED_WIDTH, fixedWidth);
        writer.writeAttribute(FIXED_HEIGHT, fixedHeight);
        writer.writeAttribute(FIXED_SCALE, fixedScale);
        writer.writeAttribute(ARROW_SHIFT, arrowShift);
        writer.writeAttribute(ARROW_LABEL_SHIFT, arrowLabelShift);
        writer.writeAttribute(CONVERTER_STATION_WIDTH, converterStationWidth);
        writer.writeAttribute(VOLTAGE_LEVEL_CIRCLE_RADIUS, voltageLevelCircleRadius);
        writer.writeAttribute(FICTITIOUS_VOLTAGE_LEVEL_CIRCLE_RADIUS, fictitiousVoltageLevelCircleRadius);
        writer.writeAttribute(TRANSFORMER_CIRCLE_RADIUS, transformerCircleRadius);
        writer.writeAttribute(NODE_HOLLOW_WIDTH, nodeHollowWidth);
        writer.writeAttribute(EDGES_FORK_LENGTH, edgesForkLength);
        writer.writeAttribute(EDGES_FORK_APERTURE, edgesForkAperture);
        writer.writeAttribute(EDGE_START_SHIFT, edgeStartShift);
        writer.writeAttribute(UNKNOWN_BUS_NODE_EXTRA_RADIUS, unknownBusNodeExtraRadius);
        writer.writeAttribute(LOOP_DISTANCE, loopDistance);
        writer.writeAttribute(LOOP_EDGES_APERTURE, loopEdgesAperture);
        writer.writeAttribute(LOOP_CONTROL_DISTANCE, loopControlDistance);
        writer.writeAttribute(EDGE_INFO_ALONG_EDGE, edgeInfoAlongEdge);
        writer.writeAttribute(EDGE_NAME_DISPLAYED, edgeNameDisplayed);
        writer.writeAttribute(INTER_ANNULUS_SPACE, interAnnulusSpace);
        writer.writeAttribute(SVG_PREFIX, svgPrefix);
        writer.writeAttribute(ID_DISPLAYED, idDisplayed);
        writer.writeAttribute(SUBSTATION_DESCRIPTION_DISPLAYED, substationDescriptionDisplayed);
        writer.writeAttribute(ARROW_HEIGHT, arrowHeight);
        writer.writeAttribute(BUS_LEGEND, busLegend);
        writer.writeAttribute(VOLTAGE_LEVEL_DETAILS, voltageLevelDetails);
        writer.writeAttribute(LANGUAGE_TAG, languageTag);
        writer.writeAttribute(VOLTAGE_VALUE_PRECISION, voltageValuePrecision);
        writer.writeAttribute(POWER_VALUE_PRECISION, powerValuePrecision);
        writer.writeAttribute(ANGLE_VALUE_PRECISION, angleValuePrecision);
        writer.writeAttribute(CURRENT_VALUE_PRECISION, currentValuePrecision);
        writer.writeAttribute(EDGE_INFO_DISPLAYED, edgeInfoDisplayed);
        writer.writeAttribute(PST_ARROW_HEAD_SIZE, pstArrowHeadSize);
        writer.writeAttribute(UNDEFINED_VALUE_SYMBOL, undefinedValueSymbol);
    }

    static void writeEmpty(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(DiagramMetadata.METADATA_PREFIX, ELEMENT_NAME, DiagramMetadata.METADATA_NAMESPACE_URI);
    }

    static class Reader {

        private final String elementName;

        Reader() {
            this.elementName = ELEMENT_NAME;
        }

        public String getElementName() {
            return elementName;
        }

        public SvgParametersMetadata read(XMLStreamReader reader) {
            try {
                String insertNameDesc = reader.getAttributeValue(null, INSERT_NAME_DESC);
                String svgWidthAndHeightAdded = reader.getAttributeValue(null, SVG_WIDTH_AND_HEIGHT_ADDED);
                String cssLocation = reader.getAttributeValue(null, CSS_LOCATION);
                String sizeConstraint = reader.getAttributeValue(null, SIZE_CONSTRAINT);
                String fixedWidth = reader.getAttributeValue(null, FIXED_WIDTH);
                String fixedHeight = reader.getAttributeValue(null, FIXED_HEIGHT);
                String fixedScale = reader.getAttributeValue(null, FIXED_SCALE);
                String arrowShift = reader.getAttributeValue(null, ARROW_SHIFT);
                String arrowLabelShift = reader.getAttributeValue(null, ARROW_LABEL_SHIFT);
                String converterStationWidth = reader.getAttributeValue(null, CONVERTER_STATION_WIDTH);
                String voltageLevelCircleRadius = reader.getAttributeValue(null, VOLTAGE_LEVEL_CIRCLE_RADIUS);
                String fictitiousVoltageLevelCircleRadius = reader.getAttributeValue(null, FICTITIOUS_VOLTAGE_LEVEL_CIRCLE_RADIUS);
                String transformerCircleRadius = reader.getAttributeValue(null, TRANSFORMER_CIRCLE_RADIUS);
                String nodeHollowWidth = reader.getAttributeValue(null, NODE_HOLLOW_WIDTH);
                String edgesForkLength = reader.getAttributeValue(null, EDGES_FORK_LENGTH);
                String edgesForkAperture = reader.getAttributeValue(null, EDGES_FORK_APERTURE);
                String edgeStartShift = reader.getAttributeValue(null, EDGE_START_SHIFT);
                String unknownBusNodeExtraRadius = reader.getAttributeValue(null, UNKNOWN_BUS_NODE_EXTRA_RADIUS);
                String loopDistance = reader.getAttributeValue(null, LOOP_DISTANCE);
                String loopEdgesAperture = reader.getAttributeValue(null, LOOP_EDGES_APERTURE);
                String loopControlDistance = reader.getAttributeValue(null, LOOP_CONTROL_DISTANCE);
                String edgeInfoAlongEdge = reader.getAttributeValue(null, EDGE_INFO_ALONG_EDGE);
                String edgeNameDisplayed = reader.getAttributeValue(null, EDGE_NAME_DISPLAYED);
                String interAnnulusSpace = reader.getAttributeValue(null, INTER_ANNULUS_SPACE);
                String svgPrefix = reader.getAttributeValue(null, SVG_PREFIX);
                String idDisplayed = reader.getAttributeValue(null, ID_DISPLAYED);
                String substationDescriptionDisplayed = reader.getAttributeValue(null, SUBSTATION_DESCRIPTION_DISPLAYED);
                String arrowHeight = reader.getAttributeValue(null, ARROW_HEIGHT);
                String busLegend = reader.getAttributeValue(null, BUS_LEGEND);
                String voltageLevelDetails = reader.getAttributeValue(null, VOLTAGE_LEVEL_DETAILS);
                String languageTag = reader.getAttributeValue(null, LANGUAGE_TAG);
                String voltageValuePrecision = reader.getAttributeValue(null, VOLTAGE_VALUE_PRECISION);
                String powerValuePrecision = reader.getAttributeValue(null, POWER_VALUE_PRECISION);
                String angleValuePrecision = reader.getAttributeValue(null, ANGLE_VALUE_PRECISION);
                String currentValuePrecision = reader.getAttributeValue(null, CURRENT_VALUE_PRECISION);
                String edgeInfoDisplayed = reader.getAttributeValue(null, EDGE_INFO_DISPLAYED);
                String pstArrowHeadSize = reader.getAttributeValue(null, PST_ARROW_HEAD_SIZE);
                String undefinedValueSymbol = reader.getAttributeValue(null, UNDEFINED_VALUE_SYMBOL);
                XmlUtil.readEndElementOrThrow(reader);
                return new SvgParametersMetadata(insertNameDesc, svgWidthAndHeightAdded, cssLocation, sizeConstraint, fixedWidth, fixedHeight, fixedScale,
                                                 arrowShift, arrowLabelShift, converterStationWidth, voltageLevelCircleRadius, fictitiousVoltageLevelCircleRadius,
                                                 transformerCircleRadius, nodeHollowWidth, edgesForkLength, edgesForkAperture, edgeStartShift,
                                                 unknownBusNodeExtraRadius, loopDistance, loopEdgesAperture, loopControlDistance, edgeInfoAlongEdge,
                                                 edgeNameDisplayed, interAnnulusSpace, svgPrefix, idDisplayed, substationDescriptionDisplayed, arrowHeight,
                                                 busLegend, voltageLevelDetails, languageTag, voltageValuePrecision, powerValuePrecision, angleValuePrecision,
                                                 currentValuePrecision, edgeInfoDisplayed, pstArrowHeadSize, undefinedValueSymbol);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }
    }
}
