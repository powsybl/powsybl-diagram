/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.powsybl.nad.model.Identifiable;
import com.powsybl.nad.model.Point;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class NodeMetadata extends AbstractMetadataItem {
    private final Point position;

    private static final String ELEMENT_NAME = "node";
    private static final String BUS_NODE_ELEMENT_NAME = "busNode";
    private static final String POSITION_X_ATTRIBUTE = "x";
    private static final String POSITION_Y_ATTRIBUTE = "y";
    private static final String POSITION_COORD_FORMAT = "%.4f";

    public NodeMetadata(Identifiable identifiable, Point position) {
        super(identifiable);
        this.position = position;
    }

    public static String getBusNodeElementName() {
        return BUS_NODE_ELEMENT_NAME;
    }

    public Point getPosition() {
        return position;
    }

    @Override
    String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    void write(DiagramMetadata.WritingContext ctx) throws XMLStreamException {
        super.write(ctx);
        ctx.writer.writeAttribute(POSITION_X_ATTRIBUTE, formatted(position.getX()));
        ctx.writer.writeAttribute(POSITION_Y_ATTRIBUTE, formatted(position.getY()));
    }

    private static String formatted(double value) {
        return String.format(POSITION_COORD_FORMAT, value);
    }

    static class Reader implements MetadataItemReader<NodeMetadata> {

        private final String elementName;

        Reader() {
            this(ELEMENT_NAME);
        }

        static Reader forBusNodes() {
            return new Reader(BUS_NODE_ELEMENT_NAME);
        }

        private Reader(String elementName) {
            this.elementName = elementName;
        }

        @Override
        public String getElementName() {
            return elementName;
        }

        public NodeMetadata read(XMLStreamReader reader) {
            Identifiable deserializedIdentifiable = readIdentifiable(reader);
            Point position = readPoint(reader);
            return new NodeMetadata(deserializedIdentifiable, position);
        }

        private static Point readPoint(XMLStreamReader reader) {
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
    }
}
