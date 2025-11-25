/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.routing;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.NadParameters;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.powsybl.nad.build.iidm.VoltageLevelFilter.NO_FILTER;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class CustomPathRoutingTest extends AbstractTest {

    private FileSystem fileSystem;
    DefaultLabelProvider.Builder builder = new DefaultLabelProvider.Builder()
        .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.ACTIVE_POWER)
        .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
        .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
        .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.NAME);

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
        return builder.build(network, getSvgParameters());
    }

    @Override
    protected EdgeRouting getEdgeRouting() {
        Map<String, List<Point>> edgesMap = Map.of(
                "L1-2-1", List.of(new Point(-0.89, -652.83)),
                "L1-5-1", List.of(new Point(296.10, -502.39), new Point(717.04, -455.84), new Point(737.27, -51.09))
        );
        Map<String, List<Point>> textMap = Map.of(
                "VL3", List.of(new Point(450, -400), new Point(479.01, -375.27))
        );
        return new CustomPathRouting(edgesMap, textMap);
    }

    @Test
    void testDrawSvg() {
        Network network = IeeeCdfNetworkFactory.create14Solved();

        Line line121 = network.getLine("L1-2-1");
        line121.getTerminal1().setP(10);
        line121.getTerminal2().setP(11);

        Line line151 = network.getLine("L1-5-1");
        line151.getTerminal1().setP(8);
        line151.getTerminal2().setP(7);

        Path svgFile = fileSystem.getPath("nad-test.svg");
        NadParameters nadParameters = new NadParameters()
                .setSvgParameters(getSvgParameters())
                .setStyleProviderFactory(this::getStyleProvider)
                .setLabelProviderFactory(builder::build)
                .setEdgeRouting(getEdgeRouting());
        NetworkAreaDiagram.draw(network, svgFile, nadParameters, NO_FILTER);
        assertFileEquals("/ieee14_custom_paths.svg", svgFile);
    }
}
