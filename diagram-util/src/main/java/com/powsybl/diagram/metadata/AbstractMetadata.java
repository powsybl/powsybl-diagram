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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@soft.it>}
 */
@JsonPropertyOrder(value = {"metadataVersion"})
public abstract class AbstractMetadata {

    //use a field to have both Serialization and Deserialization
    @JsonProperty("metadataVersion")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected String metadataVersion;

    public String getMetadataVersion() {
        return metadataVersion;
    }

    public void writeJson(Path file) {
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(writer, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
