/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.powsybl.sld.svg.SVGLoaderToDocument;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ResourcesComponentLibrary implements ComponentLibrary {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesComponentLibrary.class);

    private final Map<String, Map<String, SVGOMDocument>> svgDocuments = new HashMap<>();

    private final Map<String, Component> components = new HashMap<>();

    private final String styleSheet;

    public ResourcesComponentLibrary(String directory, String... additionalDirectories) {
        Objects.requireNonNull(directory);
        StringBuilder styleSheetBuilder = new StringBuilder();
        loadLibrary(directory, styleSheetBuilder);
        for (String addDir : additionalDirectories) {
            loadLibrary(addDir, styleSheetBuilder);
        }
        styleSheet = styleSheetBuilder.toString();
    }

    private void loadLibrary(String directory, StringBuilder styleSheetBuilder) {
        LOGGER.info("Loading component library from {}...", directory);

        // preload SVG documents
        SVGLoaderToDocument svgLoadDoc = new SVGLoaderToDocument();
        Components.load(directory).getComponents().forEach(c -> {
            ComponentMetadata componentMetaData = c.getMetadata();
            String componentType = componentMetaData.getType();
            componentMetaData.getSubComponents().forEach(s -> {
                String resourceName = directory + "/" + s.getFileName();
                LOGGER.debug("Reading subComponent {}", resourceName);
                SVGOMDocument doc = svgLoadDoc.read(resourceName);
                Map<String, SVGOMDocument> mapSubDoc;
                if (!svgDocuments.containsKey(componentType)) {
                    mapSubDoc = new TreeMap<>();
                    svgDocuments.put(componentType, mapSubDoc);
                } else {
                    mapSubDoc = svgDocuments.get(componentType);
                }
                mapSubDoc.put(s.getName(), doc);
            });
            components.put(componentType, c);
        });

        try {
            URL cssUrl = getClass().getResource(directory + "/" + "components.css");
            Path cssPath = Paths.get(URI.create(cssUrl.toString()));
            styleSheetBuilder.append(new String(Files.readAllBytes(cssPath), StandardCharsets.UTF_8));
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
    public Map<String, ComponentSize> getComponentsSize() {
        Map<String, ComponentSize> res = new HashMap<>();
        components.forEach((key, value) -> res.put(key, value.getMetadata().getSize()));
        return res;
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
