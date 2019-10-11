/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.*;
import org.apache.batik.util.XMLResourceDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SVGLoaderToDocument {

    /**
     * Read a file in svg format and return the SVGDocument corresponding
     *
     * @param fileName filename
     * @return document
     */
    public SVGOMDocument read(String fileName) {
        try (InputStream svgFile = this.getClass().getResourceAsStream(fileName)) {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            SVGOMDocument document = (SVGOMDocument) factory.createDocument("", svgFile);

            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
            bridgeContext.setDynamicState(BridgeContext.DYNAMIC);

            // Enable CSS- and SVG-specific enhancements.
            (new GVTBuilder()).build(bridgeContext, document);

            return document;
        } catch (IOException e) {
            throw new UncheckedIOException("Can't read svg file from the SVG library!", e);
        }
    }
}
