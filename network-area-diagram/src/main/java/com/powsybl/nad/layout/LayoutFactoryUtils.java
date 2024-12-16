/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.svg.metadata.DiagramMetadata;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public final class LayoutFactoryUtils {

    private LayoutFactoryUtils() {
    }

    private static FixedLayoutFactory createLayoutFactory(LayoutFactory layoutFactory, DiagramMetadata diagramMetadata) {
        return new FixedLayoutFactory(diagramMetadata.getFixedPositions(), diagramMetadata.getFixedTextPositions(), layoutFactory);
    }

    private static FixedLayoutFactory createLayoutFactory(DiagramMetadata diagramMetadata) {
        return new FixedLayoutFactory(diagramMetadata.getFixedPositions(), diagramMetadata.getFixedTextPositions());
    }

    public static FixedLayoutFactory create(InputStream metadataIs, LayoutFactory layoutFactory) {
        return createLayoutFactory(layoutFactory, DiagramMetadata.parseJson(metadataIs));
    }

    public static FixedLayoutFactory create(InputStream metadataIs) {
        return createLayoutFactory(DiagramMetadata.parseJson(metadataIs));
    }

    public static FixedLayoutFactory create(Path metadataFile, LayoutFactory layoutFactory) {
        return createLayoutFactory(layoutFactory, DiagramMetadata.parseJson(metadataFile));
    }

    public static FixedLayoutFactory create(Path metadataFile) {
        return createLayoutFactory(DiagramMetadata.parseJson(metadataFile));
    }

    public static FixedLayoutFactory create(Reader metadataReader, LayoutFactory layoutFactory) {
        return createLayoutFactory(layoutFactory, DiagramMetadata.parseJson(metadataReader));
    }

    public static FixedLayoutFactory create(Reader metadataReader) {
        return createLayoutFactory(DiagramMetadata.parseJson(metadataReader));
    }
}
