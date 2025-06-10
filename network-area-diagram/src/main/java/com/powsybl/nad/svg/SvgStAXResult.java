/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import javanet.staxutils.ContentHandlerToXMLStreamWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class SvgStAXResult extends SAXResult {
    public SvgStAXResult(XMLStreamWriter writer) {
        super.setHandler(new SvgContentHandlerToXMLStreamWriter(writer));
    }

    private static class SvgContentHandlerToXMLStreamWriter extends ContentHandlerToXMLStreamWriter {
        public SvgContentHandlerToXMLStreamWriter(XMLStreamWriter writer) {
            super(writer);
        }

        public void startDocument() {
            // do nothing
        }

        public void endDocument() throws SAXException {
            // do nothing
        }
    }
}
