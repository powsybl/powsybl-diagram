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
public class LayoutParametersMetadata {

    private static final String ELEMENT_NAME = "layoutParameters";
    private static final String TEXT_NODES_FORCE_LAYOUT = "textNodesForceLayout";
    private static final String SPRING_REPULSION_FACTOR_FORCE_LAYOUT = "springRepulsionFactorForceLayout";
    private static final String TEXT_NODE_FIXED_SHIFT_X = "textNodeFixedShiftX";
    private static final String TEXT_NODE_FIXED_SHIFT_Y = "textNodeFixedShiftY";
    private static final String MAX_STEPS = "maxSteps";
    private static final String TEXT_NODE_EDGE_CONNECTION_Y_SHIFT = "textNodeEdgeConnectionYShift";

    private final String textNodesForceLayout;
    private final String springRepulsionFactorForceLayout;
    private final String textNodeFixedShiftX;
    private final String textNodeFixedShiftY;
    private final String maxSteps;
    private final String textNodeEdgeConnectionYShift;

    public LayoutParametersMetadata(String textNodesForceLayout, String springRepulsionFactorForceLayout, String textNodeFixedShiftX,
                                    String textNodeFixedShiftY, String maxSteps, String textNodeEdgeConnectionYShift) {
        this.textNodesForceLayout = textNodesForceLayout;
        this.springRepulsionFactorForceLayout = springRepulsionFactorForceLayout;
        this.textNodeFixedShiftX = textNodeFixedShiftX;
        this.textNodeFixedShiftY = textNodeFixedShiftY;
        this.maxSteps = maxSteps;
        this.textNodeEdgeConnectionYShift = textNodeEdgeConnectionYShift;
    }

    String getElementName() {
        return ELEMENT_NAME;
    }

    void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(DiagramMetadata.METADATA_PREFIX, getElementName(), DiagramMetadata.METADATA_NAMESPACE_URI);
        writer.writeAttribute(TEXT_NODES_FORCE_LAYOUT, textNodesForceLayout);
        writer.writeAttribute(SPRING_REPULSION_FACTOR_FORCE_LAYOUT, springRepulsionFactorForceLayout);
        writer.writeAttribute(TEXT_NODE_FIXED_SHIFT_X, textNodeFixedShiftX);
        writer.writeAttribute(TEXT_NODE_FIXED_SHIFT_Y, textNodeFixedShiftY);
        writer.writeAttribute(MAX_STEPS, maxSteps);
        writer.writeAttribute(TEXT_NODE_EDGE_CONNECTION_Y_SHIFT, textNodeEdgeConnectionYShift);
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

        public LayoutParametersMetadata read(XMLStreamReader reader) {
            try {
                String textNodesForceLayout = reader.getAttributeValue(null, TEXT_NODES_FORCE_LAYOUT);
                String springRepulsionFactorForceLayout = reader.getAttributeValue(null, SPRING_REPULSION_FACTOR_FORCE_LAYOUT);
                String textNodeFixedShiftX = reader.getAttributeValue(null, TEXT_NODE_FIXED_SHIFT_X);
                String textNodeFixedShiftY = reader.getAttributeValue(null, TEXT_NODE_FIXED_SHIFT_Y);
                String maxSteps = reader.getAttributeValue(null, MAX_STEPS);
                String textNodeEdgeConnectionYShift = reader.getAttributeValue(null, TEXT_NODE_EDGE_CONNECTION_Y_SHIFT);
                XmlUtil.readEndElementOrThrow(reader);
                return new LayoutParametersMetadata(textNodesForceLayout, springRepulsionFactorForceLayout, textNodeFixedShiftX,
                                                    textNodeFixedShiftY, maxSteps, textNodeEdgeConnectionYShift);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }
    }
}
