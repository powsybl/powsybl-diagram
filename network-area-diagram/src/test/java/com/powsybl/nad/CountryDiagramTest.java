/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class CountryDiagramTest extends AbstractTest {

    protected FileSystem fileSystem;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return null;
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return null;
    }

    @Test
    void testDrawSvg() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        NadParameters nadParameters = new NadParameters()
                .setSvgParameters(new SvgParameters()
                        .setArrowPathOut("M-15 0 h5 V20 h20 V0 h5 L0 -20z")
                        .setArrowPathIn("M-15 0 h5 V-20 h20 V0 h5 L0 20z")
                        .setEdgeInfoAlongEdge(false)
                        .setArrowShift(140)
                        .setArrowLabelShift(30))
                .setStyleProviderFactory(TopologicalStyleProvider::new);
        Path svgFile = fileSystem.getPath("countries-test.svg");
        CountryDiagram.draw(network, svgFile, nadParameters);
        assertFileEquals("/eurostag_country_diagram.svg", svgFile);
    }
}
