/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.components;

import com.fasterxml.jackson.databind.ObjectReader;
import com.powsybl.commons.json.JsonUtil;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class ComponentsLoader<C> {

    private final Class<C> componentClass;

    public ComponentsLoader(Class<C> componentClass) {
        this.componentClass = componentClass;
    }

    public List<C> load(String directory) {
        return load(ComponentsLoader.class.getResourceAsStream(directory + "/components.json"));
    }

    public List<C> load(InputStream is) {
        Objects.requireNonNull(is);

        try (Reader reader = new InputStreamReader(is)) {
            return load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<C> load(Reader reader) throws IOException {
        Objects.requireNonNull(reader);
        ObjectReader objectReader = JsonUtil.createObjectMapper().readerForArrayOf(componentClass);
        C[] componentArray = objectReader.readValue(reader);
        return Arrays.asList(componentArray);
    }
}
