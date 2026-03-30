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
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.LabelProviderParameters;
import com.powsybl.nad.svg.SvgParameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLabelProviderFactoryTest {

    @Test
    void testCreate() {
        LabelProviderParameters parameters = new LabelProviderParameters();
        DefaultLabelProvider.EdgeInfoParameters edgeInfoParameters = new DefaultLabelProvider.EdgeInfoParameters(
                DefaultLabelProvider.EdgeInfoEnum.ACTIVE_POWER,
                DefaultLabelProvider.EdgeInfoEnum.REACTIVE_POWER,
                DefaultLabelProvider.EdgeInfoEnum.CURRENT,
                DefaultLabelProvider.EdgeInfoEnum.NAME);
        DefaultLabelProviderFactory factory = new DefaultLabelProviderFactory(parameters, edgeInfoParameters);

        assertSame(parameters, factory.getParameters());
        assertSame(edgeInfoParameters, factory.getEdgeInfoParameters());

        Network network = Networks.createTwoVoltageLevels();
        SvgParameters svgParameters = new SvgParameters();
        LabelProvider labelProvider = factory.create(network, svgParameters);

        assertNotNull(labelProvider);
        assertInstanceOf(DefaultLabelProvider.class, labelProvider);
        DefaultLabelProvider defaultLabelProvider = (DefaultLabelProvider) labelProvider;
        assertSame(network, defaultLabelProvider.getNetwork());
    }
}
