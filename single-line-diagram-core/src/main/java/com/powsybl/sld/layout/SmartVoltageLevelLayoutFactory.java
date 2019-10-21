/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.Graph;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SmartVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    @Override
    public VoltageLevelLayout create(Graph graph) {
        return VoltageLevelLayoutFactorySmartSelector.findBest(graph.getVoltageLevel())
                .orElseThrow(() -> new PowsyblException("Voltage level layout factory not found"))
                .createFactory()
                .create(graph);
    }
}
