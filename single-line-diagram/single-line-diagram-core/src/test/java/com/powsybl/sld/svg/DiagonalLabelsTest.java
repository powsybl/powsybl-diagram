/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class DiagonalLabelsTest extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Networks.createNetworkWithSvcVscScDl();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("vl");
    }

    @Test
    void test() {
        SldComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
        svgParameters.setFeederInfoSymmetry(true);
        svgParameters.setLabelDiagonal(true);

        // build first voltage level graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(g); // to have cell orientations (bottom / up)

        assertEquals(toString("/diagonalLabelsTest.svg"), toSVG(g, "/diagonalLabelsTest.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }
}
