/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class TypeOfEdgeInfoTest extends AbstractTest {

    @Before
    public void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setVoltageLevelDetails(false)
                .setFixedWidth(800)
                .setEdgeStartShift(2));
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
    public void testReactivePowerInfoLabel() {
        Network network = IeeeCdfNetworkFactory.create9();
        LoadFlow.run(network);
        getSvgParameters().setEdgeInfoDisplayed(SvgParameters.EdgeInfoEnum.REACTIVE_POWER);
        assertEquals(toString("/edge_info_reactive_power.svg"), generateSvgString(network, "/edge_info_reactive_power.svg"));
    }

    @Test
    public void testCurrentInfoLabel() {
        Network network = IeeeCdfNetworkFactory.create9();
        LoadFlow.run(network);
        getSvgParameters().setEdgeInfoDisplayed(SvgParameters.EdgeInfoEnum.CURRENT);
        assertEquals(toString("/edge_info_current.svg"), generateSvgString(network, "/edge_info_current.svg"));
    }

}
