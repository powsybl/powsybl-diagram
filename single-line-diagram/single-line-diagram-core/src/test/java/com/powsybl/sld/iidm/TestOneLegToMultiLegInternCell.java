/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class TestOneLegToMultiLegInternCell extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() throws IOException {

    }

    @Override
    protected void voltageLevelGraphLayout(VoltageLevelGraph voltageLevelGraph) {
        new PositionVoltageLevelLayoutFactory().create(voltageLevelGraph).run(layoutParameters);
    }

    @Test
    void testBasicInternCellOnDifferentSubsections() {
        network = Networks.createNetworkWithInternCellDifferentSubsections();
        graphBuilder = new NetworkGraphBuilder(network);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        String filename = "/TestInternCellDifferentSubsections.svg";

        assertEquals(toString(filename), toSVG(g, filename, getResourcesComponentLibrary(), layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void test3SubBlockInternCellDifferentSubsectionsSectionIndexGrouping() {
        network = Networks.createNetworkWithComplexInternCellDifferentSubsections();
        graphBuilder = new NetworkGraphBuilder(network);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        String filename = "/TestOneLegInternCellDifferentSubsectionsSi.svg";

        assertEquals(toString(filename), toSVG(g, filename, getResourcesComponentLibrary(), layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void test3SubBlockInternCellDifferentSubsectionsLegBusSetsGrouping() {
        network = Networks.createNetworkWithComplexInternCellDifferentSubsections1();
        graphBuilder = new NetworkGraphBuilder(network);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        String filename = "/TestOneLegInternCellDifferentSubsectionsLbs.svg";

        assertEquals(toString(filename), toSVG(g, filename, getResourcesComponentLibrary(), layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }
}
