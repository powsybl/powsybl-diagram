/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PositionVoltageLevelLayoutFactoryTest {

    @Test
    public void test() {
        PositionVoltageLevelLayoutFactory factory = new PositionVoltageLevelLayoutFactory();

        assertTrue(factory.isFeederStacked());
        factory.setFeederStacked(false);
        assertFalse(factory.isFeederStacked());

        assertTrue(factory.isRemoveUnnecessaryFictitiousNodes());
        factory.setRemoveUnnecessaryFictitiousNodes(false);
        assertFalse(factory.isRemoveUnnecessaryFictitiousNodes());

        assertFalse(factory.isExceptionIfPatternNotHandled());
        factory.setExceptionIfPatternNotHandled(true);
        assertTrue(factory.isExceptionIfPatternNotHandled());

        assertFalse(factory.isHandleShunts());
        factory.setHandleShunts(true);
        assertTrue(factory.isHandleShunts());

        assertFalse(factory.isVoltageLackInformationOnBus());
        factory.setVoltageLackInformationOnBus(true);
        assertTrue(factory.isVoltageLackInformationOnBus());
    }
}
