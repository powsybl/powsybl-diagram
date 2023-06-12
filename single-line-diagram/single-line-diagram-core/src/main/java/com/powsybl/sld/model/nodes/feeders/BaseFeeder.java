/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes.feeders;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.nodes.Feeder;
import com.powsybl.sld.model.nodes.FeederType;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BaseFeeder implements Feeder {
    FeederType feederType;
    private final boolean isDisconnected;

    public BaseFeeder(FeederType feederType, boolean isDisconnected) {
        this.feederType = Objects.requireNonNull(feederType);
        this.isDisconnected = isDisconnected;
    }

    public FeederType getFeederType() {
        return feederType;
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
    // nothing to add to json file in general case
    }

    @Override
    public boolean isDisconnected() {
        return this.isDisconnected;
    }
}
