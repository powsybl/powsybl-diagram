/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.DefaultSVGLegendWriter;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesZoneLayoutTest extends AbstractTest {

    @Override
    @BeforeEach
    public void setup() throws IOException {
        super.setup();
        network = Networks.createZoneDiagramNetwork();
    }

    @Test
    void testZoneLayout() throws IOException {
        List<String> zone = Arrays.asList("Substation1", "Substation2");
        ZoneGraph graph = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        var layoutParameters = new LayoutParameters().setCgmesScaleFactor(3);
        new CgmesZoneLayout(graph, network).run(layoutParameters);

        var svgParameters = new SvgParameters();
        var componentLib = new ConvergenceComponentLibrary();
        var svgWriter = new DefaultSVGWriter(componentLib, layoutParameters, svgParameters);
        var labelProvider = new DefaultLabelProvider(network, componentLib, layoutParameters, svgParameters);
        var styleProvider = new TopologicalStyleProvider(network, svgParameters);
        var legendWriter = new DefaultSVGLegendWriter(network, svgParameters);

        String filename = "/zoneLayoutTest.svg";
        Path svgOutput = tmpDir.resolve(filename);
        Writer fileWriter = Files.newBufferedWriter(svgOutput, StandardCharsets.UTF_8);
        svgWriter.write(graph, labelProvider, styleProvider, legendWriter, fileWriter);

        assertSvgEqualsReference(filename, svgOutput);
    }
}
