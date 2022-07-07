/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SmartVoltageLevelLayoutFactory extends AbstractVoltageLevelLayoutFactory {
    private final Network network;

    public SmartVoltageLevelLayoutFactory(Network network, LayoutParameters layoutParameters) {
        super(layoutParameters);
        this.network = network;
    }

    @Override
    public Layout create(VoltageLevelGraph graph) {
        return VoltageLevelLayoutFactorySmartSelector.findBest(network.getVoltageLevel(graph.getVoltageLevelInfos().getId()))
                .orElseThrow(() -> new PowsyblException("Voltage level layout factory not found"))
                .createFactory(network, getLayoutParameters())
                .create(graph);
    }
}
