/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Side;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class PositionVoltageLevelLayoutFactoryParametersTest {

    @Test
    void test() {
        PositionVoltageLevelLayoutFactoryParameters parameters = new PositionVoltageLevelLayoutFactoryParameters();

        assertTrue(parameters.isFeederStacked());
        parameters.setFeederStacked(false);
        assertFalse(parameters.isFeederStacked());

        assertTrue(parameters.isRemoveUnnecessaryFictitiousNodes());
        parameters.setRemoveUnnecessaryFictitiousNodes(false);
        assertFalse(parameters.isRemoveUnnecessaryFictitiousNodes());

        assertTrue(parameters.isSubstituteSingularFictitiousByFeederNode());
        parameters.setSubstituteSingularFictitiousByFeederNode(false);
        assertFalse(parameters.isSubstituteSingularFictitiousByFeederNode());

        assertFalse(parameters.isExceptionIfPatternNotHandled());
        parameters.setExceptionIfPatternNotHandled(true);
        assertTrue(parameters.isExceptionIfPatternNotHandled());

        assertFalse(parameters.isHandleShunts());
        parameters.setHandleShunts(true);
        assertTrue(parameters.isHandleShunts());

        assertTrue(parameters.getBusInfoMap().isEmpty());
        Map<String, Side> busInfoMap = new HashMap<>();
        busInfoMap.put("???", Side.LEFT);
        parameters.setBusInfoMap(busInfoMap);
        assertFalse(parameters.getBusInfoMap().isEmpty());
    }
}
