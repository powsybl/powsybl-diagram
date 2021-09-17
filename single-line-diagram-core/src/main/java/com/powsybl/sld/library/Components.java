/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.databind.ObjectReader;
import com.powsybl.commons.json.JsonUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class Components {

    private final List<Component> components = new ArrayList<>();

    public List<Component> getComponents() {
        return components;
    }

    public static Components load(String directory) {
        return load(Components.class.getResourceAsStream(directory + "/components.json"));
    }

    private static Components load(Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            return load(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Components load(InputStream is) {
        Objects.requireNonNull(is);

        try (Reader reader = new InputStreamReader(is)) {
            return load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Components load(Reader reader) throws IOException {
        Objects.requireNonNull(reader);

        ObjectReader objectReader = JsonUtil.createObjectMapper().readerFor(Components.class);
        return objectReader.readValue(reader);
    }
}
