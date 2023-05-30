/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.commons.config.ModuleConfigRepository;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.NominalVoltageStyleProvider;
import com.powsybl.sld.svg.styles.StyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
class TestDisconnectedComponentsBusBreaker extends AbstractTestCaseIidm {

    @Override
    public StyleProvider getDefaultDiagramStyleProvider() {
        // bypassing the config-test platform config to test the embedded base-voltages.yml file
        BaseVoltagesConfig baseVoltagesConfig = BaseVoltagesConfig.fromPlatformConfig(new PlatformConfig((ModuleConfigRepository) null, Path.of("./")));
        return new NominalVoltageStyleProvider(baseVoltagesConfig);
    }

    @BeforeEach
    public void setUp() {
        network = CreateNetworksUtil.createBusBreakerNetworkWithInternalBranches("TestInternalBranchesBusBreaker", "test");
        substation = network.getSubstation("S1");
    }

    @Test
    void testDisconnectedLoad() {
        network.getLoad("LD1").getTerminal().disconnect();
        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/disconnectedLoadBusBreaker.svg"),
                toSVG(g, "/disconnectedLoadBusBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testDisconnectedLine() {
        network.getLine("L12").getTerminal(Branch.Side.ONE).disconnect();
        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/disconnectedLineBusBreaker.svg"),
                toSVG(g, "/disconnectedLineBusBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testDisconnected2wt() {
        network.getTwoWindingsTransformer("T12").getTerminal(Branch.Side.ONE).disconnect();
        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/disconnected2wtBusBreaker.svg"),
                toSVG(g, "/disconnected2wtBusBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testDisconnectedInternal2wt() {
        network.getTwoWindingsTransformer("T11").getTerminal(Branch.Side.TWO).disconnect();
        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/disconnectedInternal2wtBusBreaker.svg"),
                toSVG(g, "/disconnectedInternal2wtBusBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testDisconnected3wt() {
        network.getThreeWindingsTransformer("T3_12").getTerminal(ThreeWindingsTransformer.Side.TWO).disconnect();
        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/disconnected3wtBusBreaker.svg"),
                toSVG(g, "/disconnected3wtBusBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testOpenSwitch() {
        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/openSwitchBusBreaker.svg"),
                toSVG(g, "/openSwitchBusBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

}
