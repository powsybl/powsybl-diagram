/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.google.common.jimfs.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.nad.*;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class TieLineTest extends AbstractTest {

    protected java.nio.file.FileSystem fileSystem;

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
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters()) {
        };
    }

    @Test
    void testTieLine() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        assertEquals(toString("/tie_line.svg"), generateSvgString(network, "/tie_line.svg"));
    }

    @Test
    void testDanglingLinePaired() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        Path svgFile = fileSystem.getPath("tie_line_filtered.svg");
        NadParameters nadParameters = new NadParameters()
                .setSvgParameters(getSvgParameters())
                .setLayoutParameters(getLayoutParameters())
                .setStyleProviderFactory(NominalVoltageStyleProvider::new);
        NetworkAreaDiagram.draw(network, svgFile, nadParameters, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, Collections.singletonList("VLHV1"), 1));
        assertEquals(toString("/tie_line_filtered.svg"), getContentFile(svgFile));
    }
}
