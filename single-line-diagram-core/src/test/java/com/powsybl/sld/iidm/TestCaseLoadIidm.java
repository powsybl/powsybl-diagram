/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.util.TopologicalStyleProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class TestCaseLoadIidm extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = Importers.loadNetwork(Path.of(System.getProperty("user.home"), "debug/ankur.xiidm"));
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Override
    protected DiagramStyleProvider getDefaultDiagramStyleProvider() {
        return new TopologicalStyleProvider(network);
    }

    @Test
    public void test() throws IOException {
        String vlId = "WI";
        layoutParameters.setShowInternalNodes(false);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vlId);

        // Run layout
        voltageLevelGraphLayout(g);

        network.getVoltageLevel(vlId).exportTopology(Path.of(System.getProperty("user.home"), "debug/graph.dot"));

        // write SVG
        toSVG(g, "/TestLoadedIidm.svg");
    }

    @Test
    public void test2() throws IOException {
        String vlId = "AAA";
        layoutParameters.setShowInternalNodes(false);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vlId);

        // Run layout
        voltageLevelGraphLayout(g);

        network.getVoltageLevel(vlId).exportTopology(Path.of(System.getProperty("user.home"), "debug/graph.dot"));

        // write SVG
        toSVG(g, "/TestLoadedIidm.svg");
    }
}
