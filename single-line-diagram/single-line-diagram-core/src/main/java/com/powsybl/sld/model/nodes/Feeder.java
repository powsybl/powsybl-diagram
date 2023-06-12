/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public interface Feeder {
    FeederType getFeederType();

    void writeJsonContent(JsonGenerator generator) throws IOException;

    boolean isDisconnected();
}
