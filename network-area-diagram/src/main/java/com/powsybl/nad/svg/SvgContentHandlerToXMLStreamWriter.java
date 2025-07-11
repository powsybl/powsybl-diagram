/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import javanet.staxutils.ContentHandlerToXMLStreamWriter;

import javax.xml.stream.XMLStreamWriter;

/**
 * This is a utility class that extends {@link ContentHandlerToXMLStreamWriter}, for the specific use case of inserting
 * an {@link javax.lang.model.element.Element} read from the component SVG files into the diagram's
 * {@link XMLStreamWriter}. Indeed, the existing {@link ContentHandlerToXMLStreamWriter} allows this but calls
 * <ul>
 *     <li>{@link XMLStreamWriter#writeStartDocument()} when starting the element insertion,</li>
 *     <li>{@link XMLStreamWriter#writeEndDocument()} at the end of the element insertion.</li>
 * </ul>
 * Both those calls are corrupting the resulting diagram's XML structure, the first one by adding the XML declaration,
 * the latter one by closing all previously opened tags.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class SvgContentHandlerToXMLStreamWriter extends ContentHandlerToXMLStreamWriter {
    public SvgContentHandlerToXMLStreamWriter(XMLStreamWriter writer) {
        super(writer);
    }

    @Override
    public void startDocument() {
        // do nothing to avoid inserting the XML declaration in the middle of the XMLStreamWriter
    }

    @Override
    public void endDocument() {
        // do nothing to avoid closing all the previously opened tags
    }
}
