/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.svg.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLabelProviderFactoryTest {

    @Test
    void testCreate() {
        LabelProviderParameters parameters = new LabelProviderParameters();
        parameters.setEdgeInfoParameters(new EdgeInfoParameters(
                EdgeInfoEnum.ACTIVE_POWER,
                EdgeInfoEnum.REACTIVE_POWER,
                EdgeInfoEnum.CURRENT,
                EdgeInfoEnum.NAME));
        DefaultLabelProviderFactory factory = new DefaultLabelProviderFactory(parameters);

        assertSame(parameters, factory.parameters());

        Network network = Networks.createTwoVoltageLevels();
        SvgParameters svgParameters = new SvgParameters();
        LabelProvider labelProvider = factory.create(network, svgParameters);

        assertNotNull(labelProvider);
        assertInstanceOf(DefaultLabelProvider.class, labelProvider);
        DefaultLabelProvider defaultLabelProvider = (DefaultLabelProvider) labelProvider;
        assertSame(network, defaultLabelProvider.getNetwork());
    }
}
