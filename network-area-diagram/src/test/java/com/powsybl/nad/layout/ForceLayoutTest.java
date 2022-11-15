/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
class ForceLayoutTest extends AbstractTest {

    @BeforeEach
    public void setup() {
        setLayoutParameters(new LayoutParameters().setTextNodesForceLayout(false));
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(false)
                .setSvgWidthAndHeightAdded(false));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void testDiamondNoSpringRepulsionFactor() {
        assertEquals(
                toString("/diamond-spring-repulsion-factor-0.0.svg"),
                generateSvgString(LayoutNetworkFactory.createDiamond(), "/diamond-spring-repulsion-factor-0.0.svg"));
    }

    @Test
    void testDiamondSmallSpringRepulsionFactor() {
        getLayoutParameters().setSpringRepulsionFactorForceLayout(0.2);
        assertEquals(
                toString("/diamond-spring-repulsion-factor-0.2.svg"),
                generateSvgString(LayoutNetworkFactory.createDiamond(), "/diamond-spring-repulsion-factor-0.2.svg"));
    }
}
