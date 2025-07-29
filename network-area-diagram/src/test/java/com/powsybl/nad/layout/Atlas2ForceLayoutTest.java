/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.layout.algorithms.parameters.Atlas2Parameters;
import com.powsybl.diagram.util.layout.setup.SquareRandomSetup;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class Atlas2ForceLayoutTest extends ForceLayoutTest {

    @Test
    void testDiamondDefault() {
        assertSvgEquals("/diamond-network_atlas2.svg", LayoutNetworkFactory.createDiamond(), VoltageLevelFilter.NO_FILTER, new Atlas2ForceLayout());
    }

    @Test
    void testIEEE14Default() {
        assertSvgEquals("/IEEE_14_atlas2.svg", IeeeCdfNetworkFactory.create14(), VoltageLevelFilter.NO_FILTER, new Atlas2ForceLayout());
    }

    @Test
    void testIEEE14Custom() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withRepulsion(10)
                        .withAttractToCenterForce(false)
                        .withSwingTolerance(0.8)
                        .build()
        );
        assertSvgEquals("/IEEE_14_atlas2_custom1.svg", IeeeCdfNetworkFactory.create14(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE14CustomMoreIteration() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withRepulsion(10)
                        .withAttractToCenterForce(false)
                        .withSwingTolerance(0.8)
                        .withIterationNumberIncreasePercent(0.5)
                        .build()
        );
        assertSvgEquals("/IEEE_14_atlas2_custom1_moreIteration.svg", IeeeCdfNetworkFactory.create14(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE30Custom() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withGravity(0.003)
                        .withMaxSpeedFactor(8)
                        .withSpeedFactor(0.9)
                        .withMaxSteps(200)
                        .withIterationNumberIncreasePercent(0.6)
                        .build()
        );
        assertSvgEquals("/IEEE_30_atlas2_custom1.svg", IeeeCdfNetworkFactory.create30(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

    @Test
    void testIEEE118Custom() {
        Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(
                new SquareRandomSetup<>(),
                new Atlas2Parameters.Builder()
                        .withRepulsion(10)
                        .withIterationNumberIncreasePercent(0.2)
                        .build()
        );
        assertSvgEquals("/IEEE_118_atlas2_custom1.svg", IeeeCdfNetworkFactory.create118(), VoltageLevelFilter.NO_FILTER, atlas2ForceLayout);
    }

}
