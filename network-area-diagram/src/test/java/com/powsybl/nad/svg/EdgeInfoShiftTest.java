/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class EdgeInfoShiftTest extends AbstractTest {

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider.Builder()
            .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setSubstationDescriptionDisplayed(false)
            .setBusLegend(true)
            .build(network, getSvgParameters());
    }

    @Test
    void testArrowShift() {
        Network network = Networks.createThreeVoltageLevelsFiveBuses();
        getSvgParameters().setArrowShift(20);
        assertSvgEquals("/edge_info_shift.svg", network);
    }
}
