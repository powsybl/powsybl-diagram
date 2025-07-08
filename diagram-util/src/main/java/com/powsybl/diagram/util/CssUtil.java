/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class CssUtil {

    public static String getFilesContent(List<URL> cssUrls) {
        Objects.requireNonNull(cssUrls);
        StringBuilder styleSheetBuilder = new StringBuilder();
        for (URL cssUrl : cssUrls) {
            try {
                styleSheetBuilder.append(new String(IOUtils.toByteArray(cssUrl), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException("Can't read css file " + cssUrl.getPath(), e);
            }
        }
        return styleSheetBuilder.toString()
                .replace("\r\n", "\n");
    }

    public static String getImportCssString(List<String> cssFilenames) {
        Objects.requireNonNull(cssFilenames);
        StringBuilder importStringBuilder = new StringBuilder();
        for (String cssFilename : cssFilenames) {
            importStringBuilder.append("@import url(").append(cssFilename).append(");");
        }
        return importStringBuilder.toString();
    }

    private CssUtil() {
    }
}
