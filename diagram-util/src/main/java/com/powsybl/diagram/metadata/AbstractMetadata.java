/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.metadata;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ServiceLoader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.tools.Version;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@soft.it>}
 */
@JsonPropertyOrder(value = {"diagramVersion"})
public abstract class AbstractMetadata {

    public static final String DEFAULT_DIAGRAM_VERSION = resolveDiagramVersion();

    private String diagramVersion;

    private static String resolveDiagramVersion() {
        for (Version v : ServiceLoader.load(Version.class)) {
            if ("powsybl-diagram".equals(v.getRepositoryName())) {
                return v.getMavenProjectVersion();
            }
        }
        return null;
    }

    @JsonProperty("diagramVersion")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getDiagramVersion() {
        return diagramVersion;
    }

    public void writeJson(Path file) {
        writeJson(file, DEFAULT_DIAGRAM_VERSION);
    }

    public void writeJson(Path file, String diagramVersion) {
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer, diagramVersion);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Writer writer) {
        writeJson(writer, DEFAULT_DIAGRAM_VERSION);
    }

    public void writeJson(Writer writer, String diagramVersion) {
        Objects.requireNonNull(writer);
        this.diagramVersion = diagramVersion;
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(writer, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
