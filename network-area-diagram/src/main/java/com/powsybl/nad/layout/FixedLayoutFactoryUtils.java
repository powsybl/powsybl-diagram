/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.metadata.DiagramMetadata;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public final class FixedLayoutFactoryUtils {

    private FixedLayoutFactoryUtils() {
    }

    private static Map<String, Point> getFixedPositions(DiagramMetadata diagramMetadata) {
        Map<String, Point> fixedPositions = new HashMap<>();
        diagramMetadata.getNodesMetadata()
                .forEach(node -> fixedPositions.put(node.getEquipmentId(), new Point(node.getX(), node.getY())));
        return fixedPositions;
    }

    private static Map<String, TextPosition> getTextNodesWithFixedPosition(DiagramMetadata diagramMetadata) {
        Map<String, TextPosition> textNodesWithFixedPosition = new HashMap<>();
        diagramMetadata.getTextNodesMetadata()
                .forEach(textNode -> textNodesWithFixedPosition.put(textNode.getEquipmentId(),
                        new TextPosition(new Point(textNode.getShiftX(), textNode.getShiftY()),
                                new Point(textNode.getConnectionShiftX(), textNode.getConnectionShiftY()))));
        return textNodesWithFixedPosition;
    }

    private static FixedLayoutFactory createFixedLayoutFactory(LayoutFactory layoutFactory, DiagramMetadata diagramMetadata) {
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata), layoutFactory);
    }

    private static FixedLayoutFactory createFixedLayoutFactory(DiagramMetadata diagramMetadata) {
        return new FixedLayoutFactory(getFixedPositions(diagramMetadata), getTextNodesWithFixedPosition(diagramMetadata));
    }

    public static FixedLayoutFactory create(InputStream metadataIs, LayoutFactory layoutFactory) {
        return createFixedLayoutFactory(layoutFactory, DiagramMetadata.parseJson(metadataIs));
    }

    public static FixedLayoutFactory create(InputStream metadataIs) {
        return createFixedLayoutFactory(DiagramMetadata.parseJson(metadataIs));
    }

    public static FixedLayoutFactory create(Path metadataFile, LayoutFactory layoutFactory) {
        return createFixedLayoutFactory(layoutFactory, DiagramMetadata.parseJson(metadataFile));
    }

    public static FixedLayoutFactory create(Path metadataFile) {
        return createFixedLayoutFactory(DiagramMetadata.parseJson(metadataFile));
    }

    public static FixedLayoutFactory create(Reader metadataReader, LayoutFactory layoutFactory) {
        return createFixedLayoutFactory(layoutFactory, DiagramMetadata.parseJson(metadataReader));
    }

    public static FixedLayoutFactory create(Reader metadataReader) {
        return createFixedLayoutFactory(DiagramMetadata.parseJson(metadataReader));
    }
}
