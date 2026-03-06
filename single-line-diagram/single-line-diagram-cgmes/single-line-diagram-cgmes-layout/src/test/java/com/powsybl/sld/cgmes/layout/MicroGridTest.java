/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sld.cgmes.layout;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class MicroGridTest extends AbstractTest {

    private static final String VL_S2_10 = "b2707f00-2554-41d2-bde2-7dd80a669e50";
    private static final String VL_S5_10 = "8d4a8238-5b31-4c16-8692-0265dae5e132";
    private static final String SUB_S3 = "974565b1-ac55-4901-9f48-afc7ef5486df";

    @BeforeEach
    void setup() throws IOException {
        super.setup();
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", List.of("cgmesDLImport"));
        network = Network.read(CgmesConformity1ModifiedCatalog.miniNodeBreakerMeasurements().dataSource(), properties);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestData")
    void test(String testName, String filename, String containerId) throws IOException {
        assertSvgDrawnEqualsReference(containerId, filename, 1);
    }

    @Test
    void testOpenSwitch() throws IOException {
        String filename = "/microgrid_S2_10kV_open_switch.svg";
        network.getSwitch("1287758d-606d-44c9-9e93-2f465ebf54b7").setOpen(true);
        assertSvgDrawnEqualsReference(VL_S2_10, filename, 2);
    }

    private static List<Arguments> provideTestData() {
        return List.of(
                Arguments.of("Test voltage level 'S2 10kV' diagram", "/microgrid_S2_10kV.svg", VL_S2_10),
                Arguments.of("Test voltage level 'S5 10kV' diagram", "/microgrid_S5_10kV.svg", VL_S5_10),
                Arguments.of("Test substation 'S3' diagram", "/microgrid_S3.svg", SUB_S3)
        );
    }
}
