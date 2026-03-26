/**
 * Copyright (c) 2022-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Luma Zamarreno {@literal <zamarrenolm at aia.es>}
 */
class ForceLayoutTest extends AbstractTest {

    private final LayoutParameters layoutParameters = new LayoutParameters().setTextNodesForceLayout(false);
    private DefaultLabelProvider.EdgeInfoEnum infoSideExternal = DefaultLabelProvider.EdgeInfoEnum.ACTIVE_POWER;
    private DefaultLabelProvider.EdgeInfoEnum infoMiddleSide1 = DefaultLabelProvider.EdgeInfoEnum.EMPTY;
    private DefaultLabelProvider.EdgeInfoEnum infoMiddleSide2 = DefaultLabelProvider.EdgeInfoEnum.EMPTY;
    private DefaultLabelProvider.EdgeInfoEnum infoSideInternal = DefaultLabelProvider.EdgeInfoEnum.EMPTY;
    private final SvgParameters svgParameters = new SvgParameters()
        .setInsertNameDesc(false)
        .setSvgWidthAndHeightAdded(false);

    @BeforeEach
    void setup() {
        setLayoutParameters(layoutParameters);
        setSvgParameters(svgParameters);
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider.Builder()
            .setInfoSideExternal(infoSideExternal)
            .setInfoSideInternal(infoSideInternal)
            .setInfoMiddleSide1(infoMiddleSide1)
            .setInfoMiddleSide2(infoMiddleSide2)
            .build(network, getSvgParameters());
    }

    @Test
    void testDiamond() {
        assertSvgEquals("/diamond-network.svg", LayoutNetworkFactory.createDiamond());
    }

    @Test
    void testScale() {
        infoSideExternal = DefaultLabelProvider.EdgeInfoEnum.NAME;
        infoSideInternal = DefaultLabelProvider.EdgeInfoEnum.NAME;
        infoMiddleSide1 = DefaultLabelProvider.EdgeInfoEnum.NAME;
        infoMiddleSide2 = DefaultLabelProvider.EdgeInfoEnum.NAME;
        assertSvgEquals("/diamond-network-labels-scale100.svg", LayoutNetworkFactory.createDiamond());
        layoutParameters.setScaleFactor(2);
        svgParameters.setArrowShift(60);
        assertSvgEquals("/diamond-network-labels-scale200.svg", LayoutNetworkFactory.createDiamond());
    }
}
