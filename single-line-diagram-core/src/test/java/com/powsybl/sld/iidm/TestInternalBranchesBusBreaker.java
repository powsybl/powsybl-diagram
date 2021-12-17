/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.commons.config.ModuleConfigRepository;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.util.NominalVoltageDiagramStyleProvider;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestInternalBranchesBusBreaker extends AbstractTestCaseIidm {

    @Override
    public DiagramStyleProvider getDefaultDiagramStyleProvider() {
        // bypassing the config-test platform config to test the embedded base-voltages.yml file
        BaseVoltagesConfig baseVoltagesConfig = BaseVoltagesConfig.fromPlatformConfig(new PlatformConfig((ModuleConfigRepository) null, Path.of("./")));
        return new NominalVoltageDiagramStyleProvider(baseVoltagesConfig, network);
    }

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createBusBreakerNetworkWithInternalBranches("TestInternalBranchesBusBreaker", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.getSubstation("S1");
    }

    @Test
    public void testVLGraph() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId(), true);

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/InternalBranchesBusBreaker.svg"),
                toSVG(g, "/InternalBranchesBusBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    public void testSubstationGraphH() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run horizontal substation layout
        substationGraphLayout(g);

        assertEquals(toString("/InternalBranchesBusBreakerH.json"), toJson(g, "/InternalBranchesBusBreakerH.json"));
    }

    @Test
    public void testSubstationGraphV() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run vertical substation layout
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/InternalBranchesBusBreakerV.json"), toJson(g, "/InternalBranchesBusBreakerV.json"));
    }
}
