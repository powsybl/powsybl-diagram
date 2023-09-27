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
import com.powsybl.iidm.network.test.*;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.diagram.test.Networks;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;
import java.util.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class NetworkAreaDiagramTest extends AbstractTest {

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
    void testDrawSvg() {
        Network network = Networks.createThreeVoltageLevelsFiveBuses();
        NetworkAreaDiagram nad = new NetworkAreaDiagram(network, VoltageLevelFilter.NO_FILTER);

        Path svgFile = fileSystem.getPath("nad-test.svg");
        nad.draw(svgFile,
                getSvgParameters(),
                getLayoutParameters(),
                getStyleProvider(network));

        assertEquals(toString("/dangling_line_connected.svg"), getContentFile(svgFile));
    }

    @Test
    void testGetVisibleVoltageLevels() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        List<String> ids = NetworkAreaDiagram.getDisplayedVoltageLevels(network, List.of("VLHV1"), 1);
        assertEquals("VLGEN, VLHV1, VLHV2", String.join(", ", ids));

        ids = NetworkAreaDiagram.getDisplayedVoltageLevels(network, List.of("VLHV1"), 2);
        assertEquals("VLGEN, VLHV1, VLHV2, VLLOAD", String.join(", ", ids));
    }
}
