/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.commons.config.ModuleConfigRepository;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.styles.NominalVoltageStyleProvider;
import com.powsybl.sld.svg.styles.StyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
 */
class TestInternalBranchesBusBreaker extends AbstractTestCaseIidm {

    @Override
    public StyleProvider getDefaultDiagramStyleProvider() {
        // bypassing the sldParameters-test platform sldParameters to test the embedded base-voltages.yml file
        BaseVoltagesConfig baseVoltagesConfig = BaseVoltagesConfig.fromPlatformConfig(new PlatformConfig((ModuleConfigRepository) null, Path.of("./")));
        return new NominalVoltageStyleProvider(baseVoltagesConfig);
    }

    @BeforeEach
    public void setUp() {
        network = Networks.createBusBreakerNetworkWithInternalBranches("TestInternalBranchesBusBreaker", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.getSubstation("S1");
    }

    @Test
    void testVLGraph() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        assertEquals(toString("/InternalBranchesBusBreaker.svg"),
                toSVG(g, "/InternalBranchesBusBreaker.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testSubstationGraphH() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run horizontal substation layout
        substationGraphLayout(g);

        assertEquals(toString("/InternalBranchesBusBreakerH.json"), toJson(g, "/InternalBranchesBusBreakerH.json"));
    }

    @Test
    void testSubstationGraphV() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run vertical substation layout
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/InternalBranchesBusBreakerV.json"), toJson(g, "/InternalBranchesBusBreakerV.json"));
    }
}
