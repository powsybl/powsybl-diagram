/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.layout.*;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class CountryDiagramTest {

    @Test
    void testDrawSvg() {
        Network network = Network.read(Path.of("/home/dupuyflo/Data/PtFige-20250926-1900-enrichi_Root.xiidm"));
        Path svgFile = Path.of("/tmp/countries.svg");
        NadParameters nadParameters = new NadParameters()
                .setSvgParameters(new SvgParameters()
                        .setArrowPathOut("M-15 0 h5 V20 h20 V0 h5 L0 -20z")
                        .setArrowPathIn("M-15 0 h5 V-20 h20 V0 h5 L0 20z")
                        .setEdgeInfoAlongEdge(false)
                        .setArrowShift(140)
                        .setArrowLabelShift(30))
                .setStyleProviderFactory(TopologicalStyleProvider::new)
                .setLayoutFactory(LayoutFactoryUtils.create(Path.of("/home/dupuyflo/countries_metadata.json"), new BasicForceLayoutFactory()));
        CountryDiagram.draw(network, svgFile, nadParameters);
    }

}
