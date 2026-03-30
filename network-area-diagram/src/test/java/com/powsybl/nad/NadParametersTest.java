/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.layout.Atlas2ForceLayout;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.library.DefaultComponentLibrary;
import com.powsybl.nad.library.NadComponentLibrary;
import com.powsybl.nad.routing.EdgeRouting;
import com.powsybl.nad.routing.StraightEdgeRouting;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.IdProviderFactory;
import com.powsybl.nad.svg.iidm.LabelProviderFactory;
import com.powsybl.nad.svg.iidm.StyleProviderFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NadParametersTest {

    @Test
    void testDefaults() {
        NadParameters params = new NadParameters();
        assertNotNull(params.getSvgParameters());
        assertNotNull(params.getLayoutParameters());
        assertNotNull(params.getStyleProviderFactory());
        assertNotNull(params.getLayoutFactory());
        assertInstanceOf(Atlas2ForceLayout.class, params.getLayoutFactory().create());
        assertNotNull(params.getIdProviderFactory());
        assertInstanceOf(IntIdProvider.class, params.getIdProviderFactory().create());
        assertNotNull(params.getComponentLibrary());
        assertInstanceOf(DefaultComponentLibrary.class, params.getComponentLibrary());
        assertNotNull(params.getEdgeRouting());
        assertInstanceOf(StraightEdgeRouting.class, params.getEdgeRouting());
    }

    @Test
    void testSettersAndGetters() {
        NadParameters params = new NadParameters();

        SvgParameters svgParams = new SvgParameters();
        params.setSvgParameters(svgParams);
        assertSame(svgParams, params.getSvgParameters());

        LayoutParameters layoutParams = new LayoutParameters();
        params.setLayoutParameters(layoutParams);
        assertSame(layoutParams, params.getLayoutParameters());

        StyleProviderFactory styleFactory = network -> null;
        params.setStyleProviderFactory(styleFactory);
        assertSame(styleFactory, params.getStyleProviderFactory());

        LabelProviderFactory labelFactory = (network, svgParameters) -> null;
        params.setLabelProviderFactory(labelFactory);
        // labelProviderFactory is private, but we can test createLabelProvider
        Network network = Networks.createTwoVoltageLevels();
        assertNull(params.createLabelProvider(network));

        LayoutFactory layoutFactory = () -> null;
        params.setLayoutFactory(layoutFactory);
        assertSame(layoutFactory, params.getLayoutFactory());

        IdProviderFactory idFactory = () -> null;
        params.setIdProviderFactory(idFactory);
        assertSame(idFactory, params.getIdProviderFactory());

        NadComponentLibrary lib = new DefaultComponentLibrary();
        params.setComponentLibrary(lib);
        assertSame(lib, params.getComponentLibrary());

        EdgeRouting routing = new StraightEdgeRouting();
        params.setEdgeRouting(routing);
        assertSame(routing, params.getEdgeRouting());
    }

    @Test
    void testCreateLabelProvider() {
        NadParameters params = new NadParameters();
        Network network = Networks.createTwoVoltageLevels();
        LabelProvider labelProvider = params.createLabelProvider(network);
        assertNotNull(labelProvider);
        assertInstanceOf(DefaultLabelProvider.class, labelProvider);
    }
}
