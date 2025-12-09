/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.svg.SvgParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class BusTopologyTest extends AbstractTest {

    @BeforeEach
    public void setup() throws IOException {
        super.setup();
        network = Networks.createBusTopologyNetwork();
    }

    @Test
    void testVoltageLevelLayout() throws IOException {
        assertSvgDrawnEqualsReference("VoltageLevel1", "/busTopologyTest.svg",
                new LayoutParameters().setCgmesScaleFactor(2), new SvgParameters());
    }
}
