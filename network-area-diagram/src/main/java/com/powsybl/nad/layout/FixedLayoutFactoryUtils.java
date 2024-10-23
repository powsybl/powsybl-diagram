/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.powsybl.nad.layout.AbstractLayout.TextPosition;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.metadata.DiagramMetadata;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public final class FixedLayoutFactoryUtils {

    private FixedLayoutFactoryUtils() {
    }

    private static Map<String, Point> getFixedPositions(DiagramMetadata diagramMetadata) {
        Map<String, Point> fixedPositions = new HashMap<>();
        diagramMetadata.getNodesMetadata()
                       .forEach(node -> fixedPositions.put(node.getEquipmentId(),
                                                           new Point(node.getX(),
                                                                     node.getY())));
        return fixedPositions;
    }

    private static Map<String, TextPosition> getTextNodesWithFixedPosition(DiagramMetadata diagramMetadata) {
        Map<String, TextPosition> textNodesWithFixedPosition = new HashMap<>();
        diagramMetadata.getTextNodesMetadata()
                       .forEach(textNode -> textNodesWithFixedPosition.put(textNode.getEquipmentId(),
                                                                           new TextPosition(new Point(textNode.getShiftX(),
                                                                                                      textNode.getShiftY()),
                                                                                            new Point(textNode.getConnectionShiftX(),
                                                                                                      textNode.getConnectionShiftY()))));
        return textNodesWithFixedPosition;
    }

    public static FixedLayoutFactory create(InputStream metadataIS, LayoutFactory layoutFactory) {
        Objects.requireNonNull(metadataIS);
        DiagramMetadata diagramMetadata = DiagramMetadata.parseJson(metadataIS);
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata), layoutFactory);
    }

    public static FixedLayoutFactory create(InputStream metadataIS) {
        Objects.requireNonNull(metadataIS);
        DiagramMetadata diagramMetadata = DiagramMetadata.parseJson(metadataIS);
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata));
    }

    public static FixedLayoutFactory create(Path metadataFile, LayoutFactory layoutFactory) {
        Objects.requireNonNull(metadataFile);
        DiagramMetadata diagramMetadata = DiagramMetadata.parseJson(metadataFile);
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata), layoutFactory);
    }

    public static FixedLayoutFactory create(Path metadataFile) {
        Objects.requireNonNull(metadataFile);
        DiagramMetadata diagramMetadata = DiagramMetadata.parseJson(metadataFile);
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata));
    }

    public static FixedLayoutFactory create(Reader metadataReader, LayoutFactory layoutFactory) {
        Objects.requireNonNull(metadataReader);
        DiagramMetadata diagramMetadata = DiagramMetadata.parseJson(metadataReader);
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata), layoutFactory);
    }

    public static FixedLayoutFactory create(Reader metadataReader) {
        Objects.requireNonNull(metadataReader);
        DiagramMetadata diagramMetadata = DiagramMetadata.parseJson(metadataReader);
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata));
    }
}
