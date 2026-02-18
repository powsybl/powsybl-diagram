/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.google.common.collect.Lists;
import com.powsybl.diagram.components.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Orientation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public interface SldComponentLibrary extends ComponentLibrary {

    static List<SldComponentLibrary> findAll() {
        return Lists.newArrayList(ServiceLoader.load(SldComponentLibrary.class));
    }

    static Optional<SldComponentLibrary> find(String name) {
        Objects.requireNonNull(name);
        return findAll().stream().filter(cl -> cl.getName().equals(name)).findFirst();
    }

    List<AnchorPoint> getAnchorPoints(String type);

    Map<Orientation, SldComponent.Transformation> getTransformations(String type);
}
