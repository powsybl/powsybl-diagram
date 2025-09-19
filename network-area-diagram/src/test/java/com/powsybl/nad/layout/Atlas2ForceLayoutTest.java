/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.layout.algorithms.parameters.Atlas2Parameters;
import com.powsybl.diagram.util.layout.postprocessing.OverlapPreventionPostProcessing;
import com.powsybl.diagram.util.layout.setup.SquareRandomSetup;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class Atlas2ForceLayoutTest extends ForceLayoutTest {

    @Test
    void testDiamondDefaultNoBH() {
        assertSvgEquals(
                "/diamond-network_atlas2_NoBH.svg",
                LayoutNetworkFactory.createDiamond(),
                VoltageLevelFilter.NO_FILTER,
                new Atlas2ForceLayout(
                        new SquareRandomSetup<>(),
                        new Atlas2Parameters.Builder().withBarnesHutTheta(0).build()
        ));
    }

    @Test
    void testIEEE14DefaultNoBH() {
        assertSvgEquals(
                "/IEEE_14_atlas2_NoBH.svg",
                IeeeCdfNetworkFactory.create14(),
                VoltageLevelFilter.NO_FILTER,
                new Atlas2ForceLayout(
                    new SquareRandomSetup<>(),
                    new Atlas2Parameters.Builder().withBarnesHutTheta(0).build()
        ));
    }

    @Test
    void testIEEE14CustomNoBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withRepulsion(10)
                        .withActivateAttractToCenterForce(false)
                        .withSwingTolerance(0.8)
                        .withBarnesHutTheta(0)
                        .build(),
                new OverlapPreventionPostProcessing<>()
        );
        assertSvgEquals("/IEEE_14_atlas2_custom1_NoBH.svg", IeeeCdfNetworkFactory.create14(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE14YesBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout();
        assertSvgEquals("/IEEE_14_atlas2_yesBH.svg", IeeeCdfNetworkFactory.create14(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE14CustomMoreIterationNoBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withRepulsion(10)
                        .withActivateAttractToCenterForce(false)
                        .withSwingTolerance(0.8)
                        .withIterationNumberIncreasePercent(50)
                        .withBarnesHutTheta(0)
                        .build(),
                new OverlapPreventionPostProcessing<>()
        );
        assertSvgEquals("/IEEE_14_atlas2_custom1_moreIteration_NoBH.svg", IeeeCdfNetworkFactory.create14(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE14CustomMoreIterationYesBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withIterationNumberIncreasePercent(50)
                        .build()
        );
        assertSvgEquals("/IEEE_14_atlas2_custom_moreIteration_yesBH.svg", IeeeCdfNetworkFactory.create14(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE30CustomNoBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withAttractToCenter(0.003)
                        .withMaxSpeedFactor(8)
                        .withSpeedFactor(0.9)
                        .withMaxSteps(200)
                        .withIterationNumberIncreasePercent(60)
                        .withBarnesHutTheta(0)
                        .build(),
                new OverlapPreventionPostProcessing<>()
        );
        assertSvgEquals("/IEEE_30_atlas2_custom1_NoBH.svg", IeeeCdfNetworkFactory.create30(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE30YesBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout();
        assertSvgEquals("/IEEE_30_atlas2_yesBH.svg", IeeeCdfNetworkFactory.create30(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE118CustomNoBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withRepulsion(10)
                        .withIterationNumberIncreasePercent(20)
                        .withBarnesHutTheta(0)
                        .build(),
                new OverlapPreventionPostProcessing<>()
        );
        assertSvgEquals("/IEEE_118_atlas2_custom1_NoBH.svg", IeeeCdfNetworkFactory.create118(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE118CustomYesBH() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withRepulsion(10)
                        .withIterationNumberIncreasePercent(20)
                        .withBarnesHutTheta(1.2)
                        .build()
        );
        assertSvgEquals("/IEEE_118_atlas2_custom_yesBH.svg", IeeeCdfNetworkFactory.create118(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

}
