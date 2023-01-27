/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class EdgeIdTest extends AbstractTest {

    @Before
    public void setup() {
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
        return new DefaultLabelProvider(network, getSvgParameters()) {
        };
    }

    @Test
    public void testNameOnEdgeDisplayed() {
        Network network = NetworkTestFactory.createThreeVoltageLevelsFiveBuses();
        getSvgParameters().setEdgeNameDisplayed(true);
        assertEquals(toString("/edge_with_id.svg"), generateSvgString(network, "/edge_with_id.svg"));
    }

    @Test
    public void testNameOnEdgeNotDisplayed() {
        Network network = NetworkTestFactory.createThreeVoltageLevelsFiveBuses();
        getSvgParameters().setEdgeNameDisplayed(false);
        assertEquals(toString("/edge_without_id.svg"), generateSvgString(network, "/edge_without_id.svg"));
    }
}
