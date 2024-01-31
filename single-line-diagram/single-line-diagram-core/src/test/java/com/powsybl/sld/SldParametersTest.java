/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld;

import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.pathfinding.*;
import com.powsybl.sld.library.*;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class SldParametersTest {

    @Test
    void test() {
        LayoutParameters layoutParameters = new LayoutParameters();
        SvgParameters svgParameters = new SvgParameters();
        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
        StyleProviderFactory styleProviderFactory = new NominalVoltageStyleProviderFactory();
        SubstationLayoutFactory substationLayoutFactory = new VerticalSubstationLayoutFactory();
        VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = i -> new PositionVoltageLevelLayoutFactory();
        ZoneLayoutFactory zoneLayoutFactory = new HorizontalZoneLayoutFactory();
        ZoneLayoutPathFinderFactory zoneLayoutPathFinderFactory = DijkstraPathFinder::new;

        SldParameters sldParameters0 = new SldParameters()
                .setLayoutParameters(layoutParameters)
                .setSvgParameters(svgParameters)
                .setComponentLibrary(componentLibrary)
                .setStyleProviderFactory(styleProviderFactory)
                .setSubstationLayoutFactory(substationLayoutFactory)
                .setVoltageLevelLayoutFactoryCreator(voltageLevelLayoutFactoryCreator)
                .setZoneLayoutFactory(zoneLayoutFactory)
                .setZoneLayoutPathFinderFactory(zoneLayoutPathFinderFactory);

        assertSame(sldParameters0.getLayoutParameters(), layoutParameters);
        assertSame(sldParameters0.getSvgParameters(), svgParameters);
        assertSame(sldParameters0.getComponentLibrary(), componentLibrary);
        assertSame(sldParameters0.getStyleProviderFactory(), styleProviderFactory);
        assertSame(sldParameters0.getSubstationLayoutFactory(), substationLayoutFactory);
        assertSame(sldParameters0.getZoneLayoutFactory(), zoneLayoutFactory);
        assertSame(sldParameters0.getZoneLayoutPathFinderFactory(), zoneLayoutPathFinderFactory);
    }
}
