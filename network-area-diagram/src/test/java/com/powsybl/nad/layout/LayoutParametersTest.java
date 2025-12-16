/**
 * Copyright (c) 2022-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class LayoutParametersTest {

    @Test
    void test() {
        LayoutParameters layoutParameters0 = new LayoutParameters()
                .setTextNodesForceLayout(true)
                .setTextNodeFixedShift(50., 50.)
                .setMaxSteps(20)
                .setTimeoutSeconds(2)
                .setTextNodeEdgeConnectionYShift(30);

        LayoutParameters layoutParameters1 = new LayoutParameters(layoutParameters0);

        assertEquals(layoutParameters0.isTextNodesForceLayout(), layoutParameters1.isTextNodesForceLayout());
        assertEquals(layoutParameters0.getTextNodeFixedShift().x(), layoutParameters1.getTextNodeFixedShift().x(), 0);
        assertEquals(layoutParameters0.getTextNodeFixedShift().y(), layoutParameters1.getTextNodeFixedShift().y(), 0);
        assertEquals(layoutParameters0.getMaxSteps(), layoutParameters1.getMaxSteps());
        assertEquals(layoutParameters0.getTimeoutSeconds(), layoutParameters1.getTimeoutSeconds());
        assertEquals(layoutParameters0.getTextNodeEdgeConnectionYShift(), layoutParameters1.getTextNodeEdgeConnectionYShift(), 0);
    }
}
