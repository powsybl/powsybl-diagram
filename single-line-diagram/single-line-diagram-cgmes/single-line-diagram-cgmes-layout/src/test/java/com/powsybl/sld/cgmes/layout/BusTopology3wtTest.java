/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.sld.SingleLineDiagram;
import com.powsybl.sld.SldParameters;
import com.powsybl.sld.svg.SvgParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class BusTopology3wtTest extends AbstractTest {

    @Override
    @BeforeEach
    public void setup() throws IOException {
        super.setup();
        network = Networks.createBusTopologyNetworkWith3WT();
    }

    @Test
    void testSubstationLayout() throws IOException {
        var sldParameters = new SldParameters()
                .setVoltageLevelLayoutFactoryCreator(n -> null)
                .setSubstationLayoutFactory(new CgmesSubstationLayoutFactory(network, null, 3.))
                .setSvgParameters(new SvgParameters().setUseName(true));

        String filename = "/busTopology3wtSubstationTest.svg";
        Path svgOutput = tmpDir.resolve(filename);
        SingleLineDiagram.draw(network, "Substation", svgOutput, sldParameters);

        assertSvgEqualsReference(filename, svgOutput);
    }
}
