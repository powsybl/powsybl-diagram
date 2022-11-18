/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ThreeWindingTransformerTest extends AbstractTest {

    private StyleProvider styleProvider;

    @Before
    public void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return styleProvider != null ? styleProvider : new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    public void test3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertEquals(toString("/3wt.svg"), generateSvgString(network, "/3wt.svg"));
    }

    @Test
    public void testDisconnected3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getTerminal(ThreeWindingsTransformer.Side.TWO).disconnect();
        network.getLoad("LOAD_33").remove();
        assertEquals(toString("/3wt_disconnected.svg"), generateSvgString(network, "/3wt_disconnected.svg"));
    }

    @Test
    public void testDisconnected3wtTopologicalStyle() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getTerminal(ThreeWindingsTransformer.Side.TWO).disconnect();
        network.getLoad("LOAD_33").remove();
        assertEquals(toString("/3wt_disconnected_topological.svg"), generateSvgString(network, "/3wt_disconnected_topological.svg"));
    }

    @Test
    public void testPartial3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        VoltageLevelFilter filter = VoltageLevelFilter.createVoltageLevelsFilter(network, Collections.singletonList("VL_11"));
        assertEquals(toString("/3wt_partial.svg"), generateSvgString(network, filter, "/3wt_partial.svg"));
    }
}
