/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.google.common.io.ByteStreams;
import com.powsybl.sld.svg.SVGLoaderToDocument;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ResourcesComponentLibrary implements ComponentLibrary {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesComponentLibrary.class);

    private final Map<String, Map<String, SVGOMDocument>> svgDocuments = new HashMap<>();

    private final Map<String, Component> components;

    private final String styleSheet;

    public ResourcesComponentLibrary(String directory) {
        Objects.requireNonNull(directory);
        LOGGER.info("Loading component library from {}...", directory);

        components = Components.load(directory).getComponents()
                .stream()
                .collect(Collectors.toMap(c -> c.getMetadata().getType(), c -> c));

        // preload SVG documents
        SVGLoaderToDocument svgLoadDoc = new SVGLoaderToDocument();
        components.values().stream().forEach(c ->
            c.getMetadata().getSubComponents().stream().forEach(s -> {
                String resourceName = directory + "/" + s.getFileName();
                LOGGER.debug("Reading subComponent {}", resourceName);
                SVGOMDocument doc = svgLoadDoc.read(resourceName);
                Map<String, SVGOMDocument> mapSubDoc;
                if (!svgDocuments.containsKey(c.getMetadata().getType())) {
                    mapSubDoc = new TreeMap<>();
                    svgDocuments.put(c.getMetadata().getType(), mapSubDoc);
                } else {
                    mapSubDoc = svgDocuments.get(c.getMetadata().getType());
                }
                mapSubDoc.put(s.getName(), doc);
            }));

        try {
            styleSheet = new String(ByteStreams.toByteArray(getClass().getResourceAsStream(directory + "/" + "components.css")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't read css file from the SVG library!", e);
        }
    }

    @Override
    public Map<String, SVGOMDocument> getSvgDocument(String type) {
        Objects.requireNonNull(type);
        return svgDocuments.get(type);
    }

    @Override
    public List<AnchorPoint> getAnchorPoints(String type) {
        Objects.requireNonNull(type);
        Component component = components.get(type);
        return component != null ? component.getMetadata().getAnchorPoints()
                                 : Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
    }

    @Override
    public ComponentSize getSize(String type) {
        Objects.requireNonNull(type);
        Component component = components.get(type);
        return component != null ? component.getMetadata().getSize() : new ComponentSize(0, 0);
    }

    @Override
    public boolean isAllowRotation(String type) {
        Objects.requireNonNull(type);
        Component component = components.get(type);
        return component == null || component.getMetadata().isAllowRotation();
    }

    public String getStyleSheet() {
        return styleSheet;
    }
}
